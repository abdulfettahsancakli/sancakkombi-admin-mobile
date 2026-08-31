package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var stockQuantity by remember { mutableStateOf("") }
    var minimumQuantity by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf(0) }

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Geri")
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Stok ve Katalog", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Par\u00e7a, hizmet ve \u00fcr\u00fcn hareketlerini y\u00f6netin.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Stoktakiler", "Fiyat Katalo\u011fu", "Hareketler").forEachIndexed { index, title ->
                        FilterChip(selected = activeTab == index, onClick = { activeTab = index }, label = { Text(title) })
                    }
                }
            }
            if (activeTab == 0) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showStockForm = !showStockForm }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Stok \u00dcr\u00fcn\u00fc")
                        }
                        OutlinedButton(onClick = { showCatalogForm = !showCatalogForm }, modifier = Modifier.weight(1f)) { Text("Katalog Kalemi") }
                    }
                }
                if (showStockForm) item {
                    FormCard(title = "Yeni / G\u00fcncel Stok \u00dcr\u00fcn\u00fc") {
                        OutlinedTextField(stockName, { stockName = it }, label = { Text("\u00dcr\u00fcn ad\u0131") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(stockSku, { stockSku = it }, label = { Text("Stok kodu") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(stockQuantity, { stockQuantity = it }, label = { Text("Mevcut adet") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(minimumQuantity, { minimumQuantity = it }, label = { Text("Kritik seviye") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), singleLine = true)
                        }
                        Button(onClick = {
                            if (stockName.isBlank()) return@Button
                            val initialQuantity = parseLocalizedDouble(stockQuantity)?.coerceAtLeast(0.0) ?: 0.0
                            // Yeni kayitta ID'yi backend uretir; miktar merkezi hareket RPC'siyle yazilir.
                            val item = StockItem(name = stockName.trim(), sku = stockSku.trim(), quantity = 0.0, minimumQuantity = parseLocalizedDouble(minimumQuantity)?.coerceAtLeast(0.0) ?: 0.0)
                            onSaveStockItem(item) { result ->
                                result.onSuccess { savedItem ->
                                    if (initialQuantity > 0) {
                                        onCreateMovement(StockMovement(UUID.randomUUID().toString(), savedItem.id, initialQuantity, StockMovementType.IN, "\u0130lk stok giri\u015fi")) { movementResult ->
                                            movementResult.onSuccess {
                                                Toast.makeText(context, "Stok \u00fcr\u00fcn\u00fc ve ba\u015flang\u0131\u00e7 miktar\u0131 kaydedildi.", Toast.LENGTH_SHORT).show()
                                                showStockForm = false
                                            }.onFailure { Toast.makeText(context, it.message, Toast.LENGTH_LONG).show() }
                                        }
                                    } else {
                                        Toast.makeText(context, "Stok \u00fcr\u00fcn\u00fc kaydedildi.", Toast.LENGTH_SHORT).show()
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
                    FormCard(title = "Katalog Kalemi") {
                        OutlinedTextField(catalogName, { catalogName = it }, label = { Text("Hizmet / \u00fcr\u00fcn ad\u0131") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(catalogPrice, { catalogPrice = it }, label = { Text("Varsay\u0131lan fiyat") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true)
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
                items(stockItems, key = { it.id }) { stock ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Inventory2, null, tint = if (stock.isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(stock.name, fontWeight = FontWeight.Bold)
                                Text("${stock.sku.ifBlank { "Kod yok" }} \u2022 Kritik: ${stock.minimumQuantity}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${stock.quantity} ${stock.unit}", fontWeight = FontWeight.Bold, color = if (stock.isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(onClick = {
                                        onCreateMovement(StockMovement(UUID.randomUUID().toString(), stock.id, 1.0, StockMovementType.IN, "Manuel stok giri\u015fi")) { result ->
                                            result.onFailure { Toast.makeText(context, it.message ?: "Stok giri\u015fi ba\u015far\u0131s\u0131z.", Toast.LENGTH_LONG).show() }
                                        }
                                    }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)) { Text("+1", fontSize = 10.sp) }
                                    OutlinedButton(onClick = {
                                        onCreateMovement(StockMovement(UUID.randomUUID().toString(), stock.id, 1.0, StockMovementType.OUT, "Manuel stok \u00e7\u0131k\u0131\u015f\u0131")) { result ->
                                            result.onFailure { Toast.makeText(context, it.message ?: "Stok \u00e7\u0131k\u0131\u015f\u0131 ba\u015far\u0131s\u0131z.", Toast.LENGTH_LONG).show() }
                                        }
                                    }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)) { Text("-1", fontSize = 10.sp) }
                                }
                            }
                        }
                    }
                }
            } else if (activeTab == 1) {
                items(catalogItems, key = { it.id }) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text(item.name, fontWeight = FontWeight.Bold); Text(item.type.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            Text("\u20ba%.2f".format(item.defaultPrice).replace('.', ','), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                if (movements.isEmpty()) item { Text("Hen\u00fcz stok hareketi bulunmuyor.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
}

@Composable
private fun FormCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, fontWeight = FontWeight.Bold); content() }
    }
}
