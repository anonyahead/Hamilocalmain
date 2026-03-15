package com.example.hamilocalmain.ui.screens.farmer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hamilocalmain.data.model.OrderStatus
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.SecondaryOrange
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import com.example.hamilocalmain.ui.viewmodel.CurrencyViewModel
import com.example.hamilocalmain.ui.viewmodel.OrderState
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel
import com.example.hamilocalmain.ui.viewmodel.ProductState
import com.example.hamilocalmain.ui.viewmodel.ProductViewModel
import java.util.Locale

/**
 * Farmer's main dashboard. onNavigateToShortageManagement is called by the Shortage Management card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDashboardScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel,
    authViewModel: AuthViewModel,
    currencyViewModel: CurrencyViewModel,
    onNavigateToShortageManagement: () -> Unit = {}
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    // 1. Add LaunchedEffect(currentUser) to load data
    LaunchedEffect(currentUser) {
        currentUser?.id?.let {
            productViewModel.loadFarmerProducts(it)
            orderViewModel.loadFarmerOrders(it)
        }
    }

    // 2. Collect states
    val farmerProductsState by productViewModel.farmerProductsState.collectAsState()
    val farmerOrdersState by orderViewModel.farmerOrdersState.collectAsState()

    // 3. Replace hardcoded values with calculated state values
    val productCount = (farmerProductsState as? ProductState.Success)?.products?.size ?: 0
    
    val activeOrdersCount = (farmerOrdersState as? OrderState.Success)?.orders?.count {
        it.status == OrderStatus.PENDING || it.status == OrderStatus.CONFIRMED
    } ?: 0

    val totalEarnings = (farmerOrdersState as? OrderState.Success)?.orders?.filter {
        it.status == OrderStatus.COMPLETED
    }?.sumOf { it.farmerEarnings } ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.ADD_PRODUCT) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        },
        bottomBar = {
            // Item 6: Add BottomNavigationBar to FarmerDashboardScreen
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Receipt, null) },
                    label = { Text("Orders") },
                    selected = false,
                    onClick = { navController.navigate(Routes.FARMER_ORDERS) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) },
                    label = { Text("Messages") },
                    selected = false,
                    onClick = { navController.navigate(Routes.CHAT_LIST) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { navController.navigate(Routes.SETTINGS) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Welcome Banner
            Text("Welcome, ${currentUser?.name ?: "Farmer"}!", style = MaterialTheme.typography.headlineSmall)
            
            Spacer(modifier = Modifier.height(16.dp))

            // Stat Cards
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "My Available Products",
                    value = productCount.toString(), 
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Active Orders", 
                    value = activeOrdersCount.toString(), 
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Earnings", 
                    value = currencyViewModel.format(totalEarnings),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { QuickActionCard("📦 My Products", "View & manage your listings") { navController.navigate(Routes.MANAGE_PRODUCTS) } }
                item { QuickActionCard("📋 View Orders", "Check incoming orders") { navController.navigate(Routes.FARMER_ORDERS) } }
                item { QuickActionCard("⚖️ Shortage Mgmt", "Allocate scarce items") { onNavigateToShortageManagement() } }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recent Orders
            Text("Recent Orders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            // Item 7: Replace placeholder with actual orders list
            when (farmerOrdersState) {
                is OrderState.Loading -> CircularProgressIndicator()
                is OrderState.Success -> {
                    val recentOrders = (farmerOrdersState as OrderState.Success).orders.take(3)
                    if (recentOrders.isEmpty()) {
                        Text("No recent orders", color = TextSecondary)
                    } else {
                        recentOrders.forEach { order ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                onClick = { navController.navigate(Routes.orderDetail(order.id)) }
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(order.consumerName, fontWeight = FontWeight.Bold)
                                    Text(currencyViewModel.format(order.totalAmount), color = SecondaryOrange)
                                    Text(order.status.name)
                                }
                            }
                        }
                    }
                }
                is OrderState.Error -> Text((farmerOrdersState as OrderState.Error).message)
            }
        }
    }
}

/**
 * Displays a key statistic in a small card.
 */
@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * A card for a quick navigation action on the dashboard.
 */
@Composable
fun QuickActionCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(150.dp, 120.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
