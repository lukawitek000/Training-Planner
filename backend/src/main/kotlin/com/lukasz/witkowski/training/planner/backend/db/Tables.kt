package com.lukasz.witkowski.training.planner.backend.db

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object UsersTable : Table("users") {
    val id = varchar("id", 36)
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 255)
    val passwordHash = varchar("password_hash", 255)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object CategoriesTable : Table("categories") {
    val id = varchar("id", 36)
    val name = varchar("name", 100).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}

object ExercisesTable : Table("exercises") {
    val id = varchar("id", 36)
    val name = varchar("name", 255)
    val description = text("description").default("")
    val ownerId = varchar(
        "owner_id",
        36
    ).references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val sourceType = varchar("source_type", 50).default("BUNDLED")
    val sharing = varchar("sharing", 50).default("PRIVATE")
    val revision = long("revision").default(1L)
    val changeVersion = long("change_version").default(1L)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object ExerciseDeletionsTable : Table("exercise_deletions") {
    val id = varchar("id", 36)
    val exerciseId = varchar("exercise_id", 36)
    val changeVersion = long("change_version")
    val deletedAt = timestamp("deleted_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object IdempotencyLogsTable : Table("idempotency_logs") {
    val key = varchar("key", 64)
    val responsePayload = text("response_payload")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(key)
}

object ExerciseCategoriesTable : Table("exercise_categories") {
    val exerciseId = varchar(
        "exercise_id",
        36
    ).references(ExercisesTable.id, onDelete = ReferenceOption.CASCADE)
    val categoryId = varchar(
        "category_id",
        36
    ).references(CategoriesTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(exerciseId, categoryId)
}

object ExerciseRecommendationsTable : Table("exercise_recommendations") {
    val id = varchar("id", 36)
    val exerciseId = varchar("exercise_id", 36).references(ExercisesTable.id, onDelete = ReferenceOption.CASCADE)
    val level = varchar("level", 20)
    val sets = integer("sets")
    val reps = integer("reps")
    val restTimeInMillis = long("rest_time_in_millis")
    val weightInKg = integer("weight_in_kg").nullable()

    override val primaryKey = PrimaryKey(id)
}

object TrainingPlansTable : Table("training_plans") {
    val id = varchar("id", 36)
    val title = varchar("title", 255)
    val description = text("description").default("")
    val restTimeInMillis = long("rest_time_in_millis").default(0)
    val ownerId = varchar(
        "owner_id",
        36
    ).references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object TrainingExercisesTable : Table("training_exercises") {
    val id = varchar("id", 36)
    val trainingPlanId = varchar(
        "training_plan_id",
        36
    ).references(TrainingPlansTable.id, onDelete = ReferenceOption.CASCADE)
    val exerciseId = varchar(
        "exercise_id",
        36
    ).references(ExercisesTable.id, onDelete = ReferenceOption.CASCADE)
    val repetitions = integer("repetitions").default(1)
    val sets = integer("sets").default(1)
    val restTimeInMillis = long("rest_time_in_millis").default(0)
    val weightInKg = double("weight_in_kg").nullable()

    override val primaryKey = PrimaryKey(id)
}
