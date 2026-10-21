package com.lukasz.witkowski.training.planner.backend.service

import com.lukasz.witkowski.training.planner.backend.db.CategoriesTable
import com.lukasz.witkowski.training.planner.backend.db.DatabaseFactory.dbQuery
import com.lukasz.witkowski.training.planner.backend.db.ExerciseCategoriesTable
import com.lukasz.witkowski.training.planner.backend.db.ExerciseDeletionsTable
import com.lukasz.witkowski.training.planner.backend.db.ExerciseRecommendationsTable
import com.lukasz.witkowski.training.planner.backend.db.ExercisesTable
import com.lukasz.witkowski.training.planner.backend.db.IdempotencyLogsTable
import com.lukasz.witkowski.training.planner.backend.db.UsersTable
import com.lukasz.witkowski.training.planner.dto.common.PagedResponseDto
import com.lukasz.witkowski.training.planner.dto.exercise.CategoryDto
import com.lukasz.witkowski.training.planner.dto.exercise.CreateExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseRecommendationDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseSyncResponseDto
import com.lukasz.witkowski.training.planner.dto.exercise.OwnerDto
import com.lukasz.witkowski.training.planner.dto.exercise.UpdateExerciseRequestDto
import java.util.UUID
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.AndOp
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.OrOp
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class ExerciseConflictException(val currentServerExercise: ExerciseDto) :
    Exception("Version conflict detected")

class ExerciseService {
    suspend fun getCategories(): List<CategoryDto> = dbQuery {
        CategoriesTable.selectAll().map {
            CategoryDto(id = it[CategoriesTable.id], name = it[CategoriesTable.name])
        }
    }

    suspend fun ensureDefaultCategoriesExist(): Unit = dbQuery {
        val defaultCategories =
            listOf(
                "Chest",
                "Back",
                "Legs",
                "Shoulders",
                "Arms",
                "Abs",
                "Cardio",
                "Full Body"
            )
        val existingNames = CategoriesTable.selectAll().map { it[CategoriesTable.name] }.toSet()
        defaultCategories.forEach { name ->
            if (name !in existingNames) {
                CategoriesTable.insert {
                    it[id] = UUID.randomUUID().toString()
                    it[CategoriesTable.name] = name
                }
            }
        }
    }

    private fun resolveCategoryIds(categoryIdentifiers: List<String>): List<String> {
        val resolvedIds = mutableListOf<String>()
        for (identifier in categoryIdentifiers) {
            val trimmed = identifier.trim()
            if (trimmed.isBlank()) continue
            val existing =
                CategoriesTable
                    .selectAll()
                    .where {
                        OrOp(
                            listOf(
                                CategoriesTable.id eq identifier,
                                CategoriesTable.name.lowerCase() eq trimmed.lowercase()
                            )
                        )
                    }
                    .singleOrNull()
            if (existing != null) {
                resolvedIds.add(existing[CategoriesTable.id])
            } else {
                val newId = UUID.randomUUID().toString()
                CategoriesTable.insert {
                    it[id] = newId
                    it[name] = trimmed
                }
                resolvedIds.add(newId)
            }
        }
        return resolvedIds.distinct()
    }

    private fun getNextChangeVersion(): Long {
        val maxExExpr = ExercisesTable.changeVersion.max()
        val maxDelExpr = ExerciseDeletionsTable.changeVersion.max()
        val maxEx = ExercisesTable.select(maxExExpr).singleOrNull()?.get(maxExExpr) ?: 0L
        val maxDel =
            ExerciseDeletionsTable.select(maxDelExpr).singleOrNull()?.get(maxDelExpr) ?: 0L
        return maxOf(maxEx, maxDel) + 1L
    }

