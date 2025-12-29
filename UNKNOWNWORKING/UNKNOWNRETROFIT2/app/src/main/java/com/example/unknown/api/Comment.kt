package com.example.unknown.api

data class Comment(
    val id: Int,
    val post_id: Int,
    val user_id: Int,
    val comment: String
)
