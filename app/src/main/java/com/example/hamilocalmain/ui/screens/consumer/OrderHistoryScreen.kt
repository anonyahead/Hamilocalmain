package com.example.hamilocalmain.ui.screens.consumer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hamilocalmain.data.model.Order
import com.example.hamilocalmain.data.model.OrderStatus
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.*
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import com.example.hamilocalmain.ui.viewmodel.OrderState
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Displays a list of the consumer's past and current orders.
 */
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    orderViewModel: OrderViewModel,
    authViewModel: AuthViewModel,
    onNavigateToRating: (orderId: String, farmerName: String) -> Unit
) {
    val orderState by orderViewModel.consumerOrdersState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.id?.let {
            orderViewModel.loadConsumerOrders(it)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Orders") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (orderState) {
                is OrderState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is OrderState.Error -> Text(
                    text = (orderState as OrderState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                is OrderState.Success -> {
                    val orders = (orderState as OrderState.Success).orders
                    if (orders.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("📦", style = MaterialTheme.typography.displayLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No orders yet", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { navController.navigate(Routes.BROWSE_PRODUCTS) }) {
                                Text("Browse Products")
                            }
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp)) {
                            items(orders) { order ->
                                OrderHistoryCard(order = order, onNavigateToRating = onNavigateToRating)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A card that displays summary information for a single order in the history list.
 */
@Composable
private fun OrderHistoryCard(
    order: Order,
    onNavigateToRating: (orderId: String, farmerName: String) -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Order on ${sdf.format(Date(order.createdAt))}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                StatusBadge(status = order.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sold by: ${order.farmerName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = order.items.joinToString { "${it.quantity.toInt()}x ${it.productName}" },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Total: NPR ${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                if (order.status == OrderStatus.COMPLETED && !order.rated) {
                    Button(
                        onClick = { onNavigateToRating(order.id, order.farmerName) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Rate Order ⭐")
                    }
                }
            }
        }
    }
}

/**
 * A colored badge to indicate the current status of an order.
 */
@Composable
private fun StatusBadge(status: OrderStatus) {
    val (text, color) = when (status) {
        OrderStatus.PENDING -> "Pending" to Warning
        OrderStatus.CONFIRMED -> "Confirmed" to PrimaryGreen
        OrderStatus.READY_FOR_PICKUP -> "Ready for Pickup" to SecondaryOrange
        OrderStatus.COMPLETED -> "Completed" to Success
        OrderStatus.CANCELLED -> "Cancelled" to Error
        OrderStatus.PICKED_UP -> "Picked Up" to Success
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

