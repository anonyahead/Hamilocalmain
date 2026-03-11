package com.example.hamilocalmain.ui.screens.farmer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel
import com.example.hamilocalmain.ui.viewmodel.ProductViewModel

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
    onNavigateToShortageManagement: () -> Unit = {}
) {
    val currentUser by authViewModel.currentUser.collectAsState()

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
                StatCard(title = "My Products", value = "12", modifier = Modifier.weight(1f))
                StatCard(title = "Active Orders", value = "5", modifier = Modifier.weight(1f))
                StatCard(title = "Earnings", value = "NPR 5,420", modifier = Modifier.weight(1f))
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
            // Placeholder for recent orders list
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
