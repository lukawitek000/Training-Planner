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
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
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

    private fun getNextChangeVersion(): Long {
        val maxEx = ExercisesTable.selectAll().map { it[ExercisesTable.changeVersion] }.maxOrNull() ?: 0L
        val maxDel = ExerciseDeletionsTable.selectAll().map { it[ExerciseDeletionsTable.changeVersion] }.maxOrNull() ?: 0L
        return maxOf(maxEx, maxDel) + 1L
    }

    suspend fun getExerciseChanges(
        sinceVersion: Long,
        requesterId: String? = null
    ): ExerciseSyncResponseDto = dbQuery {
        val visibilityCondition: Op<Boolean> = if (requesterId != null) {
            OrOp(listOf(
                ExercisesTable.ownerId.isNull(),
                ExercisesTable.ownerId eq requesterId,
                ExercisesTable.sharing eq "COMMUNITY"
            ))
        } else {
            OrOp(listOf(
                ExercisesTable.ownerId.isNull(),
                ExercisesTable.sharing eq "COMMUNITY"
            ))
        }

        val condition = AndOp(listOf(
            ExercisesTable.changeVersion greater sinceVersion,
            visibilityCondition
        ))

        val queryJoin = ExercisesTable.leftJoin(UsersTable, { ExercisesTable.ownerId }, { UsersTable.id })
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

        val maxExVersion = ExercisesTable.selectAll().map { it[ExercisesTable.changeVersion] }.maxOrNull() ?: sinceVersion
        val maxDelVersion = ExerciseDeletionsTable.selectAll().map { it[ExerciseDeletionsTable.changeVersion] }.maxOrNull() ?: sinceVersion
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
        var condition: Op<Boolean> =
            if (ownerId != null) {
                OrOp(listOf(ExercisesTable.ownerId.isNull(), ExercisesTable.ownerId eq ownerId))
            } else {
                ExercisesTable.ownerId.isNull()
            }

        if (!query.isNullOrBlank()) {
            val queryPattern = "%${query.lowercase()}%"
            condition = AndOp(listOf(condition, ExercisesTable.name.lowerCase() like queryPattern))
        }

        var matchingIds: List<String>? = null
        if (!categoryId.isNullOrBlank()) {
            matchingIds =
                ExerciseCategoriesTable
                    .selectAll()
                    .where { ExerciseCategoriesTable.categoryId eq categoryId }
                    .map { it[ExerciseCategoriesTable.exerciseId] }
        }

        val queryJoin = ExercisesTable.leftJoin(UsersTable, { ExercisesTable.ownerId }, { UsersTable.id })
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
            kotlin.math.ceil(totalItems.toDouble() / limit).toInt()
        }
        val offset = ((page - 1) * limit).coerceAtLeast(0)

        val pageRows = allRows.drop(offset).take(limit)
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
            page = page,
            limit = limit,
            totalItems = totalItems,
            totalPages = totalPages
        )
    }

    suspend fun getExerciseById(id: String, requesterId: String? = null): ExerciseDto? = dbQuery {
        val queryJoin = ExercisesTable.leftJoin(UsersTable, { ExercisesTable.ownerId }, { UsersTable.id })
        val row =
            queryJoin.selectAll().where { ExercisesTable.id eq id }.singleOrNull()
                ?: return@dbQuery null
        val ownerId = row[ExercisesTable.ownerId]
        if (ownerId != null && ownerId != requesterId) {
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
            sharing = row[ExercisesTable.sharing],
            revision = row[ExercisesTable.revision],
            recommendations = recommendations
        )
    }

    suspend fun createExercise(request: CreateExerciseRequestDto, ownerId: String): ExerciseDto =
        dbQuery {
            val exerciseId = UUID.randomUUID().toString()
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

            request.categoryIds.forEach { catId ->
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
                }
            }

            val ownerName = UsersTable.selectAll().where { UsersTable.id eq ownerId }.singleOrNull()?.get(UsersTable.username) ?: ""
            val owner = OwnerDto(id = ownerId, username = ownerName)
            val categories = getCategoriesForExercise(exerciseId)
            val recommendations = getRecommendationsForExercise(exerciseId)
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
        }

    suspend fun updateExercise(
        id: String,
        request: UpdateExerciseRequestDto,
        ownerId: String
    ): ExerciseDto = dbQuery {
        if (!request.idempotencyKey.isNullOrBlank()) {
            val existingLog = IdempotencyLogsTable.selectAll()
                .where { IdempotencyLogsTable.key eq request.idempotencyKey }
                .singleOrNull()
            if (existingLog != null) {
                val jsonPayload = existingLog[IdempotencyLogsTable.responsePayload]
                return@dbQuery Json.decodeFromString<ExerciseDto>(jsonPayload)
            }
        }

        val existing =
            ExercisesTable.selectAll().where { ExercisesTable.id eq id }.singleOrNull()
                ?: throw IllegalArgumentException("Exercise $id not found.")

        if (existing[ExercisesTable.ownerId] != null &&
            existing[ExercisesTable.ownerId] != ownerId
        ) {
            throw SecurityException("You do not have permission to update this exercise.")
        }

        val currentRevision = existing[ExercisesTable.revision]
        if (request.expectedVersion != null && request.expectedVersion != currentRevision) {
            val ownerName = UsersTable.selectAll().where { UsersTable.id eq ownerId }.singleOrNull()?.get(UsersTable.username) ?: ""
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
        request.categoryIds.forEach { catId ->
            ExerciseCategoriesTable.insert {
                it[ExerciseCategoriesTable.exerciseId] = id
                it[ExerciseCategoriesTable.categoryId] = catId
            }
        }

        if (request.recommendations != null) {
            ExerciseRecommendationsTable.deleteWhere { ExerciseRecommendationsTable.exerciseId eq id }
            request.recommendations.forEach { rec ->
                ExerciseRecommendationsTable.insert {
                    it[ExerciseRecommendationsTable.id] = UUID.randomUUID().toString()
                    it[ExerciseRecommendationsTable.exerciseId] = id
                    it[level] = rec.level
                    it[sets] = rec.sets
                    it[reps] = rec.reps
                    it[restTimeInMillis] = rec.restTimeInMillis
                    it[weightInKg] = rec.weightInKg
                }
            }
        }

        val ownerName = UsersTable.selectAll().where { UsersTable.id eq ownerId }.singleOrNull()?.get(UsersTable.username) ?: ""
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

        if (!request.idempotencyKey.isNullOrBlank()) {
            val jsonPayload = Json.encodeToString(ExerciseDto.serializer(), resultDto)
            IdempotencyLogsTable.insert {
                it[key] = request.idempotencyKey
                it[responsePayload] = jsonPayload
            }
        }

        resultDto
    }

    suspend fun deleteExercise(id: String, ownerId: String): Boolean = dbQuery {
        val existing =
            ExercisesTable.selectAll().where { ExercisesTable.id eq id }.singleOrNull()
                ?: return@dbQuery false

        if (existing[ExercisesTable.ownerId] != null &&
            existing[ExercisesTable.ownerId] != ownerId
        ) {
            throw SecurityException("You do not have permission to delete this exercise.")
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
                weightInKg = row[ExerciseRecommendationsTable.weightInKg]
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
