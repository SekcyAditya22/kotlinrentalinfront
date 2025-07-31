package com.example.rentalinn.model

import com.google.gson.annotations.SerializedName

data class Chat(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("admin_id")
    val adminId: Int?,
    val status: String,
    @SerializedName("last_message_at")
    val lastMessageAt: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val user: User?,
    val admin: User?,
    @SerializedName("lastMessage")
    val lastMessage: ChatMessage?,
    @SerializedName("unreadCount")
    val unreadCount: Int = 0
)

data class ChatMessage(
    val id: Int,
    @SerializedName("chat_id")
    val chatId: Int,
    @SerializedName("sender_id")
    val senderId: Int,
    val message: String,
    @SerializedName("message_type")
    val messageType: String = "text",
    @SerializedName("file_url")
    val fileUrl: String?,
    @SerializedName("is_read")
    val isRead: Boolean = false,
    @SerializedName("read_at")
    val readAt: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val sender: User?
) {
    fun isFromCurrentUser(currentUserId: Int): Boolean {
        return senderId == currentUserId
    }
    
    fun isFromAdmin(): Boolean {
        return sender?.role == "admin"
    }
    
    fun getFormattedTime(): String {
        // Simple time formatting - you can enhance this
        return try {
            val time = createdAt.substring(11, 16) // Extract HH:MM from ISO string
            time
        } catch (e: Exception) {
            "Now"
        }
    }
}

// Request/Response models
data class ChatResponse(
    val status: String,
    val data: ChatData
)

data class ChatData(
    val chat: Chat
)

data class ChatsResponse(
    val status: String,
    val data: ChatsData
)

data class ChatsData(
    val chats: List<Chat>,
    val pagination: ChatPagination
)

data class MessagesResponse(
    val status: String,
    val data: MessagesData
)

data class MessagesData(
    val messages: List<ChatMessage>,
    val pagination: ChatPagination
)

data class SendMessageRequest(
    val message: String,
    @SerializedName("messageType")
    val messageType: String = "text",
    @SerializedName("fileUrl")
    val fileUrl: String? = null
)

data class SendMessageResponse(
    val status: String,
    val data: MessageData
)

data class MessageData(
    val message: ChatMessage
)

data class ChatPagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    @SerializedName("totalPages")
    val totalPages: Int
)
