package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.TokenStore
import com.example.data.remote.NetworkModule
import com.example.data.repository.RemoteAdminRepositoryImpl
import com.example.ui.components.BottomNavBar
import com.example.ui.components.AdminNavigationRail
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.AppointmentsScreen
import com.example.ui.screens.CampaignsScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.CloudWhatsAppActions
import com.example.ui.screens.LocalCloudWhatsAppActions
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.GoogleAdsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MaintenanceScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.ModulePlaceholderScreen
import com.example.ui.screens.ModulesGridScreen
import com.example.ui.screens.NewProposalScreen
import com.example.ui.screens.ProposalDetailScreen
import com.example.ui.screens.ProposalsScreen
import com.example.ui.screens.QuickIbanScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ServiceReceiptScreen
import com.example.ui.screens.StockScreen
import com.example.ui.screens.WhatsAppStatusScreen
import com.example.ui.theme.SancakKombiTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.utils.AppointmentReminderReceiver
import com.example.utils.NotificationPreferenceStore
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val pendingAppointmentId = MutableStateFlow<String?>(null)

    private fun handleNotificationIntent(intent: Intent?) {
        pendingAppointmentId.value = intent?.getStringExtra(AppointmentReminderReceiver.EXTRA_APPOINTMENT_ID)
            ?.takeUnless { it.startsWith("daily_") }
    }

    private fun openServiceReceiptPdf(entryId: String) {
        if (entryId.isBlank()) {
            Toast.makeText(this, "PDF servis fişi bulunamadı.", Toast.LENGTH_LONG).show()
            return
        }
        val previewUrl = "https://www.sancakkombi.com/api/receipt/${Uri.encode(entryId)}"
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(previewUrl)))
        }.onFailure {
            Toast.makeText(this, "PDF önizlemesi açılamadı.", Toast.LENGTH_LONG).show()
        }
    }

    private val viewModel: MainViewModel by viewModels {
        viewModelFactory {
            initializer {
                val tokenStore = TokenStore(applicationContext)
                MainViewModel(
                    repository = RemoteAdminRepositoryImpl(
                        api = NetworkModule.createAdminApiService(tokenStore),
                        tokenStore = tokenStore,
                        context = applicationContext
                    )
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNotificationIntent(intent)
        com.example.utils.ReminderManager.createNotificationChannel(applicationContext)
        enableEdgeToEdge()
        setContent {
            var phoneNotificationsEnabled by remember {
                mutableStateOf(com.example.utils.ReminderManager.areNotificationsEnabled(applicationContext))
            }
            var notificationPreferences by remember {
                mutableStateOf(NotificationPreferenceStore.load(applicationContext))
            }
            val notificationAppointmentId by pendingAppointmentId.collectAsState()
            val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                phoneNotificationsEnabled = granted
                Toast.makeText(
                    applicationContext,
                    if (granted) "Telefon bildirimleri açıldı." else "Bildirim izni verilmedi.",
                    Toast.LENGTH_LONG
                ).show()
            }
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val currentRoute by viewModel.currentRoute.collectAsState()
            val selectedModule by viewModel.selectedModule.collectAsState()
            val stats by viewModel.stats.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val loginError by viewModel.loginError.collectAsState()

            val appointments by viewModel.appointments.collectAsState()
            val customers by viewModel.customers.collectAsState()

            val financeRecords by viewModel.financeRecords.collectAsState()
            val financeSummary by viewModel.financeSummary.collectAsState()
            val bankAccounts by viewModel.bankAccounts.collectAsState()
            val catalogItems by viewModel.catalogItems.collectAsState()
            val stockItems by viewModel.stockItems.collectAsState()
            val stockMovements by viewModel.stockMovements.collectAsState()
            val selectedRecordForReceipt by viewModel.selectedFinanceRecordForReceipt.collectAsState()
            var receiptReturnRoute by remember { mutableStateOf("finance") }

            val proposals by viewModel.proposals.collectAsState()
            val selectedProposal by viewModel.selectedProposal.collectAsState()

            val messagingStats by viewModel.messagingStats.collectAsState()
            val customerMessagingSettings by viewModel.customerMessagingSettings.collectAsState()
            val staffMessagingSettings by viewModel.staffMessagingSettings.collectAsState()
            val messageTemplates by viewModel.messageTemplates.collectAsState()
            val messageJobs by viewModel.messageJobs.collectAsState()
            val messageLogs by viewModel.messageLogs.collectAsState()

            val maintenanceStats by viewModel.maintenanceStats.collectAsState()
            val maintenanceRules by viewModel.maintenanceRules.collectAsState()

            val reportData by viewModel.reportData.collectAsState()
            val selectedReportRange by viewModel.selectedReportRange.collectAsState()

            val adsStats by viewModel.adsStats.collectAsState()
            val adsCampaigns by viewModel.adsCampaigns.collectAsState()
            val isAdsLoading by viewModel.isAdsLoading.collectAsState()
            val adsError by viewModel.adsError.collectAsState()
            val togglingCampaignId by viewModel.togglingCampaignId.collectAsState()
            val campaignDashboard by viewModel.campaignDashboard.collectAsState()
            val isCampaignsLoading by viewModel.isCampaignsLoading.collectAsState()
            val campaignsError by viewModel.campaignsError.collectAsState()
            val whatsAppStatus by viewModel.whatsAppStatus.collectAsState()

            LaunchedEffect(isLoggedIn, notificationAppointmentId) {
                if (isLoggedIn && !notificationAppointmentId.isNullOrBlank()) {
                    viewModel.navigateTo("appointments", com.example.data.model.AdminModule.RANDEVULAR)
                }
            }

            LaunchedEffect(appointments, phoneNotificationsEnabled, notificationPreferences) {
                if (phoneNotificationsEnabled) {
                    val today = com.example.ui.screens.todayIsoString()
                    val todayAppointments = appointments
                        .filter {
                            com.example.ui.screens.normalizeToIsoDate(it.date) == today &&
                                it.status != com.example.data.model.AppointmentStatus.IPTAL
                        }
                        .sortedBy { it.timeSlot }
                    if (notificationPreferences.dailySummary) {
                        com.example.utils.ReminderManager.scheduleDailySummaryReminders(
                            applicationContext,
                            todayAppointments.size,
                            todayAppointments.firstOrNull()?.let {
                                "${it.timeSlot} - ${it.customerName} (${it.district})"
                            }
                        )
                    } else {
                        com.example.utils.ReminderManager.cancelDailySummaryReminders(applicationContext)
                    }
                    appointments.forEach {
                        if (it.status == com.example.data.model.AppointmentStatus.IPTAL ||
                            it.status == com.example.data.model.AppointmentStatus.TAMAMLANDI ||
                            !notificationPreferences.appointmentReminders
                        ) {
                            com.example.utils.ReminderManager.cancelReminder(applicationContext, it.id)
                        } else {
                            com.example.utils.ReminderManager.scheduleAppointmentReminder(
                                applicationContext,
                                it,
                                notificationPreferences.reminderMinutes
                            )
                        }
                    }
                }
            }

            SancakKombiTheme(darkTheme = isDarkTheme) {
                if (!isLoggedIn) {
                    LoginScreen(
                        isLoading = isLoading,
                        errorMessage = loginError,
                        onLogin = { password, rememberMe -> viewModel.login(password, rememberMe) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useNavigationRail = maxWidth >= 840.dp
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopHeaderBar(
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = { viewModel.toggleTheme() },
                            onLogout = {
                                viewModel.logout()
                                Toast.makeText(applicationContext, "Yönetici oturumu yenilendi", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    bottomBar = {
                        if (!useNavigationRail) {
                            BottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { route, module -> viewModel.navigateTo(route, module) }
                            )
                        }
                    }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                        if (useNavigationRail) {
                            AdminNavigationRail(
                                currentRoute = currentRoute,
                                onNavigate = { route, module -> viewModel.navigateTo(route, module) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        CompositionLocalProvider(
                            LocalCloudWhatsAppActions provides CloudWhatsAppActions(
                                sendMessage = { phone, message, customerId, appointmentId, callback ->
                                    viewModel.sendWhatsAppMessage(phone, message, customerId, appointmentId, callback)
                                },
                                sendProposal = { proposalId, callback ->
                                    viewModel.sendProposalWhatsApp(proposalId, callback)
                                },
                                sendFinanceReminder = { recordId, message, callback ->
                                    viewModel.sendFinanceWhatsAppReminder(recordId, message, callback)
                                }
                            )
                        ) {
                        when (currentRoute) {
                            "dashboard" -> {
                                    DashboardScreen(
                                        stats = stats,
                                        appointments = appointments,
                                        phoneNotificationsEnabled = phoneNotificationsEnabled,
                                        notificationPreferences = notificationPreferences,
                                        onNotificationPreferencesChange = { updated ->
                                            notificationPreferences = updated
                                            NotificationPreferenceStore.save(applicationContext, updated)
                                        },
                                        onEnablePhoneNotifications = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                                ContextCompat.checkSelfPermission(
                                                    this@MainActivity,
                                                    Manifest.permission.POST_NOTIFICATIONS
                                                ) != PackageManager.PERMISSION_GRANTED
                                            ) {
                                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                phoneNotificationsEnabled = true
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Telefon bildirimleri açık.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        onNavigateToModule = { module ->
                                            viewModel.navigateTo(module.id, module)
                                        },
                                        onOpenAppointment = { appointmentId ->
                                            pendingAppointmentId.value = appointmentId
                                            viewModel.navigateTo("appointments", com.example.data.model.AdminModule.RANDEVULAR)
                                        },
                                        onOpenWhatsAppTemplates = {
                                            viewModel.navigateTo("message_templates", com.example.data.model.AdminModule.MESAJ_SISTEMI)
                                        }
                                    )
                                }
                                "appointments", "randevular" -> {
                                    AppointmentsScreen(
                                        appointments = appointments,
                                        customers = customers,
                                        financeRecords = financeRecords,
                                        stockItems = stockItems,
                                        bankAccounts = bankAccounts,
                                        initialAppointmentId = notificationAppointmentId,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onAddAppointment = { appt, callback -> viewModel.addAppointment(appt, callback) },
                                        onUpdateAppointment = { appt, callback -> viewModel.updateAppointment(appt, callback) },
                                        onUpdateStatus = { id, st -> viewModel.updateAppointmentStatus(id, st) },
                                        onCompleteJob = { id, report, callback -> viewModel.completeJob(id, report, callback) },
                                        onCreateServiceReceipt = { appointment, report, callback ->
                                            viewModel.createServiceReceipt(appointment, report, callback)
                                        },
                                        onViewReceipt = { record ->
                                            openServiceReceiptPdf(record.id)
                                        },
                                        onDeleteAppointment = { id -> viewModel.deleteAppointment(id) },
                                        onSendBankTransfer = { id, accKey, amt, date, callback ->
                                            viewModel.sendBankTransferMessage(id, accKey, amt, date, callback)
                                        },
                                        onGetAvailableSlots = { dateIso, callback ->
                                            viewModel.getAvailableSlots(dateIso, callback)
                                        },
                                        onInitialAppointmentHandled = { pendingAppointmentId.value = null }
                                    )
                                }
                                "customers", "musteriler" -> {
                                    CustomersScreen(
                                        customers = customers,
                                        appointments = appointments,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onAddCustomer = { cust -> viewModel.addCustomer(cust) },
                                        onAddCustomers = { list -> viewModel.addCustomers(list) },
                                        onUpdateCustomer = { cust -> viewModel.updateCustomer(cust) },
                                        onDeleteCustomer = { id, callback -> viewModel.deleteCustomer(id, callback) },
                                        onFetchDeviceHistory = { id -> viewModel.getDeviceHistory(id) },
                                        financeRecords = financeRecords,
                                        messageLogs = messageLogs,
                                        onAddAppointment = { appt, callback -> viewModel.addAppointment(appt, callback) },
                                        onUpdateFinanceRecordStatus = { id, status, onResult ->
                                            viewModel.updateFinanceRecordStatus(id, status, onResult)
                                        },
                                        onViewReceipt = { record ->
                                            receiptReturnRoute = "customers"
                                            viewModel.selectFinanceRecordForReceipt(record)
                                            viewModel.navigateTo("service_receipt")
                                        }
                                    )
                                }
                                "finance", "finans" -> {
                                    FinanceScreen(
                                        isDarkTheme = isDarkTheme,
                                        summary = financeSummary,
                                        financeRecords = financeRecords,
                                        bankAccounts = bankAccounts,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onAddFinanceRecord = { record -> viewModel.addFinanceRecord(record) },
                                        onDeleteFinanceRecord = { id -> viewModel.deleteFinanceRecord(id) },
                                        onUpdateFinanceRecordStatus = { id, status, onResult ->
                                            viewModel.updateFinanceRecordStatus(id, status, onResult)
                                        },
                                        onAddFinanceCollection = { id, amount, method, note, onResult ->
                                            viewModel.addFinanceCollection(id, amount, method, note, onResult)
                                        },
                                        onUpdateBankAccounts = { accs -> viewModel.updateBankAccounts(accs) },
                                        onViewReceipt = { record ->
                                            receiptReturnRoute = "finance"
                                            viewModel.selectFinanceRecordForReceipt(record)
                                            viewModel.navigateTo("service_receipt")
                                        }
                                    )
                                }
                                "service_receipt", "makbuz" -> {
                                    ServiceReceiptScreen(
                                        record = selectedRecordForReceipt,
                                        onBackClick = { viewModel.navigateTo(receiptReturnRoute) },
                                        onFetchReceiptDetail = { id -> viewModel.getReceiptDetail(id) }
                                    )
                                }
                                "quotes", "teklifler" -> {
                                    ProposalsScreen(
                                        proposals = proposals,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onNewProposalClick = { viewModel.navigateTo("new_proposal") },
                                        onViewProposalDetail = { prop ->
                                            viewModel.selectProposal(prop)
                                            viewModel.navigateTo("proposal_detail")
                                        },
                                        onDeleteProposal = { id, callback -> viewModel.deleteProposal(id, callback) },
                                        catalogItems = catalogItems,
                                        onUpdateProposalStatus = { id, st -> viewModel.updateProposalStatus(id, st) },
                                        onCreateProposal = { prop -> viewModel.addProposal(prop) },
                                        onSelectProposal = { prop -> viewModel.selectProposal(prop) }
                                    )
                                }
                                "new_proposal" -> {
                                    NewProposalScreen(
                                        catalogItems = catalogItems,
                                        onBackClick = { viewModel.navigateTo("quotes") },
                                        onCreateProposal = { prop ->
                                            viewModel.addProposal(prop)
                                            viewModel.navigateTo("proposal_detail")
                                        }
                                    )
                                }
                                "proposal_detail" -> {
                                    ProposalDetailScreen(
                                        proposal = selectedProposal,
                                        onBackClick = { viewModel.navigateTo("quotes") },
                                        onUpdateStatus = { st ->
                                            selectedProposal?.let { p ->
                                                viewModel.updateProposalStatus(p.id, st)
                                            }
                                        }
                                    )
                                }
                                "messages", "mesajlar" -> {
                                    MessagesScreen(
                                        stats = messagingStats,
                                        customerSettings = customerMessagingSettings,
                                        staffSettings = staffMessagingSettings,
                                        templates = messageTemplates,
                                        jobs = messageJobs,
                                        logs = messageLogs,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onUpdateCustomerSettings = { s -> viewModel.updateCustomerMessagingSettings(s) },
                                        onUpdateStaffSettings = { s -> viewModel.updateStaffMessagingSettings(s) },
                                        onUpdateTemplate = { t -> viewModel.updateMessageTemplate(t) },
                                        onRetryJob = { id -> viewModel.retryMessageJob(id) }
                                    )
                                }
                                "message_templates" -> {
                                    MessagesScreen(
                                        stats = messagingStats,
                                        customerSettings = customerMessagingSettings,
                                        staffSettings = staffMessagingSettings,
                                        templates = messageTemplates,
                                        jobs = messageJobs,
                                        logs = messageLogs,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onUpdateCustomerSettings = { s -> viewModel.updateCustomerMessagingSettings(s) },
                                        onUpdateStaffSettings = { s -> viewModel.updateStaffMessagingSettings(s) },
                                        onUpdateTemplate = { t -> viewModel.updateMessageTemplate(t) },
                                        onRetryJob = { id -> viewModel.retryMessageJob(id) },
                                        initialTab = 1
                                    )
                                }
                                "maintenance", "bakim" -> {
                                    MaintenanceScreen(
                                        stats = maintenanceStats,
                                        rules = maintenanceRules,
                                        customers = customers,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onAddRule = { r -> viewModel.addMaintenanceRule(r) },
                                        onUpdateRule = { r -> viewModel.updateMaintenanceRule(r) },
                                        onDeleteRule = { id -> viewModel.deleteMaintenanceRule(id) },
                                        onToggleRuleStatus = { id -> viewModel.toggleMaintenanceRuleStatus(id) },
                                        onSendReminder = { id, callback -> viewModel.sendMaintenanceReminder(id, callback) },
                                        onCreateAppointment = { viewModel.navigateTo("appointments") }
                                    )
                                }
                                "reports", "raporlar" -> {
                                    ReportsScreen(
                                        reportData = reportData,
                                        selectedRange = selectedReportRange,
                                        onSelectRange = { range -> viewModel.setReportTimeRange(range) },
                                        onBackClick = { viewModel.navigateTo("dashboard") }
                                    )
                                }
                                "ads", "google_ads", "googleads", "reklamlar" -> {
                                    GoogleAdsScreen(
                                        stats = adsStats,
                                        campaigns = adsCampaigns,
                                        isLoading = isAdsLoading,
                                        error = adsError,
                                        togglingCampaignId = togglingCampaignId,
                                        onToggleCampaignStatus = { id, onResult ->
                                            viewModel.toggleAdsCampaign(id, onResult)
                                        },
                                        onRefresh = {
                                            viewModel.fetchAdsData { success ->
                                                if (success) {
                                                    Toast.makeText(applicationContext, "Google Ads verileri güncellendi", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(applicationContext, "Google Ads verileri yenilenemedi", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        onBackClick = { viewModel.navigateTo("dashboard") }
                                    )
                                }
                                "quick_iban", "hizli_iban", "iban_gonder", "usta_iban" -> {
                                    QuickIbanScreen(
                                        appointments = appointments,
                                        customers = customers,
                                        bankAccounts = bankAccounts,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onAddFinanceRecord = { record -> viewModel.addFinanceRecord(record) },
                                        onSendBankTransfer = { appointmentId, accountKey, amount, date, onResult ->
                                            viewModel.sendBankTransferMessage(appointmentId, accountKey, amount, date, onResult)
                                        }
                                    )
                                }
                                "stock", "stok" -> {
                                    StockScreen(
                                        catalogItems = catalogItems,
                                        stockItems = stockItems,
                                        movements = stockMovements,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onSaveCatalogItem = { item, callback -> viewModel.saveCatalogItem(item, callback) },
                                        onSaveStockItem = { item, callback -> viewModel.saveStockItem(item, callback) },
                                        onCreateMovement = { movement, callback -> viewModel.createStockMovement(movement, callback) }
                                    )
                                }
                                "catalog" -> {
                                    StockScreen(
                                        catalogItems = catalogItems,
                                        stockItems = stockItems,
                                        movements = stockMovements,
                                        onBackClick = { viewModel.navigateTo("modules") },
                                        onSaveCatalogItem = { item, callback -> viewModel.saveCatalogItem(item, callback) },
                                        onSaveStockItem = { item, callback -> viewModel.saveStockItem(item, callback) },
                                        onCreateMovement = { movement, callback -> viewModel.createStockMovement(movement, callback) },
                                        initialTab = 1
                                    )
                                }
                                "campaigns" -> {
                                    CampaignsScreen(
                                        dashboard = campaignDashboard,
                                        isLoading = isCampaignsLoading,
                                        error = campaignsError,
                                        onBackClick = { viewModel.navigateTo("modules") },
                                        onRefresh = { viewModel.fetchCampaigns() },
                                        onCreateAndSend = { name, ids, onProgress, callback ->
                                            viewModel.createAndSendCampaign(name, ids, onProgress, callback)
                                        },
                                        onResumeCampaign = { id, onProgress, callback ->
                                            viewModel.resumeCampaign(id, onProgress, callback)
                                        },
                                        onSetConsent = { ids, status, callback ->
                                            viewModel.setCampaignConsent(ids, status, callback)
                                        },
                                        onImportContacts = { contacts, confirmed, callback ->
                                            viewModel.importCampaignContacts(contacts, confirmed, callback)
                                        }
                                    )
                                }
                                "whatsapp_status" -> {
                                    WhatsAppStatusScreen(
                                        status = whatsAppStatus,
                                        onBackClick = { viewModel.navigateTo("modules") }
                                    )
                                }
                                "modules" -> {
                                    ModulesGridScreen(
                                        whatsAppConnected = whatsAppStatus.isConnected,
                                        onNavigateToModule = { module ->
                                            viewModel.navigateTo(module.id, module)
                                        }
                                    )
                                }
                                else -> {
                                    ModulePlaceholderScreen(
                                        module = selectedModule,
                                        onBackToDashboard = {
                                            viewModel.navigateTo("dashboard")
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
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }
}