    suspend fun getExerciseChanges(
        sinceVersion: Long,
        requesterId: String? = null
    ): ExerciseSyncResponseDto = dbQuery {
        val visibilityCondition: Op<Boolean> = if (requesterId != null) {
            OrOp(
                listOf(
                    ExercisesTable.ownerId.isNull(),
                    ExercisesTable.ownerId eq requesterId,
                    ExercisesTable.sharing eq "COMMUNITY"
                )
            )
        } else {
            OrOp(
                listOf(
                    ExercisesTable.ownerId.isNull(),
                    ExercisesTable.sharing eq "COMMUNITY"
                )
            )
        }

        val condition = AndOp(
            listOf(
                ExercisesTable.changeVersion greater sinceVersion,
                visibilityCondition
            )
        )

        val queryJoin = ExercisesTable.leftJoin(UsersTable, {
            ExercisesTable.ownerId
        }, { UsersTable.id })
        val changedRows = queryJoin.selectAll().where { condition }.toList()
        val exerciseIds = changedRows.map { it[ExercisesTable.id] }
        val categoriesMap = getCategoriesForExercises(exerciseIds)
        val recommendationsMap = getRecommendationsForExercises(exerciseIds)

        val changedExercises = changedRows.map { row ->
            val exerciseId = row[ExercisesTable.id]
            val exerciseOwnerId = row[ExercisesTable.ownerId]
            val owner = exerciseOwnerId?.let { oId ->
                val username = row.getOrNull(UsersTable.username) ?: ""
                OwnerDto(id = oId, username = username)
            }
            ExerciseDto(
                id = exerciseId,
                name = row[ExercisesTable.name],
                description = row[ExercisesTable.description],
                categories = categoriesMap[exerciseId] ?: emptyList(),
                owner = owner,
                source = row[ExercisesTable.sourceType],
                sharing = row[ExercisesTable.sharing],
                revision = row[ExercisesTable.revision],
                recommendations = recommendationsMap[exerciseId]
            )
        }

        val deletedIds = ExerciseDeletionsTable.selectAll()
            .where { ExerciseDeletionsTable.changeVersion greater sinceVersion }
            .map { it[ExerciseDeletionsTable.exerciseId] }

        val maxExExpr = ExercisesTable.changeVersion.max()
        val maxDelExpr = ExerciseDeletionsTable.changeVersion.max()
        val maxExVersion =
            ExercisesTable.select(maxExExpr).singleOrNull()?.get(maxExExpr) ?: sinceVersion
        val maxDelVersion =
            ExerciseDeletionsTable.select(maxDelExpr).singleOrNull()?.get(maxDelExpr)
                ?: sinceVersion
        val currentVersion = maxOf(sinceVersion, maxExVersion, maxDelVersion)

        ExerciseSyncResponseDto(
            changes = changedExercises,
            deletedIds = deletedIds,
            currentVersion = currentVersion
        )
    }

