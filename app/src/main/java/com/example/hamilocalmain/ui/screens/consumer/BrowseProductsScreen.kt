package com.example.hamilocalmain.ui.screens.consumer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.viewmodel.ProductState
import com.example.hamilocalmain.ui.viewmodel.ProductViewModel

/**
 * Screen for browsing and filtering all available products by radius and search query.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseProductsScreen(
    navController: NavController,
    productViewModel: ProductViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRadius by remember { mutableDoubleStateOf(10.0) }
    val productsState by productViewModel.nearbyProductsState.collectAsState()

    // Reload products whenever the selected radius changes
    LaunchedEffect(selectedRadius) {
        // Assuming location is known or using default for nearby logic
        // In a real app, coordinates would come from a LocationViewModel
        productViewModel.loadNearbyProducts(27.7172, 85.3240, selectedRadius)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse Products") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search and Filter Header
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search products...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(5.0, 10.0, 20.0).forEach { radius ->
                        FilterChip(
                            selected = selectedRadius == radius,
                            onClick = { selectedRadius = radius },
                            label = { Text("${radius.toInt()} km") }
                        )
                    }
                }
            }

            // Products Display Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (productsState) {
                    is ProductState.Loading -> {
                        items(6) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }
                    is ProductState.Success -> {
                        val products = (productsState as ProductState.Success).products
                            .filter { it.name.contains(searchQuery, ignoreCase = true) }

                        items(products) { product ->
                            ProductCard(
                                product = product,
                                onClick = { navController.navigate(Routes.productDetail(product.id)) }
                            )
                        }
                    }
                    is ProductState.Error -> {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = (productsState as ProductState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
