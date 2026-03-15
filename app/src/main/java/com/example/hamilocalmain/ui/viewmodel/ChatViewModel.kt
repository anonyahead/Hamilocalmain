package com.example.hamilocalmain.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hamilocalmain.data.firebase.ChatRepository
import com.example.hamilocalmain.data.model.ChatThread
import com.example.hamilocalmain.data.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Manages chat conversations, message loading, and real-time updates.
 */
class ChatViewModel(private val repository: ChatRepository = ChatRepository()) : ViewModel() {

    private val _messagesState = MutableStateFlow<List<Message>>(emptyList())
    val messagesState: StateFlow<List<Message>> = _messagesState.asStateFlow()

    private val _threadsState = MutableStateFlow<List<ChatThread>>(emptyList())
    val threadsState: StateFlow<List<ChatThread>> = _threadsState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun loadUserThreads(userId: String) {
        viewModelScope.launch {
            repository.getUserThreadsFlow(userId)
                .catch { _threadsState.value = emptyList() }
                .collect { threads -> _threadsState.value = threads }
        }
    }

    /**
     * Loads live messages for a specific chat thread.
     * 
     * @param threadId The unique ID of the chat thread.
     */
    fun loadMessages(threadId: String) {
        viewModelScope.launch {
            repository.getMessagesFlow(threadId)
                .catch { _messagesState.value = emptyList() }
                .collect { messages -> 
                    _messagesState.value = messages
                    // Example: update unread count locally if needed
                }
        }
    }

    /**
     * Sends a new message in a chat thread.
     * 
     * @param threadId The thread to send the message to.
     * @param content The text content of the message.
     * @param senderId The ID of the user sending the message.
     * @param senderName The name of the user sending the message.
     */
    fun sendMessage(threadId: String, content: String, senderId: String, senderName: String) {
        val message = Message(
            content = content,
            senderId = senderId,
            senderName = senderName,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.sendMessage(threadId, message)
        }
    }

    /**
     * Marks all messages in a thread as read by the current user.
     * 
     * @param threadId The thread ID to mark as read.
     * @param currentUserId The ID of the user reading the messages.
     */
    fun markThreadAsRead(threadId: String, currentUserId: String) {
        viewModelScope.launch {
            repository.markAsRead(threadId, currentUserId)
        }
    }

    /**
     * Starts a new chat or retrieves an existing one between two users.
     * 
     * @param userId1 First user's ID.
     * @param userId2 Second user's ID.
     * @param onThreadCreated Callback with the generated thread ID.
     */
    fun startNewChat(userId1: String, userId2: String, onThreadCreated: (String) -> Unit) {
        viewModelScope.launch {
            val threadId = repository.getOrCreateThread(userId1, userId2)
            onThreadCreated(threadId)
        }
    }
}
