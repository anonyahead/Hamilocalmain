package com.example.hamilocalmain.ui.screens.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hamilocalmain.ui.theme.SecondaryOrange
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel
import kotlinx.coroutines.delay

/**
 * Screen for users to rate their order experience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    navController: NavController,
    orderId: String,
    farmerName: String,
    orderViewModel: OrderViewModel
) {
    var rating by remember { mutableIntStateOf(0) }
    var review by remember { mutableStateOf("") }
    var showThankYou by remember { mutableStateOf(false) }

    if (showThankYou) {
        ThankYouView { navController.popBackStack() }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Rate Your Order") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "How was your order from $farmerName?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                StarRatingRow(rating = rating, onRatingChange = { rating = it })

                val ratingLabel = when (rating) {
                    1 -> "Poor"
                    2 -> "Fair"
                    3 -> "Good"
                    4 -> "Very Good"
                    5 -> "Excellent 🌟"
                    else -> ""
                }

                Text(
                    text = ratingLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = SecondaryOrange,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = review,
                    onValueChange = { review = it },
                    label = { Text("Optional: Write a review") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        orderViewModel.submitRating(orderId, rating, review) {
                            showThankYou = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = rating > 0
                ) {
                    Text("Submit Rating")
                }

                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Skip for now")
                }
            }
        }
    }
}

/**
 * A row of 5 stars for selecting a rating.
 */
@Composable
private fun StarRatingRow(rating: Int, onRatingChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 1..5) {
            val isSelected = i <= rating
            Icon(
                imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (isSelected) SecondaryOrange else Color.Gray,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onRatingChange(i) }
            )
        }
    }
}

/**
 * View displayed after a rating is successfully submitted.
 */
@Composable
private fun ThankYouView(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onDismiss()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("❤️", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Thanks for your feedback!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
