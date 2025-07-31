package com.example.rentalinn.network

import com.example.rentalinn.model.*
import retrofit2.Response
import retrofit2.http.*

interface ChatApiService {
    
    @GET("chat/my-chat")
    suspend fun getOrCreateChat(): Response<ChatResponse>
    
    @GET("chat/all")
    suspend fun getAllChats(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ChatsResponse>
    
    @GET("chat/{chatId}/messages")
    suspend fun getChatMessages(
        @Path("chatId") chatId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<MessagesResponse>
    
    @POST("chat/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: Int,
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>
    
    @PATCH("chat/{chatId}/status")
    suspend fun updateChatStatus(
        @Path("chatId") chatId: Int,
        @Body request: Map<String, String>
    ): Response<ChatResponse>
}
