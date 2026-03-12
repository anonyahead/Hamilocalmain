package com.example.hamilocalmain.ui.screens.consumer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hamilocalmain.data.model.Product
import com.example.hamilocalmain.data.model.ProductStatus
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.AccentTeal
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel
import com.example.hamilocalmain.ui.viewmodel.ProductState
import com.example.hamilocalmain.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

// ==================== UI SCREEN ====================

/**
 * Detailed view of a single product with quantity selection and add-to-cart functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavController,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel
) {
    // ==================== STATE ====================
    val productsState by productViewModel.nearbyProductsState.collectAsState()
    val product = (productsState as? ProductState.Success)?.products?.find { it.id == productId }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var quantity by remember { mutableDoubleStateOf(1.0) }

    // ==================== UI LAYOUT ====================
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Implement share logic */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { padding ->
        if (product == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Product Image Carousel Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Image Carousel", color = TextSecondary)
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (product.isOrganic) {
                            SuggestionChip(
                                onClick = { },
                                label = { Text("Organic", fontSize = 12.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = AccentTeal.copy(alpha = 0.1f),
                                    labelColor = AccentTeal
                                )
                            )
                        }
                    }

                    PriceTag(price = product.price, unit = product.unit)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Farmer: ${product.farmerName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { 
                            navController.navigate(Routes.chat(product.farmerId)) 
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (product.status != ProductStatus.OUT_OF_STOCK) {
                        QuantitySelector(
                            quantity = quantity,
                            onQuantityChange = { quantity = it }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                orderViewModel.addToCart(product, quantity)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Added to cart!")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Text("Add to Cart")
                        }
                    }

                    OutlinedButton(
                        onClick = { navController.navigate(Routes.chat(product.farmerId)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text("Chat with Farmer")
                    }
                }
            }
        }
    }
}

// ==================== SUB-COMPOSABLES ====================

/**
 * UI component for picking quantity with plus and minus buttons.
 */
@Composable
private fun QuantitySelector(
    quantity: Double,
    onQuantityChange: (Double) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Quantity:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .padding(4.dp)
        ) {
            IconButton(onClick = { if (quantity > 1) onQuantityChange(quantity - 1) }) {
                Icon(Icons.Outlined.Remove, contentDescription = "Decrease")
            }
            Text(
                text = quantity.toInt().toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = { onQuantityChange(quantity + 1) }) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
    }
}
