package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.TokenStore
import com.example.data.remote.NetworkModule
import com.example.data.repository.RemoteAdminRepositoryImpl
import com.example.ui.components.BottomNavBar
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.AppointmentsScreen
import com.example.ui.screens.CustomersScreen
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
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ServiceReceiptScreen
import com.example.ui.screens.WhatsAppStatusScreen
import com.example.ui.theme.SancakKombiTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        viewModelFactory {
            initializer {
                val tokenStore = TokenStore(applicationContext)
                MainViewModel(
                    repository = RemoteAdminRepositoryImpl(
                        api = NetworkModule.adminApiService,
                        tokenStore = tokenStore,
                        context = applicationContext
                    )
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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
            val selectedRecordForReceipt by viewModel.selectedFinanceRecordForReceipt.collectAsState()

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

            val googleAdsStats by viewModel.googleAdsStats.collectAsState()
            val googleAdsCampaigns by viewModel.googleAdsCampaigns.collectAsState()

            val whatsAppStatus by viewModel.whatsAppStatus.collectAsState()

            SancakKombiTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (isLoggedIn) {
                            TopHeaderBar(
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleTheme() },
                                onLogout = { viewModel.logout() }
                            )
                        }
                    },
                    bottomBar = {
                        if (isLoggedIn) {
                            BottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { route, module ->
                                    viewModel.navigateTo(route, module)
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (!isLoggedIn) {
                            LoginScreen(
                                isLoading = isLoading,
                                errorMessage = loginError,
                                onLogin = { password -> viewModel.login(password) }
                            )
                        } else {
                            when (currentRoute) {
                                "dashboard" -> {
                                    DashboardScreen(
                                        stats = stats,
                                        onNavigateToModule = { module ->
                                            viewModel.navigateTo(module.id, module)
                                        }
                                    )
                                }
                                "appointments", "randevular" -> {
                                    AppointmentsScreen(
                                        appointments = appointments,
                                        bankAccounts = bankAccounts,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onAddAppointment = { appt -> viewModel.addAppointment(appt) },
                                        onUpdateAppointment = { appt -> viewModel.updateAppointment(appt) },
                                        onUpdateStatus = { id, st -> viewModel.updateAppointmentStatus(id, st) },
                                        onCompleteJob = { id, report -> viewModel.completeJob(id, report) },
                                        onDeleteAppointment = { id -> viewModel.deleteAppointment(id) },
                                        onSendBankTransfer = { id, accKey, amt, date, callback ->
                                            viewModel.sendBankTransferMessage(id, accKey, amt, date, callback)
                                        }
                                    )
                                }
                                "customers", "musteriler" -> {
                                    CustomersScreen(
                                        customers = customers,
                                        appointments = appointments,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onAddCustomer = { cust -> viewModel.addCustomer(cust) },
                                        onUpdateCustomer = { cust -> viewModel.updateCustomer(cust) }
                                    )
                                }
                                "finance", "finans" -> {
                                    FinanceScreen(
                                        summary = financeSummary,
                                        financeRecords = financeRecords,
                                        bankAccounts = bankAccounts,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onAddFinanceRecord = { record -> viewModel.addFinanceRecord(record) },
                                        onUpdateBankAccounts = { accs -> viewModel.updateBankAccounts(accs) },
                                        onViewReceipt = { record ->
                                            viewModel.selectFinanceRecordForReceipt(record)
                                            viewModel.navigateTo("service_receipt")
                                        }
                                    )
                                }
                                "service_receipt", "makbuz" -> {
                                    ServiceReceiptScreen(
                                        record = selectedRecordForReceipt,
                                        onBackClick = { viewModel.navigateTo("finance") }
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
                                        onDeleteProposal = { id -> viewModel.deleteProposal(id) }
                                    )
                                }
                                "new_proposal" -> {
                                    NewProposalScreen(
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
                                "maintenance", "bakim" -> {
                                    MaintenanceScreen(
                                        stats = maintenanceStats,
                                        rules = maintenanceRules,
                                        customers = customers,
                                        onBackClick = { viewModel.navigateTo("dashboard") },
                                        onAddRule = { r -> viewModel.addMaintenanceRule(r) },
                                        onUpdateRule = { r -> viewModel.updateMaintenanceRule(r) },
                                        onDeleteRule = { id -> viewModel.deleteMaintenanceRule(id) },
                                        onToggleRuleStatus = { id -> viewModel.toggleMaintenanceRuleStatus(id) }
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
                                        stats = googleAdsStats,
                                        campaigns = googleAdsCampaigns,
                                        onToggleCampaignStatus = { id -> viewModel.toggleCampaignStatus(id) },
                                        onBackClick = { viewModel.navigateTo("dashboard") }
                                    )
                                }
                                "whatsapp", "whatsapp_connect", "whatsapp_baglantisi" -> {
                                    WhatsAppStatusScreen(
                                        status = whatsAppStatus,
                                        onBackClick = { viewModel.navigateTo("dashboard") }
                                    )
                                }
                                "modules" -> {
                                    ModulesGridScreen(
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
