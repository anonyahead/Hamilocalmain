package com.example.hamilocalmain.data.firebase

import com.example.hamilocalmain.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.math.*

/**
 * Repository for managing product data in Firebase Firestore.
 */
class ProductRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val productsCollection = firestore.collection("products")

    /**
     * Lists a new product on the marketplace.
     */
    suspend fun addProduct(product: Product): Result<Product> {
        return try {
            val docRef = productsCollection.document()
            val productWithId = product.copy(id = docRef.id)
            docRef.set(productWithId).await()
            Result.success(productWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing product listing.
     */
    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            productsCollection.document(product.id).set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Removes a product from the marketplace.
     */
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns live Flow of this farmer's products.
     */
    fun getProductsByFarmer(farmerId: String): Flow<List<Product>> {
        return productsCollection
            .whereEqualTo("farmerId", farmerId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Product::class.java)
            }
    }

    /**
     * Fetches a single product by ID.
     */
    suspend fun getProduct(productId: String): Result<Product> {
        return try {
            val snapshot = productsCollection.document(productId).get().await()
            val product = snapshot.toObject(Product::class.java)
            if (product != null) {
                Result.success(product)
            } else {
                Result.failure(Exception("Product not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns products within radiusKm of given coordinates. Uses Haversine formula.
     */
    fun getProductsNearby(lat: Double, lng: Double, radiusKm: Double): Flow<List<Product>> {
        return productsCollection.snapshots().map { snapshot ->
            val allProducts = snapshot.toObjects(Product::class.java)
            allProducts.filter { product ->
                // If either the consumer or product has no real location (default 0,0),
                // include the product so it's always visible
                val productHasLocation = product.location.latitude != 0.0 || product.location.longitude != 0.0
                val consumerHasLocation = lat != 0.0 || lng != 0.0
                if (!productHasLocation || !consumerHasLocation) {
                    true  // show product if no location data
                } else {
                    calculateDistance(lat, lng, product.location.latitude, product.location.longitude) <= radiusKm
                }
            }
        }
    }

    /**
     * Updates available stock quantity.
     */
    suspend fun updateProductQuantity(productId: String, newQuantity: Double): Result<Unit> {
        return try {
            productsCollection.document(productId)
                .update("availableQuantity", newQuantity)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculates the distance between two points in kilometers using the Haversine formula.
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
