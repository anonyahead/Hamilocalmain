/**
 * Reusable product display components for consumer screens.
 */
package com.example.hamilocalmain.ui.screens.consumer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hamilocalmain.data.model.Product
import com.example.hamilocalmain.data.model.ProductCategory
import com.example.hamilocalmain.data.model.ProductStatus
import com.example.hamilocalmain.data.model.UnitType
import com.example.hamilocalmain.ui.theme.AccentTeal
import com.example.hamilocalmain.ui.theme.SecondaryOrange
import com.example.hamilocalmain.ui.theme.Success
import com.example.hamilocalmain.ui.theme.TextPrimary
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.theme.Warning
import com.example.hamilocalmain.ui.theme.Error

/**
 * Product listing card used in BrowseProductsScreen and ConsumerHomeScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    distance: String? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Placeholder for product image
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Image Placeholder", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriceTag(price = product.price, unit = product.unit)
                
                if (product.isOrganic) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text("Organic", style = MaterialTheme.typography.labelSmall) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = AccentTeal.copy(alpha = 0.1f),
                            labelColor = AccentTeal
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = AccentTeal)
                    )
                }
            }

            Text(
                text = "Farmer: ${product.farmerName}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            if (distance != null) {
                Text(
                    text = distance,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val (statusText, statusColor) = when (product.status) {
                ProductStatus.AVAILABLE -> "Available" to Success
                ProductStatus.LOW_STOCK -> "Low Stock" to Warning
                ProductStatus.OUT_OF_STOCK -> "Out of Stock" to Error
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Category filter chip. Teal when selected.
 */
@Composable
fun CategoryChip(
    category: ProductCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val emoji = when (category) {
        ProductCategory.VEGETABLES -> "🥦"
        ProductCategory.FRUITS -> "🍎"
        ProductCategory.GRAINS -> "🌾"
        ProductCategory.DAIRY -> "🥛"
        ProductCategory.HERBS -> "🌿"
        ProductCategory.SPICES -> "🌶️"
        ProductCategory.OTHER -> "🛒"
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { 
            Text("$emoji ${category.name.lowercase().replaceFirstChar { it.uppercase() }}") 
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentTeal,
            selectedLabelColor = Color.White
        )
    )
}

/**
 * Formatted price with unit. Orange color to draw attention.
 */
@Composable
fun PriceTag(
    price: Double,
    unit: UnitType,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(
            text = "NPR ${price.toInt()}",
            style = MaterialTheme.typography.titleMedium,
            color = SecondaryOrange,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = " / ${unit.name.lowercase()}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
