package com.example.hamilocalmain.ui.navigation

/**
 * Navigation routes for the Hami Local application.
 */
object Routes {
    /**
     * Routes related to user onboarding and authentication.
     */
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val PHONE_VERIFICATION = "phone_verification"
    const val PROFILE_SETUP = "profile_setup"

    /**
     * Routes specific to the Consumer role.
     */
    const val CONSUMER_HOME = "consumer_home"
    const val BROWSE_PRODUCTS = "browse_products"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val CART = "cart"
    const val ORDER_HISTORY = "order_history"
    const val PAYMENT = "payment/{orderId}/{totalAmount}/{farmerName}/{pickupAddress}"

    /**
     * Routes specific to the Farmer role.
     */
    const val FARMER_DASHBOARD = "farmer_dashboard"
    const val MANAGE_PRODUCTS = "manage_products"
    const val ADD_PRODUCT = "add_product"
    const val FARMER_ORDERS = "farmer_orders"
    const val SHORTAGE_MANAGEMENT = "shortage_management"

    /**
     * Routes shared between both Consumer and Farmer roles.
     */
    const val ORDER_DETAIL = "order_detail/{orderId}"
    const val MAP = "map"
    const val SETTINGS = "settings"
    const val RATING = "rating/{orderId}/{farmerName}"

    /**
     * Routes related to the chat system.
     */
    const val CHAT_LIST = "chat_list"
    const val CHAT = "chat/{threadId}"

    /**
     * Helper functions to build routes with arguments.
     */
    fun productDetail(id: String) = "product_detail/$id"
    fun orderDetail(id: String) = "order_detail/$id"
    fun chat(id: String) = "chat/$id"
    fun payment(orderId: String, amount: Double, farmerName: String, pickupAddress: String) =
        "payment/$orderId/$amount/$farmerName/$pickupAddress"
    fun rating(orderId: String, farmerName: String) = "rating/$orderId/$farmerName"
}
