package com.example.rentalinn.utils

import android.util.Log
import com.example.rentalinn.model.ChatMessage
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.net.URISyntaxException

class SocketManager private constructor() {
    
    companion object {
        private const val TAG = "SocketManager"
        private const val SERVER_URL = "https://beexpress.peachy.icu"
        
        @Volatile
        private var INSTANCE: SocketManager? = null
        
        fun getInstance(): SocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SocketManager().also { INSTANCE = it }
            }
        }
    }
    
    private var socket: Socket? = null
    private val gson = Gson()
    
    // State flows for real-time updates
    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()
    
    private val _newMessage = MutableStateFlow<ChatMessage?>(null)
    val newMessage: StateFlow<ChatMessage?> = _newMessage.asStateFlow()
    
    private val _userTyping = MutableStateFlow<TypingInfo?>(null)
    val userTyping: StateFlow<TypingInfo?> = _userTyping.asStateFlow()
    
    private val _messagesRead = MutableStateFlow<ReadInfo?>(null)
    val messagesRead: StateFlow<ReadInfo?> = _messagesRead.asStateFlow()

    private val _chatUpdated = MutableStateFlow<ChatUpdateInfo?>(null)
    val chatUpdated: StateFlow<ChatUpdateInfo?> = _chatUpdated.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun connect(token: String) {
        try {
            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                transports = arrayOf("websocket")
            }
            
            socket = IO.socket(SERVER_URL, options)
            
            socket?.let { socket ->
                socket.on(Socket.EVENT_CONNECT) {
                    Log.d(TAG, "Socket connected")
                    _connectionState.value = true
                }
                
                socket.on(Socket.EVENT_DISCONNECT) {
                    Log.d(TAG, "Socket disconnected")
                    _connectionState.value = false
                }
                
                socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.e(TAG, "Socket connection error: ${args.contentToString()}")
                    _connectionState.value = false
                    _error.value = "Connection failed: ${args.firstOrNull()}"
                }
                
                socket.on("new_message") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val messageJson = data.getJSONObject("message")
                        val message = gson.fromJson(messageJson.toString(), ChatMessage::class.java)
                        _newMessage.value = message
                        Log.d(TAG, "New message received: ${message.message}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing new message", e)
                    }
                }
                
                socket.on("user_typing") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val typingInfo = TypingInfo(
                            userId = data.getInt("userId"),
                            userName = data.getString("userName"),
                            userRole = data.optString("userRole", "user"),
                            isTyping = data.getBoolean("isTyping")
                        )
                        _userTyping.value = typingInfo
                        Log.d(TAG, "User typing: ${typingInfo.userName} (${typingInfo.userRole}) - ${typingInfo.isTyping}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing typing info", e)
                    }
                }
                
                socket.on("messages_read") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val readInfo = ReadInfo(
                            userId = data.getInt("userId"),
                            chatId = data.getInt("chatId")
                        )
                        _messagesRead.value = readInfo
                        Log.d(TAG, "Messages read by user: ${readInfo.userId}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing read info", e)
                    }
                }
                
                socket.on("chat_updated") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val chatUpdateInfo = ChatUpdateInfo(
                            chatId = data.getInt("chatId"),
                            timestamp = data.getString("timestamp")
                        )
                        _chatUpdated.value = chatUpdateInfo
                        Log.d(TAG, "Chat updated: ${chatUpdateInfo.chatId}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing chat update", e)
                    }
                }

                socket.on("error") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val errorMessage = data.getString("message")
                        _error.value = errorMessage
                        Log.e(TAG, "Socket error: $errorMessage")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing socket error", e)
                    }
                }
                
                socket.connect()
            }
            
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Socket URI error", e)
            _error.value = "Invalid server URL"
        } catch (e: Exception) {
            Log.e(TAG, "Socket connection error", e)
            _error.value = "Failed to connect: ${e.message}"
        }
    }
    
    fun disconnect() {
        socket?.disconnect()
        socket = null
        _connectionState.value = false
        Log.d(TAG, "Socket disconnected manually")
    }
    
    fun joinChat(chatId: Int) {
        socket?.emit("join_chat", JSONObject().apply {
            put("chatId", chatId)
        })
        Log.d(TAG, "Joining chat: $chatId")
    }
    
    fun leaveChat(chatId: Int) {
        socket?.emit("leave_chat", JSONObject().apply {
            put("chatId", chatId)
        })
        Log.d(TAG, "Leaving chat: $chatId")
    }
    
    fun sendMessage(chatId: Int, message: String, messageType: String = "text", fileUrl: String? = null) {
        socket?.emit("send_message", JSONObject().apply {
            put("chatId", chatId)
            put("message", message)
            put("messageType", messageType)
            fileUrl?.let { put("fileUrl", it) }
        })
        Log.d(TAG, "Sending message to chat $chatId: $message")
    }
    
    fun sendTyping(chatId: Int, isTyping: Boolean) {
        socket?.emit("typing", JSONObject().apply {
            put("chatId", chatId)
            put("isTyping", isTyping)
        })
    }
    
    fun markAsRead(chatId: Int) {
        socket?.emit("mark_as_read", JSONObject().apply {
            put("chatId", chatId)
        })
        Log.d(TAG, "Marking messages as read for chat: $chatId")
    }
    
    fun isConnected(): Boolean {
        return socket?.connected() == true
    }
    
    // Clear state flows
    fun clearNewMessage() {
        _newMessage.value = null
    }
    
    fun clearTyping() {
        _userTyping.value = null
    }
    
    fun clearError() {
        _error.value = null
    }

    fun clearChatUpdated() {
        _chatUpdated.value = null
    }
}

data class TypingInfo(
    val userId: Int,
    val userName: String,
    val userRole: String? = null,
    val isTyping: Boolean
)

data class ReadInfo(
    val userId: Int,
    val chatId: Int
)

data class ChatUpdateInfo(
    val chatId: Int,
    val timestamp: String
)
