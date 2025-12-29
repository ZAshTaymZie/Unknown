package com.example.unknown.api

data class LoginResponse(
    val message: String,
    val token: String,
    val data: User?
)
