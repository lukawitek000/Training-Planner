package com.lukasz.witkowski.training.planner.backend.service

import com.lukasz.witkowski.training.planner.backend.db.CategoriesTable
import com.lukasz.witkowski.training.planner.backend.db.DatabaseFactory.dbQuery
import com.lukasz.witkowski.training.planner.backend.db.ExerciseCategoriesTable
import com.lukasz.witkowski.training.planner.backend.db.ExercisesTable
import com.lukasz.witkowski.training.planner.dto.common.PagedResponseDto
import com.lukasz.witkowski.training.planner.dto.exercise.CategoryDto
import com.lukasz.witkowski.training.planner.dto.exercise.CreateExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.UpdateExerciseRequestDto
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import java.util.UUID

class ExerciseService {

    suspend fun getCategories(): List<CategoryDto> = dbQuery {
        CategoriesTable.selectAll().map {
            CategoryDto(id = it[CategoriesTable.id], name = it[CategoriesTable.name])
        }
    }

    suspend fun ensureDefaultCategoriesExist(): Unit = dbQuery {
        val defaultCategories = listOf(
            "Chest", "Back", "Legs", "Shoulders", "Arms", "Abs", "Cardio", "Full Body"
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

    suspend fun getExercises(
        query: String? = null,
        categoryId: String? = null,
        ownerId: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): PagedResponseDto<ExerciseDto> = dbQuery {
        var condition: Op<Boolean> = if (ownerId != null) {
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
            matchingIds = ExerciseCategoriesTable
                .selectAll()
                .where { ExerciseCategoriesTable.categoryId eq categoryId }
                .map { it[ExerciseCategoriesTable.exerciseId] }
        }

        val allRows = ExercisesTable.selectAll().where { condition }.toList()
            .filter { row -> matchingIds == null || row[ExercisesTable.id] in matchingIds }

        val totalItems = allRows.size.toLong()
        val totalPages = if (totalItems == 0L) 1 else kotlin.math.ceil(totalItems.toDouble() / limit).toInt()
        val offset = ((page - 1) * limit).coerceAtLeast(0)

        val paginatedExercises = allRows.drop(offset).take(limit).map { row ->
            val exerciseId = row[ExercisesTable.id]
            val categories = getCategoriesForExercise(exerciseId)
            ExerciseDto(
                id = exerciseId,
                name = row[ExercisesTable.name],
                description = row[ExercisesTable.description],
                categories = categories,
                ownerId = row[ExercisesTable.ownerId]
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

    suspend fun getExerciseById(id: String): ExerciseDto? = dbQuery {
        val row = ExercisesTable.selectAll().where { ExercisesTable.id eq id }.singleOrNull() ?: return@dbQuery null
        val categories = getCategoriesForExercise(id)
        ExerciseDto(
            id = id,
            name = row[ExercisesTable.name],
            description = row[ExercisesTable.description],
            categories = categories,
            ownerId = row[ExercisesTable.ownerId]
        )
    }

    suspend fun createExercise(request: CreateExerciseRequestDto, ownerId: String): ExerciseDto = dbQuery {
        val exerciseId = UUID.randomUUID().toString()
        ExercisesTable.insert {
            it[id] = exerciseId
            it[name] = request.name
            it[description] = request.description
            it[ExercisesTable.ownerId] = ownerId
        }

        request.categoryIds.forEach { catId ->
            ExerciseCategoriesTable.insert {
                it[ExerciseCategoriesTable.exerciseId] = exerciseId
                it[ExerciseCategoriesTable.categoryId] = catId
            }
        }

        val categories = getCategoriesForExercise(exerciseId)
        ExerciseDto(
            id = exerciseId,
            name = request.name,
            description = request.description,
            categories = categories,
            ownerId = ownerId
        )
    }

    suspend fun updateExercise(id: String, request: UpdateExerciseRequestDto, ownerId: String): ExerciseDto = dbQuery {
        val existing = ExercisesTable.selectAll().where { ExercisesTable.id eq id }.singleOrNull()
            ?: throw IllegalArgumentException("Exercise $id not found.")

        if (existing[ExercisesTable.ownerId] != null && existing[ExercisesTable.ownerId] != ownerId) {
            throw SecurityException("You do not have permission to update this exercise.")
        }

        ExercisesTable.update({ ExercisesTable.id eq id }) {
            it[name] = request.name
            it[description] = request.description
        }

        ExerciseCategoriesTable.deleteWhere { ExerciseCategoriesTable.exerciseId eq id }
        request.categoryIds.forEach { catId ->
            ExerciseCategoriesTable.insert {
                it[ExerciseCategoriesTable.exerciseId] = id
                it[ExerciseCategoriesTable.categoryId] = catId
            }
        }

        val categories = getCategoriesForExercise(id)
        ExerciseDto(
            id = id,
            name = request.name,
            description = request.description,
            categories = categories,
            ownerId = existing[ExercisesTable.ownerId]
        )
    }

    suspend fun deleteExercise(id: String, ownerId: String): Boolean = dbQuery {
        val existing = ExercisesTable.selectAll().where { ExercisesTable.id eq id }.singleOrNull()
            ?: return@dbQuery false

        if (existing[ExercisesTable.ownerId] != null && existing[ExercisesTable.ownerId] != ownerId) {
            throw SecurityException("You do not have permission to delete this exercise.")
        }

        ExercisesTable.deleteWhere { ExercisesTable.id eq id } > 0
    }

    private fun getCategoriesForExercise(exerciseId: String): List<CategoryDto> {
        val categoryIds = ExerciseCategoriesTable
            .selectAll()
            .where { ExerciseCategoriesTable.exerciseId eq exerciseId }
            .map { it[ExerciseCategoriesTable.categoryId] }

        return CategoriesTable
            .selectAll()
            .where { CategoriesTable.id inList categoryIds }
            .map { CategoryDto(id = it[CategoriesTable.id], name = it[CategoriesTable.name]) }
    }
}
