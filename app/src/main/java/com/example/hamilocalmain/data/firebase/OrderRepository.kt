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
 * This class handles order creation, status updates, and the fair allocation algorithm.
 */
class OrderRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // ==================== COLLECTIONS ====================
    private val ordersCollection = firestore.collection("orders")
    private val productsCollection = firestore.collection("products")

    // ==================== ORDER OPERATIONS ====================

    /**
     * Places a new order in Firestore.
     * 
     * @param order The order data to save.
     * @return Result containing the saved order with generated ID.
     */
    suspend fun createOrder(order: Order): Result<Order> {
        return try {
            val docRef = ordersCollection.document()
            val orderWithId = order.copy(id = docRef.id)
            
            // Save order AND update pendingOrderQuantity on each product in one batch
            firestore.runBatch { batch ->
                batch.set(docRef, orderWithId)
                for (item in order.items) {
                    val productRef = productsCollection.document(item.productId)
                    batch.update(productRef, "pendingOrderQuantity", 
                        com.google.firebase.firestore.FieldValue.increment(item.quantity))
                }
            }.await()
            
            Result.success(orderWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Provides a live flow of orders placed by a specific consumer.
     * Used by the consumer's order history screen.
     */
    fun getConsumerOrdersFlow(consumerId: String): Flow<List<Order>> {
        return ordersCollection
            .whereEqualTo("consumerId", consumerId)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Order::class.java) }
    }

    /**
     * Provides a live flow of orders received by a specific farmer.
     * Used by the farmer's dashboard and order management screens.
     */
    fun getFarmerOrdersFlow(farmerId: String): Flow<List<Order>> {
        return ordersCollection
            .whereEqualTo("farmerId", farmerId)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Order::class.java) }
    }

    /**
     * Updates order lifecycle status.
     * 
     * @param orderId The unique ID of the order.
     * @param status The new status to apply.
     */
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        return try {
            val orderSnap = ordersCollection.document(orderId).get().await()
            val order = orderSnap.toObject(Order::class.java)
            
            firestore.runBatch { batch ->
                batch.update(ordersCollection.document(orderId), "status", status)
                // When cancelled, release the pending quantity back
                if (status == OrderStatus.CANCELLED && order != null) {
                    for (item in order.items) {
                        val productRef = productsCollection.document(item.productId)
                        batch.update(productRef, "pendingOrderQuantity",
                            com.google.firebase.firestore.FieldValue.increment(-item.quantity))
                    }
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Proportional Fair Allocation Algorithm. 
     * used when demand exceeds supply to distribute available stock fairly among buyers.
     * 
     * ALGORITHM:
     * 1. Sum total requested quantity (Demand) across all pending orders for this product.
     * 2. Calculate allocation factor: Available / Demand.
     * 3. Set new quantity for each order: OrderedQty * AllocationFactor.
     * 
     * EXAMPLE:
     * - 20kg available stock.
     * - Buyers ordered: 10kg, 15kg, and 5kg (Total Demand = 30kg).
     * - Allocation Factor = 20 / 30 = 0.667
     * - Result:
     *   - Buyer1 (ordered 10kg): 10 * 0.667 = 6.67kg
     *   - Buyer2 (ordered 15kg): 15 * 0.667 = 10.00kg
     *   - Buyer3 (ordered 5kg): 5 * 0.667 = 3.33kg
     * 
     * @param productId The ID of the product to allocate.
     * @return List of results showing what each consumer was allocated.
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
