package com.example.hamilocalmain.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import com.example.hamilocalmain.ui.viewmodel.ChatViewModel
import com.example.hamilocalmain.ui.viewmodel.LocationViewModel
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel
import com.example.hamilocalmain.ui.viewmodel.ProductViewModel

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

/**
 * Main navigation host. Wires all 20+ routes to their screen composables.
 */
@Composable
fun HamiLocalNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel,
    chatViewModel: ChatViewModel,
    locationViewModel: LocationViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController, 
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {
        composable(Routes.SPLASH) { /* Placeholder: SplashScreen(navController) */ Text(Routes.SPLASH) }
        composable(Routes.WELCOME) { /* Placeholder: WelcomeScreen(navController) */ Text(Routes.WELCOME) }
        composable(Routes.LOGIN) { /* Placeholder: LoginScreen(authViewModel, navController) */ Text(Routes.LOGIN) }
        composable(Routes.PHONE_VERIFICATION) { /* Placeholder: PhoneVerificationScreen(authViewModel, navController) */ Text(Routes.PHONE_VERIFICATION) }
        composable(Routes.PROFILE_SETUP) { /* Placeholder: ProfileSetupScreen(authViewModel, navController) */ Text(Routes.PROFILE_SETUP) }

        composable(Routes.CONSUMER_HOME) { /* Placeholder: ConsumerHomeScreen(navController) */ Text(Routes.CONSUMER_HOME) }
        composable(Routes.BROWSE_PRODUCTS) { /* Placeholder: BrowseProductsScreen(productViewModel, navController) */ Text(Routes.BROWSE_PRODUCTS) }
        composable(Routes.PRODUCT_DETAIL) { /* Placeholder: ProductDetailScreen(...) */ Text(Routes.PRODUCT_DETAIL) }
        composable(Routes.CART) { /* Placeholder: CartScreen(orderViewModel, navController) */ Text(Routes.CART) }
        composable(Routes.ORDER_HISTORY) {
            // OrderHistoryScreen(orderViewModel = orderViewModel, onNavigateToRating = { orderId, farmerName ->
            //     navController.navigate(Routes.rating(orderId, farmerName))
            // })
            Text(Routes.ORDER_HISTORY)
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
            val farmerName = backStackEntry.arguments?.getString("farmerName") ?: ""
            val pickupAddress = backStackEntry.arguments?.getString("pickupAddress") ?: ""
            // PaymentScreen(orderId, totalAmount, farmerName, pickupAddress, ...)
            Text("$Routes.PAYMENT ($orderId, $totalAmount, $farmerName, $pickupAddress)")
        }

        composable(Routes.FARMER_DASHBOARD) {
            // FarmerDashboardScreen(onNavigateToShortageManagement = {
            //     navController.navigate(Routes.SHORTAGE_MANAGEMENT)
            // })
            Text(Routes.FARMER_DASHBOARD)
        }
        composable(Routes.MANAGE_PRODUCTS) { /* Placeholder: ManageProductsScreen(productViewModel, navController) */ Text(Routes.MANAGE_PRODUCTS) }
        composable(Routes.ADD_PRODUCT) { /* Placeholder: AddProductScreen(productViewModel, navController) */ Text(Routes.ADD_PRODUCT) }
        composable(Routes.FARMER_ORDERS) { /* Placeholder: FarmerOrdersScreen(orderViewModel, navController) */ Text(Routes.FARMER_ORDERS) }
        composable(Routes.SHORTAGE_MANAGEMENT) {
            // ShortageManagementScreen(orderViewModel = orderViewModel)
            Text(Routes.SHORTAGE_MANAGEMENT)
        }

        composable(Routes.ORDER_DETAIL) { /* Placeholder: OrderDetailScreen(...) */ Text(Routes.ORDER_DETAIL) }
        composable(Routes.MAP) { /* Placeholder: MapScreen(locationViewModel) */ Text(Routes.MAP) }
        composable(Routes.SETTINGS) { /* Placeholder: SettingsScreen(authViewModel, navController) */ Text(Routes.SETTINGS) }
        composable(
            Routes.RATING,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("farmerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val farmerName = backStackEntry.arguments?.getString("farmerName") ?: ""
            // RatingScreen(orderId, farmerName, ...)
            Text("$Routes.RATING ($orderId, $farmerName)")
        }

        composable(Routes.CHAT_LIST) { /* Placeholder: ChatListScreen(chatViewModel, navController) */ Text(Routes.CHAT_LIST) }
        composable(Routes.CHAT) { /* Placeholder: ChatScreen(...) */ Text(Routes.CHAT) }
    }
}