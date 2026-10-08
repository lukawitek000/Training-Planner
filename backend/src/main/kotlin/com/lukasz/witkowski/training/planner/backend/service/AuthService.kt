package com.lukasz.witkowski.training.planner.backend.service

import com.lukasz.witkowski.training.planner.backend.auth.JwtConfig
import com.lukasz.witkowski.training.planner.backend.auth.PasswordHasher
import com.lukasz.witkowski.training.planner.backend.db.DatabaseFactory.dbQuery
import com.lukasz.witkowski.training.planner.backend.db.UsersTable
import com.lukasz.witkowski.training.planner.dto.auth.AuthResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.LoginRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RegisterRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.TokenResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.UserDto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class AuthService {

    suspend fun register(request: RegisterRequestDto): AuthResponseDto = dbQuery {
        val existingUser = UsersTable.selectAll().where { UsersTable.email eq request.email }.singleOrNull()
        if (existingUser != null) {
            throw IllegalArgumentException("User with email ${request.email} already exists.")
        }

        val userId = UUID.randomUUID().toString()
        val hashedPassword = PasswordHasher.hashPassword(request.password)

        UsersTable.insert {
            it[id] = userId
            it[email] = request.email
            it[username] = request.username
            it[passwordHash] = hashedPassword
        }

        val userDto = UserDto(id = userId, email = request.email, username = request.username)
        val tokens = generateTokens(userId, request.email, request.username)
        AuthResponseDto(user = userDto, tokens = tokens)
    }

    suspend fun login(request: LoginRequestDto): AuthResponseDto = dbQuery {
        val row = UsersTable.selectAll().where { UsersTable.email eq request.email }.singleOrNull()
            ?: throw IllegalArgumentException("Invalid email or password.")

        val hashedPassword = row[UsersTable.passwordHash]
        if (!PasswordHasher.verifyPassword(request.password, hashedPassword)) {
            throw IllegalArgumentException("Invalid email or password.")
        }

        val userId = row[UsersTable.id]
        val username = row[UsersTable.username]
        val userDto = UserDto(id = userId, email = request.email, username = username)
        val tokens = generateTokens(userId, request.email, username)

        AuthResponseDto(user = userDto, tokens = tokens)
    }

    suspend fun refreshToken(refreshToken: String): TokenResponseDto = dbQuery {
        val decoded = try {
            JwtConfig.verifier.verify(refreshToken)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid or expired refresh token.")
        }

        val userId = decoded.subject ?: throw IllegalArgumentException("Invalid token subject.")
        val row = UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()
            ?: throw IllegalArgumentException("User not found.")

        generateTokens(userId, row[UsersTable.email], row[UsersTable.username])
    }

    suspend fun getUserById(userId: String): UserDto? = dbQuery {
        UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()?.toUserDto()
    }

    private fun generateTokens(userId: String, email: String, username: String): TokenResponseDto {
        val accessToken = JwtConfig.generateAccessToken(userId, email, username)
        val refreshToken = JwtConfig.generateRefreshToken(userId)
        return TokenResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresInMs = JwtConfig.ACCESS_TOKEN_EXPIRATION_MS
        )
    }

    private fun ResultRow.toUserDto(): UserDto = UserDto(
        id = this[UsersTable.id],
        email = this[UsersTable.email],
        username = this[UsersTable.username]
    )
}