    suspend fun getExercises(
        query: String? = null,
        categoryId: String? = null,
        ownerId: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): PagedResponseDto<ExerciseDto> = dbQuery {
        val safePage = page.coerceAtLeast(1)
        val safeLimit = limit.coerceIn(1, 100)

        var condition: Op<Boolean> =
            if (ownerId != null) {
                OrOp(
                    listOf(
                        ExercisesTable.ownerId.isNull(),
                        ExercisesTable.ownerId eq ownerId,
                        ExercisesTable.sharing eq "COMMUNITY"
                    )
                )
            } else {
                OrOp(
                    listOf(
                        ExercisesTable.ownerId.isNull(),
                        ExercisesTable.sharing eq "COMMUNITY"
                    )
                )
            }

        if (!query.isNullOrBlank()) {
            val queryPattern = "%${query.lowercase()}%"
            condition = AndOp(listOf(condition, ExercisesTable.name.lowerCase() like queryPattern))
        }

        var matchingIds: Set<String>? = null
        if (!categoryId.isNullOrBlank()) {
            val matchedCategoryIds =
                CategoriesTable
                    .selectAll()
                    .where {
                        OrOp(
                            listOf(
                                CategoriesTable.id eq categoryId,
                                CategoriesTable.name.lowerCase() eq categoryId.trim().lowercase()
                            )
                        )
                    }
                    .map { it[CategoriesTable.id] }
            matchingIds =
                if (matchedCategoryIds.isEmpty()) {
                    emptySet()
                } else {
                    ExerciseCategoriesTable
                        .selectAll()
                        .where { ExerciseCategoriesTable.categoryId inList matchedCategoryIds }
                        .map { it[ExerciseCategoriesTable.exerciseId] }
                        .toSet()
                }
        }

        val queryJoin = ExercisesTable.leftJoin(UsersTable, {
            ExercisesTable.ownerId
        }, { UsersTable.id })
        val allRows =
            queryJoin
                .selectAll()
                .where { condition }
                .toList()
                .filter { row -> matchingIds == null || row[ExercisesTable.id] in matchingIds }

        val totalItems = allRows.size.toLong()
        val totalPages = if (totalItems ==
            0L
        ) {
            1
        } else {
            kotlin.math.ceil(totalItems.toDouble() / safeLimit).toInt()
        }
        val offset = ((safePage - 1) * safeLimit).coerceAtLeast(0)

        val pageRows = allRows.drop(offset).take(safeLimit)
        val exerciseIds = pageRows.map { it[ExercisesTable.id] }
        val categoriesMap = getCategoriesForExercises(exerciseIds)
        val recommendationsMap = getRecommendationsForExercises(exerciseIds)

        val paginatedExercises =
            pageRows.map { row ->
                val exerciseId = row[ExercisesTable.id]
                val exerciseOwnerId = row[ExercisesTable.ownerId]
                val owner = exerciseOwnerId?.let { oId ->
                    val username = row.getOrNull(UsersTable.username) ?: ""
                    OwnerDto(id = oId, username = username)
                }
                ExerciseDto(
                    id = exerciseId,
                    name = row[ExercisesTable.name],
                    description = row[ExercisesTable.description],
                    categories = categoriesMap[exerciseId] ?: emptyList(),
                    owner = owner,
                    source = row[ExercisesTable.sourceType],
                    sharing = row[ExercisesTable.sharing],
                    revision = row[ExercisesTable.revision],
                    recommendations = recommendationsMap[exerciseId]
                )
            }

        PagedResponseDto(
            items = paginatedExercises,
            page = safePage,
            limit = safeLimit,
            totalItems = totalItems,
            totalPages = totalPages
        )
    }

    suspend fun getExerciseById(id: String, requesterId: String? = null): ExerciseDto? = dbQuery {
        val queryJoin = ExercisesTable.leftJoin(UsersTable, {
            ExercisesTable.ownerId
        }, { UsersTable.id })
        val row =
            queryJoin.selectAll().where { ExercisesTable.id eq id }.singleOrNull()
                ?: return@dbQuery null
        val ownerId = row[ExercisesTable.ownerId]
        val sharing = row[ExercisesTable.sharing]
        if (ownerId != null && ownerId != requesterId && sharing != "COMMUNITY") {
            throw SecurityException("You do not have permission to access this exercise.")
        }
        val owner = ownerId?.let { oId ->
            val username = row.getOrNull(UsersTable.username) ?: ""
            OwnerDto(id = oId, username = username)
        }
        val categories = getCategoriesForExercise(id)
        val recommendations = getRecommendationsForExercise(id)
        ExerciseDto(
            id = id,
            name = row[ExercisesTable.name],
            description = row[ExercisesTable.description],
            categories = categories,
            owner = owner,
            source = row[ExercisesTable.sourceType],
            sharing = sharing,
            revision = row[ExercisesTable.revision],
            recommendations = recommendations
        )
    }

