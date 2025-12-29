package com.example.unknown.api

data class PostingResponse(
    val success: Boolean,
    val message: String?,
    val post: Post? // Assuming 'Post' is the data class representing the post you created
)
