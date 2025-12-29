package com.example.unknown.api

import com.example.unknown.CreatePostRequest
import retrofit2.Call
import retrofit2.http.*

interface
UnknownInterface {
    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("/api/register")
    fun register(
        @Field("nickname") nickname: String,
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<SignUpResponse>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("/api/login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<LoginResponse>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("/api/change_nick")
    fun changeNickname(
        @Field("nickname") nickname: String,
    ): Call<Void>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("/api/change_pass")
    fun changePassword(
        @Field("current_password") current_password: String,
        @Field("new_password") new_password: String,
    ): Call<Void>

    @DELETE("/api/delete_account")
    fun deleteAccount(
        @Header("Authorization") token: String
    ): Call<Void>

    @GET("/api/post")
    fun getPosts(): Call<List<Post>>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("/api/post")
    fun post(
        @Field("post") post: String
    ): Call<Post>

    @DELETE("/api/post/{id}")
    fun deletePost(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Void>

    @GET("/api/posts/{postId}/comments")
    fun getComments(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int
    ): Call<List<Comment>>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("/api/posts/{postId}/comments")
    fun comment(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int,
        @Field("comments") comment: String
    ): Call<CommentResponse>

    @DELETE("/api/comments/{id}")
    fun deleteComment(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Void>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("/api/feedback")
    fun createFeedback(
        @Field("suggestion") suggestion: String,
        @Field("additional_feedback") additional_feedback: String
    ): Call<Void>
}