    suspend fun createExercise(request: CreateExerciseRequestDto, ownerId: String): ExerciseDto =
        dbQuery {
            val idempotencyKey = request.idempotencyKey?.takeIf { it.isNotBlank() }
            if (idempotencyKey != null) {
                val existingLog =
                    IdempotencyLogsTable
                        .selectAll()
                        .where {
                            AndOp(
                                listOf(
                                    IdempotencyLogsTable.ownerId eq ownerId,
                                    IdempotencyLogsTable.key eq idempotencyKey
                                )
                            )
                        }
                        .singleOrNull()
                if (existingLog != null) {
                    val jsonPayload = existingLog[IdempotencyLogsTable.responsePayload]
                    return@dbQuery Json.decodeFromString<ExerciseDto>(jsonPayload)
                }
            }

            val requestedId = request.id?.takeIf { it.isNotBlank() }?.trim()
            if (requestedId != null) {
                val existingExercise =
                    ExercisesTable
                        .selectAll()
                        .where { ExercisesTable.id eq requestedId }
                        .singleOrNull()
                if (existingExercise != null) {
                    val existingOwnerId = existingExercise[ExercisesTable.ownerId]
                    if (existingOwnerId == null || existingOwnerId != ownerId) {
                        throw SecurityException("Not authorized to modify this exercise")
                    }
                    val ownerName =
                        UsersTable
                            .selectAll()
                            .where { UsersTable.id eq ownerId }
                            .singleOrNull()
                            ?.get(UsersTable.username) ?: ""
                    val owner = OwnerDto(id = ownerId, username = ownerName)
                    val categories = getCategoriesForExercise(requestedId)
                    val recommendations = getRecommendationsForExercise(requestedId)
                    val existingDto =
                        ExerciseDto(
                            id = requestedId,
                            name = existingExercise[ExercisesTable.name],
                            description = existingExercise[ExercisesTable.description],
                            categories = categories,
                            owner = owner,
                            source = existingExercise[ExercisesTable.sourceType],
                            sharing = existingExercise[ExercisesTable.sharing],
                            revision = existingExercise[ExercisesTable.revision],
                            recommendations = recommendations
                        )
                    if (idempotencyKey != null) {
                        val jsonPayload = Json.encodeToString(ExerciseDto.serializer(), existingDto)
                        IdempotencyLogsTable.insert {
                            it[IdempotencyLogsTable.ownerId] = ownerId
                            it[key] = idempotencyKey
                            it[responsePayload] = jsonPayload
                        }
                    }
                    return@dbQuery existingDto
                }
            }

            val exerciseId = requestedId ?: UUID.randomUUID().toString()
            val version = getNextChangeVersion()
            val sharingValue = request.sharing.ifBlank { "PRIVATE" }
            ExercisesTable.insert {
                it[id] = exerciseId
                it[name] = request.name
                it[description] = request.description
                it[ExercisesTable.ownerId] = ownerId
                it[sourceType] = "USER_CREATED"
                it[sharing] = sharingValue
                it[revision] = 1L
                it[changeVersion] = version
            }

            val resolvedCategoryIds = resolveCategoryIds(request.categoryIds)
            resolvedCategoryIds.forEach { catId ->
                ExerciseCategoriesTable.insert {
                    it[ExerciseCategoriesTable.exerciseId] = exerciseId
                    it[ExerciseCategoriesTable.categoryId] = catId
                }
            }

            request.recommendations?.forEach { rec ->
                ExerciseRecommendationsTable.insert {
                    it[id] = UUID.randomUUID().toString()
                    it[ExerciseRecommendationsTable.exerciseId] = exerciseId
                    it[level] = rec.level
                    it[sets] = rec.sets
                    it[reps] = rec.reps
                    it[restTimeInMillis] = rec.restTimeInMillis
                    it[weightInKg] = rec.weightInKg
                    it[timeInMillis] = rec.timeInMillis
                }
            }

            val ownerName =
                UsersTable.selectAll().where {
                    UsersTable.id eq ownerId
                }.singleOrNull()?.get(UsersTable.username)
                    ?: ""
            val owner = OwnerDto(id = ownerId, username = ownerName)
            val categories = getCategoriesForExercise(exerciseId)
            val recommendations = getRecommendationsForExercise(exerciseId)
            val resultDto =
                ExerciseDto(
                    id = exerciseId,
                    name = request.name,
                    description = request.description,
                    categories = categories,
                    owner = owner,
                    source = "USER_CREATED",
                    sharing = sharingValue,
                    revision = 1L,
                    recommendations = recommendations
                )

            if (idempotencyKey != null) {
                val jsonPayload = Json.encodeToString(ExerciseDto.serializer(), resultDto)
                IdempotencyLogsTable.insert {
                    it[IdempotencyLogsTable.ownerId] = ownerId
                    it[key] = idempotencyKey
                    it[responsePayload] = jsonPayload
                }
            }

            resultDto
        }

