package com.example.hamilocalmain.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hamilocalmain.ui.screens.chat.ChatListScreen
import com.example.hamilocalmain.ui.screens.chat.ChatScreen
import com.example.hamilocalmain.ui.screens.consumer.*
import com.example.hamilocalmain.ui.screens.farmer.*
import com.example.hamilocalmain.ui.screens.onboarding.*
import com.example.hamilocalmain.ui.screens.shared.*
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import com.example.hamilocalmain.ui.viewmodel.ChatViewModel
import com.example.hamilocalmain.ui.viewmodel.CurrencyViewModel
import com.example.hamilocalmain.ui.viewmodel.LocationViewModel
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel
import com.example.hamilocalmain.ui.viewmodel.ProductViewModel

/**
 * Navigation routes for the Hami Local application.
 */
object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PHONE_VERIFICATION = "phone_verification/{phoneNumber}"
    const val PROFILE_SETUP = "profile_setup"
    const val CONSUMER_HOME = "consumer_home"
    const val BROWSE_PRODUCTS = "browse_products"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val CART = "cart"
    const val ORDER_HISTORY = "order_history"
    const val PAYMENT = "payment/{orderId}/{totalAmount}/{farmerName}/{pickupAddress}"
    const val FARMER_DASHBOARD = "farmer_dashboard"
    const val MANAGE_PRODUCTS = "manage_products"
    const val ADD_PRODUCT = "add_product"
    const val FARMER_ORDERS = "farmer_orders"
    const val SHORTAGE_MANAGEMENT = "shortage_management"
    const val ORDER_DETAIL = "order_detail/{orderId}"
    const val MAP = "map"
    const val SETTINGS = "settings"
    const val RATING = "rating/{orderId}/{farmerName}"
    const val CHAT_LIST = "chat_list"
    const val CHAT = "chat/{threadId}/{otherUserName}"

    fun phoneVerification(phoneNumber: String) = "phone_verification/$phoneNumber"
    fun productDetail(id: String) = "product_detail/$id"
    fun orderDetail(id: String) = "order_detail/$id"
    fun chat(id: String, otherUserName: String) = "chat/$id/$otherUserName"
    
    fun payment(orderId: String, amount: Double, farmerName: String, pickupAddress: String): String {
        val encodedFarmerName = java.net.URLEncoder.encode(farmerName, "UTF-8")
        val encodedPickupAddress = java.net.URLEncoder.encode(pickupAddress, "UTF-8")
        return "payment/$orderId/$amount/$encodedFarmerName/$encodedPickupAddress"
    }
    
    fun rating(orderId: String, farmerName: String) = "rating/$orderId/$farmerName"
}

/**
 * Main navigation host. Wires all routes to their screen composables.
 */
