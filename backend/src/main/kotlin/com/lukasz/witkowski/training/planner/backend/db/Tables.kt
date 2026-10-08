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
    val ownerId = varchar("owner_id", 36).references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object ExerciseCategoriesTable : Table("exercise_categories") {
    val exerciseId = varchar("exercise_id", 36).references(ExercisesTable.id, onDelete = ReferenceOption.CASCADE)
    val categoryId = varchar("category_id", 36).references(CategoriesTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(exerciseId, categoryId)
}

object TrainingPlansTable : Table("training_plans") {
    val id = varchar("id", 36)
    val title = varchar("title", 255)
    val description = text("description").default("")
    val restTimeInMillis = long("rest_time_in_millis").default(0)
    val ownerId = varchar("owner_id", 36).references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object TrainingExercisesTable : Table("training_exercises") {
    val id = varchar("id", 36)
    val trainingPlanId = varchar("training_plan_id", 36).references(TrainingPlansTable.id, onDelete = ReferenceOption.CASCADE)
    val exerciseId = varchar("exercise_id", 36).references(ExercisesTable.id, onDelete = ReferenceOption.CASCADE)
    val repetitions = integer("repetitions").default(1)
    val sets = integer("sets").default(1)
    val restTimeInMillis = long("rest_time_in_millis").default(0)
    val weightInKg = double("weight_in_kg").nullable()

    override val primaryKey = PrimaryKey(id)
}
