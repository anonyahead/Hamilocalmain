package com.example.hamilocalmain.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hamilocalmain.data.firebase.OrderRepository
import com.example.hamilocalmain.data.model.AllocationResult
import com.example.hamilocalmain.data.model.Order
import com.example.hamilocalmain.data.model.OrderItem
import com.example.hamilocalmain.data.model.OrderStatus
import com.example.hamilocalmain.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Represents a product and its quantity in the shopping cart.
 * 
 * @property product The product being purchased.
 * @property quantity The amount ordered by the consumer.
 */
data class CartItem(val product: Product, val quantity: Double)

/**
 * State representing order data availability or errors.
 */
sealed class OrderState {
    object Loading : OrderState()
    data class Success(val orders: List<Order>) : OrderState()
    data class Error(val message: String) : OrderState()
}

/**
 * Manages the order lifecycle, shopping cart, and fair allocation logic.
 * Features:
 * - Real-time order tracking for both consumers and farmers.
 * - Local shopping cart management with reactive total calculation.
 * - Proportional Fair Allocation algorithm trigger for shortage management.
 * - Atomic payment confirmation and rating submission.
 */
class OrderViewModel(private val repository: OrderRepository = OrderRepository()) : ViewModel() {

    private val _consumerOrdersState = MutableStateFlow<OrderState>(OrderState.Loading)
    val consumerOrdersState: StateFlow<OrderState> = _consumerOrdersState.asStateFlow()

    private val _farmerOrdersState = MutableStateFlow<OrderState>(OrderState.Loading)
    val farmerOrdersState: StateFlow<OrderState> = _farmerOrdersState.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _allocationResults = MutableStateFlow<List<AllocationResult>>(emptyList())
    val allocationResults: StateFlow<List<AllocationResult>> = _allocationResults.asStateFlow()

    private val _isAllocating = MutableStateFlow(false)
    val isAllocating: StateFlow<Boolean> = _isAllocating.asStateFlow()

    /**
     * Derived state calculating the total price of all items currently in the cart.
     */
    val cartTotal: StateFlow<Double> = _cartItems
        .map { items -> items.sumOf { it.product.price * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * Loads live updates of orders placed by a specific consumer.
     * 
     * @param consumerId The unique ID of the consumer.
     */
    fun loadConsumerOrders(consumerId: String) {
        viewModelScope.launch {
            _consumerOrdersState.value = OrderState.Loading
            repository.getConsumerOrdersFlow(consumerId)
                .catch { e -> _consumerOrdersState.value = OrderState.Error(e.message ?: "Unknown error") }
                .collect { orders -> _consumerOrdersState.value = OrderState.Success(orders) }
        }
    }

    /**
     * Loads live updates of orders received by a specific farmer.
     * 
     * @param farmerId The unique ID of the farmer.
     */
    fun loadFarmerOrders(farmerId: String) {
        viewModelScope.launch {
            _farmerOrdersState.value = OrderState.Loading
            repository.getFarmerOrdersFlow(farmerId)
                .catch { e -> _farmerOrdersState.value = OrderState.Error(e.message ?: "Unknown error") }
                .collect { orders -> _farmerOrdersState.value = OrderState.Success(orders) }
        }
    }

    /**
     * Adds a product to the local shopping cart. If it exists, increments quantity.
     * 
     * @param product The product details.
     * @param quantity The amount to add.
     */
    fun addToCart(product: Product, quantity: Double) {
        val current = _cartItems.value.toMutableList()
        val existing = current.find { it.product.id == product.id }
        if (existing != null) {
            val index = current.indexOf(existing)
            val newQuantity = existing.quantity + quantity
            if (newQuantity <= 0) {
                current.removeAt(index)
            } else {
                current[index] = existing.copy(quantity = newQuantity)
            }
        } else {
            if (quantity > 0) {
                current.add(CartItem(product, quantity))
            }
        }
        _cartItems.value = current
    }

    /**
     * Removes a product from the local shopping cart by ID.
     * 
     * @param productId The ID of the product to remove.
     */
    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    /**
     * Clears all items from the shopping cart.
     */
    fun clearCart() {
        _cartItems.value = emptyList()
    }

    /**
     * Places a new order in Firestore based on the current cart items.
     * 
     * @param consumerId The ID of the buyer.
     * @param consumerName The name of the buyer.
     * @param pickupAddress Where the items will be collected.
     * @param onOrdersPlaced Callback with the list of created orders.
     */
    fun placeOrder(consumerId: String, consumerName: String, pickupAddress: String, onOrdersPlaced: (List<Order>) -> Unit) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        viewModelScope.launch {
            val createdOrders = mutableListOf<Order>()
            val groupedByFarmer = items.groupBy { it.product.farmerId }
            
            for ((farmerId, farmerItems) in groupedByFarmer) {
                val orderItems = farmerItems.map { 
                    OrderItem(
                        productId = it.product.id,
                        productName = it.product.name,
                        farmerId = farmerId,
                        quantity = it.quantity,
                        unit = it.product.unit,
                        pricePerUnit = it.product.price,
                        totalPrice = it.product.price * it.quantity
                    )
                }
                
                val total = orderItems.sumOf { it.totalPrice }
                val platformFee = total * 0.02
                val farmerEarnings = total - platformFee
                val order = Order(
                    consumerId = consumerId,
                    consumerName = consumerName,
                    farmerId = farmerId,
                    farmerName = farmerItems.first().product.farmerName,
                    items = orderItems,
                    totalAmount = total,
                    platformFee = platformFee,
                    farmerEarnings = farmerEarnings,
                    pickupAddress = pickupAddress
                )
                repository.createOrder(order).onSuccess { createdOrder ->
                    createdOrders.add(createdOrder)
                }
            }
            clearCart()
            onOrdersPlaced(createdOrders)
        }
    }

    /**
     * Updates the status of an existing order (e.g., READY_FOR_PICKUP).
     * 
     * @param orderId The unique ID of the order.
     * @param status The new status to apply.
     */
    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    /**
     * Triggers the Proportional Fair Allocation algorithm for a specific product.
     * Always ensures [isAllocating] is reset via try-finally.
     * 
     * @param productId The ID of the product with the shortage.
     */
    fun triggerFairAllocation(productId: String) {
        viewModelScope.launch {
            _isAllocating.value = true
            try {
                val results = repository.processFairAllocation(productId)
                _allocationResults.value = results
            } finally {
                _isAllocating.value = false
            }
        }
    }

    /**
     * Confirms the payment for an order and sets its status to CONFIRMED.
     * 
     * @param orderId The unique ID of the order.
     * @param onDone Callback executed after successful confirmation.
     */
    fun confirmOrderPayment(orderId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.confirmPayment(orderId).onSuccess { onDone() }
        }
    }

    /**
     * Submits a consumer rating and review for a completed order.
     * 
     * @param orderId The unique ID of the order.
     * @param rating Star rating (e.g., 1-5).
     * @param review Textual feedback.
     * @param onDone Callback executed after successful submission.
     */
    fun submitRating(orderId: String, rating: Int, review: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.submitRating(orderId, rating, review).onSuccess { onDone() }
        }
    }
}
