package com.lukasz.witkowski.training.planner.auth.domain.model

data class SignUpForm(
    val username: String,
    val email: String,
    val password: String,
) {
    fun isValid(): Boolean {
        return username.isNotBlank() && isValidEmail() && password.length >= 6
    }

    fun isValidEmail(): Boolean {
        return email.isNotBlank() && email.contains("@") && email.contains(".")
    }
}
