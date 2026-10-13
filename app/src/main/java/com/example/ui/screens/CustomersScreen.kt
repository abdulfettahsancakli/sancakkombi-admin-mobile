package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.Customer
import com.example.data.model.FinanceRecord
import com.example.data.model.MessageLog
import com.example.data.remote.DeviceHistoryDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

@Composable
fun CustomersScreen(
    customers: List<Customer>,
    appointments: List<Appointment> = emptyList(),
    financeRecords: List<FinanceRecord> = emptyList(),
    messageLogs: List<MessageLog> = emptyList(),
    onBackClick: () -> Unit,
    onAddCustomer: (Customer) -> Unit = {},
    onAddCustomers: (List<Customer>) -> Unit = {},
    onUpdateCustomer: (Customer) -> Unit = {},
    onDeleteCustomer: (String, (Result<Unit>) -> Unit) -> Unit = { _, _ -> },
    onFetchDeviceHistory: suspend (String) -> Result<DeviceHistoryDto> = { Result.failure(Exception("Not implemented")) },
    onAddAppointment: (Appointment, (Result<Unit>) -> Unit) -> Unit = { _, cb -> cb(Result.success(Unit)) },
    onUpdateFinanceRecordStatus: (String, String, (Result<Unit>) -> Unit) -> Unit = { _, _, cb -> cb(Result.success(Unit)) },
    onViewReceipt: (FinanceRecord) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tokens = rememberAppointmentTokens()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Local session state so UI reacts immediately even before remote refresh completes
    val localOverrides = remember { mutableStateMapOf<String, CustomerLocalOverride>() }
    val locallyCreatedCustomers = remember { mutableStateListOf<Customer>() }
    val locallyArchivedIds = remember { mutableStateListOf<String>() }
    val locallyCreatedAppointments = remember { mutableStateListOf<Appointment>() }
    val locallyPaidReceiptIds = remember { mutableStateListOf<String>() }
    val sessionMessagesByCustomer = remember { mutableStateMapOf<String, List<CustomerMessageItem>>() }
    val deviceHistoryCache = remember { mutableStateMapOf<String, DeviceHistoryDto>() }

    // Toast notification
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastTrigger by remember { mutableIntStateOf(0) }
    fun showToast(msg: String) {
        toastMessage = msg
        toastTrigger += 1
    }
    LaunchedEffect(toastTrigger) {
        if (toastMessage != null) {
            delay(2400)
            toastMessage = null
        }
    }

    // Merge customers
    val allMergedCustomers = remember(customers, locallyCreatedCustomers.toList(), locallyArchivedIds.toList()) {
        val remoteIds = customers.map { it.id }.toSet()
        val remoteNormPhones = customers.map { normalizeTrPhoneDigits(it.phone) }.filter { it.isNotBlank() }.toSet()
        val pendingLocal = locallyCreatedCustomers.filter { local ->
            local.id !in remoteIds && normalizeTrPhoneDigits(local.phone) !in remoteNormPhones
        }
        (pendingLocal + customers).filter { !it.isArchived && it.id !in locallyArchivedIds }
    }

    val allMergedAppointments = remember(appointments, locallyCreatedAppointments.toList()) {
        val remoteIds = appointments.map { it.id }.toSet()
        locallyCreatedAppointments.filter { it.id !in remoteIds } + appointments
    }

    // Resolve rich profiles
    val resolvedProfiles = remember(
        allMergedCustomers,
        localOverrides.toMap(),
        allMergedAppointments,
        financeRecords,
        messageLogs,
        sessionMessagesByCustomer.toMap(),
        locallyPaidReceiptIds.toList(),
        deviceHistoryCache.toMap()
    ) {
        allMergedCustomers.map { cust ->
            resolveCustomerProfile(
                customer = cust,
                override = localOverrides[cust.id],
                allAppointments = allMergedAppointments,
                allFinanceRecords = financeRecords,
                allMessageLogs = messageLogs,
                sessionMessages = sessionMessagesByCustomer[cust.id].orEmpty(),
                paidReceiptIds = locallyPaidReceiptIds.toSet(),
                deviceHistory = deviceHistoryCache[cust.id]
            )
        }
    }

    // Filters & Search
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // "all" | "active" | "loyal" | "boiler"
    var selectedDistrictFilter by remember { mutableStateOf("all") }

    // Navigation & Selection
    var phoneScreen by remember { mutableStateOf("list") } // "list" | "detail" | "new" | "import"
    var selectedCustomerId by remember { mutableStateOf<String?>(null) }
    var activeDetailTab by remember { mutableStateOf("profile") } // "profile" | "boiler" | "appts" | "finance" | "messages"
    var tabletModal by remember { mutableStateOf<String?>(null) } // null | "new" | "import"
    var tabletPortraitSheetOpen by remember { mutableStateOf(false) }

    // Action Modals
    var waDrawerTarget by remember { mutableStateOf<ResolvedCustomerProfile?>(null) }
    var archiveTarget by remember { mutableStateOf<ResolvedCustomerProfile?>(null) }
    var editBoilerTarget by remember { mutableStateOf<ResolvedCustomerProfile?>(null) }
    var quickApptTarget by remember { mutableStateOf<ResolvedCustomerProfile?>(null) }

    // Ensure selectedCustomerId always points to a valid customer on tablet landscape
    val activeSelectedProfile = remember(resolvedProfiles, selectedCustomerId) {
        resolvedProfiles.find { it.customer.id == selectedCustomerId } ?: resolvedProfiles.firstOrNull()
    }

    LaunchedEffect(activeSelectedProfile?.customer?.id) {
        val cid = activeSelectedProfile?.customer?.id
        if (cid != null && cid !in deviceHistoryCache) {
            onFetchDeviceHistory(cid).onSuccess { dto ->
                deviceHistoryCache[cid] = dto
            }
        }
    }

    BackHandler(enabled = phoneScreen != "list" || tabletPortraitSheetOpen) {
        if (tabletPortraitSheetOpen) {
            tabletPortraitSheetOpen = false
        } else if (phoneScreen != "list") {
            phoneScreen = "list"
        }
    }

    // Computed stats
    val totalCount = resolvedProfiles.size
    val activeCount = remember(resolvedProfiles) { resolvedProfiles.count { it.isActive } }
    val loyalCount = remember(resolvedProfiles) { resolvedProfiles.count { it.isLoyal } }
    val withBoilerCount = remember(resolvedProfiles) { resolvedProfiles.count { it.hasBoiler } }

    // District counts map
    val districtCounts = remember(resolvedProfiles) {
        val map = linkedMapOf<String, Int>()
        resolvedProfiles.forEach { p ->
            val d = p.district.ifBlank { "Belirtilmemiş" }
            map[d] = (map[d] ?: 0) + 1
        }
        map
    }

    // Filtered profiles
    val filteredProfiles = remember(resolvedProfiles, searchQuery, selectedFilter, selectedDistrictFilter) {
        val q = searchQuery.trim().lowercase(TrLocale)
        val qDigits = searchQuery.filter { it.isDigit() }
        resolvedProfiles.filter { p ->
            if (selectedDistrictFilter != "all" && p.district != selectedDistrictFilter) return@filter false
            if (selectedFilter == "active" && !p.isActive) return@filter false
            if (selectedFilter == "loyal" && !p.isLoyal) return@filter false
            if (selectedFilter == "boiler" && !p.hasBoiler) return@filter false
            if (q.isNotEmpty()) {
                val hay = "${p.customer.name} ${p.displayPhone} ${p.district} ${p.address} ${p.brand} ${p.model}"
                    .lowercase(TrLocale)
                val matchesText = hay.contains(q)
                val matchesDigits = qDigits.isNotEmpty() && p.normalizedPhone.contains(qDigits)
                if (!matchesText && !matchesDigits) return@filter false
            }
            true
        }
    }

    fun handleSendWaMessage(target: ResolvedCustomerProfile, templateTitle: String, text: String, openExternal: Boolean) {
        val cid = target.customer.id
        val nowStr = SimpleDateFormat("dd.MM.yyyy · HH:mm", TrLocale).format(Date())
        val msgId = "m_${System.currentTimeMillis()}"
        val newItem = CustomerMessageItem(
            id = msgId,
            date = nowStr,
            channel = "WhatsApp",
            template = templateTitle,
            status = "Gönderildi",
            preview = text
        )
        sessionMessagesByCustomer[cid] = listOf(newItem) + sessionMessagesByCustomer[cid].orEmpty()
        waDrawerTarget = null

        if (openExternal) {
            openCustomerWhatsApp(context, target.displayPhone, text)
            showToast("💬 WhatsApp açılıyor…")
        } else {
            activeDetailTab = "messages"
            showToast("💬 WhatsApp mesajı kuyruğa alındı")
        }

        // Simulate delivery -> read status progression
        scope.launch {
            delay(900)
            sessionMessagesByCustomer[cid] = sessionMessagesByCustomer[cid].orEmpty().map {
                if (it.id == msgId) it.copy(status = "İletildi") else it
            }
            delay(1300)
            sessionMessagesByCustomer[cid] = sessionMessagesByCustomer[cid].orEmpty().map {
                if (it.id == msgId) it.copy(status = "Okundu") else it
            }
        }
    }

    fun handleSaveCustomerEdits(
        profile: ResolvedCustomerProfile,
        newName: String,
        newPhone: String,
        newDistrict: String,
        newAddress: String,
        newGateNote: String,
        newServiceNote: String,
        newAutoWa: Boolean
    ) {
        val cid = profile.customer.id
        val prev = localOverrides[cid] ?: CustomerLocalOverride()
        localOverrides[cid] = prev.copy(
            name = newName.trim().ifBlank { profile.customer.name },
            phone = formatTrPhoneDisplay(newPhone.trim().ifBlank { profile.displayPhone }),
            district = newDistrict.ifBlank { "Bayrampaşa" },
            address = newAddress.trim(),
            gateNote = newGateNote.trim(),
            serviceNote = newServiceNote.trim(),
            autoWhatsApp = newAutoWa
        )
        val updatedModel = profile.customer.copy(
            name = newName.trim().ifBlank { profile.customer.name },
            phone = formatTrPhoneDisplay(newPhone.trim().ifBlank { profile.displayPhone }),
            district = newDistrict.ifBlank { "Bayrampaşa" },
            address = newAddress.trim(),
            notes = newServiceNote.trim(),
            gateNote = newGateNote.trim(),
            serviceNote = newServiceNote.trim(),
            autoWhatsApp = newAutoWa
        )
        onUpdateCustomer(updatedModel)
        showToast("✓ Müşteri bilgileri güncellendi")
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.bg)
    ) {
        val isTablet = maxWidth >= 600.dp
        val isTabletLandscape = maxWidth >= 840.dp

        if (!isTablet) {
            // ==================== PHONE VIEWPORT ====================
            when (phoneScreen) {
                "list" -> {
                    PhoneCustomersListView(
                        tokens = tokens,
                        totalCount = totalCount,
                        activeCount = activeCount,
                        loyalCount = loyalCount,
                        withBoilerCount = withBoilerCount,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        selectedFilter = selectedFilter,
                        onFilterChange = { selectedFilter = it },
                        selectedDistrict = selectedDistrictFilter,
                        districtCounts = districtCounts,
                        onDistrictChange = { selectedDistrictFilter = it },
                        onResetFilters = {
                            searchQuery = ""
                            selectedFilter = "all"
                            selectedDistrictFilter = "all"
                        },
                        filteredProfiles = filteredProfiles,
                        onOpenCustomer = { profile ->
                            selectedCustomerId = profile.customer.id
                            activeDetailTab = "profile"
                            phoneScreen = "detail"
                        },
                        onCallCustomer = { profile ->
                            dialCustomerPhone(context, profile.displayPhone)
                            showToast("📞 ${profile.customer.name} aranıyor…")
                        },
                        onWhatsAppCustomer = { profile ->
                            waDrawerTarget = profile
                        },
                        onOpenNewCustomer = { phoneScreen = "new" },
                        onOpenImport = { phoneScreen = "import" }
                    )
                }

                "detail" -> {
                    val target = activeSelectedProfile
                    if (target == null) {
                        phoneScreen = "list"
                    } else {
                        PhoneCustomerDetailScreen(
                            profile = target,
                            tokens = tokens,
                            activeTab = activeDetailTab,
                            onTabChange = { activeDetailTab = it },
                            onBack = { phoneScreen = "list" },
                            onRequestArchive = { archiveTarget = target },
                            onCopyPhone = {
                                copyPhoneToClipboard(context, target.displayPhone)
                                showToast("📋 Telefon kopyalandı: ${target.displayPhone}")
                            },
                            onCall = {
                                dialCustomerPhone(context, target.displayPhone)
                                showToast("📞 ${target.customer.name} aranıyor…")
                            },
                            onWhatsApp = { waDrawerTarget = target },
                            onMap = {
                                openGoogleMapsDirections(context, target.address, target.district)
                                showToast("🧭 ${target.district} yol tarifi açılıyor…")
                            },
                            onNewAppointment = { quickApptTarget = target },
                            onSaveProfile = { n, p, d, a, g, s, wa ->
                                handleSaveCustomerEdits(target, n, p, d, a, g, s, wa)
                            },
                            onToggleAutoWa = { enabled ->
                                val cid = target.customer.id
                                val prev = localOverrides[cid] ?: CustomerLocalOverride()
                                localOverrides[cid] = prev.copy(autoWhatsApp = enabled)
                                onUpdateCustomer(target.customer.copy(autoWhatsApp = enabled))
                                showToast(if (enabled) "💬 Otomatik WhatsApp açık" else "🔕 Otomatik WhatsApp kapatıldı")
                            },
                            onEditBoiler = { editBoilerTarget = target },
                            onCollectReceipt = { receipt ->
                                locallyPaidReceiptIds.add(receipt.id)
                                if (receipt.backingRecord != null) {
                                    onUpdateFinanceRecordStatus(receipt.id, "Ödendi") {}
                                }
                                showToast("💚 Tahsilat kaydedildi (${receipt.no})")
                            },
                            onViewReceiptItem = { receipt ->
                                if (receipt.backingRecord != null) {
                                    onViewReceipt(receipt.backingRecord)
                                } else {
                                    showToast("🧾 ${receipt.no} dijital servis fişi görüntüleniyor")
                                }
                            }
                        )
                    }
                }

                "new" -> {
                    NewCustomerFormContent(
                        isModal = false,
                        existingCustomers = allMergedCustomers,
                        tokens = tokens,
                        onClose = { phoneScreen = "list" },
                        onShowToast = ::showToast,
                        onOpenExistingCustomer = { existing ->
                            selectedCustomerId = existing.id
                            activeDetailTab = "profile"
                            phoneScreen = "detail"
                        },
                        onCustomerCreated = { created, override ->
                            locallyCreatedCustomers.add(0, created)
                            localOverrides[created.id] = override
                            onAddCustomer(created)
                            selectedCustomerId = created.id
                            activeDetailTab = "profile"
                            phoneScreen = "detail"
                            showToast("🎉 ${created.name} portföye eklendi")
                        }
                    )
                }

                "import" -> {
                    ContactImportFlowContent(
                        isModal = false,
                        isTablet = false,
                        existingCustomers = allMergedCustomers,
                        tokens = tokens,
                        onClose = { phoneScreen = "list" },
                        onShowToast = ::showToast,
                        onImportCustomers = { importedList ->
                            locallyCreatedCustomers.addAll(0, importedList)
                            onAddCustomers(importedList)
                            showToast("🎉 ${importedList.size} yeni müşteri portföye eklendi")
                        }
                    )
                }
            }
        } else {
            // ==================== TABLET VIEWPORT ====================
            TabletCustomersCenterView(
                isLandscape = isTabletLandscape,
                tokens = tokens,
                totalCount = totalCount,
                activeCount = activeCount,
                loyalCount = loyalCount,
                withBoilerCount = withBoilerCount,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
                selectedDistrict = selectedDistrictFilter,
                districtCounts = districtCounts,
                onDistrictChange = { selectedDistrictFilter = it },
                onResetFilters = {
                    searchQuery = ""
                    selectedFilter = "all"
                    selectedDistrictFilter = "all"
                },
                filteredProfiles = filteredProfiles,
                selectedProfile = activeSelectedProfile,
                activeDetailTab = activeDetailTab,
                onDetailTabChange = { activeDetailTab = it },
                portraitSheetOpen = tabletPortraitSheetOpen,
                onClosePortraitSheet = { tabletPortraitSheetOpen = false },
                onSelectCustomer = { profile ->
                    selectedCustomerId = profile.customer.id
                    if (!isTabletLandscape) {
                        tabletPortraitSheetOpen = true
                    }
                },
                onOpenNewCustomerModal = { tabletModal = "new" },
                onOpenImportModal = { tabletModal = "import" },
                onRequestArchive = { profile -> archiveTarget = profile },
                onCopyPhone = { profile ->
                    copyPhoneToClipboard(context, profile.displayPhone)
                    showToast("📋 Telefon kopyalandı: ${profile.displayPhone}")
                },
                onCallCustomer = { profile ->
                    dialCustomerPhone(context, profile.displayPhone)
                    showToast("📞 ${profile.customer.name} aranıyor…")
                },
                onWhatsAppCustomer = { profile ->
                    waDrawerTarget = profile
                },
                onMapCustomer = { profile ->
                    openGoogleMapsDirections(context, profile.address, profile.district)
                    showToast("🧭 ${profile.district} yol tarifi açılıyor…")
                },
                onNewAppointment = { profile -> quickApptTarget = profile },
                onSaveProfile = { profile, n, p, d, a, g, s, wa ->
                    handleSaveCustomerEdits(profile, n, p, d, a, g, s, wa)
                },
                onToggleAutoWa = { profile, enabled ->
                    val cid = profile.customer.id
                    val prev = localOverrides[cid] ?: CustomerLocalOverride()
                    localOverrides[cid] = prev.copy(autoWhatsApp = enabled)
                    onUpdateCustomer(profile.customer.copy(autoWhatsApp = enabled))
                    showToast(if (enabled) "💬 Otomatik WhatsApp açık" else "🔕 Otomatik WhatsApp kapatıldı")
                },
                onEditBoiler = { profile -> editBoilerTarget = profile },
                onCollectReceipt = { receipt ->
                    locallyPaidReceiptIds.add(receipt.id)
                    if (receipt.backingRecord != null) {
                        onUpdateFinanceRecordStatus(receipt.id, "Ödendi") {}
                    }
                    showToast("💚 Tahsilat kaydedildi (${receipt.no})")
                },
                onViewReceiptItem = { receipt ->
                    if (receipt.backingRecord != null) {
                        onViewReceipt(receipt.backingRecord)
                    } else {
                        showToast("🧾 ${receipt.no} dijital servis fişi görüntüleniyor")
                    }
                }
            )

            // Tablet Modals (New Customer / Contact Import)
            if (tabletModal == "new") {
                Dialog(
                    onDismissRequest = { tabletModal = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { tabletModal = null }
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.clickable(enabled = false) {}) {
                            NewCustomerFormContent(
                                isModal = true,
                                existingCustomers = allMergedCustomers,
                                tokens = tokens,
                                onClose = { tabletModal = null },
                                onShowToast = ::showToast,
                                onOpenExistingCustomer = { existing ->
                                    selectedCustomerId = existing.id
                                    tabletModal = null
                                    if (!isTabletLandscape) tabletPortraitSheetOpen = true
                                },
                                onCustomerCreated = { created, override ->
                                    locallyCreatedCustomers.add(0, created)
                                    localOverrides[created.id] = override
                                    onAddCustomer(created)
                                    selectedCustomerId = created.id
                                    activeDetailTab = "profile"
                                    tabletModal = null
                                    if (!isTabletLandscape) tabletPortraitSheetOpen = true
                                    showToast("🎉 ${created.name} portföye eklendi")
                                }
                            )
                        }
                    }
                }
            } else if (tabletModal == "import") {
                Dialog(
                    onDismissRequest = { tabletModal = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { tabletModal = null }
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.clickable(enabled = false) {}) {
                            ContactImportFlowContent(
                                isModal = true,
                                isTablet = true,
                                existingCustomers = allMergedCustomers,
                                tokens = tokens,
                                onClose = { tabletModal = null },
                                onShowToast = ::showToast,
                                onImportCustomers = { importedList ->
                                    locallyCreatedCustomers.addAll(0, importedList)
                                    onAddCustomers(importedList)
                                    showToast("🎉 ${importedList.size} yeni müşteri portföye eklendi")
                                }
                            )
                        }
                    }
                }
            }
        }

        // ==================== SHARED OVERLAY MODALS ====================
        waDrawerTarget?.let { target ->
            WaTemplateDrawerModal(
                customer = target,
                tokens = tokens,
                onDismiss = { waDrawerTarget = null },
                onSendInApp = { tplTitle, txt ->
                    handleSendWaMessage(target, tplTitle, txt, openExternal = false)
                },
                onOpenRealWhatsApp = { tplTitle, txt ->
                    handleSendWaMessage(target, tplTitle, txt, openExternal = true)
                }
            )
        }

        archiveTarget?.let { target ->
            ArchiveConfirmModal(
                customerName = target.customer.name,
                tokens = tokens,
                onDismiss = { archiveTarget = null },
                onConfirmArchive = {
                    val cid = target.customer.id
                    locallyArchivedIds.add(cid)
                    archiveTarget = null
                    tabletPortraitSheetOpen = false
                    if (phoneScreen == "detail") {
                        phoneScreen = "list"
                    }
                    val nextProfile = resolvedProfiles.firstOrNull { it.customer.id != cid }
                    selectedCustomerId = nextProfile?.customer?.id
                    onDeleteCustomer(cid) {}
                    showToast("🗄️ ${target.customer.name} arşivlendi")
                }
            )
        }

        editBoilerTarget?.let { target ->
            EditBoilerModal(
                initialBrand = target.brand,
                initialModel = target.model,
                initialNotes = target.boilerNote,
                tokens = tokens,
                onDismiss = { editBoilerTarget = null },
                onSave = { newBrand, newModel, newNotes ->
                    val cid = target.customer.id
                    val prev = localOverrides[cid] ?: CustomerLocalOverride()
                    localOverrides[cid] = prev.copy(
                        deviceBrand = newBrand,
                        deviceModel = newModel,
                        deviceNotes = newNotes
                    )
                    val updatedCust = target.customer.copy(
                        deviceBrand = newBrand,
                        deviceModel = newModel,
                        deviceNotes = newNotes,
                        notes = buildString {
                            if (newBrand.isNotBlank()) append("Kombi: $newBrand $newModel".trim())
                            if (newNotes.isNotBlank()) {
                                if (isNotEmpty()) append(" · ")
                                append(newNotes)
                            }
                        }.ifBlank { target.customer.notes }
                    )
                    onUpdateCustomer(updatedCust)
                    editBoilerTarget = null
                    showToast("🔥 Cihaz kimlik kartı güncellendi")
                }
            )
        }

        quickApptTarget?.let { target ->
            CustomerQuickAppointmentModal(
                customer = target,
                tokens = tokens,
                onDismiss = { quickApptTarget = null },
                onCreateAppointment = { appt ->
                    locallyCreatedAppointments.add(0, appt)
                    quickApptTarget = null
                    activeDetailTab = "appts"
                    onAddAppointment(appt) {}
                    showToast("📅 ${target.customer.name} için randevu oluşturuldu")
                }
            )
        }

        // Toast Host
        CustomerToastHost(
            toastMessage = toastMessage,
            tokens = tokens,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 78.dp, start = 16.dp, end = 16.dp)
        )
    }
}

