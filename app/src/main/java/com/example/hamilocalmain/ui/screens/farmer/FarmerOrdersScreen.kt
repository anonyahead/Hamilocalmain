package com.example.hamilocalmain.ui.screens.farmer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hamilocalmain.data.model.Order
import com.example.hamilocalmain.data.model.OrderStatus
import com.example.hamilocalmain.ui.screens.consumer.StatusBadge
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import com.example.hamilocalmain.ui.viewmodel.OrderState
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel

/**
 * Screen for farmers to view and manage incoming orders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerOrdersScreen(
    navController: NavController,
    orderViewModel: OrderViewModel,
    authViewModel: AuthViewModel
) {
    val orderState by orderViewModel.farmerOrdersState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.id?.let { orderViewModel.loadFarmerOrders(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Incoming Orders") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
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
                        Text("No orders yet.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp)) {
                            items(orders) { order ->
                                FarmerOrderCard(order = order, orderViewModel = orderViewModel)
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
 * A card that displays a single incoming order with actions for the farmer.
 */
@Composable
private fun FarmerOrderCard(order: Order, orderViewModel: OrderViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.consumerName, fontWeight = FontWeight.Bold)
                StatusBadge(status = order.status)
            }
            Text(
                text = order.items.joinToString { "${it.quantity.toInt()}x ${it.productName}" },
                style = MaterialTheme.typography.bodyMedium, 
                color = TextSecondary
            )
            Text("Pickup at: ${order.pickupAddress}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                when (order.status) {
                    OrderStatus.PENDING -> {
                        Button(onClick = { orderViewModel.updateOrderStatus(order.id, OrderStatus.CONFIRMED) }) {
                            Text("Accept Order")
                        }
                    }
                    OrderStatus.CONFIRMED -> {
                        Button(onClick = { orderViewModel.updateOrderStatus(order.id, OrderStatus.READY_FOR_PICKUP) }) {
                            Text("Mark Ready")
                        }
                    }
                    OrderStatus.READY_FOR_PICKUP -> {
                        Button(onClick = { orderViewModel.updateOrderStatus(order.id, OrderStatus.PICKED_UP) }) {
                            Text("Mark Picked Up")
                        }
                    }
                    else -> { /* No action for other statuses */ }
                }
            }
        }
    }
}
