package com.example.vvusa_jc.ui.market

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MarketViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // UI state
    private val _uiState = MutableStateFlow<MarketUiState>(MarketUiState.Loading)
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    // Products data
    var products by mutableStateOf<List<Product>>(emptyList())
        private set

    // Filters
    var selectedCategory by mutableStateOf<String?>(null)
        private set
    var searchQuery by mutableStateOf("")
        private set

    // New product form
    var newProductName by mutableStateOf("")
        private set
    var newProductPrice by mutableStateOf("")
        private set
    var newProductCategory by mutableStateOf("Clothes")
        private set
    var newProductCondition by mutableStateOf("Brand new")
        private set
    var newProductDescription by mutableStateOf("")
        private set
    var newProductImageUri by mutableStateOf<Uri?>(null)
        private set
    var isProduct by mutableStateOf(true) // true for product, false for service
        private set

    // Constants
    val productCategories = listOf("Clothes", "Shoes", "Bags", "Gadgets", "Stationary", "Cosmetics")
    val serviceCategories = listOf("Gadget Repairs", "Hair Dressing", "Barbering", "Catering")
    val conditions = listOf("Brand new", "Fairly used", "Used")

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            try {
                _uiState.value = MarketUiState.Loading

                // Using a single "products" collection with category field
                val productsRef = firestore.collection("Products")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)

                // Apply category filter if selected
                val query = if (selectedCategory != null) {
                    productsRef.whereEqualTo("category", selectedCategory)
                } else {
                    productsRef
                }

                val result = query.get().await()
                var fetchedProducts = result.documents.mapNotNull { doc ->
                    try {
                        Product(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getLong("price")?.toInt() ?: 0,
                            category = doc.getString("category") ?: "",
                            description = doc.getString("description") ?: "",
                            condition = doc.getString("condition") ?: "Brand new",
                            imageUrl = doc.getString("imageUrl"),
                            sellerId = doc.getString("sellerId") ?: "",
                            isProduct = doc.getBoolean("isProduct") ?: true,
                            timestamp = doc.getLong("timestamp") ?: 0
                        )
                    } catch (e: Exception) {
                        Log.e("MarketViewModel", "Error parsing product: ${doc.id}", e)
                        null
                    }
                }

                // Apply search filter in memory (Firestore doesn't support text search directly)
                    if (searchQuery.isNotBlank()) {
                        val searchLower = searchQuery.lowercase()
                        fetchedProducts = fetchedProducts.filter { product ->
                            product.name.lowercase().contains(searchLower) ||
                                    product.description.lowercase().contains(searchLower) ||
                                    product.category.lowercase().contains(searchLower)
                        }
                    }

                products = fetchedProducts
                _uiState.value = MarketUiState.ProductsList

            } catch (e: Exception) {
                Log.e("MarketViewModel", "Error loading products", e)
                _uiState.value = MarketUiState.Error("Failed to load products: ${e.message}")
            }
        }
    }

    // Renamed method to avoid signature clash
    fun updateSelectedCategory(category: String?) {
        selectedCategory = category
        loadProducts()
    }

    // Renamed method to avoid signature clash
    fun updateSearchQuery(query: String) {
        searchQuery = query
        loadProducts()
        // In a production app, you would implement debounce for search
    }

    // Renamed method to avoid signature clash
    fun updateProductType(isProductType: Boolean) {
        isProduct = isProductType
        // Reset category when switching type
        newProductCategory = if (isProductType) productCategories.first() else serviceCategories.first()
    }

    fun updateNewProductName(name: String) {
        newProductName = name
    }

    fun updateNewProductPrice(price: String) {
        // Only allow digits
        if (price.isEmpty() || price.all { it.isDigit() }) {
            newProductPrice = price
        }
    }

    fun updateNewProductCategory(category: String) {
        newProductCategory = category
    }

    fun updateNewProductCondition(condition: String) {
        newProductCondition = condition
    }

    fun updateNewProductDescription(description: String) {
        newProductDescription = description
    }

    fun updateNewProductImage(uri: Uri?) {
        newProductImageUri = uri
    }

    fun uploadProduct(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (newProductName.isBlank() || newProductPrice.isBlank()) {
            onError("Please fill all required fields")
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("You must be logged in to sell items")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = MarketUiState.Loading

                // Upload image if provided
                var imageUrl: String? = null

                if (newProductImageUri != null) {
                    val imageRef = storage.reference
                        .child("market_images")
                        .child(UUID.randomUUID().toString())

                    val uploadTask = imageRef.putFile(newProductImageUri!!).await()
                    imageUrl = imageRef.downloadUrl.await().toString()
                }

                // Create product data
                val productData = hashMapOf(
                    "name" to newProductName,
                    "price" to (newProductPrice.toIntOrNull() ?: 0),
                    "category" to newProductCategory,
                    "description" to newProductDescription,
                    "condition" to newProductCondition,
                    "imageUrl" to imageUrl,
                    "sellerId" to userId,
                    "isProduct" to isProduct,
                    "timestamp" to System.currentTimeMillis()
                )

                // Add to Firestore
                firestore.collection("Products")
                    .add(productData)
                    .await()

                // Clear form
                resetNewProductForm()

                // Reload products
                loadProducts()

                onSuccess()

            } catch (e: Exception) {
                Log.e("MarketViewModel", "Error uploading product", e)
                onError("Failed to upload: ${e.message}")
                _uiState.value = MarketUiState.ProductsList
            }
        }
    }

    private fun resetNewProductForm() {
        newProductName = ""
        newProductPrice = ""
        newProductDescription = ""
        newProductImageUri = null
        newProductCategory = if (isProduct) productCategories.first() else serviceCategories.first()
        newProductCondition = conditions.first()
    }

    // Add this function to your MarketViewModel
    fun migrateExistingProducts() {
        viewModelScope.launch {
            try {
                _uiState.value = MarketUiState.Loading

                // Collection names to migrate from (based on your Firebase structure)
                val collections = listOf("Clothes", "Shoes", "Bags", "Gadgets")

                for (collection in collections) {
                    val collectionRef = firestore.collection(collection)
                    val documents = collectionRef.get().await()

                    for (doc in documents.documents) {
                        val productData = hashMapOf(
                            "name" to (doc.getString("productName") ?: ""),
                            "price" to (doc.getString("productPrice")?.toIntOrNull() ?: 0),
                            "category" to collection,
                            "description" to (doc.getString("description") ?: ""),
                            "condition" to (doc.getString("productCondition") ?: "Brand new"),
                            "imageUrl" to (doc.getString("productImage") ?: ""),
                            "sellerId" to (doc.getString("Student ID") ?: ""),
                            "isProduct" to true,
                            "timestamp" to System.currentTimeMillis()
                        )

                        // Add to new products collection
                        firestore.collection("Products").add(productData).await()
                    }
                }

                // Now migrate services
                val serviceCollections = listOf("Gadget Repairs", "Hair Dressing", "Barbering", "Catering")
                // Similar logic for services...

                // Reload products
                loadProducts()

            } catch (e: Exception) {
                Log.e("MarketViewModel", "Error migrating products", e)
                _uiState.value = MarketUiState.Error("Failed to migrate products: ${e.message}")
            }
        }
    }
}

// UI States
sealed class MarketUiState {
    object Loading : MarketUiState()
    object ProductsList : MarketUiState()
    object AddProduct : MarketUiState()
    data class Error(val message: String) : MarketUiState()
}

// Data model
data class Product(
    val id: String,
    val name: String,
    val price: Int,
    val category: String,
    val description: String,
    val condition: String,
    val imageUrl: String?,
    val sellerId: String,
    val isProduct: Boolean,
    val timestamp: Long
)