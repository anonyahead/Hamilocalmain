package com.example.hamilocalmain.ui.screens.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hamilocalmain.data.model.Order
import com.example.hamilocalmain.data.model.OrderStatus
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.screens.consumer.StatusBadge
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.viewmodel.OrderState
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel

/**
 * Detailed view of a specific order, showing items, status history, and price breakdown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    navController: NavController,
    orderViewModel: OrderViewModel
) {
    // In a real app, we would load the specific order by ID
    // For now, we assume it's loaded in the state
    val orderState by orderViewModel.consumerOrdersState.collectAsState()
    val order = (orderState as? OrderState.Success)?.orders?.find { it.id == orderId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (order == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Status", style = MaterialTheme.typography.titleMedium)
                    StatusBadge(status = order.status)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(order.items) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.quantity.toInt()}x ${item.productName}", modifier = Modifier.weight(1f))
                            Text("NPR ${item.totalPrice.toInt()}")
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal")
                        Text("NPR ${(order.totalAmount - order.platformFee).toInt()}")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Platform Fee", color = TextSecondary)
                        Text("NPR ${order.platformFee.toInt()}", color = TextSecondary)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("NPR ${order.totalAmount.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { navController.navigate(Routes.chat(order.farmerId)) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Message Farmer")
                }
            }
        }
    }
}
