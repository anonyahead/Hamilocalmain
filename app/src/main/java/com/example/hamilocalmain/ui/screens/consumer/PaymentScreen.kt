/**
 * Payment screen for Hami Local.
 * Currently implements a "Cash on Pickup" model where orders are confirmed without immediate digital payment.
 *
 * NOTE: For production scaling, this is the location to integrate digital payment gateways
 * like Khalti or eSewa by replacing the confirm button logic with the respective SDK calls.
 */
package com.example.hamilocalmain.ui.screens.consumer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.SecondaryOrange
import com.example.hamilocalmain.ui.theme.Success
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel
import kotlinx.coroutines.delay

@Composable
fun PaymentScreen(
    navController: NavController,
    orderId: String,
    totalAmount: Double,
    farmerName: String,
    pickupAddress: String,
    orderViewModel: OrderViewModel
) {
    var showSuccessView by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    if (showSuccessView) {
        PaymentSuccessView(totalAmount, navController)
    } else {
        PaymentConfirmView(
            totalAmount = totalAmount,
            farmerName = farmerName,
            pickupAddress = pickupAddress,
            isLoading = isLoading,
            onBack = { navController.popBackStack() },
            onConfirm = {
                isLoading = true
                orderViewModel.confirmOrderPayment(orderId) {
                    isLoading = false
                    showSuccessView = true
                }
            }
        )
    }
}

/**
 * Confirmation view where the user reviews the amount and selects the payment method.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentConfirmView(
    totalAmount: Double,
    farmerName: String,
    pickupAddress: String,
    isLoading: Boolean,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Confirmation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Due Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Amount Due", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "NPR ${totalAmount.toInt()}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Order Details Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order Details", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Farmer: $farmerName", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text("Pickup at: $pickupAddress", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Payment Method Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = true, onClick = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Cash on Pickup", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryOrange),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirm Order — Pay on Pickup", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * Success view displayed after the order is confirmed.
 */
@Composable
private fun PaymentSuccessView(totalAmount: Double, navController: NavController) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        navController.navigate(Routes.ORDER_HISTORY) {
            popUpTo(Routes.PAYMENT) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.8f)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Success,
                    modifier = Modifier.size(100.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Order Confirmed!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "You'll pay NPR ${totalAmount.toInt()} when you pick up",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }
    }
}
