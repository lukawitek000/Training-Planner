package com.lukasz.witkowski.training.planner.auth.domain.model

data class SignInForm(
    val email: String,
    val password: String,
) {
    fun isValid(): Boolean {
        return isValidEmail() && password.isNotBlank()
    }

    fun isValidEmail(): Boolean {
        return email.isNotBlank() && email.contains("@") && email.contains(".")
    }
}
