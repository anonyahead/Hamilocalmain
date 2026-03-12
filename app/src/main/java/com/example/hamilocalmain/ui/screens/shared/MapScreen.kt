package com.example.hamilocalmain.ui.screens.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hamilocalmain.ui.viewmodel.LocationViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/**
 * Full-screen map view showing the user's location and nearby farmers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(locationViewModel: LocationViewModel) {
    val currentLocation by locationViewModel.currentLocation.collectAsState()
    val userLatLng = currentLocation?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(27.7172, 85.3240) // Default to Kathmandu
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 12f)
    }

    // Modal Bottom Sheet state
    var selectedFarmer by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Nearby Farmers Map") })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = currentLocation != null),
                uiSettings = MapUiSettings(myLocationButtonEnabled = true)
            ) {
                // Example Farmer Marker (Green Pin)
                Marker(
                    state = MarkerState(position = LatLng(userLatLng.latitude + 0.01, userLatLng.longitude + 0.01)),
                    title = "Organic Valley Farm",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                    onClick = {
                        selectedFarmer = "Organic Valley Farm"
                        showBottomSheet = true
                        true
                    }
                )
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Text(text = selectedFarmer ?: "", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Products: 15 available", style = MaterialTheme.typography.bodyLarge)
                        Text("Distance: 1.2 km away", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { /* Navigate to Farmer Profile */ }, modifier = Modifier.fillMaxWidth()) {
                            Text("View Harvest")
                        }
                    }
                }
            }
        }
    }
}
