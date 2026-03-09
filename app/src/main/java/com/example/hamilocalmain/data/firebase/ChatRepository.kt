package com.example.hamilocalmain.data.firebase

import com.example.hamilocalmain.data.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing chat data in Firebase Firestore.
 */
class ChatRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val threadsCollection = firestore.collection("threads")

    /**
     * Gets or creates a chat thread between two users. Thread ID = sorted user IDs joined by underscore.
     */
    suspend fun getOrCreateThread(userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        val threadId = "${sortedIds[0]}_${sortedIds[1]}"
        
        val threadRef = threadsCollection.document(threadId)
        val snapshot = threadRef.get().await()
        
        if (!snapshot.exists()) {
            val threadData = mapOf(
                "id" to threadId,
                "userIds" to sortedIds,
                "lastMessage" to "",
                "lastMessageTime" to System.currentTimeMillis()
            )
            threadRef.set(threadData).await()
        }
        
        return threadId
    }

    /**
     * Returns live messages for a thread, ordered by timestamp.
     */
    fun getMessagesFlow(threadId: String): Flow<List<Message>> {
        return threadsCollection.document(threadId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Message::class.java) }
    }

    /**
     * Sends a message and updates the thread's lastMessage and lastMessageTime.
     */
    suspend fun sendMessage(threadId: String, message: Message): Result<Unit> {
        return try {
            val messageRef = threadsCollection.document(threadId).collection("messages").document()
            val messageWithId = message.copy(id = messageRef.id, threadId = threadId)
            
            firestore.runBatch { batch ->
                batch.set(messageRef, messageWithId)
                batch.update(
                    threadsCollection.document(threadId),
                    mapOf(
                        "lastMessage" to message.content,
                        "lastMessageTime" to message.timestamp
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marks all messages in thread as read for this user.
     */
    suspend fun markAsRead(threadId: String, userId: String): Result<Unit> {
        return try {
            val unreadMessages = threadsCollection.document(threadId)
                .collection("messages")
                .whereNotEqualTo("senderId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            if (unreadMessages.isEmpty) return Result.success(Unit)

            firestore.runBatch { batch ->
                unreadMessages.documents.forEach { doc ->
                    batch.update(doc.reference, "isRead", true)
                }
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
