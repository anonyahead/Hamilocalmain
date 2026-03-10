package com.example.hamilocalmain.ui.screens.consumer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hamilocalmain.data.model.ProductCategory
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.PrimaryGreen
import com.example.hamilocalmain.ui.theme.TextPrimary
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.viewmodel.*

/**
 * Main home screen for consumers. Displays welcome banner, categories, and nearby products.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumerHomeScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel,
    locationViewModel: LocationViewModel,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val nearbyProductsState by productViewModel.nearbyProductsState.collectAsState()
    val cartItems by orderViewModel.cartItems.collectAsState()
    val currentLocation by locationViewModel.currentLocation.collectAsState()

    // Load nearby products when location is available
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            productViewModel.loadNearbyProducts(it.latitude, it.longitude, 10.0)
        } ?: run {
            // Default location for testing if not available, or request it
            // locationViewModel.requestLocation(context) 
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌾 ", fontSize = 24.sp)
                        Text("Hami Local", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Navigate to Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    BadgedBox(
                        badge = {
                            if (cartItems.isNotEmpty()) {
                                Badge { Text(cartItems.size.toString()) }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = { navController.navigate(Routes.CART) }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Welcome Banner
            item(span = { GridItemSpan(2) }) {
                WelcomeBanner(userName = currentUser?.name ?: "User")
            }

            // Search Bar
            item(span = { GridItemSpan(2) }) {
                SearchBar(onClick = { navController.navigate(Routes.BROWSE_PRODUCTS) })
            }

            // Categories
            item(span = { GridItemSpan(2) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(ProductCategory.values()) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = false, // Home shows all, clicking one can go to Browse with filter
                            onClick = { 
                                // Navigate to browse products with this category selected
                                navController.navigate(Routes.BROWSE_PRODUCTS) 
                            }
                        )
                    }
                }
            }

            // Nearby Products Section Header
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Nearby Products",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Nearby Products Grid
            when (nearbyProductsState) {
                is ProductState.Loading -> {
                    items(4) {
                        // Placeholder for shimmer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                is ProductState.Success -> {
                    val products = (nearbyProductsState as ProductState.Success).products
                    if (products.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No products nearby. Increase your search radius.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        items(products) { product ->
                            ProductCard(
                                product = product,
                                onClick = { navController.navigate(Routes.productDetail(product.id)) }
                            )
                        }
                    }
                }
                is ProductState.Error -> {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = (nearbyProductsState as ProductState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays a welcome message to the user.
 */
@Composable
private fun WelcomeBanner(userName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Namaste, $userName! 🙏",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Fresh harvest is waiting for you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Interactive search bar that triggers navigation.
 */
@Composable
private fun SearchBar(onClick: () -> Unit) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        enabled = false, // Clicking the disabled field triggers onClick
        placeholder = { Text("Search for fresh products...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
