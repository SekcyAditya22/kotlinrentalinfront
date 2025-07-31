package com.example.rentalinn.repository

import android.util.Log
import com.example.rentalinn.model.*
import com.example.rentalinn.network.ChatApiService
import com.example.rentalinn.utils.DataStoreManager
import com.example.rentalinn.utils.SocketManager
import kotlinx.coroutines.flow.first

class ChatRepository(
    private val chatApiService: ChatApiService,
    private val dataStoreManager: DataStoreManager,
    private val socketManager: SocketManager
) {
    
    companion object {
        private const val TAG = "ChatRepository"
    }
    
    // Initialize socket connection
    suspend fun initializeSocket() {
        try {
            val token = dataStoreManager.token.first()
            if (!token.isNullOrEmpty()) {
                socketManager.connect(token)
                Log.d(TAG, "Socket initialized with token")
            } else {
                Log.w(TAG, "No token available for socket connection")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing socket", e)
        }
    }
    
    // Disconnect socket
    fun disconnectSocket() {
        socketManager.disconnect()
    }
    
    // Get or create chat for current user
    suspend fun getOrCreateChat(): Result<Chat> {
        return try {
            val response = chatApiService.getOrCreateChat()
            if (response.isSuccessful) {
                response.body()?.let { chatResponse ->
                    Result.success(chatResponse.data.chat)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting/creating chat", e)
            Result.failure(e)
        }
    }
    
    // Get all chats (for admin)
    suspend fun getAllChats(page: Int = 1, limit: Int = 20): Result<ChatsData> {
        return try {
            val response = chatApiService.getAllChats(page, limit)
            if (response.isSuccessful) {
                response.body()?.let { chatsResponse ->
                    Result.success(chatsResponse.data)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all chats", e)
            Result.failure(e)
        }
    }
    
    // Get messages for a chat
    suspend fun getChatMessages(chatId: Int, page: Int = 1, limit: Int = 50): Result<MessagesData> {
        return try {
            val response = chatApiService.getChatMessages(chatId, page, limit)
            if (response.isSuccessful) {
                response.body()?.let { messagesResponse ->
                    Result.success(messagesResponse.data)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chat messages", e)
            Result.failure(e)
        }
    }
    
    // Send message via REST API (fallback)
    suspend fun sendMessageRest(chatId: Int, message: String, messageType: String = "text", fileUrl: String? = null): Result<ChatMessage> {
        return try {
            val request = SendMessageRequest(message, messageType, fileUrl)
            val response = chatApiService.sendMessage(chatId, request)
            if (response.isSuccessful) {
                response.body()?.let { messageResponse ->
                    Result.success(messageResponse.data.message)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message via REST", e)
            Result.failure(e)
        }
    }
    
    // Socket operations
    fun joinChat(chatId: Int) {
        socketManager.joinChat(chatId)
    }
    
    fun leaveChat(chatId: Int) {
        socketManager.leaveChat(chatId)
    }
    
    fun sendMessage(chatId: Int, message: String, messageType: String = "text", fileUrl: String? = null) {
        if (socketManager.isConnected()) {
            socketManager.sendMessage(chatId, message, messageType, fileUrl)
        } else {
            Log.w(TAG, "Socket not connected, message not sent")
        }
    }
    
    fun sendTyping(chatId: Int, isTyping: Boolean) {
        if (socketManager.isConnected()) {
            socketManager.sendTyping(chatId, isTyping)
        }
    }
    
    fun markAsRead(chatId: Int) {
        if (socketManager.isConnected()) {
            socketManager.markAsRead(chatId)
        }
    }
    
    // Socket state flows
    val connectionState = socketManager.connectionState
    val newMessage = socketManager.newMessage
    val userTyping = socketManager.userTyping
    val messagesRead = socketManager.messagesRead
    val chatUpdated = socketManager.chatUpdated
    val socketError = socketManager.error
    
    // Clear socket states
    fun clearNewMessage() = socketManager.clearNewMessage()
    fun clearTyping() = socketManager.clearTyping()
    fun clearChatUpdated() = socketManager.clearChatUpdated()
    fun clearSocketError() = socketManager.clearError()
}