// ============================================================================
// PHONE LIST VIEW
// ============================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhoneCustomersListView(
    tokens: AppointmentTokens,
    totalCount: Int,
    activeCount: Int,
    loyalCount: Int,
    withBoilerCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    selectedDistrict: String,
    districtCounts: Map<String, Int>,
    onDistrictChange: (String) -> Unit,
    onResetFilters: () -> Unit,
    filteredProfiles: List<ResolvedCustomerProfile>,
    onOpenCustomer: (ResolvedCustomerProfile) -> Unit,
    onCallCustomer: (ResolvedCustomerProfile) -> Unit,
    onWhatsAppCustomer: (ResolvedCustomerProfile) -> Unit,
    onOpenNewCustomer: () -> Unit,
    onOpenImport: () -> Unit
) {
    var visibleLimit by remember { mutableIntStateOf(30) }
    val listState = rememberLazyListState()

    val nearBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 3
        }
    }

    LaunchedEffect(nearBottom, filteredProfiles.size) {
        if (nearBottom && visibleLimit < filteredProfiles.size) {
            visibleLimit += 25
        }
    }

    val shownProfiles = remember(filteredProfiles, visibleLimit) {
        filteredProfiles.take(visibleLimit)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Header + 4 Stat Cards + Import Banner + Search/Filters
            item(key = "header_section") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Müşteriler",
                                color = tokens.text,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Portföyün büyüyor, harika gidiyorsun 💪",
                                color = tokens.text2,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(tokens.primaryC)
                                .border(1.dp, tokens.primary.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(tokens.green)
                            )
                            Text(
                                text = "$totalCount Müşteri",
                                color = tokens.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // 4 Stat Cards Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("PORTFÖY", "$totalCount", Triple("👥", tokens.text, tokens.primaryC)),
                            Triple("AKTİF", "$activeCount", Triple("🛠️", tokens.amber, tokens.amberC)),
                            Triple("SADIK", "$loyalCount", Triple("💜", tokens.green, tokens.greenC)),
                            Triple("KOMBİ", "$withBoilerCount", Triple("🔥", tokens.primary, tokens.primaryC))
                        ).forEach { (label, value, meta) ->
                            val (icon, valueColor, badgeBg) = meta
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(tokens.surface)
                                    .border(1.dp, tokens.border, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        color = tokens.text2,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.2.sp,
                                        maxLines = 1
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(badgeBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(icon, fontSize = 11.sp)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = value,
                                    color = valueColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.6).sp
                                )
                            }
                        }
                    }

                    // Tüm Rehberi Aktar Banner Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        tokens.primary.copy(alpha = 0.16f),
                                        tokens.green.copy(alpha = 0.10f)
                                    )
                                )
                            )
                            .border(1.dp, tokens.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .clickable(onClick = onOpenImport)
                            .padding(horizontal = 13.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.primaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📇", fontSize = 20.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tüm Rehberi Aktar",
                                color = tokens.text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Telefon rehberindeki kişileri tek dokunuşla portföye ekle",
                                color = tokens.text2,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(tokens.primary)
                                .padding(horizontal = 11.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Başla ›",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Search + District Dropdown Row
                    CustomerSearchAndDistrictRow(
                        searchQuery = searchQuery,
                        onSearchChange = onSearchChange,
                        selectedDistrict = selectedDistrict,
                        districtCounts = districtCounts,
                        onDistrictChange = onDistrictChange,
                        tokens = tokens
                    )

                    // Horizontal Filter Chips
                    CustomerFilterChipsBar(
                        totalCount = totalCount,
                        activeCount = activeCount,
                        loyalCount = loyalCount,
                        withBoilerCount = withBoilerCount,
                        selectedFilter = selectedFilter,
                        onFilterChange = onFilterChange,
                        tokens = tokens
                    )
                }
            }

            if (shownProfiles.isEmpty()) {
                item(key = "empty_state") {
                    CustomerEmptyFilterState(
                        tokens = tokens,
                        onResetFilters = onResetFilters,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(shownProfiles, key = { it.customer.id }) { profile ->
                    PhoneCustomerListCard(
                        profile = profile,
                        tokens = tokens,
                        onClick = { onOpenCustomer(profile) },
                        onCall = { onCallCustomer(profile) },
                        onWhatsApp = { onWhatsAppCustomer(profile) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item(key = "footer_count") {
                    Text(
                        text = "${shownProfiles.size} / ${filteredProfiles.size} müşteri gösteriliyor · kaydırdıkça yüklenir",
                        color = tokens.text2,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    )
                }
            }
        }

        // Floating Action Button: 🙋 Yeni Müşteri
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 14.dp)
                .height(50.dp)
                .shadow(10.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(tokens.primaryGradient)
                .clickable(onClick = onOpenNewCustomer)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🙋", fontSize = 17.sp)
            Text(
                text = "Yeni Müşteri",
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhoneCustomerListCard(
    profile: ResolvedCustomerProfile,
    tokens: AppointmentTokens,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(tokens.surface)
            .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        // 46dp Avatar (14dp radius)
        CustomerAvatarBox(
            name = profile.customer.name,
            idOrKey = profile.customer.id,
            size = 46.dp,
            cornerRadius = 14.dp,
            fontSize = 15.sp
        )

        // Info Column
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = profile.customer.name,
                    color = tokens.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (profile.isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tokens.amberC)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚡ Aktif",
                            color = tokens.amber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = profile.displayPhone,
                color = tokens.text2,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )

            Spacer(Modifier.height(5.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tokens.surface2)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "📍 ${profile.district}",
                        color = tokens.text2,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (profile.hasBoiler) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tokens.primaryC)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🔥 ${profile.brand}",
                            color = tokens.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (profile.appointmentCount > 0) {
                    Text(
                        text = "· ${profile.appointmentCount} servis",
                        color = tokens.green,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }

        // Right Quick Action Buttons: 📞 & 💬 (42dp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.primaryC)
                    .border(1.dp, tokens.primary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onCall),
                contentAlignment = Alignment.Center
            ) {
                Text("📞", fontSize = 16.sp)
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.greenC)
                    .border(1.dp, tokens.green.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onWhatsApp),
                contentAlignment = Alignment.Center
            ) {
                Text("💬", fontSize = 16.sp)
            }
        }
    }
}

// ============================================================================
// PHONE DETAIL SCREEN ("Müşteri Kartı")
// ============================================================================
@Composable
private fun PhoneCustomerDetailScreen(
    profile: ResolvedCustomerProfile,
    tokens: AppointmentTokens,
    activeTab: String,
    onTabChange: (String) -> Unit,
    onBack: () -> Unit,
    onRequestArchive: () -> Unit,
    onCopyPhone: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onMap: () -> Unit,
    onNewAppointment: () -> Unit,
    onSaveProfile: (
        name: String,
        phone: String,
        district: String,
        address: String,
        gateNote: String,
        serviceNote: String,
        autoWa: Boolean
    ) -> Unit,
    onToggleAutoWa: (Boolean) -> Unit,
    onEditBoiler: () -> Unit,
    onCollectReceipt: (CustomerReceiptItem) -> Unit,
    onViewReceiptItem: (CustomerReceiptItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.bg)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.surface)
                    .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("‹", color = tokens.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Müşteri Kartı",
                color = tokens.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.redC)
                    .border(1.dp, tokens.red.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onRequestArchive)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🗄️ Arşivle",
                    color = tokens.red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Shared Detail Content (Phone layout)
        CustomerDetailPaneContent(
            profile = profile,
            isTablet = false,
            showSheetCloseButton = false,
            onCloseSheet = {},
            tokens = tokens,
            activeTab = activeTab,
            onTabChange = onTabChange,
            onRequestArchive = onRequestArchive,
            onCopyPhone = onCopyPhone,
            onCall = onCall,
            onWhatsApp = onWhatsApp,
            onMap = onMap,
            onNewAppointment = onNewAppointment,
            onSaveProfile = onSaveProfile,
            onToggleAutoWa = onToggleAutoWa,
            onEditBoiler = onEditBoiler,
            onCollectReceipt = onCollectReceipt,
            onViewReceiptItem = onViewReceiptItem,
            modifier = Modifier.weight(1f)
        )
    }
}

