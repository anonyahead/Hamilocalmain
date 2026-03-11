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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hamilocalmain.data.model.AllocationResult
import com.example.hamilocalmain.ui.theme.PrimaryGreen
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.viewmodel.ProductState
import com.example.hamilocalmain.ui.viewmodel.ProductViewModel
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel

/**
 * Screen for farmers to manage product shortages using a fair allocation algorithm.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortageManagementScreen(
    navController: NavController,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel
) {
    val productState by productViewModel.farmerProductsState.collectAsState()
    val isAllocating by orderViewModel.isAllocating.collectAsState()
    val allocationResults by orderViewModel.allocationResults.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shortage Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Info Banner
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Text(
                        text = "When demand exceeds supply, use the Fair Allocation tool to distribute stock proportionally among all pending orders.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (productState) {
                    is ProductState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    is ProductState.Error -> Text((productState as ProductState.Error).message, color = MaterialTheme.colorScheme.error)
                    is ProductState.Success -> {
                        val shortageProducts = (productState as ProductState.Success).products
                            .filter { it.pendingOrderQuantity > it.availableQuantity }

                        if (shortageProducts.isEmpty()) {
                            Text("✅ All products have sufficient stock", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(shortageProducts) { product ->
                                    ShortageProductCard(
                                        productName = product.name,
                                        available = product.availableQuantity,
                                        demanded = product.pendingOrderQuantity,
                                        onRunAllocation = { orderViewModel.triggerFairAllocation(product.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                
                if (allocationResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Allocation Results", style = MaterialTheme.typography.titleLarge)
                    LazyColumn {
                        items(allocationResults) { result ->
                            AllocationResultCard(result = result)
                        }
                    }
                }
            }

            // Spinner Overlay
            if (isAllocating) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * A card that highlights a product with a stock shortage.
 */
@Composable
private fun ShortageProductCard(
    productName: String,
    available: Double,
    demanded: Double,
    onRunAllocation: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Available: $available | Demanded: $demanded", style = MaterialTheme.typography.bodySmall)
                Text("Shortage: ${demanded - available}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = onRunAllocation) {
                Text("Run Fair Allocation ⚖️")
            }
        }
    }
}

/**
 * Displays the result of the allocation for a single consumer.
 */
@Composable
private fun AllocationResultCard(result: AllocationResult) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(result.consumerName, style = MaterialTheme.typography.bodyMedium)
        Text("Requested: ${result.requestedQuantity} → Allocated: ${result.allocatedQuantity}", color = TextSecondary)
    }
}
