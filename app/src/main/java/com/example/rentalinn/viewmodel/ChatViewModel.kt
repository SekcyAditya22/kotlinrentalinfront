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

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "ChatViewModel"
        private const val TYPING_TIMEOUT = 3000L // 3 seconds
    }
    
    // UI State
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    // Current chat
    private val _currentChat = MutableStateFlow<Chat?>(null)
    val currentChat: StateFlow<Chat?> = _currentChat.asStateFlow()
    
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
                    addMessageToList(it)
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
    }
    
    fun initializeChat() {
        viewModelScope.launch {
            updateUiState { copy(isLoading = true, error = null) }
            
            try {
                // Initialize socket connection
                chatRepository.initializeSocket()
                
                // Get or create chat
                val result = chatRepository.getOrCreateChat()
                result.fold(
                    onSuccess = { chat ->
                        _currentChat.value = chat
                        joinChat(chat.id)
                        loadMessages(chat.id)
                        updateUiState { copy(isLoading = false) }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error initializing chat", error)
                        updateUiState { 
                            copy(
                                isLoading = false, 
                                error = "Failed to initialize chat: ${error.message}"
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in initializeChat", e)
                updateUiState { 
                    copy(
                        isLoading = false, 
                        error = "Failed to initialize chat: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private fun joinChat(chatId: Int) {
        chatRepository.joinChat(chatId)
        Log.d(TAG, "Joined chat: $chatId")
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
        
        val chatId = _currentChat.value?.id ?: return
        
        viewModelScope.launch {
            try {
                // Send via socket if connected, otherwise use REST API
                if (connectionState.value) {
                    chatRepository.sendMessage(chatId, message.trim())
                } else {
                    // Fallback to REST API
                    val result = chatRepository.sendMessageRest(chatId, message.trim())
                    result.fold(
                        onSuccess = { sentMessage ->
                            addMessageToList(sentMessage)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Error sending message via REST", error)
                            updateUiState { copy(error = "Failed to send message: ${error.message}") }
                        }
                    )
                }
                
                // Stop typing indicator
                stopTyping()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                updateUiState { copy(error = "Failed to send message: ${e.message}") }
            }
        }
    }
    
    fun startTyping() {
        val chatId = _currentChat.value?.id ?: return
        
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
        val chatId = _currentChat.value?.id ?: return
        
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
        val chatId = _currentChat.value?.id ?: return
        loadMessages(chatId)
    }
    
    private fun updateUiState(update: ChatUiState.() -> ChatUiState) {
        _uiState.value = _uiState.value.update()
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // Leave current chat
        _currentChat.value?.let { chat ->
            chatRepository.leaveChat(chat.id)
        }
        
        // Stop typing
        stopTyping()
        
        // Disconnect socket
        chatRepository.disconnectSocket()
        
        Log.d(TAG, "ChatViewModel cleared")
    }
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                val retrofitClient = RetrofitClient.getInstance(context)
                val dataStoreManager = DataStoreManager.getInstance(context)
                val socketManager = SocketManager.getInstance()
                val chatRepository = ChatRepository(
                    retrofitClient.chatApiService,
                    dataStoreManager,
                    socketManager
                )
                return ChatViewModel(chatRepository, dataStoreManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class ChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
