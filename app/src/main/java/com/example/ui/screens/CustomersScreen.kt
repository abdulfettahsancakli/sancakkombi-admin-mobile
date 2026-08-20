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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.res.painterResource
import com.example.R
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    customers: List<Customer>,
    appointments: List<Appointment>,
    onBackClick: () -> Unit,
    onAddCustomer: (Customer) -> Unit,
    onAddCustomers: ((List<Customer>) -> Unit)? = null,
    onUpdateCustomer: (Customer) -> Unit,
    onDeleteCustomer: ((String, (Result<Unit>) -> Unit) -> Unit)? = null,
    onFetchDeviceHistory: (suspend (String) -> Result<com.example.data.remote.DeviceHistoryDto>)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var activeTab by remember { mutableStateOf("bilgiler") } // "bilgiler", "cihaz_gecmisi", "randevular", "islemler"
    var showNewCustomerDialog by remember { mutableStateOf(false) }
    var showBulkImportDialog by remember { mutableStateOf(false) }
    var filterOnlyActiveAppointments by remember { mutableStateOf(false) }
    var isDeletingCustomer by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    var deviceHistoryState by remember { mutableStateOf<com.example.data.remote.DeviceHistoryDto?>(null) }
    var isDeviceHistoryLoading by remember { mutableStateOf(false) }
    var deviceHistoryError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedCustomer?.id) {
        val custId = selectedCustomer?.id
        if (custId != null && onFetchDeviceHistory != null) {
            isDeviceHistoryLoading = true
            deviceHistoryError = null
            val res = onFetchDeviceHistory(custId)
            if (res.isSuccess) {
                deviceHistoryState = res.getOrNull()
            } else {
                deviceHistoryState = null
                deviceHistoryError = res.exceptionOrNull()?.message ?: "Cihaz geçmişi yüklenemedi."
            }
            isDeviceHistoryLoading = false
        } else {
            deviceHistoryState = null
            isDeviceHistoryLoading = false
            deviceHistoryError = null
        }
    }

    // Position Y for details card smooth scroll
    var detailCardPositionY by remember { mutableStateOf(0) }

    val filteredCustomers = remember(customers, searchQuery, filterOnlyActiveAppointments) {
        customers.filter { cust ->
            !cust.isArchived &&
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
    var editDistrict by remember(selectedCustomer) { mutableStateOf(selectedCustomer?.district ?: "") }
    var editAddress by remember(selectedCustomer) { mutableStateOf(selectedCustomer?.address ?: "") }
    var editNotes by remember(selectedCustomer) { mutableStateOf(selectedCustomer?.notes ?: "") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Compact Mobile Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = onBackClick,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        shadowElevation = 1.dp,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Müşteri Yönetimi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.3).sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${customers.size} Müşteri",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Search Bar (Compact Pill Shape)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "4000+ müşteri, isim veya ilçe ara...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Temizle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
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

            Spacer(modifier = Modifier.height(6.dp))

            // Segmented Control (Compact Filters)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = { filterOnlyActiveAppointments = false },
                        shape = RoundedCornerShape(9.dp),
                        color = if (!filterOnlyActiveAppointments) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (!filterOnlyActiveAppointments) 1.dp else 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Tüm Müşteriler (${customers.size})",
                            fontSize = 11.5.sp,
                            fontWeight = if (!filterOnlyActiveAppointments) FontWeight.Bold else FontWeight.Medium,
                            color = if (!filterOnlyActiveAppointments) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 7.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Surface(
                        onClick = { filterOnlyActiveAppointments = true },
                        shape = RoundedCornerShape(9.dp),
                        color = if (filterOnlyActiveAppointments) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (filterOnlyActiveAppointments) 1.dp else 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Aktif Randevusu Olanlar",
                            fontSize = 11.5.sp,
                            fontWeight = if (filterOnlyActiveAppointments) FontWeight.Bold else FontWeight.Medium,
                            color = if (filterOnlyActiveAppointments) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 7.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Scrollable LazyColumn Area (Maximized Middle Height & Infinite High Performance for 4000+ items)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Item 0: Hero Action Cards Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "Yeni Müşteri Ekle" Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showNewCustomerDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF43A047), Color(0xFF1B5E20))
                                        )
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Yeni Müşteri Ekle",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Tekil Form Kaydı",
                                        fontSize = 10.5.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        // "Tüm Rehberi İçe Aktar" Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showBulkImportDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF0288D1), Color(0xFF01579B))
                                        )
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Contacts,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tüm Rehberi Aktar",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Tek Tıkla Tüm Rehber",
                                        fontSize = 10.5.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Item 1: Customer List Label
                item {
                    Text(
                        text = "MÜŞTERİ LİSTESİ (${filteredCustomers.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (filteredCustomers.isEmpty()) {
                    item {
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
                    }
                } else {
                    items(filteredCustomers, key = { it.id }) { cust ->
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

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissVal ->
                                if (dismissVal == SwipeToDismissBoxValue.EndToStart && !isSelected) {
                                    customerToDelete = cust
                                }
                                false
                            }
                        )

                        LaunchedEffect(isSelected) {
                            if (isSelected && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = onDeleteCustomer != null && !isSelected,
                            backgroundContent = {
                                if (!isSelected) {
                                    val color by animateColorAsState(
                                        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                            Color(0xFFDC2626)
                                        else
                                            Color(0xFFDC2626).copy(alpha = 0.85f),
                                        label = "swipeBgColor"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 6.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(color)
                                            .padding(horizontal = 24.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Sil",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Sil",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        ) {
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
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
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

                                    // Quick Action Bar: Sadeleştirilmiş 2 Buton (Ara & Yol Tarifi)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Call Button (Geniş & Rahat)
                                        Surface(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cust.phone}"))
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                            modifier = Modifier.weight(1f).height(40.dp)
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
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Müşteriyi Ara",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        // Yol Tarifi (Maps) Button (Geniş & Rahat)
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
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                            modifier = Modifier.weight(1f).height(40.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Map,
                                                    contentDescription = "Yol Tarifi",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Yol Tarifi",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    // Inline Expanded Content (Açılan Detay Paneli - Temiz & Yüksek Kontrastlı Arka Plan)
                                    AnimatedVisibility(visible = isSelected) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 14.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                // Modern Segmented Tab Bar
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(MaterialTheme.colorScheme.surface)
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
                                                        title = "Cihaz",
                                                        icon = Icons.Default.Build,
                                                        isSelected = activeTab == "cihaz_gecmisi",
                                                        onClick = { activeTab = "cihaz_gecmisi" },
                                                        modifier = Modifier.weight(1f)
                                                    )

                                                    TabOption(
                                                        title = "Randevular",
                                                        icon = Icons.Default.History,
                                                        isSelected = activeTab == "randevular",
                                                        onClick = { activeTab = "randevular" },
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
                                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
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
                                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
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
                                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
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
                                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
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
                                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
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
                                                        shape = RoundedCornerShape(16.dp),
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
                                                                    shape = RoundedCornerShape(16.dp)
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

                                                    if (onDeleteCustomer != null) {
                                                        Spacer(modifier = Modifier.height(10.dp))
                                                        OutlinedButton(
                                                            onClick = { customerToDelete = cust },
                                                            shape = RoundedCornerShape(16.dp),
                                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                                            border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.35f)),
                                                            modifier = Modifier.fillMaxWidth().height(42.dp)
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Müşteriyi Sil", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text("Müşteri Kaydını Sil", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFFDC2626))
                                                            }
                                                        }
                                                    }
                                                } else if (activeTab == "cihaz_gecmisi") {
                                                    if (isDeviceHistoryLoading) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(24.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                                        }
                                                    } else if (deviceHistoryError != null) {
                                                        Text(deviceHistoryError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                                                    } else {
                                                        val history = deviceHistoryState
                                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                            Card(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                shape = RoundedCornerShape(12.dp),
                                                                colors = CardDefaults.cardColors(
                                                                    containerColor = MaterialTheme.colorScheme.surface
                                                                ),
                                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                                            ) {
                                                                Column(modifier = Modifier.padding(12.dp)) {
                                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Build,
                                                                            contentDescription = null,
                                                                            tint = MaterialTheme.colorScheme.primary,
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(8.dp))
                                                                        Text(
                                                                            text = "Cihaz & Garanti Sicil Kartı",
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontSize = 13.sp,
                                                                            color = MaterialTheme.colorScheme.primary
                                                                        )
                                                                    }
                                                                    Spacer(modifier = Modifier.height(6.dp))
                                                                    val bName = history?.deviceBrand?.takeIf { it.isNotBlank() } ?: cust.notes.takeIf { it.isNotBlank() } ?: ""
                                                                    val mName = history?.deviceModel?.takeIf { it.isNotBlank() } ?: ""
                                                                    Text(
                                                                        text = if (bName.isNotBlank() || mName.isNotBlank()) "Cihaz: $bName $mName".trim() else "Cihaz Bilgisi: Belirtilmemiş",
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 13.sp,
                                                                        color = MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                    if (!history?.deviceNotes.isNullOrBlank() && history?.deviceNotes != bName) {
                                                                        Spacer(modifier = Modifier.height(4.dp))
                                                                        Text(
                                                                            text = "Notlar: ${history?.deviceNotes}",
                                                                            fontSize = 11.sp,
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                    }
                                                                }
                                                            }

                                                            val records = history?.records ?: emptyList()
                                                            if (records.isEmpty()) {
                                                                Surface(
                                                                    shape = RoundedCornerShape(12.dp),
                                                                    color = MaterialTheme.colorScheme.surface,
                                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                                                    modifier = Modifier.fillMaxWidth()
                                                                ) {
                                                                    Column(
                                                                        modifier = Modifier.padding(16.dp),
                                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Info,
                                                                            contentDescription = null,
                                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                            modifier = Modifier.size(26.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.height(6.dp))
                                                                        Text(
                                                                            text = "Bu müşteri için henüz tamamlanmış servis kaydı yok.",
                                                                            fontSize = 12.sp,
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                    }
                                                                }
                                                            } else {
                                                                records.forEach { record ->
                                                                    Card(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        shape = RoundedCornerShape(12.dp),
                                                                        colors = CardDefaults.cardColors(
                                                                            containerColor = MaterialTheme.colorScheme.surface
                                                                        ),
                                                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                                                    ) {
                                                                        Column(modifier = Modifier.padding(12.dp)) {
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Text(
                                                                                    text = "${record.date} — ${record.serviceTitle}",
                                                                                    fontWeight = FontWeight.Bold,
                                                                                    fontSize = 12.sp,
                                                                                    color = MaterialTheme.colorScheme.onSurface
                                                                                )
                                                                            }

                                                                            if (record.workDescription.isNotBlank()) {
                                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                                Text(
                                                                                    text = record.workDescription,
                                                                                    fontSize = 11.sp,
                                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                                )
                                                                            }

                                                                            if (record.parts.isNotEmpty()) {
                                                                                Spacer(modifier = Modifier.height(6.dp))
                                                                                Text(
                                                                                    text = "Kullanılan Parçalar:",
                                                                                    fontWeight = FontWeight.SemiBold,
                                                                                    fontSize = 11.sp,
                                                                                    color = MaterialTheme.colorScheme.onSurface
                                                                                )
                                                                                record.parts.forEach { p ->
                                                                                    Text(
                                                                                        text = "• ${p.name} x${p.quantity}",
                                                                                        fontSize = 11.sp,
                                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                                        modifier = Modifier.padding(start = 6.dp, top = 1.dp)
                                                                                    )
                                                                                }
                                                                            }

                                                                            if (record.warrantyMonths != null) {
                                                                                Spacer(modifier = Modifier.height(8.dp))
                                                                                Surface(
                                                                                    shape = RoundedCornerShape(6.dp),
                                                                                    color = if (record.isUnderWarranty) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                                                ) {
                                                                                    Row(
                                                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                                        verticalAlignment = Alignment.CenterVertically
                                                                                    ) {
                                                                                        Icon(
                                                                                            imageVector = if (record.isUnderWarranty) Icons.Default.Verified else Icons.Default.Shield,
                                                                                            contentDescription = null,
                                                                                            tint = if (record.isUnderWarranty) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                                            modifier = Modifier.size(14.dp)
                                                                                        )
                                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                                        Text(
                                                                                            text = if (record.isUnderWarranty) {
                                                                                                "Garantili — ${record.warrantyUntil ?: ""} tarihine kadar"
                                                                                            } else {
                                                                                                "Garanti sona erdi — ${record.warrantyUntil ?: ""}"
                                                                                            },
                                                                                            fontSize = 10.sp,
                                                                                            fontWeight = FontWeight.Bold,
                                                                                            color = if (record.isUnderWarranty) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
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
                                                            color = MaterialTheme.colorScheme.surface,
                                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
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
                                                                    containerColor = MaterialTheme.colorScheme.surface
                                                                ),
                                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
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
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
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
                if (onAddCustomers != null) {
                    onAddCustomers(newCustomers)
                } else {
                    newCustomers.forEach { cust -> onAddCustomer(cust) }
                }
                if (newCustomers.isNotEmpty()) {
                    selectedCustomer = newCustomers.first()
                }
                showBulkImportDialog = false
            }
        )
    }

    // Delete Customer Confirmation Dialog
    if (customerToDelete != null) {
        val cust = customerToDelete!!
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Müşteriyi Sil",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${cust.name} isimli müşteriyi silmek istediğinize emin misiniz?",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "📞 ${cust.phone}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (cust.district.isNotBlank()) {
                                Text(
                                    text = "📍 ${cust.district}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bu işlem müşteriyi sistemden kaldıracaktır.",
                        fontSize = 11.sp,
                        color = Color(0xFFDC2626),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idToDelete = cust.id
                        val nameDeleted = cust.name
                        if (selectedCustomer?.id == idToDelete) {
                            selectedCustomer = null
                        }
                        customerToDelete = null
                        isDeletingCustomer = true
                        deleteError = null
                        onDeleteCustomer?.invoke(idToDelete, { result ->
                            isDeletingCustomer = false
                            result.onSuccess {
                                customerToDelete = null
                                Toast.makeText(context, "$nameDeleted arşivlendi.", Toast.LENGTH_SHORT).show()
                            }.onFailure {
                                deleteError = it.message ?: "Müşteri arşivlenemedi."
                                Toast.makeText(context, deleteError, Toast.LENGTH_LONG).show()
                            }
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isDeletingCustomer) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Arşivle", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { customerToDelete = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Vazgeç", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
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
    var district by remember { mutableStateOf("") }
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
                    placeholder = { Text("Örn: Bayrampaşa, Esenler, Gaziosmanpaşa...") },
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
                    label = { Text("Müşteri / Cihaz Notu") },
                    placeholder = { Text("Örn: Demirdöküm kombi, 2. kat") },
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

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("İptal", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank()) {
                                Toast.makeText(context, "Lütfen isim ve telefon alanlarını doldurunuz.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val newCustomer = Customer(
                                id = UUID.randomUUID().toString(),
                                name = name.trim(),
                                phone = phone.trim(),
                                district = district.trim(),
                                address = address.trim(),
                                appointmentCount = 0,
                                activeAppointmentCount = 0,
                                notes = notes.trim()
                            )
                            onSave(newCustomer)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Kaydet", color = Color.White, fontWeight = FontWeight.Bold)
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
    var defaultDistrict by remember { mutableStateOf("") }

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
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = {
                                val allPhones = filteredContacts.filter { !it.isAlreadyAdded }.map { it.phone }.toSet()
                                selectedPhones = selectedPhones + allPhones
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text(
                                text = "⚡ Tüm Rehber",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 7.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            onClick = {
                                val first100 = filteredContacts.filter { !it.isAlreadyAdded }.take(100).map { it.phone }.toSet()
                                selectedPhones = first100
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "İlk 100",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 7.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            onClick = {
                                val first500 = filteredContacts.filter { !it.isAlreadyAdded }.take(500).map { it.phone }.toSet()
                                selectedPhones = first500
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "İlk 500",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(vertical = 7.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            onClick = {
                                val first1000 = filteredContacts.filter { !it.isAlreadyAdded }.take(1000).map { it.phone }.toSet()
                                selectedPhones = first1000
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "İlk 1000",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 7.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            onClick = { selectedPhones = emptySet() },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Text(
                                text = "Temizle",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 7.dp),
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
                            text = if (selectedPhones.isNotEmpty()) "Seçilen: ${selectedPhones.size}" else "Tüm Rehber Hazır",
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
                            .height(240.dp)
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("İptal", color = Color(0xFF757575))
                        }

                        val nonAddedContacts = remember(contacts) { contacts.filter { !it.isAlreadyAdded } }
                        val effectiveContactsToImport = if (selectedPhones.isNotEmpty()) {
                            contacts.filter { selectedPhones.contains(it.phone) }
                        } else {
                            nonAddedContacts
                        }

                        Button(
                            onClick = {
                                val newCustomers = effectiveContactsToImport.map { c ->
                                    Customer(
                                        id = UUID.randomUUID().toString(),
                                        name = c.name,
                                        phone = c.phone,
                                        district = defaultDistrict.trim(),
                                        address = "Rehberden Toplu İçe Aktarıldı",
                                        appointmentCount = 0,
                                        activeAppointmentCount = 0,
                                        notes = "Telefon rehberinden toplu aktarıldı"
                                    )
                                }
                                onImportCustomers(newCustomers)
                                Toast.makeText(context, "${newCustomers.size} müşteri başarıyla rehberden aktarıldı!", Toast.LENGTH_LONG).show()
                            },
                            enabled = effectiveContactsToImport.isNotEmpty(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                            modifier = Modifier.weight(2f).height(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = if (effectiveContactsToImport.isNotEmpty()) {
                                            Brush.linearGradient(colors = listOf(Color(0xFF0288D1), Color(0xFF01579B)))
                                        } else {
                                            Brush.linearGradient(colors = listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E)))
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Contacts,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (selectedPhones.isNotEmpty()) {
                                            "Seçili ${selectedPhones.size} Kişiyi İçe Aktar"
                                        } else {
                                            "Tüm Rehberi İçe Aktar (${effectiveContactsToImport.size})"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
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
}

