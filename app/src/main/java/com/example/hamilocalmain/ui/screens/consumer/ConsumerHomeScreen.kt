package com.example.hamilocalmain.ui.screens.consumer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.hamilocalmain.data.model.ProductCategory
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.PrimaryGreen
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
    authViewModel: AuthViewModel,
    currencyViewModel: CurrencyViewModel
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val nearbyProductsState by productViewModel.nearbyProductsState.collectAsState()
    val cartItems by orderViewModel.cartItems.collectAsState()
    val currentLocation by locationViewModel.currentLocation.collectAsState()

    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }
    var selectedRadius by remember { mutableDoubleStateOf(10.0) }  // 0.0 = show all

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) locationViewModel.requestLocation(context)
        else productViewModel.loadNearbyProducts(0.0, 0.0, 10.0) // fallback
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationViewModel.requestLocation(context)
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Load nearby products when location or radius changes
    LaunchedEffect(currentLocation, selectedRadius) {
        val lat = currentLocation?.latitude ?: 0.0
        val lng = currentLocation?.longitude ?: 0.0
        productViewModel.loadNearbyProducts(lat, lng, if (selectedRadius == 0.0) 500.0 else selectedRadius)
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
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                    IconButton(onClick = { navController.navigate(Routes.CHAT_LIST) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Messages")
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
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingBag, null) },
                    label = { Text("Orders") },
                    selected = false,
                    onClick = { navController.navigate(Routes.ORDER_HISTORY) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) },
                    label = { Text("Messages") },
                    selected = false,
                    onClick = { navController.navigate(Routes.CHAT_LIST) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Map, null) },
                    label = { Text("Map") },
                    selected = false,
                    onClick = { navController.navigate(Routes.MAP) }
                )
            }
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

            // Radius Filter Chips
            item(span = { GridItemSpan(2) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(listOf(5.0, 10.0, 20.0, 0.0)) { radius ->
                        FilterChip(
                            selected = selectedRadius == radius,
                            onClick = {
                                selectedRadius = radius
                            },
                            label = { Text(if (radius == 0.0) "All" else "${radius.toInt()} km") }
                        )
                    }
                }
            }

            // Categories
            item(span = { GridItemSpan(2) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(ProductCategory.values()) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategory == category,
                            onClick = {
                                selectedCategory = if (selectedCategory == category) null else category
                            }
                        )
                    }
                }
            }

            // Nearby Products Section Header
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Nearby Products",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = {
                        selectedRadius = 0.0
                        selectedCategory = null
                        val lat = currentLocation?.latitude ?: 0.0
                        val lng = currentLocation?.longitude ?: 0.0
                        productViewModel.loadNearbyProducts(lat, lng, 500.0)
                    }) {
                        Text("Show All", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Nearby Products Grid
            when (nearbyProductsState) {
                is ProductState.Loading -> {
                    items(4) {
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
                    val allProducts = (nearbyProductsState as ProductState.Success).products
                    val products = if (selectedCategory != null) {
                        allProducts.filter { it.category == selectedCategory }
                    } else {
                        allProducts
                    }

                    if (products.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (selectedCategory != null)
                                            "No ${selectedCategory?.name?.lowercase()} nearby."
                                        else
                                            "No products nearby.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    TextButton(onClick = {
                                        selectedCategory = null
                                        selectedRadius = 0.0
                                        productViewModel.loadNearbyProducts(0.0, 0.0, 500.0)
                                    }) {
                                        Text("Show All Products")
                                    }
                                }
                            }
                        }
                    } else {
                        items(products) { product ->
                            ProductCard(
                                product = product,
                                onClick = { navController.navigate(Routes.productDetail(product.id)) },
                                currencyViewModel = currencyViewModel
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
        enabled = false,
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