@Composable
fun HamiLocalNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    currencyViewModel: CurrencyViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Routes.WELCOME) {
                WelcomeScreen(navController = navController)
            }
            composable(Routes.LOGIN) {
                LoginScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Routes.REGISTER) {
                RegisterScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(
                Routes.PHONE_VERIFICATION,
                arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
            ) { backStackEntry ->
                val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                PhoneVerificationScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    phone = phoneNumber
                )
            }
            composable(Routes.PROFILE_SETUP) {
                ProfileSetupScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    locationViewModel = locationViewModel
                )
            }

            composable(Routes.CONSUMER_HOME) {
                ConsumerHomeScreen(
                    navController = navController,
                    productViewModel = productViewModel,
                    orderViewModel = orderViewModel,
                    locationViewModel = locationViewModel,
                    authViewModel = authViewModel,
                    currencyViewModel = currencyViewModel
                )
            }
            composable(Routes.BROWSE_PRODUCTS) {
                BrowseProductsScreen(
                    navController = navController,
                    productViewModel = productViewModel,
                    currencyViewModel = currencyViewModel
                )
            }
            composable(
                Routes.PRODUCT_DETAIL,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    productId = productId,
                    navController = navController,
                    productViewModel = productViewModel,
                    orderViewModel = orderViewModel,
                    authViewModel = authViewModel,
                    currencyViewModel = currencyViewModel
                )
            }
            composable(Routes.CART) {
                CartScreen(
                    navController = navController,
                    orderViewModel = orderViewModel,
                    authViewModel = authViewModel,
                    currencyViewModel = currencyViewModel
                )
            }
            composable(Routes.ORDER_HISTORY) {
                OrderHistoryScreen(
                    navController = navController,
                    orderViewModel = orderViewModel,
                    authViewModel = authViewModel,
                    onNavigateToRating = { orderId, farmerName ->
                        navController.navigate(Routes.rating(orderId, farmerName))
                    }
                )
            }
            composable(
                Routes.PAYMENT,
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType },
                    navArgument("totalAmount") { type = NavType.FloatType },
                    navArgument("farmerName") { type = NavType.StringType },
                    navArgument("pickupAddress") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val totalAmount = backStackEntry.arguments?.getFloat("totalAmount")?.toDouble() ?: 0.0
                val farmerName = backStackEntry.arguments?.getString("farmerName")
                    ?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: ""
                val pickupAddress = backStackEntry.arguments?.getString("pickupAddress")
                    ?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: ""
                PaymentScreen(
                    navController = navController,
                    orderId = orderId,
                    totalAmount = totalAmount,
                    farmerName = farmerName,
                    pickupAddress = pickupAddress,
                    orderViewModel = orderViewModel,
                    currencyViewModel = currencyViewModel
                )
            }

            composable(Routes.FARMER_DASHBOARD) {
                FarmerDashboardScreen(
                    navController = navController,
                    productViewModel = productViewModel,
                    orderViewModel = orderViewModel,
                    authViewModel = authViewModel,
                    onNavigateToShortageManagement = {
                        navController.navigate(Routes.SHORTAGE_MANAGEMENT)
                    },
                    currencyViewModel = currencyViewModel
                )
            }
            composable(Routes.MANAGE_PRODUCTS) {
                ManageProductsScreen(
                    navController = navController,
                    productViewModel = productViewModel,
                    farmerId = currentUser?.id ?: ""
                )
            }
            composable(Routes.ADD_PRODUCT) {
                AddProductScreen(
                    navController = navController,
                    productViewModel = productViewModel,
                    authViewModel = authViewModel
                )
            }
            composable(Routes.FARMER_ORDERS) {
                FarmerOrdersScreen(
                    navController = navController,
                    orderViewModel = orderViewModel,
                    authViewModel = authViewModel
                )
            }
            composable(Routes.SHORTAGE_MANAGEMENT) {
                ShortageManagementScreen(
                    navController = navController,
                    orderViewModel = orderViewModel,
                    productViewModel = productViewModel,
                    authViewModel = authViewModel
                )
            }

            composable(
                Routes.ORDER_DETAIL,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderDetailScreen(
                    orderId = orderId,
                    navController = navController,
                    orderViewModel = orderViewModel,
                    authViewModel = authViewModel,
                    currencyViewModel = currencyViewModel
                )
            }
            composable(Routes.MAP) {
                MapScreen(
                    locationViewModel = locationViewModel,
                    navController = navController,
                    productViewModel = productViewModel
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    currencyViewModel = currencyViewModel
                )
            }
            composable(
                Routes.RATING,
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType },
                    navArgument("farmerName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val farmerName = backStackEntry.arguments?.getString("farmerName") ?: ""
                RatingScreen(
                    navController = navController,
                    orderId = orderId,
                    farmerName = farmerName,
                    orderViewModel = orderViewModel
                )
            }

            composable(Routes.CHAT_LIST) {
                ChatListScreen(
                    navController = navController,
                    chatViewModel = chatViewModel,
                    authViewModel = authViewModel
                )
            }
            composable(
                Routes.CHAT,
                arguments = listOf(
                    navArgument("threadId") { type = NavType.StringType },
                    navArgument("otherUserName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val threadId = backStackEntry.arguments?.getString("threadId") ?: ""
                val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: "Chat"
                ChatScreen(
                    threadId = threadId,
                    otherUserName = otherUserName,
                    navController = navController,
                    chatViewModel = chatViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
