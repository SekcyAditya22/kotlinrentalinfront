package com.example.rentalinn.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentalinn.model.*
import com.example.rentalinn.network.RetrofitClient
import com.example.rentalinn.repository.ChatRepository
import com.example.rentalinn.utils.DataStoreManager
import com.example.rentalinn.utils.SocketManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminChatDetailViewModel(
    private val chatRepository: ChatRepository,
    private val dataStoreManager: DataStoreManager,
    private val chatId: Int
) : ViewModel() {
    
    companion object {
        private const val TAG = "AdminChatDetailViewModel"
        private const val TYPING_TIMEOUT = 3000L // 3 seconds
    }
    
    // UI State
    private val _uiState = MutableStateFlow(AdminChatDetailUiState())
    val uiState: StateFlow<AdminChatDetailUiState> = _uiState.asStateFlow()
    
    // Messages
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    // Current user info
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Typing indicator
    private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
    val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()
    
    // Socket connection state
    val connectionState = chatRepository.connectionState
    
    private var typingJob: Job? = null
    private var isTyping = false
    
    init {
        loadCurrentUser()
        observeSocketEvents()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                dataStoreManager.currentUser.collect { user ->
                    _currentUser.value = user
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading current user", e)
            }
        }
    }
    
    private fun observeSocketEvents() {
        viewModelScope.launch {
            // Observe new messages
            chatRepository.newMessage.collect { message ->
                message?.let {
                    Log.d(TAG, "Received new message for chat ${it.chatId}, current chat: $chatId")
                    if (it.chatId == chatId) {
                        addMessageToList(it)
                        // Mark as read since admin is viewing the chat
                        chatRepository.markAsRead(chatId)
                        Log.d(TAG, "Message added and marked as read")
                    }
                    chatRepository.clearNewMessage()
                }
            }
        }

        viewModelScope.launch {
            // Observe typing events
            chatRepository.userTyping.collect { typingInfo ->
                typingInfo?.let {
                    updateTypingUsers(it.userName, it.isTyping)
                    chatRepository.clearTyping()
                }
            }
        }

        viewModelScope.launch {
            // Observe socket errors
            chatRepository.socketError.collect { error ->
                error?.let {
                    updateUiState { copy(error = it) }
                    chatRepository.clearSocketError()
                }
            }
        }

        viewModelScope.launch {
            // Observe connection state changes
            chatRepository.connectionState.collect { isConnected ->
                if (isConnected) {
                    // When socket connects, join the chat and refresh messages
                    delay(500)
                    joinChat(chatId)
                    loadMessages(chatId, page = 1)
                }
            }
        }
    }
    
    fun initializeChatDetail(chatId: Int) {
        viewModelScope.launch {
            updateUiState { copy(isLoading = true, error = null) }

            try {
                // Initialize socket connection if not already connected
                if (!connectionState.value) {
                    chatRepository.initializeSocket()
                    // Wait a bit for socket to connect
                    delay(1000)
                }

                // Always join the specific chat (even if already connected)
                joinChat(chatId)

                // Wait a bit for join to complete
                delay(500)

                // Load messages
                loadMessages(chatId)

                updateUiState { copy(isLoading = false) }

                // Start periodic refresh if socket is not connected
                startPeriodicRefresh()

            } catch (e: Exception) {
                Log.e(TAG, "Error initializing chat detail", e)
                updateUiState {
                    copy(
                        isLoading = false,
                        error = "Failed to initialize chat: ${e.message}"
                    )
                }
            }
        }
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(5000) // Refresh every 5 seconds
                if (!connectionState.value) {
                    // Only refresh if socket is not connected
                    try {
                        loadMessages(chatId, page = 1)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in periodic refresh", e)
                    }
                } else {
                    // When connected, refresh less frequently
                    delay(10000) // Wait additional 10 seconds when connected
                }
            }
        }
    }
    
    private fun joinChat(chatId: Int) {
        chatRepository.joinChat(chatId)
        Log.d(TAG, "Admin joining chat: $chatId, socket connected: ${connectionState.value}")
    }
    
    private fun loadMessages(chatId: Int, page: Int = 1) {
        viewModelScope.launch {
            try {
                val result = chatRepository.getChatMessages(chatId, page)
                result.fold(
                    onSuccess = { messagesData ->
                        if (page == 1) {
                            _messages.value = messagesData.messages
                        } else {
                            _messages.value = messagesData.messages + _messages.value
                        }
                        
                        // Mark messages as read
                        chatRepository.markAsRead(chatId)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error loading messages", error)
                        updateUiState { copy(error = "Failed to load messages: ${error.message}") }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadMessages", e)
                updateUiState { copy(error = "Failed to load messages: ${e.message}") }
            }
        }
    }
    
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        viewModelScope.launch {
            try {
                // Use REST API for reliable message sending, socket for realtime updates
                val result = chatRepository.sendMessageRest(chatId, message.trim())
                result.fold(
                    onSuccess = { sentMessage ->
                        addMessageToList(sentMessage)
                        Log.d(TAG, "Message sent successfully: ${sentMessage.message}")

                        // Also send via socket for immediate updates to other users
                        if (connectionState.value) {
                            chatRepository.sendMessage(chatId, message.trim())
                            Log.d(TAG, "Message also sent via socket for realtime updates")
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error sending message", error)
                        updateUiState { copy(error = "Failed to send message: ${error.message}") }
                    }
                )
                
                // Stop typing indicator
                stopTyping()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                updateUiState { copy(error = "Failed to send message: ${e.message}") }
            }
        }
    }
    
    fun startTyping() {
        if (!isTyping) {
            isTyping = true
            chatRepository.sendTyping(chatId, true)
        }
        
        // Reset typing timeout
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(TYPING_TIMEOUT)
            stopTyping()
        }
    }
    
    fun stopTyping() {
        if (isTyping) {
            isTyping = false
            chatRepository.sendTyping(chatId, false)
        }
        
        typingJob?.cancel()
        typingJob = null
    }
    
    private fun addMessageToList(message: ChatMessage) {
        val currentMessages = _messages.value.toMutableList()

        // Check if message already exists (avoid duplicates)
        if (currentMessages.none { it.id == message.id }) {
            currentMessages.add(message)
            _messages.value = currentMessages.sortedBy { it.createdAt }
            Log.d(TAG, "Added message to list: ${message.message} (ID: ${message.id})")
        } else {
            Log.d(TAG, "Message already exists in list: ${message.id}")
        }
    }
    
    private fun updateTypingUsers(userName: String, isTyping: Boolean) {
        val currentTypingUsers = _typingUsers.value.toMutableList()
        
        if (isTyping) {
            if (!currentTypingUsers.contains(userName)) {
                currentTypingUsers.add(userName)
            }
        } else {
            currentTypingUsers.remove(userName)
        }
        
        _typingUsers.value = currentTypingUsers
    }
    
    fun clearError() {
        updateUiState { copy(error = null) }
    }
    
    fun refreshMessages() {
        loadMessages(chatId)
    }
    
    private fun updateUiState(update: AdminChatDetailUiState.() -> AdminChatDetailUiState) {
        _uiState.value = _uiState.value.update()
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // Leave current chat
        chatRepository.leaveChat(chatId)
        
        // Stop typing
        stopTyping()
        
        Log.d(TAG, "AdminChatDetailViewModel cleared")
    }
    
    class Factory(private val context: Context, private val chatId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminChatDetailViewModel::class.java)) {
                val retrofitClient = RetrofitClient.getInstance(context)
                val dataStoreManager = DataStoreManager.getInstance(context)
                val socketManager = SocketManager.getInstance()
                val chatRepository = ChatRepository(
                    retrofitClient.chatApiService,
                    dataStoreManager,
                    socketManager
                )
                return AdminChatDetailViewModel(chatRepository, dataStoreManager, chatId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class AdminChatDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
