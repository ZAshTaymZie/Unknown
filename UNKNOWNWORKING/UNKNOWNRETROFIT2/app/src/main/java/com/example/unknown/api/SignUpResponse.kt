package com.example.unknown.api

data class SignUpResponse(
    val message: String,
    val token: String,
    val data: User?
)
