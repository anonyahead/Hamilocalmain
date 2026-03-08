package com.example.hamilocalmain.data.model

/**
 * Unit of measurement for product quantity.
 */
enum class UnitType {
    KG,
    GRAM,
    PIECE,
    BUNDLE,
    LITER,
    DOZEN
}

/**
 * Category for filtering and browsing products.
 */
enum class ProductCategory {
    VEGETABLES,
    FRUITS,
    GRAINS,
    DAIRY,
    SPICES,
    HERBS,
    OTHER
}

/**
 * Current stock availability status.
 */
enum class ProductStatus {
    AVAILABLE,
    LOW_STOCK,
    OUT_OF_STOCK
}

/**
 * A farm product listed for sale. pendingOrderQuantity is used by ShortageManagementScreen to detect when demand exceeds supply.
 *
 * @property id Unique identifier for the product.
 * @property farmerId Unique identifier of the farmer who owns this product.
 * @property farmerName Name of the farmer or farm.
 * @property name Name of the product (e.g., "Organic Tomatoes").
 * @property description Detailed description of the product.
 * @property price Price per unit.
 * @property availableQuantity Current quantity available for sale.
 * @property pendingOrderQuantity Quantity requested in orders but not yet fulfilled.
 * @property unit Unit of measurement for the quantities.
 * @property category Broad category the product belongs to.
 * @property images List of URLs or paths to product images.
 * @property isOrganic Whether the product is certified organic or grown using organic practices.
 * @property status Current availability status in the store.
 * @property location Geographic coordinates where the product is located/sourced.
 * @property createdAt Timestamp of when the product listing was created.
 */
data class Product(
    val id: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val availableQuantity: Double = 0.0,
    val pendingOrderQuantity: Double = 0.0,
    val unit: UnitType = UnitType.KG,
    val category: ProductCategory = ProductCategory.VEGETABLES,
    val images: List<String> = emptyList(),
    val isOrganic: Boolean = false,
    val status: ProductStatus = ProductStatus.AVAILABLE,
    val location: GeoPoint = GeoPoint(),
    val createdAt: Long = System.currentTimeMillis()
)
