package com.example.hamilocalmain.ui.screens.farmer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hamilocalmain.data.model.Product
import com.example.hamilocalmain.data.model.ProductStatus
import com.example.hamilocalmain.ui.theme.Success
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.viewmodel.ProductState
import com.example.hamilocalmain.ui.viewmodel.ProductViewModel

/**
 * Screen for farmers to manage their listed products.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductsScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    farmerId: String
) {
    val productsState by productViewModel.farmerProductsState.collectAsState()
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(farmerId) {
        productViewModel.loadFarmerProducts(farmerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Products") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (productsState) {
                is ProductState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ProductState.Error -> Text(
                    text = (productsState as ProductState.Error).message,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
                is ProductState.Success -> {
                    val products = (productsState as ProductState.Success).products
                    if (products.isEmpty()) {
                        Text(
                            text = "No products listed yet",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(products) { product ->
                                ManageProductCard(
                                    product = product,
                                    onEdit = { /* Navigate to edit screen */ },
                                    onDelete = { productToDelete = product },
                                    onToggleAvailability = { isAvailable ->
                                        val newStatus = if (isAvailable) ProductStatus.AVAILABLE else ProductStatus.OUT_OF_STOCK
                                        productViewModel.updateProduct(product.copy(status = newStatus))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (productToDelete != null) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("Delete Product?") },
                text = { Text("Are you sure you want to remove '${productToDelete?.name}' from the marketplace?") },
                confirmButton = {
                    TextButton(onClick = {
                        productToDelete?.let { productViewModel.deleteProduct(it.id) }
                        productToDelete = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * A card representing a single product listing with management controls.
 */
@Composable
private fun ManageProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "NPR ${product.price.toInt()} | Qty: ${product.availableQuantity.toInt()} ${product.unit.name.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Simple status text
                Text(
                    text = product.status.name,
                    color = if (product.status == ProductStatus.AVAILABLE) Success else Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Row {
                Switch(
                    checked = product.status == ProductStatus.AVAILABLE,
                    onCheckedChange = onToggleAvailability
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
