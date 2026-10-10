package com.lukasz.witkowski.training.planner.backend.service

import com.lukasz.witkowski.training.planner.backend.auth.JwtConfig
import com.lukasz.witkowski.training.planner.backend.auth.PasswordHasher
import com.lukasz.witkowski.training.planner.backend.db.DatabaseFactory.dbQuery
import com.lukasz.witkowski.training.planner.backend.db.UsersTable
import com.lukasz.witkowski.training.planner.backend.exception.AuthException
import com.lukasz.witkowski.training.planner.dto.auth.AuthResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.LoginRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RegisterRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.TokenResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.UserDto
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.slf4j.LoggerFactory
import java.util.UUID

class AuthService {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    suspend fun register(request: RegisterRequestDto): AuthResponseDto {
        logger.info("Register attempt for email: {}, username: {}", request.email, request.username)
        return dbQuery {
            val existingUser = UsersTable.selectAll().where { UsersTable.email eq request.email }.singleOrNull()
            if (existingUser != null) {
                logger.warn("Register failed: User already exists for email: {}", request.email)
                throw AuthException.UserAlreadyExists(request.email)
            }

            val userId = UUID.randomUUID().toString()
            val hashedPassword = PasswordHasher.hashPassword(request.password)

            UsersTable.insert {
                it[id] = userId
                it[email] = request.email
                it[username] = request.username
                it[passwordHash] = hashedPassword
            }

            logger.info("User registered successfully with userId: {}", userId)
            val userDto = UserDto(id = userId, email = request.email, username = request.username)
            val tokens = generateTokens(userId, request.email, request.username)
            AuthResponseDto(user = userDto, tokens = tokens)
        }
    }

    suspend fun login(request: LoginRequestDto): AuthResponseDto {
        logger.info("Login attempt for email: {}", request.email)
        return dbQuery {
            val row =
                UsersTable.selectAll().where { UsersTable.email eq request.email }.singleOrNull()
                    ?: run {
                        logger.warn("Login failed: User not found for email: {}", request.email)
                        throw AuthException.UserNotFound("User with email ${request.email} not found.")
                    }

            val hashedPassword = row[UsersTable.passwordHash]
            if (!PasswordHasher.verifyPassword(request.password, hashedPassword)) {
                logger.warn("Login failed: Incorrect password for email: {}", request.email)
                throw AuthException.IncorrectPassword()
            }

            val userId = row[UsersTable.id]
            val username = row[UsersTable.username]
            logger.info("Login successful for userId: {}", userId)
            val userDto = UserDto(id = userId, email = request.email, username = username)
            val tokens = generateTokens(userId, request.email, username)

            AuthResponseDto(user = userDto, tokens = tokens)
        }
    }

    suspend fun refreshToken(refreshToken: String): TokenResponseDto {
        logger.info("Token refresh attempt")
        return dbQuery {
            val decoded =
                try {
                    JwtConfig.verifier.verify(refreshToken)
                } catch (e: Exception) {
                    logger.warn("Token refresh failed: Invalid token - {}", e.message)
                    throw AuthException.InvalidRefreshToken()
                }

            val userId = decoded.subject ?: run {
                logger.warn("Token refresh failed: Missing subject claim in token")
                throw AuthException.InvalidRefreshToken("Invalid token subject.")
            }
            val row =
                UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()
                    ?: run {
                        logger.warn("Token refresh failed: User not found for userId: {}", userId)
                        throw AuthException.UserNotFound("User with ID $userId not found.")
                    }

            logger.info("Token refreshed successfully for userId: {}", userId)
            generateTokens(userId, row[UsersTable.email], row[UsersTable.username])
        }
    }

    suspend fun getUserById(userId: String): UserDto? {
        logger.info("Fetching user profile for userId: {}", userId)
        return dbQuery {
            UsersTable
                .selectAll()
                .where { UsersTable.id eq userId }
                .singleOrNull()
                ?.toUserDto()
        }
    }

    private fun generateTokens(
        userId: String,
        email: String,
        username: String,
    ): TokenResponseDto {
        val accessToken = JwtConfig.generateAccessToken(userId, email, username)
        val refreshToken = JwtConfig.generateRefreshToken(userId)
        return TokenResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresInMs = JwtConfig.ACCESS_TOKEN_EXPIRATION_MS,
        )
    }

    private fun ResultRow.toUserDto(): UserDto =
        UserDto(
            id = this[UsersTable.id],
            email = this[UsersTable.email],
            username = this[UsersTable.username],
        )
}
