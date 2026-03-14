package com.example.hamilocalmain.ui.screens.consumer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import com.example.hamilocalmain.ui.viewmodel.CartItem
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel

/**
 * Manages the user's shopping cart, allowing for item adjustments, address input,
 * and final order placement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    orderViewModel: OrderViewModel,
    authViewModel: AuthViewModel
) {
    val cartItems by orderViewModel.cartItems.collectAsState()
    val cartTotal by orderViewModel.cartTotal.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    var pickupAddress by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cart") }
            )
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your cart is empty", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { navController.navigate(Routes.BROWSE_PRODUCTS) }) {
                        Text("Browse Products")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(cartItems) { cartItem ->
                        CartItemRow(cartItem = cartItem, orderViewModel = orderViewModel)
                        HorizontalDivider()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PriceBreakdownCard(subtotal = cartTotal)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text("Pickup Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        currentUser?.let {
                            orderViewModel.placeOrder(
                                consumerId = it.id,
                                consumerName = it.name,
                                pickupAddress = pickupAddress,
                                onOrdersPlaced = { createdOrders ->
                                    createdOrders.firstOrNull()?.let { firstOrder ->
                                        navController.navigate(
                                            Routes.payment(
                                                orderId = firstOrder.id,
                                                amount = firstOrder.totalAmount,
                                                farmerName = firstOrder.farmerName,
                                                pickupAddress = firstOrder.pickupAddress
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pickupAddress.isNotBlank()
                ) {
                    Text("Confirm Order")
                }
            }
        }
    }
}

/**
 * Displays a single item in the cart with quantity controls.
 */
@Composable
private fun CartItemRow(cartItem: CartItem, orderViewModel: OrderViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(cartItem.product.name, fontWeight = FontWeight.Bold)
            Text("Farmer: ${cartItem.product.farmerName}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { 
                if (cartItem.quantity > 1) {
                    orderViewModel.addToCart(cartItem.product, -1.0) 
                } else {
                    orderViewModel.removeFromCart(cartItem.product.id)
                }
            }) {
                Icon(Icons.Outlined.Remove, contentDescription = "Decrease")
            }
            Text(cartItem.quantity.toInt().toString(), style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = { orderViewModel.addToCart(cartItem.product, 1.0) }) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        val totalItemPrice = cartItem.product.price * cartItem.quantity
        Text("NPR ${totalItemPrice.toInt()}", fontWeight = FontWeight.Bold)

        IconButton(onClick = { orderViewModel.removeFromCart(cartItem.product.id) }) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
        }
    }
}

/**
 * A card that shows the subtotal, platform fee, and total amount of the order.
 */
@Composable
private fun PriceBreakdownCard(subtotal: Double) {
    val platformFee = subtotal * 0.02
    val total = subtotal + platformFee

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Items subtotal:")
                Text("NPR ${subtotal.toInt()}")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Platform fee (2%):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("NPR ${platformFee.toInt()}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total:", fontWeight = FontWeight.Bold)
                Text("NPR ${total.toInt()}", fontWeight = FontWeight.Bold)
            }
        }
    }
}