    suspend fun updateExercise(
        id: String,
        request: UpdateExerciseRequestDto,
        ownerId: String
    ): ExerciseDto = dbQuery {
        val existing =
            ExercisesTable.selectAll().where { ExercisesTable.id eq id }.singleOrNull()
                ?: throw IllegalArgumentException("Exercise $id not found.")

        if (existing[ExercisesTable.ownerId] == null ||
            existing[ExercisesTable.ownerId] != ownerId
        ) {
            throw SecurityException("Not authorized to modify this exercise")
        }

        val idempotencyKey = request.idempotencyKey?.takeIf { it.isNotBlank() }
        if (idempotencyKey != null) {
            val existingLog =
                IdempotencyLogsTable
                    .selectAll()
                    .where {
                        AndOp(
                            listOf(
                                IdempotencyLogsTable.ownerId eq ownerId,
                                IdempotencyLogsTable.key eq idempotencyKey
                            )
                        )
                    }
                    .singleOrNull()
            if (existingLog != null) {
                val jsonPayload = existingLog[IdempotencyLogsTable.responsePayload]
                return@dbQuery Json.decodeFromString<ExerciseDto>(jsonPayload)
            }
        }

        val currentRevision = existing[ExercisesTable.revision]
        if (request.expectedVersion != null && request.expectedVersion != currentRevision) {
            val ownerName =
                UsersTable.selectAll().where {
                    UsersTable.id eq ownerId
                }.singleOrNull()?.get(UsersTable.username)
                    ?: ""
            val owner = OwnerDto(id = ownerId, username = ownerName)
            val categories = getCategoriesForExercise(id)
            val recommendations = getRecommendationsForExercise(id)
            val currentServerExercise = ExerciseDto(
                id = id,
                name = existing[ExercisesTable.name],
                description = existing[ExercisesTable.description],
                categories = categories,
                owner = owner,
                source = existing[ExercisesTable.sourceType],
                sharing = existing[ExercisesTable.sharing],
                revision = currentRevision,
                recommendations = recommendations
            )
            throw ExerciseConflictException(currentServerExercise)
        }

        val nextRevision = currentRevision + 1L
        val version = getNextChangeVersion()
        val sharingValue = request.sharing.ifBlank { existing[ExercisesTable.sharing] }

        ExercisesTable.update({ ExercisesTable.id eq id }) {
            it[name] = request.name
            it[description] = request.description
            it[sharing] = sharingValue
            it[revision] = nextRevision
            it[changeVersion] = version
        }

        ExerciseCategoriesTable.deleteWhere { ExerciseCategoriesTable.exerciseId eq id }
        val resolvedCategoryIds = resolveCategoryIds(request.categoryIds)
        resolvedCategoryIds.forEach { catId ->
            ExerciseCategoriesTable.insert {
                it[ExerciseCategoriesTable.exerciseId] = id
                it[ExerciseCategoriesTable.categoryId] = catId
            }
        }

        request.recommendations?.let { recs ->
            ExerciseRecommendationsTable.deleteWhere {
                ExerciseRecommendationsTable.exerciseId eq id
            }
            recs.forEach { rec ->
                ExerciseRecommendationsTable.insert {
                    it[ExerciseRecommendationsTable.id] = UUID.randomUUID().toString()
                    it[ExerciseRecommendationsTable.exerciseId] = id
                    it[level] = rec.level
                    it[sets] = rec.sets
                    it[reps] = rec.reps
                    it[restTimeInMillis] = rec.restTimeInMillis
                    it[weightInKg] = rec.weightInKg
                    it[timeInMillis] = rec.timeInMillis
                }
            }
        }

        val ownerName =
            UsersTable.selectAll().where {
                UsersTable.id eq ownerId
            }.singleOrNull()?.get(UsersTable.username)
                ?: ""
        val owner = OwnerDto(id = ownerId, username = ownerName)
        val categories = getCategoriesForExercise(id)
        val recommendations = getRecommendationsForExercise(id)
        val sourceType = existing[ExercisesTable.sourceType]
        val resultDto = ExerciseDto(
            id = id,
            name = request.name,
            description = request.description,
            categories = categories,
            owner = owner,
            source = sourceType,
            sharing = sharingValue,
            revision = nextRevision,
            recommendations = recommendations
        )

        if (idempotencyKey != null) {
            val jsonPayload = Json.encodeToString(ExerciseDto.serializer(), resultDto)
            IdempotencyLogsTable.insert {
                it[IdempotencyLogsTable.ownerId] = ownerId
                it[key] = idempotencyKey
                it[responsePayload] = jsonPayload
            }
        }

        resultDto
    }

