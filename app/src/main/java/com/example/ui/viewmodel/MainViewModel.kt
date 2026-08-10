package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdminModule
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.BankAccount
import com.example.data.model.Customer
import com.example.data.model.DashboardStats
import com.example.data.model.FinanceRecord
import com.example.data.model.FinanceSummary
import com.example.data.model.JobReport
import com.example.data.model.Proposal
import com.example.data.model.ProposalStatus
import com.example.data.model.CustomerMessagingSettings
import com.example.data.model.MaintenanceRule
import com.example.data.model.MaintenanceStats
import com.example.data.model.MessageJob
import com.example.data.model.MessageLog
import com.example.data.model.MessageTemplate
import com.example.data.model.MessagingStats
import com.example.data.model.StaffMessagingSettings
import com.example.data.repository.AdminRepository
import com.example.data.repository.MockAdminRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: AdminRepository = MockAdminRepositoryImpl()
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authToken = MutableStateFlow<String?>("sk_admin_token_default")
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _currentRoute = MutableStateFlow("dashboard")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    private val _selectedModule = MutableStateFlow<AdminModule?>(null)
    val selectedModule: StateFlow<AdminModule?> = _selectedModule.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    // Finance State
    private val _financeRecords = MutableStateFlow<List<FinanceRecord>>(emptyList())
    val financeRecords: StateFlow<List<FinanceRecord>> = _financeRecords.asStateFlow()

    private val _financeSummary = MutableStateFlow(FinanceSummary())
    val financeSummary: StateFlow<FinanceSummary> = _financeSummary.asStateFlow()

    private val _bankAccounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val bankAccounts: StateFlow<List<BankAccount>> = _bankAccounts.asStateFlow()

    private val _selectedFinanceRecordForReceipt = MutableStateFlow<FinanceRecord?>(null)
    val selectedFinanceRecordForReceipt: StateFlow<FinanceRecord?> = _selectedFinanceRecordForReceipt.asStateFlow()

    // Proposals State
    private val _proposals = MutableStateFlow<List<Proposal>>(emptyList())
    val proposals: StateFlow<List<Proposal>> = _proposals.asStateFlow()

    private val _selectedProposal = MutableStateFlow<Proposal?>(null)
    val selectedProposal: StateFlow<Proposal?> = _selectedProposal.asStateFlow()

    // Messaging State
    private val _messagingStats = MutableStateFlow(MessagingStats())
    val messagingStats: StateFlow<MessagingStats> = _messagingStats.asStateFlow()

    private val _customerMessagingSettings = MutableStateFlow(CustomerMessagingSettings())
    val customerMessagingSettings: StateFlow<CustomerMessagingSettings> = _customerMessagingSettings.asStateFlow()

    private val _staffMessagingSettings = MutableStateFlow(StaffMessagingSettings())
    val staffMessagingSettings: StateFlow<StaffMessagingSettings> = _staffMessagingSettings.asStateFlow()

    private val _messageTemplates = MutableStateFlow<List<MessageTemplate>>(emptyList())
    val messageTemplates: StateFlow<List<MessageTemplate>> = _messageTemplates.asStateFlow()

    private val _messageJobs = MutableStateFlow<List<MessageJob>>(emptyList())
    val messageJobs: StateFlow<List<MessageJob>> = _messageJobs.asStateFlow()

    private val _messageLogs = MutableStateFlow<List<MessageLog>>(emptyList())
    val messageLogs: StateFlow<List<MessageLog>> = _messageLogs.asStateFlow()

    // Maintenance State
    private val _maintenanceStats = MutableStateFlow(MaintenanceStats())
    val maintenanceStats: StateFlow<MaintenanceStats> = _maintenanceStats.asStateFlow()

    private val _maintenanceRules = MutableStateFlow<List<MaintenanceRule>>(emptyList())
    val maintenanceRules: StateFlow<List<MaintenanceRule>> = _maintenanceRules.asStateFlow()

    // Reports State
    private val _selectedReportRange = MutableStateFlow(com.example.data.model.ReportTimeRange.WEEK)
    val selectedReportRange: StateFlow<com.example.data.model.ReportTimeRange> = _selectedReportRange.asStateFlow()

    private val _reportData = MutableStateFlow(com.example.data.model.ReportData())
    val reportData: StateFlow<com.example.data.model.ReportData> = _reportData.asStateFlow()

    // Google Ads State
    private val _googleAdsStats = MutableStateFlow(com.example.data.model.GoogleAdsStats())
    val googleAdsStats: StateFlow<com.example.data.model.GoogleAdsStats> = _googleAdsStats.asStateFlow()

    private val _googleAdsCampaigns = MutableStateFlow<List<com.example.data.model.GoogleAdsCampaign>>(emptyList())
    val googleAdsCampaigns: StateFlow<List<com.example.data.model.GoogleAdsCampaign>> = _googleAdsCampaigns.asStateFlow()

    // WhatsApp Status State
    private val _whatsAppStatus = MutableStateFlow(com.example.data.model.WhatsAppStatus())
    val whatsAppStatus: StateFlow<com.example.data.model.WhatsAppStatus> = _whatsAppStatus.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getDashboardStats().collect { data ->
                _stats.value = data
            }
        }
        viewModelScope.launch {
            repository.getAppointments().collect { list ->
                _appointments.value = list
            }
        }
        viewModelScope.launch {
            repository.getCustomers().collect { list ->
                _customers.value = list
            }
        }
        viewModelScope.launch {
            repository.getFinanceRecords().collect { list ->
                _financeRecords.value = list
            }
        }
        viewModelScope.launch {
            repository.getFinanceSummary().collect { sum ->
                _financeSummary.value = sum
            }
        }
        viewModelScope.launch {
            repository.getBankAccounts().collect { accs ->
                _bankAccounts.value = accs
            }
        }
        viewModelScope.launch {
            repository.getProposals().collect { props ->
                _proposals.value = props
                if (_selectedProposal.value == null && props.isNotEmpty()) {
                    _selectedProposal.value = props.first()
                }
            }
        }
        viewModelScope.launch {
            repository.getMessagingStats().collect { _messagingStats.value = it }
        }
        viewModelScope.launch {
            repository.getCustomerMessagingSettings().collect { _customerMessagingSettings.value = it }
        }
        viewModelScope.launch {
            repository.getStaffMessagingSettings().collect { _staffMessagingSettings.value = it }
        }
        viewModelScope.launch {
            repository.getMessageTemplates().collect { _messageTemplates.value = it }
        }
        viewModelScope.launch {
            repository.getMessageJobs().collect { _messageJobs.value = it }
        }
        viewModelScope.launch {
            repository.getMessageLogs().collect { _messageLogs.value = it }
        }
        viewModelScope.launch {
            repository.getMaintenanceStats().collect { _maintenanceStats.value = it }
        }
        viewModelScope.launch {
            repository.getMaintenanceRules().collect { _maintenanceRules.value = it }
        }
        viewModelScope.launch {
            _selectedReportRange.collect { range ->
                repository.getReportData(range).collect { data ->
                    _reportData.value = data
                }
            }
        }
        viewModelScope.launch {
            repository.getGoogleAdsStats().collect { _googleAdsStats.value = it }
        }
        viewModelScope.launch {
            repository.getGoogleAdsCampaigns().collect { _googleAdsCampaigns.value = it }
        }
        viewModelScope.launch {
            repository.getWhatsAppStatus().collect { _whatsAppStatus.value = it }
        }
    }

    fun login(password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            
            val result = repository.login(password)
            _isLoading.value = false
            
            result.onSuccess { token ->
                _authToken.value = token
                _isLoggedIn.value = true
                _currentRoute.value = "dashboard"
            }.onFailure { error ->
                _loginError.value = error.message ?: "Giriş hatası. Lütfen şifrenizi kontrol edin."
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _authToken.value = null
        _currentRoute.value = "login"
        _selectedModule.value = null
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun navigateTo(route: String, module: AdminModule? = null) {
        _currentRoute.value = route
        _selectedModule.value = module
        repository.refreshAll()
    }

    // Appointments Actions
    fun addAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.addAppointment(appointment)
        }
    }

    fun updateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.updateAppointment(appointment)
        }
    }

    fun updateAppointmentStatus(id: String, status: AppointmentStatus) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(id, status)
        }
    }

    fun completeJob(appointmentId: String, jobReport: JobReport) {
        viewModelScope.launch {
            repository.completeJob(appointmentId, jobReport)
        }
    }

    fun sendBankTransferMessage(
        appointmentId: String,
        paymentAccountKey: String,
        amount: Double? = null,
        promisedPaymentDate: String? = null,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.sendBankTransferMessage(
                appointmentId = appointmentId,
                paymentAccountKey = paymentAccountKey,
                amount = amount,
                promisedPaymentDate = promisedPaymentDate
            )
            onResult(res)
        }
    }

    fun getAvailableSlots(dateIso: String, onResult: (Result<List<String>>) -> Unit) {
        viewModelScope.launch {
            val res = repository.getAvailableSlots(dateIso)
            onResult(res)
        }
    }

    fun deleteAppointment(id: String) {
        viewModelScope.launch {
            repository.deleteAppointment(id)
        }
    }

    // Customers Actions
    fun addCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.addCustomer(customer)
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    // Finance Actions
    fun addFinanceRecord(record: FinanceRecord) {
        viewModelScope.launch {
            repository.addFinanceRecord(record)
        }
    }

    fun updateBankAccounts(accounts: List<BankAccount>) {
        viewModelScope.launch {
            repository.updateBankAccounts(accounts)
        }
    }

    fun selectFinanceRecordForReceipt(record: FinanceRecord?) {
        _selectedFinanceRecordForReceipt.value = record
    }

    suspend fun getReceiptDetail(entryId: String): Result<com.example.data.remote.ReceiptDetailDto> {
        return repository.getReceiptDetail(entryId)
    }

    // Proposals Actions
    fun addProposal(proposal: Proposal) {
        viewModelScope.launch {
            repository.addProposal(proposal)
            _selectedProposal.value = proposal
        }
    }

    fun updateProposalStatus(id: String, status: ProposalStatus) {
        viewModelScope.launch {
            repository.updateProposalStatus(id, status)
            _selectedProposal.value = _selectedProposal.value?.takeIf { it.id == id }?.copy(status = status) ?: _selectedProposal.value
        }
    }

    fun selectProposal(proposal: Proposal?) {
        _selectedProposal.value = proposal
    }

    fun deleteProposal(id: String) {
        viewModelScope.launch {
            repository.deleteProposal(id)
        }
    }

    // Messaging Actions
    fun updateCustomerMessagingSettings(settings: CustomerMessagingSettings) {
        viewModelScope.launch {
            repository.updateCustomerMessagingSettings(settings)
        }
    }

    fun updateStaffMessagingSettings(settings: StaffMessagingSettings) {
        viewModelScope.launch {
            repository.updateStaffMessagingSettings(settings)
        }
    }

    fun updateMessageTemplate(template: MessageTemplate) {
        viewModelScope.launch {
            repository.updateMessageTemplate(template)
        }
    }

    fun retryMessageJob(jobId: String) {
        viewModelScope.launch {
            repository.retryMessageJob(jobId)
        }
    }

    // Maintenance Actions
    fun addMaintenanceRule(rule: MaintenanceRule) {
        viewModelScope.launch {
            repository.addMaintenanceRule(rule)
        }
    }

    fun updateMaintenanceRule(rule: MaintenanceRule) {
        viewModelScope.launch {
            repository.updateMaintenanceRule(rule)
        }
    }

    fun deleteMaintenanceRule(ruleId: String) {
        viewModelScope.launch {
            repository.deleteMaintenanceRule(ruleId)
        }
    }

    fun toggleMaintenanceRuleStatus(ruleId: String) {
        viewModelScope.launch {
            repository.toggleMaintenanceRuleStatus(ruleId)
        }
    }

    // Report Actions
    fun setReportTimeRange(range: com.example.data.model.ReportTimeRange) {
        _selectedReportRange.value = range
    }

    // Google Ads Actions
    fun toggleCampaignStatus(campaignId: String) {
        viewModelScope.launch {
            repository.toggleCampaignStatus(campaignId)
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }
}
