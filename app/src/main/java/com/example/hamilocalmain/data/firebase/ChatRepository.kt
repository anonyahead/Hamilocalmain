package com.example.hamilocalmain.data.firebase

import com.example.hamilocalmain.data.model.ChatThread
import com.example.hamilocalmain.data.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ChatRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val threadsCollection = firestore.collection("threads")

    fun getUserThreadsFlow(userId: String): Flow<List<ChatThread>> {
        return firestore.collection("threads")
            .whereArrayContains("userIds", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(ChatThread::class.java) }
    }

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

    fun getMessagesFlow(threadId: String): Flow<List<Message>> {
        return threadsCollection.document(threadId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Message::class.java) }
    }

    suspend fun sendMessage(threadId: String, message: Message): Result<Unit> {
        return try {
            val messageRef = threadsCollection.document(threadId).collection("messages").document()
            val messageWithId = message.copy(id = messageRef.id, threadId = threadId)

            // Use set() with merge=true on thread doc so it works even if thread doesn't exist yet
            val threadRef = threadsCollection.document(threadId)
            
            firestore.runBatch { batch ->
                batch.set(messageRef, messageWithId)
                batch.set(
                    threadRef,
                    mapOf(
                        "id" to threadId,
                        "lastMessage" to message.content,
                        "lastMessageTime" to message.timestamp
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