    suspend fun deleteExercise(id: String, ownerId: String): Boolean = dbQuery {
        val existing =
            ExercisesTable.selectAll().where { ExercisesTable.id eq id }.singleOrNull()
                ?: return@dbQuery false

        if (existing[ExercisesTable.ownerId] == null ||
            existing[ExercisesTable.ownerId] != ownerId
        ) {
            throw SecurityException("Not authorized to modify this exercise")
        }

        val version = getNextChangeVersion()
        ExerciseDeletionsTable.insert {
            it[ExerciseDeletionsTable.id] = UUID.randomUUID().toString()
            it[ExerciseDeletionsTable.exerciseId] = id
            it[ExerciseDeletionsTable.changeVersion] = version
        }

        ExercisesTable.deleteWhere { ExercisesTable.id eq id } > 0
    }

    private fun getCategoriesForExercise(exerciseId: String): List<CategoryDto> =
        getCategoriesForExercises(listOf(exerciseId))[exerciseId] ?: emptyList()

    private fun getRecommendationsForExercise(exerciseId: String): List<ExerciseRecommendationDto> =
        getRecommendationsForExercises(listOf(exerciseId))[exerciseId] ?: emptyList()

    private fun getRecommendationsForExercises(
        exerciseIds: List<String>
    ): Map<String, List<ExerciseRecommendationDto>> {
        if (exerciseIds.isEmpty()) return emptyMap()

        val rows = ExerciseRecommendationsTable
            .selectAll()
            .where { ExerciseRecommendationsTable.exerciseId inList exerciseIds }
            .toList()

        val resultMap = mutableMapOf<String, MutableList<ExerciseRecommendationDto>>()
        rows.forEach { row ->
            val exId = row[ExerciseRecommendationsTable.exerciseId]
            val recDto = ExerciseRecommendationDto(
                level = row[ExerciseRecommendationsTable.level],
                sets = row[ExerciseRecommendationsTable.sets],
                reps = row[ExerciseRecommendationsTable.reps],
                restTimeInMillis = row[ExerciseRecommendationsTable.restTimeInMillis],
                weightInKg = row[ExerciseRecommendationsTable.weightInKg],
                timeInMillis = row[ExerciseRecommendationsTable.timeInMillis]
            )
            resultMap.getOrPut(exId) { mutableListOf() }.add(recDto)
        }
        return resultMap
    }

    private fun getCategoriesForExercises(
        exerciseIds: List<String>
    ): Map<String, List<CategoryDto>> {
        if (exerciseIds.isEmpty()) return emptyMap()

        val categoryLinks =
            ExerciseCategoriesTable
                .selectAll()
                .where { ExerciseCategoriesTable.exerciseId inList exerciseIds }
                .map {
                    it[ExerciseCategoriesTable.exerciseId] to
                        it[ExerciseCategoriesTable.categoryId]
                }

        val categoryIds = categoryLinks.map { it.second }.distinct()
        val categoryMap =
            if (categoryIds.isNotEmpty()) {
                CategoriesTable
                    .selectAll()
                    .where { CategoriesTable.id inList categoryIds }
                    .associate {
                        it[CategoriesTable.id] to
                            CategoryDto(
                                id = it[CategoriesTable.id],
                                name = it[CategoriesTable.name]
                            )
                    }
            } else {
                emptyMap()
            }

        val resultMap = mutableMapOf<String, MutableList<CategoryDto>>()
        categoryLinks.forEach { (exId, catId) ->
            val catDto = categoryMap[catId]
            if (catDto != null) {
                resultMap.getOrPut(exId) { mutableListOf() }.add(catDto)
            }
        }
        return resultMap
    }
}
