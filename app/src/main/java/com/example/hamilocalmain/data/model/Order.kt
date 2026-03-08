package com.example.hamilocalmain.data.model

/**
 * Lifecycle stages of an order from placement to completion.
 */
enum class OrderStatus {
    PENDING,
    CONFIRMED,
    READY_FOR_PICKUP,
    PICKED_UP,
    COMPLETED,
    CANCELLED
}

/**
 * Payment state. Moves from PENDING to PAID after consumer confirms cash on pickup.
 */
enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED
}

/**
 * A single product line within an order.
 *
 * @property productId Unique identifier for the product.
 * @property productName Name of the product at the time of order.
 * @property farmerId Unique identifier of the farmer selling the product.
 * @property quantity Amount ordered.
 * @property unit Unit of measurement for the quantity.
 * @property pricePerUnit Price per unit at the time of order.
 * @property totalPrice Total cost for this line item (quantity * pricePerUnit).
 */
data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val farmerId: String = "",
    val quantity: Double = 0.0,
    val unit: UnitType = UnitType.KG,
    val pricePerUnit: Double = 0.0,
    val totalPrice: Double = 0.0
)

/**
 * Result of fair allocation algorithm — what a buyer actually gets vs what they requested.
 *
 * @property consumerId Unique identifier of the consumer.
 * @property consumerName Name of the consumer.
 * @property requestedQuantity The amount the consumer wanted to buy.
 * @property allocatedQuantity The amount actually assigned to the consumer after fair distribution.
 */
data class AllocationResult(
    val consumerId: String = "",
    val consumerName: String = "",
    val requestedQuantity: Double = 0.0,
    val allocatedQuantity: Double = 0.0
)

/**
 * A purchase order. rated/consumerRating/consumerReview are set by RatingScreen.
 *
 * @property id Unique identifier for the order.
 * @property consumerId ID of the user who placed the order.
 * @property consumerName Name of the user who placed the order.
 * @property farmerId ID of the farmer selling the products.
 * @property farmerName Name of the farmer selling the products.
 * @property items List of products included in this order.
 * @property totalAmount Total price of the order including fees.
 * @property platformFee Fee charged by the platform.
 * @property farmerEarnings Amount the farmer receives after fees.
 * @property status Current progress of the order lifecycle.
 * @property paymentStatus Current state of payment.
 * @property pickupAddress Location where the consumer should collect the order.
 * @property notes Additional instructions or comments for the order.
 * @property createdAt Timestamp when the order was placed.
 * @property rated Whether the consumer has provided a rating for this order.
 * @property consumerRating Rating given by the consumer (e.g., 1-5 stars).
 * @property consumerReview Textual feedback provided by the consumer.
 */
data class Order(
    val id: String = "",
    val consumerId: String = "",
    val consumerName: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val platformFee: Double = 0.0,
    val farmerEarnings: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val pickupAddress: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val rated: Boolean = false,
    val consumerRating: Int = 0,
    val consumerReview: String = ""
)
