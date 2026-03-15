package com.example.hamilocalmain.ui.screens.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.PrimaryGreen
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.viewmodel.LocationViewModel
import com.example.hamilocalmain.ui.viewmodel.ProductState
import com.example.hamilocalmain.ui.viewmodel.ProductViewModel

/**
 * Screen displaying a list of nearby farmers instead of a heavy map.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    locationViewModel: LocationViewModel,
    navController: NavController,
    productViewModel: ProductViewModel
) {
    val currentLocation by locationViewModel.currentLocation.collectAsState()
    val nearbyProductsState by productViewModel.nearbyProductsState.collectAsState()

    LaunchedEffect(Unit) {
        val lat = currentLocation?.latitude ?: 0.0
        val lng = currentLocation?.longitude ?: 0.0
        productViewModel.loadNearbyProducts(lat, lng, 50.0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Farmers") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (nearbyProductsState) {
            is ProductState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is ProductState.Success -> {
                val farmers = (nearbyProductsState as ProductState.Success).products
                    .groupBy { it.farmerId }
                if (farmers.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("No farmers found nearby.", color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        farmers.forEach { (_, products) ->
                            val farmer = products.first()
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { navController.navigate(Routes.BROWSE_PRODUCTS) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(48.dp),
                                            shape = CircleShape,
                                            color = PrimaryGreen.copy(alpha = 0.15f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("🌾", fontSize = 22.sp)
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(farmer.farmerName, fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium)
                                            Text("${products.size} product(s) available",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                                            tint = PrimaryGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is ProductState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text((nearbyProductsState as ProductState.Error).message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
