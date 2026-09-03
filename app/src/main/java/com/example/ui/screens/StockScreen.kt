package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CatalogItem
import com.example.data.model.CatalogItemType
import com.example.data.model.StockItem
import com.example.data.model.StockMovement
import com.example.data.model.StockMovementType
import com.example.utils.parseLocalizedDouble
import coil.compose.AsyncImage
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import org.json.JSONObject
import java.util.UUID

@Composable
fun StockScreen(
    catalogItems: List<CatalogItem>,
    stockItems: List<StockItem>,
    movements: List<StockMovement>,
    onBackClick: () -> Unit,
    onSaveCatalogItem: (CatalogItem, (Result<Unit>) -> Unit) -> Unit,
    onSaveStockItem: (StockItem, (Result<StockItem>) -> Unit) -> Unit,
    onCreateMovement: (StockMovement, (Result<Unit>) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCatalogForm by remember { mutableStateOf(false) }
    var showStockForm by remember { mutableStateOf(false) }
    var catalogName by remember { mutableStateOf("") }
    var catalogPrice by remember { mutableStateOf("") }
    var stockName by remember { mutableStateOf("") }
    var stockSku by remember { mutableStateOf("") }
    var stockBarcode by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("") }
    var minimumQuantity by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf("Tümü") }
    var statusFilter by remember { mutableStateOf("Tümü") }
    var activeTab by remember { mutableStateOf(0) }
    var selectedStock by remember { mutableStateOf<StockItem?>(null) }
    val resetStockForm = {
        stockName = ""
        stockSku = ""
        stockBarcode = ""
        stockQuantity = ""
        minimumQuantity = ""
    }

    val categories = remember(stockItems) {
        listOf("Tümü") + stockItems.map { it.category.trim() }.filter { it.isNotEmpty() }.distinct().sorted()
    }
    val filteredStock = remember(stockItems, searchQuery, categoryFilter, statusFilter) {
        val query = searchQuery.trim().lowercase()
        stockItems.filter { item ->
            val searchable = listOf(item.name, item.sku, item.barcode, item.category, item.brand, item.location)
                .joinToString(" ").lowercase()
            val status = stockStatus(item)
            (query.isEmpty() || searchable.contains(query)) &&
                (categoryFilter == "Tümü" || item.category == categoryFilter) &&
                (statusFilter == "Tümü" || status == statusFilter)
        }
    }
    val codeScanner = remember(context) {
        val options = GmsBarcodeScannerOptions.Builder().enableAutoZoom().build()
        GmsBarcodeScanning.getClient(context, options)
    }
    val startBarcodeScan: () -> Unit = {
        codeScanner.startScan()
            .addOnSuccessListener { barcode ->
                val code = decodeInventoryBarcode(barcode.rawValue.orEmpty())
                if (code.isBlank()) {
                    Toast.makeText(context, "Barkod değeri okunamadı.", Toast.LENGTH_SHORT).show()
                } else {
                    searchQuery = code
                    val match = stockItems.firstOrNull { it.barcode == code || it.sku == code }
                    if (match != null) {
                        selectedStock = match
                    } else {
                        if (!showStockForm) resetStockForm()
                        stockBarcode = code
                        stockSku = stockSku.ifBlank { code }
                        showStockForm = true
                        Toast.makeText(context, "Ürün bulunamadı; barkod yeni ürün formuna aktarıldı.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, error.message ?: "Barkod tarayıcı açılamadı.", Toast.LENGTH_LONG).show()
            }
        Unit
    }

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onBackClick, contentPadding = PaddingValues(horizontal = 10.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Geri")
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Stok ve Katalog", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Merkezi stok, barkod ve hareket yönetimi.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Stoktakiler", "Fiyat Kataloğu", "Hareketler").forEachIndexed { index, title ->
                        FilterChip(selected = activeTab == index, onClick = { activeTab = index }, label = { Text(title) })
                    }
                }
            }
            if (activeTab == 0) {
                item { StockSummary(stockItems) }
                item {
                    Button(onClick = startBarcodeScan, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Barkod / QR tara")
                    }
                }
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("İsim, SKU, barkod veya lokasyon ara") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            { TextButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, "Temizle", Modifier.size(18.dp)) } }
                        } else null
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("Kategori", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { category ->
                                FilterChip(selected = categoryFilter == category, onClick = { categoryFilter = category }, label = { Text(category) })
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("Tümü", "Yeterli", "Kritik", "Tükendi")) { status ->
                                FilterChip(selected = statusFilter == status, onClick = { statusFilter = status }, label = { Text(status) })
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showStockForm = !showStockForm }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Stok ürünü")
                        }
                        OutlinedButton(onClick = { showCatalogForm = !showCatalogForm }, modifier = Modifier.weight(1f)) { Text("Katalog kalemi") }
                    }
                }
                if (showStockForm) item {
                    FormCard(title = "Yeni stok ürünü") {
                        OutlinedTextField(stockName, { stockName = it }, label = { Text("Ürün adı") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(stockSku, { stockSku = it }, label = { Text("Stok kodu") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(stockBarcode, { stockBarcode = it }, label = { Text("Barkod / GTIN") }, modifier = Modifier.fillMaxWidth(), singleLine = true, trailingIcon = { TextButton(onClick = startBarcodeScan) { Icon(Icons.Default.QrCode2, "Barkod tara", Modifier.size(18.dp)) } })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(stockQuantity, { stockQuantity = it }, label = { Text("İlk miktar") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(minimumQuantity, { minimumQuantity = it }, label = { Text("Kritik seviye") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), singleLine = true)
                        }
                        Button(onClick = {
                            if (stockName.isBlank()) return@Button
                            val initialQuantity = parseLocalizedDouble(stockQuantity)?.coerceAtLeast(0.0) ?: 0.0
                            val item = StockItem(name = stockName.trim(), sku = stockSku.trim().ifBlank { stockBarcode.trim() }, quantity = 0.0, minimumQuantity = parseLocalizedDouble(minimumQuantity)?.coerceAtLeast(0.0) ?: 0.0, barcode = stockBarcode.trim(), catalogLinked = stockBarcode.isNotBlank())
                            onSaveStockItem(item) { result ->
                                result.onSuccess { savedItem ->
                                    if (initialQuantity > 0) {
                                        onCreateMovement(StockMovement(UUID.randomUUID().toString(), savedItem.id, initialQuantity, StockMovementType.IN, "İlk stok girişi")) { movementResult ->
                                            movementResult.onSuccess {
                                                Toast.makeText(context, "Stok ürünü ve başlangıç miktarı kaydedildi.", Toast.LENGTH_SHORT).show()
                                                resetStockForm()
                                                showStockForm = false
                                            }.onFailure {
                                                resetStockForm()
                                                showStockForm = false
                                                Toast.makeText(context, "Ürün kaydedildi; başlangıç stok hareketi eklenemedi: ${it.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Stok ürünü kaydedildi.", Toast.LENGTH_SHORT).show()
                                        resetStockForm()
                                        showStockForm = false
                                    }
                                }.onFailure { Toast.makeText(context, it.message, Toast.LENGTH_LONG).show() }
                            }
                        }) {
                            Icon(Icons.Default.Save, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Kaydet")
                        }
                    }
                }
                if (showCatalogForm) item {
                    FormCard(title = "Katalog kalemi") {
                        OutlinedTextField(catalogName, { catalogName = it }, label = { Text("Hizmet / ürün adı") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(catalogPrice, { catalogPrice = it }, label = { Text("Varsayılan fiyat") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Button(onClick = {
                            if (catalogName.isBlank()) return@Button
                            val item = CatalogItem(UUID.randomUUID().toString(), catalogName.trim(), CatalogItemType.PRODUCT, defaultPrice = parseLocalizedDouble(catalogPrice) ?: 0.0)
                            onSaveCatalogItem(item) { result -> result.onSuccess { Toast.makeText(context, "Katalog kalemi kaydedildi.", Toast.LENGTH_SHORT).show(); showCatalogForm = false }.onFailure { Toast.makeText(context, it.message, Toast.LENGTH_LONG).show() } }
                        }) {
                            Icon(Icons.Default.Save, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Kaydet")
                        }
                    }
                }
                if (filteredStock.isEmpty()) item { EmptyCard("Filtreye uyan stok ürünü bulunamadı.") }
                items(filteredStock, key = { it.id }) { stock ->
                    StockCard(
                        stock = stock,
                        onClick = { selectedStock = stock },
                        onMovement = { type ->
                            onCreateMovement(StockMovement(UUID.randomUUID().toString(), stock.id, 1.0, type, if (type == StockMovementType.IN) "Manuel stok girişi" else "Manuel stok çıkışı")) { result ->
                                result.onFailure { Toast.makeText(context, it.message ?: "Stok hareketi başarısız.", Toast.LENGTH_LONG).show() }
                            }
                        }
                    )
                }
            } else if (activeTab == 1) {
                if (catalogItems.isEmpty()) item { EmptyCard("Henüz katalog kalemi bulunmuyor.") }
                items(catalogItems, key = { it.id }) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text(item.name, fontWeight = FontWeight.Bold); Text(item.type.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            Text("₺%.2f".format(item.defaultPrice).replace('.', ','), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                if (movements.isEmpty()) item { EmptyCard("Henüz stok hareketi bulunmuyor.") }
                items(movements, key = { it.id }) { movement ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text(movement.reason.ifBlank { "Stok hareketi" }, fontWeight = FontWeight.Bold); Text(movement.type.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            Text("${movement.quantity} adet", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    selectedStock?.let { stock ->
        StockDetailDialog(
            stock = stock,
            onDismiss = { selectedStock = null },
            onMovement = { type ->
                onCreateMovement(StockMovement(UUID.randomUUID().toString(), stock.id, 1.0, type, if (type == StockMovementType.IN) "Mobil stok girişi" else "Mobil stok çıkışı")) { result ->
                    result.onFailure { Toast.makeText(context, it.message ?: "Stok hareketi başarısız.", Toast.LENGTH_LONG).show() }
                }
            }
        )
    }
}

private fun stockStatus(stock: StockItem): String = when {
    stock.quantity <= 0 -> "Tükendi"
    stock.quantity <= stock.minimumQuantity -> "Kritik"
    else -> "Yeterli"
}

@Composable
private fun StockSummary(stockItems: List<StockItem>) {
    val low = stockItems.count { stockStatus(it) == "Kritik" }
    val out = stockItems.count { stockStatus(it) == "Tükendi" }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryValue("Ürün", stockItems.size.toString())
            SummaryValue("Toplam", formatQuantity(stockItems.sumOf { it.quantity }))
            SummaryValue("Kritik", low.toString(), MaterialTheme.colorScheme.error)
            SummaryValue("Tükendi", out.toString(), MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StockCard(stock: StockItem, onClick: () -> Unit, onMovement: (StockMovementType) -> Unit) {
    val status = stockStatus(stock)
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (stock.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = stock.imageUrl,
                        contentDescription = stock.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(38.dp).clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Default.Inventory2, null, tint = if (status == "Yeterli") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(stock.name, fontWeight = FontWeight.Bold)
                    Text(listOf(stock.sku.ifBlank { "Kod yok" }, stock.category, stock.brand).filter { it.isNotBlank() }.joinToString(" · "), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (status == "Yeterli") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${formatQuantity(stock.quantity)} ${stock.unit}", fontWeight = FontWeight.Bold)
                    Text("Kritik: ${formatQuantity(stock.minimumQuantity)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (stock.location.isNotBlank() && stock.location != "Konumsuz") {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(3.dp))
                    Text(stock.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                }
                if (stock.barcode.isNotBlank()) {
                    Icon(Icons.Default.QrCode2, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                }
                OutlinedButton(onClick = { onMovement(StockMovementType.IN) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)) { Text("+1", fontSize = 11.sp) }
                Spacer(Modifier.width(4.dp))
                OutlinedButton(onClick = { onMovement(StockMovementType.OUT) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)) { Text("-1", fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun StockDetailDialog(stock: StockItem, onDismiss: () -> Unit, onMovement: (StockMovementType) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stock.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                DetailLine("Stok", "${formatQuantity(stock.quantity)} ${stock.unit}")
                DetailLine("Durum", stockStatus(stock))
                DetailLine("Kritik seviye", formatQuantity(stock.minimumQuantity))
                DetailLine("SKU", stock.sku.ifBlank { "Yok" })
                DetailLine("Barkod", stock.barcode.ifBlank { "Yok" })
                DetailLine("Kategori / marka", listOf(stock.category, stock.brand).filter { it.isNotBlank() }.joinToString(" / ").ifBlank { "Yok" })
                DetailLine("Lokasyon / raf", listOf(stock.location, stock.shelf).filter { it.isNotBlank() }.joinToString(" / ").ifBlank { "Yok" })
                Spacer(Modifier.height(4.dp))
                Text("Hızlı stok hareketi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onMovement(StockMovementType.IN) }, modifier = Modifier.weight(1f)) { Text("+1 giriş") }
                    OutlinedButton(onClick = { onMovement(StockMovementType.OUT) }, modifier = Modifier.weight(1f)) { Text("-1 çıkış") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Kapat") } }
    )
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text("$label: ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(value, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatQuantity(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else "%.2f".format(value).replace('.', ',')

private fun decodeInventoryBarcode(rawValue: String): String {
    val raw = rawValue.trim()
    if (raw.isBlank()) return ""
    return runCatching {
        val json = JSONObject(raw)
        json.optString("barcode").ifBlank { json.optString("sku") }.trim()
    }.getOrDefault(raw).take(160)
}

@Composable
private fun EmptyCard(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)), shape = RoundedCornerShape(14.dp)) {
        Text(message, modifier = Modifier.fillMaxWidth().padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FormCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, fontWeight = FontWeight.Bold); content() }
    }
}
