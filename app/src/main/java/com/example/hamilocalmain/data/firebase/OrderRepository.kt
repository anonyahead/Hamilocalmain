package com.example.hamilocalmain.data.firebase

import com.example.hamilocalmain.data.model.AllocationResult
import com.example.hamilocalmain.data.model.Order
import com.example.hamilocalmain.data.model.OrderStatus
import com.example.hamilocalmain.data.model.PaymentStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing order data in Firebase Firestore.
 */
class OrderRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val ordersCollection = firestore.collection("orders")
    private val productsCollection = firestore.collection("products")

    /**
     * Places a new order in Firestore.
     */
    suspend fun createOrder(order: Order): Result<Order> {
        return try {
            val docRef = ordersCollection.document()
            val orderWithId = order.copy(id = docRef.id)
            docRef.set(orderWithId).await()
            Result.success(orderWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Live orders for consumer.
     */
    fun getConsumerOrdersFlow(consumerId: String): Flow<List<Order>> {
        return ordersCollection
            .whereEqualTo("consumerId", consumerId)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Order::class.java) }
    }

    /**
     * Live orders for farmer.
     */
    fun getFarmerOrdersFlow(farmerId: String): Flow<List<Order>> {
        return ordersCollection
            .whereEqualTo("farmerId", farmerId)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Order::class.java) }
    }

    /**
     * Updates order lifecycle status.
     */
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        return try {
            ordersCollection.document(orderId).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Proportional Fair Allocation Algorithm. Example: 20kg available, buyers ordered 10kg+15kg+5kg (30kg total) → Buyer1 gets (10/30)*20=6.67kg, Buyer2 gets 10kg, Buyer3 gets 3.33kg.
     */
    suspend fun processFairAllocation(productId: String): List<AllocationResult> {
        return try {
            // 1. Fetch all PENDING orders containing this productId
            val pendingOrders = ordersCollection
                .whereEqualTo("status", OrderStatus.PENDING)
                .get()
                .await()
                .toObjects(Order::class.java)
                .filter { order -> order.items.any { it.productId == productId } }

            if (pendingOrders.isEmpty()) return emptyList()

            // 2. Sum totalDemand across all orders
            val totalDemand = pendingOrders.sumOf { order ->
                order.items.find { it.productId == productId }?.quantity ?: 0.0
            }

            // 3. Get availableQuantity from product document
            val productSnapshot = productsCollection.document(productId).get().await()
            val availableQuantity = productSnapshot.getDouble("availableQuantity") ?: 0.0

            val allocationResults = mutableListOf<AllocationResult>()

            // 4. Proportional allocation logic
            firestore.runBatch { batch ->
                for (order in pendingOrders) {
                    val item = order.items.find { it.productId == productId }!!
                    val requestedQty = item.quantity
                    
                    // allocatedQty = (orderedQty / totalDemand) * availableQuantity
                    // If available exceeds demand, buyers get exactly what they asked for
                    val allocatedQty = if (availableQuantity >= totalDemand) {
                        requestedQty
                    } else {
                        (requestedQty / totalDemand) * availableQuantity
                    }

                    val updatedItems = order.items.map {
                        if (it.productId == productId) it.copy(quantity = allocatedQty) else it
                    }
                    
                    // 5. Update each order in Firestore with allocated quantity
                    batch.update(ordersCollection.document(order.id), "items", updatedItems)
                    
                    allocationResults.add(
                        AllocationResult(
                            consumerId = order.consumerId,
                            consumerName = order.consumerName,
                            requestedQuantity = requestedQty,
                            allocatedQuantity = allocatedQty
                        )
                    )
                }
            }.await()

            allocationResults
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Confirms cash on pickup payment. Sets both order status and payment status atomically.
     */
    suspend fun confirmPayment(orderId: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId).update(
                mapOf(
                    "status" to OrderStatus.CONFIRMED,
                    "paymentStatus" to PaymentStatus.PAID
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves consumer's star rating and review. Sets rated=true to prevent double submission.
     */
    suspend fun submitRating(orderId: String, rating: Int, review: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId).update(
                mapOf(
                    "consumerRating" to rating,
                    "consumerReview" to review,
                    "rated" to true
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
