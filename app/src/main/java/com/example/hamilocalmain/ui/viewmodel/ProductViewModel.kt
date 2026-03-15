package com.example.hamilocalmain.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hamilocalmain.data.firebase.ProductRepository
import com.example.hamilocalmain.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * State representing product data availability or errors.
 */
sealed class ProductState {
    object Loading : ProductState()
    data class Success(val products: List<Product>) : ProductState()
    data class Error(val message: String) : ProductState()
}

/**
 * Manages product logic for both farmer listings and consumer browsing.
 */
class ProductViewModel(private val repository: ProductRepository = ProductRepository()) : ViewModel() {

    private val _farmerProductsState = MutableStateFlow<ProductState>(ProductState.Loading)
    val farmerProductsState: StateFlow<ProductState> = _farmerProductsState.asStateFlow()

    private val _nearbyProductsState = MutableStateFlow<ProductState>(ProductState.Loading)
    val nearbyProductsState: StateFlow<ProductState> = _nearbyProductsState.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // all Firebase and repository calls here
        }
    }

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            val result = repository.getProduct(productId)
            result.onSuccess { _selectedProduct.value = it }
        }
    }

    /**
     * Loads live updates of products belonging to a specific farmer.
     * 
     * @param farmerId The unique identifier of the farmer.
     */
    fun loadFarmerProducts(farmerId: String) {
        viewModelScope.launch {
            _farmerProductsState.value = ProductState.Loading
            repository.getProductsByFarmer(farmerId)
                .catch { e -> _farmerProductsState.value = ProductState.Error(e.message ?: "Unknown error") }
                .collect { products -> _farmerProductsState.value = ProductState.Success(products) }
        }
    }

    /**
     * Loads products within a specific radius of a location.
     * 
     * @param lat Latitude of the search center.
     * @param lng Longitude of the search center.
     * @param radiusKm Search radius in kilometers.
     */
    fun loadNearbyProducts(lat: Double, lng: Double, radiusKm: Double) {
        viewModelScope.launch {
            _nearbyProductsState.value = ProductState.Loading
            repository.getProductsNearby(lat, lng, radiusKm)
                .catch { e -> _nearbyProductsState.value = ProductState.Error(e.message ?: "Unknown error") }
                .collect { products -> _nearbyProductsState.value = ProductState.Success(products) }
        }
    }

    /**
     * Lists a new product on the marketplace.
     * 
     * @param product The product details to add.
     */
    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.addProduct(product)
        }
    }

    /**
     * Updates an existing product listing.
     * 
     * @param product The updated product details.
     */
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    /**
     * Removes a product from the marketplace.
     * 
     * @param productId The unique ID of the product to delete.
     */
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }
}
