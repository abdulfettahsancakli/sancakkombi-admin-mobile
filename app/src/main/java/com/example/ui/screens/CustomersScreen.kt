package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.Appointment
import com.example.data.model.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun CustomersScreen(
    customers: List<Customer>,
    appointments: List<Appointment>,
    onBackClick: () -> Unit,
    onAddCustomer: (Customer) -> Unit,
    onUpdateCustomer: (Customer) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf(customers.firstOrNull()) }
    var activeTab by remember { mutableStateOf("bilgiler") } // "bilgiler", "randevular", "islemler"
    var showNewCustomerDialog by remember { mutableStateOf(false) }
    var showBulkImportDialog by remember { mutableStateOf(false) }
    var filterOnlyActiveAppointments by remember { mutableStateOf(false) }

    // Position Y for details card smooth scroll
    var detailCardPositionY by remember { mutableStateOf(0) }

    val filteredCustomers = remember(customers, searchQuery, filterOnlyActiveAppointments) {
        customers.filter { cust ->
            val matchesSearch = searchQuery.isBlank() ||
                    cust.name.contains(searchQuery, ignoreCase = true) ||
                    cust.phone.contains(searchQuery) ||
                    cust.district.contains(searchQuery, ignoreCase = true)

            val matchesActiveFilter = !filterOnlyActiveAppointments || cust.activeAppointmentCount > 0

            matchesSearch && matchesActiveFilter
        }
    }

    // Editable form state for selected customer
    var editName by remember(selectedCustomer) { mutableStateOf(selectedCustomer?.name ?: "") }
    var editPhone by remember(selectedCustomer) { mutableStateOf(selectedCustomer?.phone ?: "") }
    var editDistrict by remember(selectedCustomer) { mutableStateOf(selectedCustomer?.district ?: "Bayrampaşa") }
    var editAddress by remember(selectedCustomer) { mutableStateOf(selectedCustomer?.address ?: "") }
    var editNotes by remember(selectedCustomer) { mutableStateOf(selectedCustomer?.notes ?: "") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 2026 Mobile UI Header / Navigasyon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${customers.size} Müşteri",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Screen Title - Large Title Style
            Text(
                text = "Müşteri Yönetimi",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar (24dp Radius Pill-Shape)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Ad, telefon veya ilçe ara...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Temizle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_customer_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Control (iOS 19 / MD3 Expressive Synthesis)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = { filterOnlyActiveAppointments = false },
                        shape = RoundedCornerShape(10.dp),
                        color = if (!filterOnlyActiveAppointments) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (!filterOnlyActiveAppointments) 2.dp else 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Tüm Müşteriler (${customers.size})",
                            fontSize = 12.sp,
                            fontWeight = if (!filterOnlyActiveAppointments) FontWeight.Bold else FontWeight.Medium,
                            color = if (!filterOnlyActiveAppointments) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Surface(
                        onClick = { filterOnlyActiveAppointments = true },
                        shape = RoundedCornerShape(10.dp),
                        color = if (filterOnlyActiveAppointments) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (filterOnlyActiveAppointments) 2.dp else 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Aktif Randevusu Olanlar",
                            fontSize = 12.sp,
                            fontWeight = if (filterOnlyActiveAppointments) FontWeight.Bold else FontWeight.Medium,
                            color = if (filterOnlyActiveAppointments) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Scrollable Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // Hero Action Cards Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // "Yeni Müşteri Ekle" Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showNewCustomerDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF43A047), Color(0xFF1B5E20))
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Yeni Müşteri Ekle",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tekil Form Kaydı",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    // "Toplu Rehber Aktar" Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showBulkImportDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF0288D1), Color(0xFF01579B))
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Contacts,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Toplu Rehber Aktar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tüm Rehber / 100+ Kişi",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // Customer List Label
                Text(
                    text = "MÜŞTERİ LİSTESİ (${filteredCustomers.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredCustomers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aramanıza uygun müşteri bulunamadı.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    filteredCustomers.forEach { cust ->
                        val isSelected = selectedCustomer?.id == cust.id
                        val avatarColor = remember(cust.id) { getAvatarColor(cust.name) }
                        val initials = remember(cust.name) { getInitials(cust.name) }

                        val rotationAngle by animateFloatAsState(
                            targetValue = if (isSelected) 180f else 0f,
                            label = "chevronRotation"
                        )

                        val cardBg by animateColorAsState(
                            targetValue = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else
                                MaterialTheme.colorScheme.surface,
                            label = "cardBg"
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    selectedCustomer = if (isSelected) null else cust
                                },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Top Header Row: Avatar, Name & Info, Detay Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar Circle with Initials
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        avatarColor.copy(alpha = 0.25f),
                                                        avatarColor.copy(alpha = 0.10f)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = avatarColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // Customer Info Column
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cust.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = cust.phone,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Modern Detay / Kapat Toggle Pill
                                    Surface(
                                        onClick = {
                                            selectedCustomer = if (isSelected) null else cust
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = if (isSelected) "Kapat" else "Detay",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                                            )
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (isSelected) "Kapat" else "Detay Göster",
                                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .rotate(rotationAngle)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick Action Bar (Ara, WhatsApp, Yol Tarifi)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Call Button
                                    Surface(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cust.phone}"))
                                            context.startActivity(intent)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = "Ara",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Ara",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    // WhatsApp Button
                                    Surface(
                                        onClick = {
                                            val formattedPhone = cust.phone.replace("[^0-9]".toRegex(), "")
                                            val waUri = Uri.parse("https://api.whatsapp.com/send?phone=90$formattedPhone")
                                            val waIntent = Intent(Intent.ACTION_VIEW, waUri)
                                            try {
                                                context.startActivity(waIntent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "WhatsApp açılamadı", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "WhatsApp",
                                                tint = Color(0xFF25D366),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "WhatsApp",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF059669)
                                            )
                                        }
                                    }

                                    // Yol Tarifi (Maps) Button
                                    Surface(
                                        onClick = {
                                            val query = if (cust.address.isNotBlank()) "${cust.district} ${cust.address}" else cust.district
                                            val mapUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                                            try {
                                                context.startActivity(mapIntent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Harita uygulaması açılamadı", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Map,
                                                contentDescription = "Yol Tarifi",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Yol Tarifi",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Inline Expanded Content
                                AnimatedVisibility(visible = isSelected) {
                                    Column(modifier = Modifier.padding(top = 16.dp)) {
                                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Modern Segmented Tab Bar
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                                .padding(4.dp)
                                        ) {
                                            TabOption(
                                                title = "Bilgiler",
                                                icon = Icons.Default.Info,
                                                isSelected = activeTab == "bilgiler",
                                                onClick = { activeTab = "bilgiler" },
                                                modifier = Modifier.weight(1f)
                                            )

                                            TabOption(
                                                title = "Randevular",
                                                icon = Icons.Default.History,
                                                isSelected = activeTab == "randevular",
                                                onClick = { activeTab = "randevular" },
                                                modifier = Modifier.weight(1f)
                                            )

                                            TabOption(
                                                title = "İşlemler",
                                                icon = Icons.Default.Phone,
                                                isSelected = activeTab == "islemler",
                                                onClick = { activeTab = "islemler" },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        if (activeTab == "bilgiler") {
                                            OutlinedTextField(
                                                value = editName,
                                                onValueChange = { editName = it },
                                                label = { Text("Müşteri Ad Soyad") },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            OutlinedTextField(
                                                value = editPhone,
                                                onValueChange = { editPhone = it },
                                                label = { Text("Telefon Numarası") },
                                                leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            OutlinedTextField(
                                                value = editDistrict,
                                                onValueChange = { editDistrict = it },
                                                label = { Text("İlçe / Bölge") },
                                                leadingIcon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            OutlinedTextField(
                                                value = editAddress,
                                                onValueChange = { editAddress = it },
                                                label = { Text("Açık Adres Detayı") },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                minLines = 2
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            OutlinedTextField(
                                                value = editNotes,
                                                onValueChange = { editNotes = it },
                                                label = { Text("Özel Notlar / Kombi Markası") },
                                                placeholder = { Text("Örn: Demirdöküm Nitromix kombi, 3. kat") },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                minLines = 2
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Button(
                                                onClick = {
                                                    val updated = cust.copy(
                                                        name = editName,
                                                        phone = editPhone,
                                                        district = editDistrict,
                                                        address = editAddress,
                                                        notes = editNotes
                                                    )
                                                    onUpdateCustomer(updated)
                                                    selectedCustomer = updated
                                                    Toast.makeText(context, "Müşteri bilgileri güncellendi", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(20.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                                                modifier = Modifier.fillMaxWidth().height(48.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            brush = Brush.linearGradient(
                                                                colors = listOf(
                                                                    Color(0xFF43A047),
                                                                    Color(0xFF1B5E20)
                                                                )
                                                            ),
                                                            shape = RoundedCornerShape(20.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Kaydı Güncelle", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                                    }
                                                }
                                            }
                                        } else if (activeTab == "randevular") {
                                            val customerAppointments = appointments.filter {
                                                it.customerId == cust.id || it.customerName.equals(cust.name, ignoreCase = true)
                                            }

                                            if (customerAppointments.isEmpty()) {
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(16.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Event,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(28.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = "Kayıtlı randevu bulunmuyor.",
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            } else {
                                                customerAppointments.forEach { appt ->
                                                    Card(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 3.dp),
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                                        )
                                                    ) {
                                                        Column(modifier = Modifier.padding(10.dp)) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = "${appt.date} • ${appt.timeSlot}",
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 12.sp,
                                                                    color = MaterialTheme.colorScheme.onSurface
                                                                )
                                                                Spacer(modifier = Modifier.weight(1f))
                                                                Surface(
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                                ) {
                                                                    Text(
                                                                        text = appt.status.label,
                                                                        fontSize = 10.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = MaterialTheme.colorScheme.primary,
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                    )
                                                                }
                                                            }
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = appt.serviceType,
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                QuickActionButton(
                                                    title = "Telefon ile Ara",
                                                    subtitle = cust.phone,
                                                    icon = Icons.Default.Call,
                                                    tint = Color(0xFF0288D1),
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cust.phone}"))
                                                        context.startActivity(intent)
                                                    }
                                                )

                                                QuickActionButton(
                                                    title = "WhatsApp Mesajı Gönder",
                                                    subtitle = "Direkt sohbet başlat",
                                                    icon = Icons.Default.Send,
                                                    tint = Color(0xFF25D366),
                                                    onClick = {
                                                        val formattedPhone = cust.phone.replace("[^0-9]".toRegex(), "")
                                                        val waUri = Uri.parse("https://api.whatsapp.com/send?phone=90$formattedPhone")
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, waUri))
                                                    }
                                                )

                                                QuickActionButton(
                                                    title = "Harita ve Yol Tarifi",
                                                    subtitle = "${cust.district} / ${cust.address}",
                                                    icon = Icons.Default.Map,
                                                    tint = Color(0xFFF59E0B),
                                                    onClick = {
                                                        val geoUri = Uri.parse("geo:0,0?q=${Uri.encode("${cust.district} ${cust.address}")}")
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, geoUri))
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // New Customer Dialog
    if (showNewCustomerDialog) {
        NewCustomerDialog(
            onDismiss = { showNewCustomerDialog = false },
            onSave = { newCust ->
                onAddCustomer(newCust)
                selectedCustomer = newCust
                showNewCustomerDialog = false
            }
        )
    }

    // Bulk Import Contacts Dialog
    if (showBulkImportDialog) {
        BulkImportContactsDialog(
            existingCustomers = customers,
            onDismiss = { showBulkImportDialog = false },
            onImportCustomers = { newCustomers ->
                newCustomers.forEach { cust -> onAddCustomer(cust) }
                if (newCustomers.isNotEmpty()) {
                    selectedCustomer = newCustomers.first()
                }
                showBulkImportDialog = false
            }
        )
    }
}

@Composable
private fun TabOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NewCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Bayrampaşa") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            try {
                val cursor = context.contentResolver.query(
                    contactUri,
                    null, null, null, null
                )
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                        val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val hasPhoneIndex = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                        val contactId = if (idIndex != -1) c.getString(idIndex) else ""
                        val contactName = if (nameIndex != -1) c.getString(nameIndex) ?: "" else ""
                        val hasPhone = if (hasPhoneIndex != -1) c.getInt(hasPhoneIndex) else 0

                        if (contactName.isNotBlank()) {
                            name = contactName
                        }

                        if (hasPhone > 0 && contactId.isNotEmpty()) {
                            val pCursor = context.contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                arrayOf(contactId),
                                null
                            )
                            pCursor?.use { pc ->
                                if (pc.moveToFirst()) {
                                    val pIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    if (pIndex != -1) {
                                        val number = pc.getString(pIndex)
                                        if (!number.isNullOrBlank()) {
                                            phone = number
                                        }
                                    }
                                }
                            }
                        }
                        Toast.makeText(context, "Rehberden Aktarıldı: $name ($phone)", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Kişi bilgisi aktarılamadı.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pickContactLauncher.launch(null)
        } else {
            Toast.makeText(context, "Rehber erişim izni verilmedi. Manuel doldurabilirsiniz.", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Yeni Müşteri Kaydı",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Import contacts quick button
                OutlinedButton(
                    onClick = {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.READ_CONTACTS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPerm) {
                            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        } else {
                            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Telefon Rehberinden İçe Aktar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Müşteri Ad Soyad *") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon Numarası *") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("İlçe / Bölge") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adres Detayı") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Kombi / Özel Notlar") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Vazgeç", color = Color(0xFF757575), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                val newCust = Customer(
                                    id = UUID.randomUUID().toString(),
                                    name = name,
                                    phone = phone,
                                    district = district,
                                    address = address,
                                    appointmentCount = 0,
                                    activeAppointmentCount = 0,
                                    notes = notes
                                )
                                onSave(newCust)
                            }
                        },
                        enabled = name.isNotBlank() && phone.isNotBlank(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                        modifier = Modifier.weight(1.2f).height(48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = if (name.isNotBlank() && phone.isNotBlank()) {
                                        Brush.linearGradient(colors = listOf(Color(0xFF43A047), Color(0xFF1B5E20)))
                                    } else {
                                        Brush.linearGradient(colors = listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E)))
                                    },
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Müşteri Kaydet", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun getInitials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "M"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts.last().take(1)).uppercase()
    }
}

private fun getAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFF0288D1),
        Color(0xFF10B981),
        Color(0xFF8B5CF6),
        Color(0xFFF59E0B),
        Color(0xFFEC4899),
        Color(0xFF059669),
        Color(0xFF3B82F6)
    )
    val hash = name.hashCode()
    val index = (hash % colors.size + colors.size) % colors.size
    return colors[index]
}

private data class PhoneContactItem(
    val name: String,
    val phone: String,
    val isAlreadyAdded: Boolean = false
)

@Composable
private fun BulkImportContactsDialog(
    existingCustomers: List<Customer>,
    onDismiss: () -> Unit,
    onImportCustomers: (List<Customer>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<PhoneContactItem>>(emptyList()) }
    var selectedPhones by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var hideExisting by remember { mutableStateOf(true) }
    var defaultDistrict by remember { mutableStateOf("Bayrampaşa") }

    val existingPhonesSet = remember(existingCustomers) {
        existingCustomers.map { it.phone.replace("[^0-9]".toRegex(), "") }.filter { it.isNotBlank() }.toSet()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Telefon Rehberi erişim izni verilmedi.", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        val hasPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPerm) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        withContext(Dispatchers.IO) {
            val list = mutableListOf<PhoneContactItem>()
            val seenNumbers = mutableSetOf<String>()
            try {
                val cr = context.contentResolver
                val cursor = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
                )
                cursor?.use { c ->
                    val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (c.moveToNext()) {
                        val rawName = if (nameIdx != -1) c.getString(nameIdx) ?: "" else ""
                        val rawNum = if (numIdx != -1) c.getString(numIdx) ?: "" else ""
                        val cleanNum = rawNum.replace("[^0-9]".toRegex(), "")

                        if (rawName.isNotBlank() && cleanNum.length >= 7 && !seenNumbers.contains(cleanNum)) {
                            seenNumbers.add(cleanNum)
                            val isAdded = existingPhonesSet.contains(cleanNum)
                            list.add(PhoneContactItem(name = rawName, phone = rawNum, isAlreadyAdded = isAdded))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                contacts = list
                isLoading = false
            }
        }
    }

    val filteredContacts = remember(contacts, searchQuery, hideExisting) {
        contacts.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.phone.contains(searchQuery)
            val matchesHide = !hideExisting || !item.isAlreadyAdded
            matchesSearch && matchesHide
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Toplu Rehber İçe Aktar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rehberinizdeki tüm veya seçili kişileri tek tıkla ekleyin",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Telefon rehberindeki kişiler taranıyor...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (contacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Rehberde aktarılacak kişi bulunamadı veya rehber izni yok.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Rehberde isim veya numara ara...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Options Bar: Default District & Hide Existing Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = defaultDistrict,
                            onValueChange = { defaultDistrict = it },
                            label = { Text("Varsayılan İlçe", fontSize = 11.sp) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(150.dp),
                            singleLine = true
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { hideExisting = !hideExisting }
                        ) {
                            Text(
                                text = "Ekli Olanları Gizle",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = hideExisting,
                                onCheckedChange = { hideExisting = it },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Select Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = {
                                val allPhones = filteredContacts.filter { !it.isAlreadyAdded }.map { it.phone }.toSet()
                                selectedPhones = selectedPhones + allPhones
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Tümünü Seç",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            onClick = {
                                val first50 = filteredContacts.filter { !it.isAlreadyAdded }.take(50).map { it.phone }.toSet()
                                selectedPhones = first50
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "İlk 50 Kişi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            onClick = {
                                val first100 = filteredContacts.filter { !it.isAlreadyAdded }.take(100).map { it.phone }.toSet()
                                selectedPhones = first100
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "İlk 100 Kişi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            onClick = { selectedPhones = emptySet() },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Temizle",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Counter Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BULUNAN KİŞİLER (${filteredContacts.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Seçilen: ${selectedPhones.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Contacts List Area
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredContacts, key = { it.phone }) { contact ->
                            val isSelected = selectedPhones.contains(contact.phone)
                            val isDisabled = contact.isAlreadyAdded

                            Surface(
                                onClick = {
                                    if (!isDisabled) {
                                        selectedPhones = if (isSelected) {
                                            selectedPhones - contact.phone
                                        } else {
                                            selectedPhones + contact.phone
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected || isDisabled,
                                        onCheckedChange = { checked ->
                                            if (!isDisabled) {
                                                selectedPhones = if (checked) selectedPhones + contact.phone else selectedPhones - contact.phone
                                            }
                                        },
                                        enabled = !isDisabled,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = contact.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = contact.phone,
                                            fontSize = 11.sp,
                                            color = if (isDisabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (isDisabled) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Kayıtlı",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF059669),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("İptal", color = Color(0xFF757575))
                        }

                        Button(
                            onClick = {
                                val selectedContacts = contacts.filter { selectedPhones.contains(it.phone) }
                                val newCustomers = selectedContacts.map { c ->
                                    Customer(
                                        id = UUID.randomUUID().toString(),
                                        name = c.name,
                                        phone = c.phone,
                                        district = defaultDistrict.ifBlank { "Bayrampaşa" },
                                        address = "Rehberden Toplu İçe Aktarıldı",
                                        appointmentCount = 0,
                                        activeAppointmentCount = 0,
                                        notes = "Telefon rehberinden toplu aktarıldı"
                                    )
                                }
                                onImportCustomers(newCustomers)
                                Toast.makeText(context, "${newCustomers.size} müşteri başarıyla rehberden aktarıldı!", Toast.LENGTH_LONG).show()
                            },
                            enabled = selectedPhones.isNotEmpty(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                            modifier = Modifier.weight(1.8f).height(46.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = if (selectedPhones.isNotEmpty()) {
                                            Brush.linearGradient(colors = listOf(Color(0xFF0288D1), Color(0xFF01579B)))
                                        } else {
                                            Brush.linearGradient(colors = listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E)))
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${selectedPhones.size} Kişiyi İçe Aktar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

