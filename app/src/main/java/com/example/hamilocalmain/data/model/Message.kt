package com.example.hamilocalmain.data.model

/**
 * A chat message between farmer and consumer.
 *
 * @property id Unique identifier for the message.
 * @property threadId ID of the chat thread this message belongs to.
 * @property senderId ID of the user who sent the message.
 * @property senderName Name of the user who sent the message.
 * @property content The text content of the message.
 * @property timestamp Time when the message was sent.
 * @property isRead Whether the recipient has read the message.
 */
data class Message(
    val id: String = "",
    val threadId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
