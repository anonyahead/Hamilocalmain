package com.example.hamilocalmain.ui.screens.farmer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hamilocalmain.data.model.Product
import com.example.hamilocalmain.data.model.ProductCategory
import com.example.hamilocalmain.data.model.UnitType
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import com.example.hamilocalmain.ui.viewmodel.ProductViewModel

/**
 * Screen for farmers to list a new product on the marketplace.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel
) {
    // 1. Get current user
    val currentUser by authViewModel.currentUser.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(UnitType.KG) }
    var selectedCategory by remember { mutableStateOf(ProductCategory.VEGETABLES) }
    var isOrganic by remember { mutableStateOf(false) }

    var unitExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("List New Product") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Info
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Price and Quantity
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (NPR)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            // Unit Selection
            Box {
                OutlinedTextField(
                    value = selectedUnit.name,
                    onValueChange = { },
                    label = { Text("Unit") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) }
                )
                DropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UnitType.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.name) },
                            onClick = {
                                selectedUnit = unit
                                unitExpanded = false
                            }
                        )
                    }
                }
                Box(Modifier.matchParentSize().clickable { unitExpanded = true })
            }

            // Category Selection
            Box {
                OutlinedTextField(
                    value = selectedCategory.name,
                    onValueChange = { },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                )
                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProductCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
                Box(Modifier.matchParentSize().clickable { categoryExpanded = true })
            }

            // Organic Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Is this product organic?", modifier = Modifier.weight(1f))
                Switch(checked = isOrganic, onCheckedChange = { isOrganic = it })
            }

            // Image Picker Placeholder
            OutlinedButton(
                onClick = { /* Image picker logic */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Product Images")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    val product = Product(
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        availableQuantity = quantity.toDoubleOrNull() ?: 0.0,
                        unit = selectedUnit,
                        category = selectedCategory,
                        isOrganic = isOrganic,
                        // 2. Set farmer details from current user
                        farmerId = currentUser?.id ?: "",
                        farmerName = currentUser?.name ?: ""
                    )
                    productViewModel.addProduct(product)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
            ) {
                Text("List Product")
            }
        }
    }
}
