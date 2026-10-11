package com.lukasz.witkowski.training.planner.auth.domain.model

data class SignUpForm(val username: String, val email: String, val password: String) {
    fun isValid(): Boolean = username.isNotBlank() && isValidEmail() && password.length >= 6

    fun isValidEmail(): Boolean = email.isNotBlank() && email.contains("@") && email.contains(".")
}
