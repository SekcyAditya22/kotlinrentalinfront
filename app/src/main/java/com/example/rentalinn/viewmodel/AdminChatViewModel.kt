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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminChatViewModel(
    private val chatRepository: ChatRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "AdminChatViewModel"
    }
    
    // UI State
    private val _uiState = MutableStateFlow(AdminChatUiState())
    val uiState: StateFlow<AdminChatUiState> = _uiState.asStateFlow()
    
    // Chats list
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()
    
    // Current user info
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Socket connection state
    val connectionState = chatRepository.connectionState
    
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
            // Observe new messages to update chat list
            chatRepository.newMessage.collect { message ->
                message?.let {
                    updateChatWithNewMessage(it)
                    chatRepository.clearNewMessage()

                    // Also refresh the chat list to get updated info
                    delay(500)
                    loadAllChats(page = 1)
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
                    // When socket connects, refresh chat list
                    delay(1000)
                    loadAllChats(page = 1)
                }
            }
        }

        viewModelScope.launch {
            // Observe chat updates
            chatRepository.chatUpdated.collect { chatUpdate ->
                chatUpdate?.let {
                    // Refresh chat list when any chat is updated
                    loadAllChats(page = 1)
                    chatRepository.clearChatUpdated()
                }
            }
        }
    }
    
    fun initializeAdminChat() {
        viewModelScope.launch {
            updateUiState { copy(isLoading = true, error = null) }

            try {
                // Initialize socket connection
                chatRepository.initializeSocket()

                // Wait a bit for socket to connect
                delay(1000)

                // Load all chats
                loadAllChats()

                updateUiState { copy(isLoading = false) }

                // Start periodic refresh for realtime updates
                startPeriodicRefresh()

            } catch (e: Exception) {
                Log.e(TAG, "Error initializing admin chat", e)
                updateUiState {
                    copy(
                        isLoading = false,
                        error = "Failed to initialize admin chat: ${e.message}"
                    )
                }
            }
        }
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(15000) // Refresh every 15 seconds
                try {
                    // Only refresh if socket is not connected or for periodic updates
                    if (!connectionState.value) {
                        loadAllChats(page = 1)
                    } else {
                        // Even when connected, refresh occasionally to sync
                        delay(30000) // Wait additional 30 seconds when connected
                        loadAllChats(page = 1)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in periodic refresh", e)
                }
            }
        }
    }
    
    private fun loadAllChats(page: Int = 1) {
        viewModelScope.launch {
            try {
                val result = chatRepository.getAllChats(page)
                result.fold(
                    onSuccess = { chatsData ->
                        if (page == 1) {
                            _chats.value = chatsData.chats
                        } else {
                            _chats.value = _chats.value + chatsData.chats
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error loading chats", error)
                        updateUiState { copy(error = "Failed to load chats: ${error.message}") }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadAllChats", e)
                updateUiState { copy(error = "Failed to load chats: ${e.message}") }
            }
        }
    }
    
    fun refreshChats() {
        loadAllChats()
    }
    
    fun joinChat(chatId: Int) {
        chatRepository.joinChat(chatId)
        Log.d(TAG, "Admin joined chat: $chatId")
    }
    
    fun leaveChat(chatId: Int) {
        chatRepository.leaveChat(chatId)
        Log.d(TAG, "Admin left chat: $chatId")
    }
    
    fun sendMessage(chatId: Int, message: String) {
        if (message.isBlank()) return
        
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
                            updateChatWithNewMessage(sentMessage)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Error sending message via REST", error)
                            updateUiState { copy(error = "Failed to send message: ${error.message}") }
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                updateUiState { copy(error = "Failed to send message: ${e.message}") }
            }
        }
    }
    
    fun markAsRead(chatId: Int) {
        chatRepository.markAsRead(chatId)
    }
    
    private fun updateChatWithNewMessage(message: ChatMessage) {
        val currentChats = _chats.value.toMutableList()
        val chatIndex = currentChats.indexOfFirst { it.id == message.chatId }
        
        if (chatIndex != -1) {
            val chat = currentChats[chatIndex]
            val updatedChat = chat.copy(
                lastMessage = message,
                lastMessageAt = message.createdAt,
                unreadCount = if (message.senderId != _currentUser.value?.id) {
                    chat.unreadCount + 1
                } else {
                    chat.unreadCount
                }
            )
            currentChats[chatIndex] = updatedChat
            
            // Move to top of list
            currentChats.removeAt(chatIndex)
            currentChats.add(0, updatedChat)
            
            _chats.value = currentChats
        } else {
            // If chat not in list, refresh the list
            refreshChats()
        }
    }
    
    fun clearError() {
        updateUiState { copy(error = null) }
    }
    
    private fun updateUiState(update: AdminChatUiState.() -> AdminChatUiState) {
        _uiState.value = _uiState.value.update()
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // Disconnect socket
        chatRepository.disconnectSocket()
        
        Log.d(TAG, "AdminChatViewModel cleared")
    }
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminChatViewModel::class.java)) {
                val retrofitClient = RetrofitClient.getInstance(context)
                val dataStoreManager = DataStoreManager.getInstance(context)
                val socketManager = SocketManager.getInstance()
                val chatRepository = ChatRepository(
                    retrofitClient.chatApiService,
                    dataStoreManager,
                    socketManager
                )
                return AdminChatViewModel(chatRepository, dataStoreManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class AdminChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
