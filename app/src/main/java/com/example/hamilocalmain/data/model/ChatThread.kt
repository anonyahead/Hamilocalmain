package com.example.hamilocalmain.data.model

/**
 * A conversation thread between two users. Used in ChatListScreen.
 *
 * @property id Unique identifier for the chat thread.
 * @property participantIds List of user IDs involved in the conversation.
 * @property participantNames List of names of users involved in the conversation.
 * @property lastMessage The content of the most recent message in the thread.
 * @property lastMessageTime Timestamp of the most recent message.
 * @property unreadCount Number of messages that have not been read by the current user.
 */
data class ChatThread(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0
)
