package com.example.data.model

import com.squareup.moshi.JsonClass

enum class CatalogItemType {
    SERVICE,
    PRODUCT,
    BOILER,
    SECOND_HAND_BOILER
}

@JsonClass(generateAdapter = true)
data class CatalogItem(
    val id: String = "",
    val name: String = "",
    val type: CatalogItemType = CatalogItemType.PRODUCT,
    val unit: String = "adet",
    val defaultPrice: Double = 0.0,
    val active: Boolean = true
)

@JsonClass(generateAdapter = true)
data class StockItem(
    val id: String = "",
    val name: String = "",
    val sku: String = "",
    val unit: String = "adet",
    val quantity: Double = 0.0,
    val minimumQuantity: Double = 0.0,
    val purchasePrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val active: Boolean = true,
    val productId: String? = null,
    val barcode: String = "",
    val category: String = "",
    val brand: String = "",
    val location: String = "Konumsuz",
    val shelf: String = "",
    val imageUrl: String = "",
    val status: String = "",
    val archived: Boolean = false,
    val createdAt: String? = null,
    val catalogLinked: Boolean = false
) {
    val isLowStock: Boolean
        get() = quantity <= minimumQuantity
}

enum class StockMovementType { IN, OUT, ADJUSTMENT, REVERSAL }

@JsonClass(generateAdapter = true)
data class StockMovement(
    val id: String = "",
    val stockItemId: String = "",
    val quantity: Double = 0.0,
    val type: StockMovementType = StockMovementType.ADJUSTMENT,
    val reason: String = "",
    val appointmentId: String? = null,
    val createdAt: String = ""
)
