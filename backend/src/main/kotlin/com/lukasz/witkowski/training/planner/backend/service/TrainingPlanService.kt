package com.lukasz.witkowski.training.planner.backend.service

import com.lukasz.witkowski.training.planner.backend.db.DatabaseFactory.dbQuery
import com.lukasz.witkowski.training.planner.backend.db.TrainingExercisesTable
import com.lukasz.witkowski.training.planner.backend.db.TrainingPlansTable
import com.lukasz.witkowski.training.planner.dto.common.PagedResponseDto
import com.lukasz.witkowski.training.planner.dto.training.CreateTrainingPlanRequestDto
import com.lukasz.witkowski.training.planner.dto.training.TrainingExerciseDto
import com.lukasz.witkowski.training.planner.dto.training.TrainingPlanDto
import com.lukasz.witkowski.training.planner.dto.training.TrainingPlanOverviewDto
import com.lukasz.witkowski.training.planner.dto.training.UpdateTrainingPlanRequestDto
import org.jetbrains.exposed.v1.core.AndOp
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.OrOp
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class TrainingPlanService(
    private val exerciseService: ExerciseService,
) {
    suspend fun getTrainingPlans(
        search: String? = null,
        ownerId: String? = null,
        page: Int = 1,
        limit: Int = 20,
    ): PagedResponseDto<TrainingPlanOverviewDto> =
        dbQuery {
            var condition: Op<Boolean> =
                if (ownerId != null) {
                    OrOp(listOf(TrainingPlansTable.ownerId.isNull(), TrainingPlansTable.ownerId eq ownerId))
                } else {
                    TrainingPlansTable.ownerId.isNull()
                }

            if (!search.isNullOrBlank()) {
                val searchPattern = "%${search.lowercase()}%"
                condition = AndOp(listOf(condition, TrainingPlansTable.title.lowerCase() like searchPattern))
            }

            val allRows = TrainingPlansTable.selectAll().where { condition }.toList()
            val totalItems = allRows.size.toLong()
            val totalPages = if (totalItems == 0L) 1 else kotlin.math.ceil(totalItems.toDouble() / limit).toInt()
            val offset = ((page - 1) * limit).coerceAtLeast(0)

            val paginated =
                allRows.drop(offset).take(limit).map { row ->
                    val planId = row[TrainingPlansTable.id]
                    val exerciseCount =
                        TrainingExercisesTable
                            .selectAll()
                            .where { TrainingExercisesTable.trainingPlanId eq planId }
                            .toList()
                            .size
                    TrainingPlanOverviewDto(
                        id = planId,
                        title = row[TrainingPlansTable.title],
                        description = row[TrainingPlansTable.description],
                        exerciseCount = exerciseCount,
                        ownerId = row[TrainingPlansTable.ownerId],
                    )
                }

            PagedResponseDto(
                items = paginated,
                page = page,
                limit = limit,
                totalItems = totalItems,
                totalPages = totalPages,
            )
        }

    suspend fun getTrainingPlanById(id: String): TrainingPlanDto? =
        dbQuery {
            val row = TrainingPlansTable.selectAll().where { TrainingPlansTable.id eq id }.singleOrNull() ?: return@dbQuery null
            val exercises = getTrainingExercisesForPlan(id)

            TrainingPlanDto(
                id = id,
                title = row[TrainingPlansTable.title],
                description = row[TrainingPlansTable.description],
                exercises = exercises,
                restTimeInMillis = row[TrainingPlansTable.restTimeInMillis],
                ownerId = row[TrainingPlansTable.ownerId],
            )
        }

    suspend fun createTrainingPlan(
        request: CreateTrainingPlanRequestDto,
        ownerId: String,
    ): TrainingPlanDto =
        dbQuery {
            val planId = UUID.randomUUID().toString()
            TrainingPlansTable.insert {
                it[id] = planId
                it[title] = request.title
                it[description] = request.description
                it[restTimeInMillis] = request.restTimeInMillis
                it[TrainingPlansTable.ownerId] = ownerId
            }

            request.exercises.forEach { reqEx ->
                val trainingExerciseId = UUID.randomUUID().toString()
                TrainingExercisesTable.insert {
                    it[id] = trainingExerciseId
                    it[trainingPlanId] = planId
                    it[exerciseId] = reqEx.exerciseId
                    it[repetitions] = reqEx.repetitions
                    it[sets] = reqEx.sets
                    it[restTimeInMillis] = reqEx.restTimeInMillis
                    it[weightInKg] = reqEx.weightInKg
                }
            }

            getTrainingPlanById(planId)!!
        }

    suspend fun updateTrainingPlan(
        id: String,
        request: UpdateTrainingPlanRequestDto,
        ownerId: String,
    ): TrainingPlanDto =
        dbQuery {
            val existing =
                TrainingPlansTable.selectAll().where { TrainingPlansTable.id eq id }.singleOrNull()
                    ?: throw IllegalArgumentException("Training Plan $id not found.")

            if (existing[TrainingPlansTable.ownerId] != null && existing[TrainingPlansTable.ownerId] != ownerId) {
                throw SecurityException("You do not have permission to update this training plan.")
            }

            TrainingPlansTable.update({ TrainingPlansTable.id eq id }) {
                it[title] = request.title
                it[description] = request.description
                it[restTimeInMillis] = request.restTimeInMillis
            }

            TrainingExercisesTable.deleteWhere { TrainingExercisesTable.trainingPlanId eq id }
            request.exercises.forEach { reqEx ->
                val trainingExerciseId = UUID.randomUUID().toString()
                TrainingExercisesTable.insert {
                    it[this.id] = trainingExerciseId
                    it[trainingPlanId] = id
                    it[exerciseId] = reqEx.exerciseId
                    it[repetitions] = reqEx.repetitions
                    it[sets] = reqEx.sets
                    it[restTimeInMillis] = reqEx.restTimeInMillis
                    it[weightInKg] = reqEx.weightInKg
                }
            }

            getTrainingPlanById(id)!!
        }

    suspend fun deleteTrainingPlan(
        id: String,
        ownerId: String,
    ): Boolean =
        dbQuery {
            val existing =
                TrainingPlansTable.selectAll().where { TrainingPlansTable.id eq id }.singleOrNull()
                    ?: return@dbQuery false

            if (existing[TrainingPlansTable.ownerId] != null && existing[TrainingPlansTable.ownerId] != ownerId) {
                throw SecurityException("You do not have permission to delete this training plan.")
            }

            TrainingPlansTable.deleteWhere { TrainingPlansTable.id eq id } > 0
        }

    private suspend fun getTrainingExercisesForPlan(planId: String): List<TrainingExerciseDto> {
        val rows = TrainingExercisesTable.selectAll().where { TrainingExercisesTable.trainingPlanId eq planId }.toList()
        return rows.mapNotNull { row ->
            val exerciseId = row[TrainingExercisesTable.exerciseId]
            val exerciseDto = exerciseService.getExerciseById(exerciseId) ?: return@mapNotNull null
            TrainingExerciseDto(
                id = row[TrainingExercisesTable.id],
                exercise = exerciseDto,
                repetitions = row[TrainingExercisesTable.repetitions],
                sets = row[TrainingExercisesTable.sets],
                restTimeInMillis = row[TrainingExercisesTable.restTimeInMillis],
                weightInKg = row[TrainingExercisesTable.weightInKg],
            )
        }
    }
}
