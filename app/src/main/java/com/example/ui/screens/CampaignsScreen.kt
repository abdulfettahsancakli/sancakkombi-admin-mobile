package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Customer
import com.example.data.remote.CampaignDashboardDto
import com.example.data.remote.CampaignDto

@Composable
fun CampaignsScreen(
    dashboard: CampaignDashboardDto,
    customers: List<Customer>,
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onCreateCampaign: (String, List<String>, (Result<Unit>) -> Unit) -> Unit,
    onSendCampaign: (String, (Result<Unit>) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCreate by remember { mutableStateOf(false) }
    var campaignToSend by remember { mutableStateOf<CampaignDto?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
                Column(Modifier.weight(1f)) {
                    Text("Kampanyalar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("WhatsApp Cloud API gönderimleri", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, contentDescription = "Yenile") }
                Button(onClick = { showCreate = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(" Yeni")
                }
            }
        }
        if (isLoading) item { CircularProgressIndicator(Modifier.padding(20.dp)) }
        if (error != null) item {
            Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
        }
        if (!isLoading && error == null && dashboard.campaigns.isEmpty()) item {
            Text(
                "Henüz kampanya yok. Müşteri seçerek yeni kampanya oluşturabilirsiniz.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 28.dp)
            )
        }
        items(dashboard.campaigns, key = { it.id }) { campaign ->
            CampaignCard(campaign = campaign, onSend = { campaignToSend = campaign })
        }
    }

    if (showCreate) {
        CreateCampaignDialog(
            customers = customers,
            onDismiss = { showCreate = false },
            onCreate = { name, ids ->
                onCreateCampaign(name, ids) { result ->
                    Toast.makeText(
                        context,
                        result.fold({ "Kampanya oluşturuldu" }, { it.message ?: "Kampanya oluşturulamadı" }),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (result.isSuccess) showCreate = false
                }
            }
        )
    }

    campaignToSend?.let { campaign ->
        AlertDialog(
            onDismissRequest = { campaignToSend = null },
            title = { Text("Kampanyayı gönder?") },
            text = { Text(campaign.name + " kampanyasının sıradaki gönderim partisi WhatsApp Cloud API üzerinden işlenecek.") },
            confirmButton = {
                Button(onClick = {
                    campaignToSend = null
                    onSendCampaign(campaign.id) { result ->
                        Toast.makeText(
                            context,
                            result.fold({ "Gönderim başlatıldı" }, { it.message ?: "Gönderim başlatılamadı" }),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) { Text("Gönder") }
            },
            dismissButton = { TextButton(onClick = { campaignToSend = null }) { Text("Vazgeç") } }
        )
    }
}

@Composable
private fun CampaignCard(campaign: CampaignDto, onSend: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(campaign.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(campaign.status, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(campaign.recipientCount.toString() + " alıcı", color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!campaign.messageBody.isNullOrBlank()) {
                Text(campaign.messageBody, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
            FilledTonalButton(onClick = onSend, modifier = Modifier.align(Alignment.End)) {
                Text("Gönderimi başlat")
            }
        }
    }
}

@Composable
private fun CreateCampaignDialog(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onCreate: (String, List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni kampanya") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Kampanya adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Alıcılar", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(customers, key = { it.id }) { customer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIds = if (customer.id in selectedIds) selectedIds - customer.id else selectedIds + customer.id
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = customer.id in selectedIds,
                                onCheckedChange = {
                                    selectedIds = if (it) selectedIds + customer.id else selectedIds - customer.id
                                }
                            )
                            Text(customer.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && selectedIds.isNotEmpty(),
                onClick = { onCreate(name.trim(), selectedIds.toList()) }
            ) { Text("Oluştur") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } }
    )
}