// ============================================================================
// TABLET VIEW (LANDSCAPE SPLIT PANE + PORTRAIT 2-COL GRID & BOTTOM SHEET)
// ============================================================================
@Composable
private fun TabletCustomersCenterView(
    isLandscape: Boolean,
    tokens: AppointmentTokens,
    totalCount: Int,
    activeCount: Int,
    loyalCount: Int,
    withBoilerCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    selectedDistrict: String,
    districtCounts: Map<String, Int>,
    onDistrictChange: (String) -> Unit,
    onResetFilters: () -> Unit,
    filteredProfiles: List<ResolvedCustomerProfile>,
    selectedProfile: ResolvedCustomerProfile?,
    activeDetailTab: String,
    onDetailTabChange: (String) -> Unit,
    portraitSheetOpen: Boolean,
    onClosePortraitSheet: () -> Unit,
    onSelectCustomer: (ResolvedCustomerProfile) -> Unit,
    onOpenNewCustomerModal: () -> Unit,
    onOpenImportModal: () -> Unit,
    onRequestArchive: (ResolvedCustomerProfile) -> Unit,
    onCopyPhone: (ResolvedCustomerProfile) -> Unit,
    onCallCustomer: (ResolvedCustomerProfile) -> Unit,
    onWhatsAppCustomer: (ResolvedCustomerProfile) -> Unit,
    onMapCustomer: (ResolvedCustomerProfile) -> Unit,
    onNewAppointment: (ResolvedCustomerProfile) -> Unit,
    onSaveProfile: (
        profile: ResolvedCustomerProfile,
        name: String,
        phone: String,
        district: String,
        address: String,
        gateNote: String,
        serviceNote: String,
        autoWa: Boolean
    ) -> Unit,
    onToggleAutoWa: (ResolvedCustomerProfile, Boolean) -> Unit,
    onEditBoiler: (ResolvedCustomerProfile) -> Unit,
    onCollectReceipt: (CustomerReceiptItem) -> Unit,
    onViewReceiptItem: (CustomerReceiptItem) -> Unit
) {
    val loyalPct = if (totalCount > 0) (loyalCount * 100) / totalCount else 0
    val boilerPct = if (totalCount > 0) (withBoilerCount * 100) / totalCount else 0

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ana Panel / Müşteri CRM",
                        color = tokens.text2,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Müşteri Yönetim Merkezi",
                        color = tokens.text,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.6).sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(tokens.surface2)
                            .border(1.dp, tokens.border, RoundedCornerShape(14.dp))
                            .clickable(onClick = onOpenImportModal)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📇", fontSize = 16.sp)
                        Text(
                            text = "Tüm Rehberi Aktar",
                            color = tokens.text,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .height(46.dp)
                            .shadow(8.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(tokens.primaryGradient)
                            .clickable(onClick = onOpenNewCustomerModal)
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🙋", fontSize = 16.sp)
                        Text(
                            text = "Yeni Müşteri Ekle",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // 4 Wide Stat Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    listOf("MÜŞTERİ PORTFÖYÜ", "$totalCount", "Kayıtlı", "👥", tokens.text, tokens.primaryC),
                    listOf("AKTİF SERVİSTE", "$activeCount", "İşlem / Beklemede", "🛠️", tokens.amber, tokens.amberC),
                    listOf("SADIK (2+ SERVİS)", "$loyalCount", "%$loyalPct sadakat", "💜", tokens.green, tokens.greenC),
                    listOf("KOMBİ KAYITLI", "$withBoilerCount", "%$boilerPct cihaz kartı", "🔥", tokens.primary, tokens.primaryC)
                ).forEach { data ->
                    val label = data[0] as String
                    val value = data[1] as String
                    val sub = data[2] as String
                    val icon = data[3] as String
                    val valueColor = data[4] as Color
                    val badgeBg = data[5] as Color

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(tokens.surface)
                            .border(1.dp, tokens.border, RoundedCornerShape(16.dp))
                            .padding(horizontal = 15.dp, vertical = 13.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = tokens.text2,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.4.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(badgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(icon, fontSize = 15.sp)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = value,
                                color = valueColor,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.8).sp
                            )
                            Text(
                                text = sub,
                                color = tokens.text2,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }
                }
            }

            // Main Body: Landscape Split-Pane vs Portrait 2-Col List
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Left 380dp Customer List Panel
                    TabletCustomerListPanel(
                        columns = 1,
                        tokens = tokens,
                        totalCount = totalCount,
                        activeCount = activeCount,
                        loyalCount = loyalCount,
                        withBoilerCount = withBoilerCount,
                        searchQuery = searchQuery,
                        onSearchChange = onSearchChange,
                        selectedFilter = selectedFilter,
                        onFilterChange = onFilterChange,
                        selectedDistrict = selectedDistrict,
                        districtCounts = districtCounts,
                        onDistrictChange = onDistrictChange,
                        onResetFilters = onResetFilters,
                        filteredProfiles = filteredProfiles,
                        selectedCustomerId = selectedProfile?.customer?.id,
                        highlightSelection = true,
                        onSelectCustomer = onSelectCustomer,
                        onCallCustomer = onCallCustomer,
                        onWhatsAppCustomer = onWhatsAppCustomer,
                        modifier = Modifier
                            .width(380.dp)
                            .fillMaxHeight()
                    )

                    // Right Detail Panel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        if (selectedProfile == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(tokens.surface)
                                    .border(1.dp, tokens.border, RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Müşteri seçin", color = tokens.text2, fontSize = 14.sp)
                            }
                        } else {
                            CustomerDetailPaneContent(
                                profile = selectedProfile,
                                isTablet = true,
                                showSheetCloseButton = false,
                                onCloseSheet = {},
                                tokens = tokens,
                                activeTab = activeDetailTab,
                                onTabChange = onDetailTabChange,
                                onRequestArchive = { onRequestArchive(selectedProfile) },
                                onCopyPhone = { onCopyPhone(selectedProfile) },
                                onCall = { onCallCustomer(selectedProfile) },
                                onWhatsApp = { onWhatsAppCustomer(selectedProfile) },
                                onMap = { onMapCustomer(selectedProfile) },
                                onNewAppointment = { onNewAppointment(selectedProfile) },
                                onSaveProfile = { n, p, d, a, g, s, wa ->
                                    onSaveProfile(selectedProfile, n, p, d, a, g, s, wa)
                                },
                                onToggleAutoWa = { enabled -> onToggleAutoWa(selectedProfile, enabled) },
                                onEditBoiler = { onEditBoiler(selectedProfile) },
                                onCollectReceipt = onCollectReceipt,
                                onViewReceiptItem = onViewReceiptItem,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else {
                // Tablet Portrait: Full-width 2-Column Grid
                TabletCustomerListPanel(
                    columns = 2,
                    tokens = tokens,
                    totalCount = totalCount,
                    activeCount = activeCount,
                    loyalCount = loyalCount,
                    withBoilerCount = withBoilerCount,
                    searchQuery = searchQuery,
                    onSearchChange = onSearchChange,
                    selectedFilter = selectedFilter,
                    onFilterChange = onFilterChange,
                    selectedDistrict = selectedDistrict,
                    districtCounts = districtCounts,
                    onDistrictChange = onDistrictChange,
                    onResetFilters = onResetFilters,
                    filteredProfiles = filteredProfiles,
                    selectedCustomerId = selectedProfile?.customer?.id,
                    highlightSelection = false,
                    onSelectCustomer = onSelectCustomer,
                    onCallCustomer = onCallCustomer,
                    onWhatsAppCustomer = onWhatsAppCustomer,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }

        // Tablet Portrait Bottom Sheet Overlay (86% height)
        if (!isLandscape && portraitSheetOpen && selectedProfile != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(onClick = onClosePortraitSheet),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.86f)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(tokens.bg)
                        .border(
                            width = 1.dp,
                            color = tokens.border,
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                        )
                        .clickable(enabled = false) {}
                ) {
                    // Drag Handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(tokens.border)
                        )
                    }

                    CustomerDetailPaneContent(
                        profile = selectedProfile,
                        isTablet = true,
                        showSheetCloseButton = true,
                        onCloseSheet = onClosePortraitSheet,
                        tokens = tokens,
                        activeTab = activeDetailTab,
                        onTabChange = onDetailTabChange,
                        onRequestArchive = { onRequestArchive(selectedProfile) },
                        onCopyPhone = { onCopyPhone(selectedProfile) },
                        onCall = { onCallCustomer(selectedProfile) },
                        onWhatsApp = { onWhatsAppCustomer(selectedProfile) },
                        onMap = { onMapCustomer(selectedProfile) },
                        onNewAppointment = { onNewAppointment(selectedProfile) },
                        onSaveProfile = { n, p, d, a, g, s, wa ->
                            onSaveProfile(selectedProfile, n, p, d, a, g, s, wa)
                        },
                        onToggleAutoWa = { enabled -> onToggleAutoWa(selectedProfile, enabled) },
                        onEditBoiler = { onEditBoiler(selectedProfile) },
                        onCollectReceipt = onCollectReceipt,
                        onViewReceiptItem = onViewReceiptItem,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletCustomerListPanel(
    columns: Int,
    tokens: AppointmentTokens,
    totalCount: Int,
    activeCount: Int,
    loyalCount: Int,
    withBoilerCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    selectedDistrict: String,
    districtCounts: Map<String, Int>,
    onDistrictChange: (String) -> Unit,
    onResetFilters: () -> Unit,
    filteredProfiles: List<ResolvedCustomerProfile>,
    selectedCustomerId: String?,
    highlightSelection: Boolean,
    onSelectCustomer: (ResolvedCustomerProfile) -> Unit,
    onCallCustomer: (ResolvedCustomerProfile) -> Unit,
    onWhatsAppCustomer: (ResolvedCustomerProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(tokens.surface)
            .border(1.dp, tokens.border, RoundedCornerShape(20.dp))
    ) {
        // Search & Filters Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomerSearchAndDistrictRow(
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                selectedDistrict = selectedDistrict,
                districtCounts = districtCounts,
                onDistrictChange = onDistrictChange,
                tokens = tokens,
                surfaceBackground = tokens.surface2
            )

            CustomerFilterChipsBar(
                totalCount = totalCount,
                activeCount = activeCount,
                loyalCount = loyalCount,
                withBoilerCount = withBoilerCount,
                selectedFilter = selectedFilter,
                onFilterChange = onFilterChange,
                tokens = tokens,
                unselectedBg = tokens.surface2
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(tokens.border)
        )

        // List / Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (filteredProfiles.isEmpty()) {
                item(span = { GridItemSpan(columns) }) {
                    CustomerEmptyFilterState(
                        tokens = tokens,
                        onResetFilters = onResetFilters,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(filteredProfiles, key = { it.customer.id }) { profile ->
                    val isSelected = highlightSelection && profile.customer.id == selectedCustomerId
                    val rowBg = when {
                        isSelected -> tokens.primaryC
                        columns == 2 -> tokens.surface2
                        else -> Color.Transparent
                    }
                    val rowBorder = when {
                        isSelected -> tokens.primary
                        columns == 2 -> tokens.border
                        else -> Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(rowBg)
                            .border(1.dp, rowBorder, RoundedCornerShape(14.dp))
                            .clickable { onSelectCustomer(profile) }
                            .padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CustomerAvatarBox(
                            name = profile.customer.name,
                            idOrKey = profile.customer.id,
                            size = 42.dp,
                            cornerRadius = 12.dp,
                            fontSize = 14.sp
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = profile.customer.name,
                                    color = tokens.text,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (profile.isActive) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(tokens.amberC)
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "⚡ Aktif",
                                            color = tokens.amber,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = profile.displayPhone,
                                color = tokens.text2,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "📍 ${profile.district}",
                                    color = tokens.text2,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                if (profile.hasBoiler) {
                                    Text(
                                        text = "· 🔥 ${profile.brand}",
                                        color = tokens.primary,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                                if (profile.appointmentCount > 0) {
                                    Text(
                                        text = "· ${profile.appointmentCount} srv",
                                        color = tokens.green,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(tokens.primaryC)
                                    .clickable { onCallCustomer(profile) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📞", fontSize = 14.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(tokens.greenC)
                                    .clickable { onWhatsAppCustomer(profile) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💬", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(tokens.border)
        )

        Text(
            text = "${filteredProfiles.size} / $totalCount müşteri gösteriliyor",
            color = tokens.text2,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

// ============================================================================
// SHARED CUSTOMER DETAIL PANE (HERO + 5 TABS)
// ============================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomerDetailPaneContent(
    profile: ResolvedCustomerProfile,
    isTablet: Boolean,
    showSheetCloseButton: Boolean,
    onCloseSheet: () -> Unit,
    tokens: AppointmentTokens,
    activeTab: String,
    onTabChange: (String) -> Unit,
    onRequestArchive: () -> Unit,
    onCopyPhone: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onMap: () -> Unit,
    onNewAppointment: () -> Unit,
    onSaveProfile: (
        name: String,
        phone: String,
        district: String,
        address: String,
        gateNote: String,
        serviceNote: String,
        autoWa: Boolean
    ) -> Unit,
    onToggleAutoWa: (Boolean) -> Unit,
    onEditBoiler: () -> Unit,
    onCollectReceipt: (CustomerReceiptItem) -> Unit,
    onViewReceiptItem: (CustomerReceiptItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Editable form states keyed to current customer
    var editName by remember(profile.customer.id, profile.customer.name) { mutableStateOf(profile.customer.name) }
    var editPhone by remember(profile.customer.id, profile.displayPhone) { mutableStateOf(profile.displayPhone) }
    var editDistrict by remember(profile.customer.id, profile.district) { mutableStateOf(profile.district) }
    var editAddress by remember(profile.customer.id, profile.address) { mutableStateOf(profile.address) }
    var editGateNote by remember(profile.customer.id, profile.gateNote) { mutableStateOf(profile.gateNote) }
    var editServiceNote by remember(profile.customer.id, profile.serviceNote) { mutableStateOf(profile.serviceNote) }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isTablet) 0.dp else 16.dp)
                .padding(bottom = if (!isTablet && activeTab == "profile") 88.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. HERO CARD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(tokens.surface)
                    .border(1.dp, tokens.border, RoundedCornerShape(20.dp))
                    .padding(15.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    CustomerAvatarBox(
                        name = profile.customer.name,
                        idOrKey = profile.customer.id,
                        size = 58.dp,
                        cornerRadius = 16.dp,
                        fontSize = 20.sp
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.customer.name,
                            color = tokens.text,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.4).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(5.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(tokens.surface2)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "📍 ${profile.district}",
                                    color = tokens.text2,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (profile.hasBoiler) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(tokens.primaryC)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "🔥 ${profile.brand}",
                                        color = tokens.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            if (profile.isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(tokens.amberC)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "⚡ Aktif Randevu",
                                        color = tokens.amber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    if (isTablet) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(tokens.redC)
                                    .border(1.dp, tokens.red.copy(alpha = 0.3f), RoundedCornerShape(11.dp))
                                    .clickable(onClick = onRequestArchive)
                                    .padding(horizontal = 11.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (showSheetCloseButton) "🗄️" else "🗄️ Arşivle",
                                    color = tokens.red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (showSheetCloseButton) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(tokens.surface2)
                                        .border(1.dp, tokens.border, RoundedCornerShape(11.dp))
                                        .clickable(onClick = onCloseSheet),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✕", color = tokens.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Phone Copy Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.surface2)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = profile.displayPhone,
                        color = tokens.text,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(tokens.surface)
                            .border(1.dp, tokens.border, RoundedCornerShape(8.dp))
                            .clickable(onClick = onCopyPhone)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "📋 Kopyala",
                            color = tokens.text2,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "· Kayıt ${profile.registeredAt}",
                        color = tokens.text2,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Action Buttons: 4-Col Grid on Phone, Single Row on Tablet
                if (!isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        // Ara
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(tokens.primaryC)
                                .border(1.dp, tokens.primary.copy(alpha = 0.3f), RoundedCornerShape(13.dp))
                                .clickable(onClick = onCall),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📞", fontSize = 18.sp)
                            Text("Ara", color = tokens.primary, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        // WhatsApp
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(tokens.greenC)
                                .border(1.dp, tokens.green.copy(alpha = 0.3f), RoundedCornerShape(13.dp))
                                .clickable(onClick = onWhatsApp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💬", fontSize = 18.sp)
                            Text("WhatsApp", color = tokens.green, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        // Harita
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(tokens.surface2)
                                .border(1.dp, tokens.border, RoundedCornerShape(13.dp))
                                .clickable(onClick = onMap),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🧭", fontSize = 18.sp)
                            Text("Harita", color = tokens.text, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        // Randevu
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(tokens.primaryGradient)
                                .clickable(onClick = onNewAppointment),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📅", fontSize = 18.sp)
                            Text("Randevu", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.greenC)
                                .border(1.dp, tokens.green.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable(onClick = onWhatsApp)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💬 WhatsApp Gönder", color = tokens.green, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Box(
                            modifier = Modifier
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.primaryC)
                                .border(1.dp, tokens.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable(onClick = onCall)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📞 Hemen Ara", color = tokens.primary, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Box(
                            modifier = Modifier
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.surface2)
                                .border(1.dp, tokens.border, RoundedCornerShape(12.dp))
                                .clickable(onClick = onMap)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🧭 Yol Tarifi", color = tokens.text, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        if (profile.receipts.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(tokens.amberC)
                                    .border(1.dp, tokens.amber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .clickable { onViewReceiptItem(profile.receipts.first()) }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🧾 Son Fiş", color = tokens.amber, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.primaryGradient)
                                .clickable(onClick = onNewAppointment)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📅 Yeni Randevu", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // 2. 5-TAB BAR
            val tabs = listOf(
                Triple("profile", "👤 Profil", null),
                Triple("boiler", "🔥 Kombi", if (profile.hasBoiler) "✓" else null),
                Triple("appts", "📅 Randevu", "${profile.appointments.size}"),
                Triple("finance", "💳 Finans", "${profile.receipts.size}"),
                Triple("messages", "💬 Gönderimler", "${profile.messages.size}")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(tokens.surface)
                    .border(1.dp, tokens.border, RoundedCornerShape(14.dp))
                    .then(if (!isTablet) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEach { (tabId, label, badge) ->
                    val isSelected = activeTab == tabId
                    Row(
                        modifier = Modifier
                            .then(if (isTablet) Modifier.weight(1f) else Modifier)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) tokens.primary else Color.Transparent)
                            .clickable { onTabChange(tabId) }
                            .padding(horizontal = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else tokens.text2,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                        if (badge != null) {
                            Spacer(Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (isSelected) Color.White.copy(alpha = 0.24f) else tokens.surface2)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = badge,
                                    color = if (isSelected) Color.White else tokens.text2,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // 3. TAB CONTENT
            when (activeTab) {
                "profile" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(tokens.surface)
                            .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        if (isTablet) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CustomerLabeledInput(
                                    label = "AD SOYAD",
                                    value = editName,
                                    onValueChange = { editName = it },
                                    tokens = tokens,
                                    modifier = Modifier.weight(1f)
                                )
                                CustomerLabeledInput(
                                    label = "TELEFON",
                                    value = editPhone,
                                    onValueChange = { editPhone = it },
                                    tokens = tokens,
                                    isMonospace = true,
                                    keyboardType = KeyboardType.Phone,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            CustomerLabeledInput(
                                label = "AD SOYAD",
                                value = editName,
                                onValueChange = { editName = it },
                                tokens = tokens
                            )
                            CustomerLabeledInput(
                                label = "TELEFON",
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                tokens = tokens,
                                isMonospace = true,
                                keyboardType = KeyboardType.Phone
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val districtOpts = remember(editDistrict) {
                                (listOf(editDistrict) + CustomerPriorityDistricts).distinct().filter { it.isNotBlank() }
                            }
                            CustomerDistrictSelectorField(
                                label = "İLÇE",
                                selectedDistrict = editDistrict,
                                options = districtOpts,
                                onSelect = { editDistrict = it },
                                tokens = tokens,
                                modifier = Modifier.weight(1f)
                            )
                            CustomerLabeledInput(
                                label = "BİNA / KAPI NOTU",
                                value = editGateNote,
                                onValueChange = { editGateNote = it },
                                placeholder = "Kat, daire, zil bilgisi...",
                                tokens = tokens,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        CustomerLabeledInput(
                            label = "AÇIK ADRES",
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            placeholder = "Mahalle, sokak, bina no...",
                            tokens = tokens,
                            singleLine = false,
                            minHeight = 64.dp
                        )

                        CustomerLabeledInput(
                            label = "SERVİS NOTU",
                            value = editServiceNote,
                            onValueChange = { editServiceNote = it },
                            placeholder = "Müşteriye özel hatırlatma veya geçmiş not...",
                            tokens = tokens,
                            singleLine = false,
                            minHeight = 64.dp
                        )
                    }

                    // Auto WhatsApp Toggle Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(tokens.surface)
                            .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "💬 Otomatik WhatsApp Bildirimleri",
                                color = tokens.text,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Randevu onayı ve bakım hatırlatmaları otomatik gönderilsin",
                                color = tokens.text2,
                                fontSize = 11.5.sp
                            )
                        }
                        CustomerSwitchToggle(
                            checked = profile.autoWhatsApp,
                            onCheckedChange = onToggleAutoWa,
                            tokens = tokens
                        )
                    }

                    if (isTablet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(46.dp)
                                    .shadow(8.dp, RoundedCornerShape(13.dp))
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(tokens.primaryGradient)
                                    .clickable {
                                        onSaveProfile(
                                            editName,
                                            editPhone,
                                            editDistrict,
                                            editAddress,
                                            editGateNote,
                                            editServiceNote,
                                            profile.autoWhatsApp
                                        )
                                    }
                                    .padding(horizontal = 22.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✓ Değişiklikleri Kaydet",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                "boiler" -> {
                    // Cihaz Kimlik Kartı
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(tokens.surface)
                            .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
                            .padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(tokens.primaryC),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔥", fontSize = 24.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CİHAZ KİMLİK KARTI",
                                    color = tokens.text2,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.3.sp
                                )
                                Text(
                                    text = profile.brand.ifBlank { "Kombi tanımlanmadı" },
                                    color = tokens.text,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = profile.model.ifBlank { "Model bilgisi girilmemiş" },
                                    color = tokens.text2,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tokens.surface2)
                                    .border(1.dp, tokens.border, RoundedCornerShape(10.dp))
                                    .clickable(onClick = onEditBoiler)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✏️ Düzenle",
                                    color = tokens.text,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (profile.boilerNote.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(tokens.surface2)
                                    .padding(horizontal = 11.dp, vertical = 9.dp)
                            ) {
                                Text(
                                    text = "📝 ${profile.boilerNote}",
                                    color = tokens.text2,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    // Servis & Parça Geçmişi
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(tokens.surface)
                            .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔧 SERVİS & PARÇA GEÇMİŞİ",
                                color = tokens.text2,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${profile.serviceHistory.size} kayıt",
                                color = tokens.text2,
                                fontSize = 11.sp
                            )
                        }

                        if (profile.serviceHistory.isEmpty()) {
                            Text(
                                text = "Henüz tamamlanmış servis kaydı yok",
                                color = tokens.text2,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp)
                            )
                        } else {
                            profile.serviceHistory.forEach { rec ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(tokens.surface2)
                                        .padding(11.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = rec.title,
                                            color = tokens.text,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = rec.date,
                                            color = tokens.primary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = rec.desc,
                                        color = tokens.text2,
                                        fontSize = 12.sp
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "🔩 ${rec.partsText}",
                                            color = tokens.text2,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = rec.priceText,
                                            color = tokens.green,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "appts" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Toplam ${profile.appointments.size} randevu",
                            color = tokens.text2,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(tokens.primaryGradient)
                                .clickable(onClick = onNewAppointment)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "＋ Yeni Randevu",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    if (profile.appointments.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(tokens.surface)
                                .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📅", fontSize = 30.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Bu müşteriye ait randevu kaydı yok",
                                color = tokens.text,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    } else {
                        profile.appointments.forEach { appt ->
                            val (statusLabel, statusBg, statusColor) = when (appt.status) {
                                AppointmentStatus.TAMAMLANDI -> Triple("Tamamlandı", tokens.greenC, tokens.green)
                                AppointmentStatus.ONAYLANDI -> Triple("Onaylandı", tokens.primaryC, tokens.primary)
                                AppointmentStatus.IPTAL -> Triple("İptal", tokens.redC, tokens.red)
                                AppointmentStatus.BEKLIYOR -> Triple("Beklemede", tokens.amberC, tokens.amber)
                            }
                            val priceNum = appt.jobReport?.collectedAmount?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(tokens.surface)
                                    .border(1.dp, tokens.border, RoundedCornerShape(15.dp))
                                    .padding(13.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = appt.serviceType.ifBlank { "Kombi Bakım & Servis" },
                                        color = tokens.text,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(statusBg)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = statusLabel,
                                            color = statusColor,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                Text(
                                    text = "🗓️ ${appt.date} · ⏰ ${appt.timeSlot}",
                                    color = tokens.text2,
                                    fontSize = 12.sp
                                )
                                val noteText = appt.jobReport?.workDoneNote?.ifBlank { appt.problemNote } ?: appt.problemNote
                                if (noteText.isNotBlank()) {
                                    Text(
                                        text = "💬 $noteText",
                                        color = tokens.text2,
                                        fontSize = 12.sp
                                    )
                                }
                                if (priceNum > 0) {
                                    Text(
                                        text = "₺${formatCustomerCurrency(priceNum)}",
                                        color = tokens.green,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                "finance" -> {
                    // 3 Summary Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        listOf(
                            Triple("TOPLAM TUTAR", "₺${formatCustomerCurrency(profile.billedAmount)}", tokens.text),
                            Triple("TAHSİL EDİLEN", "₺${formatCustomerCurrency(profile.paidAmount)}", tokens.green),
                            Triple(
                                "AÇIK BAKİYE",
                                "₺${formatCustomerCurrency(profile.pendingAmount)}",
                                if (profile.pendingAmount > 0) tokens.amber else tokens.text2
                            )
                        ).forEach { (lbl, valueStr, color) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(tokens.surface)
                                    .border(1.dp, tokens.border, RoundedCornerShape(14.dp))
                                    .padding(11.dp)
                            ) {
                                Text(
                                    text = lbl,
                                    color = tokens.text2,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = valueStr,
                                    color = color,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Receipts Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(tokens.surface)
                            .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🧾 SERVİS FİŞLERİ & TAHSİLATLAR",
                            color = tokens.text2,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        if (profile.receipts.isEmpty()) {
                            Text(
                                text = "Kayıtlı fiş veya tahsilat bulunmuyor",
                                color = tokens.text2,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp)
                            )
                        } else {
                            profile.receipts.forEach { receipt ->
                                val isPaid = receipt.status == "Ödendi"
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(tokens.surface2)
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = receipt.no,
                                                color = tokens.primary,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = " · ${receipt.date}",
                                                color = tokens.text2,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isPaid) tokens.greenC else tokens.amberC)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isPaid) "✓ Ödendi" else "⏳ Bekliyor",
                                                color = if (isPaid) tokens.green else tokens.amber,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    Text(
                                        text = receipt.items,
                                        color = tokens.text,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "₺${formatCustomerCurrency(receipt.amount)}",
                                            color = tokens.text,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .height(32.dp)
                                                    .clip(RoundedCornerShape(9.dp))
                                                    .background(tokens.surface)
                                                    .border(1.dp, tokens.border, RoundedCornerShape(9.dp))
                                                    .clickable { onViewReceiptItem(receipt) }
                                                    .padding(horizontal = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "📄 Fiş",
                                                    color = tokens.text,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (!isPaid) {
                                                Box(
                                                    modifier = Modifier
                                                        .height(32.dp)
                                                        .clip(RoundedCornerShape(9.dp))
                                                        .background(tokens.green)
                                                        .clickable { onCollectReceipt(receipt) }
                                                        .padding(horizontal = 11.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "💳 Tahsilat Al",
                                                        color = Color.White,
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.ExtraBold
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

                "messages" -> {
                    // Quick Send Template Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(tokens.greenC)
                            .border(1.dp, tokens.green.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .clickable(onClick = onWhatsApp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💬 Hızlı WhatsApp Şablonu Gönder",
                            color = tokens.green,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Message History Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(tokens.surface)
                            .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📨 GÖNDERİM GEÇMİŞİ",
                            color = tokens.text2,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        if (profile.messages.isEmpty()) {
                            Text(
                                text = "Henüz gönderim kaydı yok",
                                color = tokens.text2,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp)
                            )
                        } else {
                            profile.messages.forEach { msg ->
                                val (stBadge, stBg, stColor) = when (msg.status) {
                                    "Okundu" -> Triple("✓✓ Okundu", tokens.greenC, tokens.green)
                                    "İletildi" -> Triple("✓✓ İletildi", tokens.primaryC, tokens.primary)
                                    else -> Triple("✓ ${msg.status}", tokens.surface, tokens.text2)
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(tokens.surface2)
                                        .padding(11.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "💬 ${msg.template}",
                                            color = tokens.text,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(stBg)
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = stBadge,
                                                color = stColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                    Text(
                                        text = msg.preview,
                                        color = tokens.text2,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                    Text(
                                        text = msg.date,
                                        color = tokens.text2,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Phone Sticky Save Bar (only on Profil tab)
        if (!isTablet && activeTab == "profile") {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, tokens.bg.copy(alpha = 0.96f), tokens.bg)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(10.dp, RoundedCornerShape(15.dp))
                        .clip(RoundedCornerShape(15.dp))
                        .background(tokens.primaryGradient)
                        .clickable {
                            onSaveProfile(
                                editName,
                                editPhone,
                                editDistrict,
                                editAddress,
                                editGateNote,
                                editServiceNote,
                                profile.autoWhatsApp
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓ Değişiklikleri Kaydet",
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

// ============================================================================
// SHARED SEARCH, DISTRICT FILTER & FILTER CHIPS
// ============================================================================
@Composable
private fun CustomerSearchAndDistrictRow(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedDistrict: String,
    districtCounts: Map<String, Int>,
    onDistrictChange: (String) -> Unit,
    tokens: AppointmentTokens,
    surfaceBackground: Color = tokens.surface
) {
    var districtMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Input
        Row(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(surfaceBackground)
                .border(1.dp, tokens.border, RoundedCornerShape(13.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🔎", fontSize = 14.sp, color = tokens.text2)
            Box(modifier = Modifier.weight(1f)) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "İsim, telefon, mahalle, kombi ara...",
                        color = tokens.text2.copy(alpha = 0.65f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = tokens.text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(tokens.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (searchQuery.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(tokens.surface2)
                        .clickable { onSearchChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = tokens.text2, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // District Dropdown Selector
        Box {
            val labelText = if (selectedDistrict == "all") {
                "📍 Tüm İlçeler"
            } else {
                val cnt = districtCounts[selectedDistrict] ?: 0
                "📍 $selectedDistrict ($cnt)"
            }
            Row(
                modifier = Modifier
                    .height(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(surfaceBackground)
                    .border(1.dp, tokens.border, RoundedCornerShape(13.dp))
                    .clickable { districtMenuExpanded = true }
                    .padding(horizontal = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = labelText,
                    color = tokens.text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text("▾", color = tokens.text2, fontSize = 11.sp)
            }

            DropdownMenu(
                expanded = districtMenuExpanded,
                onDismissRequest = { districtMenuExpanded = false },
                modifier = Modifier
                    .background(tokens.surface)
                    .heightIn(max = 320.dp)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "📍 Tüm İlçeler",
                            color = if (selectedDistrict == "all") tokens.primary else tokens.text,
                            fontSize = 13.sp,
                            fontWeight = if (selectedDistrict == "all") FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                    },
                    onClick = {
                        onDistrictChange("all")
                        districtMenuExpanded = false
                    }
                )
                districtCounts.forEach { (dist, cnt) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "$dist ($cnt)",
                                color = if (selectedDistrict == dist) tokens.primary else tokens.text,
                                fontSize = 13.sp,
                                fontWeight = if (selectedDistrict == dist) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        },
                        onClick = {
                            onDistrictChange(dist)
                            districtMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerFilterChipsBar(
    totalCount: Int,
    activeCount: Int,
    loyalCount: Int,
    withBoilerCount: Int,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    tokens: AppointmentTokens,
    unselectedBg: Color = tokens.surface
) {
    val chips = listOf(
        Triple("all", "Tümü", totalCount),
        Triple("active", "Aktif Randevu", activeCount),
        Triple("loyal", "Sadık (2+)", loyalCount),
        Triple("boiler", "Kombi Bilgisi", withBoilerCount)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        chips.forEach { (id, label, count) ->
            val active = selectedFilter == id
            Row(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (active) tokens.primary else unselectedBg)
                    .border(
                        width = 1.dp,
                        color = if (active) tokens.primary else tokens.border,
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable { onFilterChange(id) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = label,
                    color = if (active) Color.White else tokens.text2,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (active) Color.White.copy(alpha = 0.22f) else tokens.surface2)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$count",
                        color = if (active) Color.White else tokens.text2,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerEmptyFilterState(
    tokens: AppointmentTokens,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(tokens.surface)
            .border(1.dp, tokens.border, RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔍", fontSize = 34.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Eşleşen müşteri bulunamadı",
            color = tokens.text,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Arama terimini veya filtreyi değiştirin",
            color = tokens.text2,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tokens.primaryC)
                .border(1.dp, tokens.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .clickable(onClick = onResetFilters)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Filtreleri Sıfırla",
                color = tokens.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
