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
import com.example.data.model.CatalogItem
import com.example.data.model.StockItem
import com.example.data.model.StockMovement
import com.example.data.model.MaintenanceRule
import com.example.data.model.MaintenanceStats
import com.example.data.model.MessageJob
import com.example.data.model.MessageLog
import com.example.data.model.MessageTemplate
import com.example.data.model.MessagingStats
import com.example.data.model.StaffMessagingSettings
import com.example.data.repository.AdminRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authToken = MutableStateFlow<String?>(null)
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
    private val deletedFinanceRecordIds = mutableSetOf<String>()
    private val deletedAppointmentIds = mutableSetOf<String>()
    private val deletedAdsExpenseDates = mutableSetOf<String>()

    private val _financeRecords = MutableStateFlow<List<FinanceRecord>>(emptyList())
    val financeRecords: StateFlow<List<FinanceRecord>> = _financeRecords.asStateFlow()

    private val _financeSummary = MutableStateFlow(FinanceSummary())
    val financeSummary: StateFlow<FinanceSummary> = _financeSummary.asStateFlow()

    private val _bankAccounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val bankAccounts: StateFlow<List<BankAccount>> = _bankAccounts.asStateFlow()

    private val _catalogItems = MutableStateFlow<List<CatalogItem>>(emptyList())
    val catalogItems: StateFlow<List<CatalogItem>> = _catalogItems.asStateFlow()

    private val _stockItems = MutableStateFlow<List<StockItem>>(emptyList())
    val stockItems: StateFlow<List<StockItem>> = _stockItems.asStateFlow()

    private val _stockMovements = MutableStateFlow<List<StockMovement>>(emptyList())
    val stockMovements: StateFlow<List<StockMovement>> = _stockMovements.asStateFlow()

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

    private val _adsStats = MutableStateFlow<com.example.data.remote.AdsStatsDto?>(null)
    val adsStats: StateFlow<com.example.data.remote.AdsStatsDto?> = _adsStats.asStateFlow()

    private val _adsCampaigns = MutableStateFlow<List<com.example.data.remote.AdsCampaignDto>>(emptyList())
    val adsCampaigns: StateFlow<List<com.example.data.remote.AdsCampaignDto>> = _adsCampaigns.asStateFlow()

    private val _isAdsLoading = MutableStateFlow(false)
    val isAdsLoading: StateFlow<Boolean> = _isAdsLoading.asStateFlow()

    private val _adsError = MutableStateFlow<String?>(null)
    val adsError: StateFlow<String?> = _adsError.asStateFlow()

    private val _togglingCampaignId = MutableStateFlow<String?>(null)
    val togglingCampaignId: StateFlow<String?> = _togglingCampaignId.asStateFlow()

    // WhatsApp Status State
    private val _whatsAppStatus = MutableStateFlow(com.example.data.model.WhatsAppStatus())
    val whatsAppStatus: StateFlow<com.example.data.model.WhatsAppStatus> = _whatsAppStatus.asStateFlow()

    private fun recalculateFinanceSummary(records: List<FinanceRecord>) {
        val totalIncome = records.filter { it.type == com.example.data.model.FinanceType.GELIR }.sumOf { it.amount }
        val totalExpense = records.filter { it.type == com.example.data.model.FinanceType.GIDER }.sumOf { it.amount }
        val outstanding = records.filter { it.status == "Kısmi" || it.status == "Bekliyor" }.sumOf { (it.totalAmount - it.collectedAmount).coerceAtLeast(0.0) }
        _financeSummary.value = FinanceSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            outstandingReceivable = outstanding
        )
    }

    init {
        viewModelScope.launch {
            repository.getAuthToken().collect { token ->
                _authToken.value = token
                _isLoggedIn.value = !token.isNullOrBlank()
                if (!token.isNullOrBlank()) {
                    fetchAdsData()
                }
            }
        }
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
                _customers.value = list.filterNot { it.isArchived }
            }
        }
        viewModelScope.launch {
            repository.getFinanceRecords().collect { list ->
                val filtered = list.filterNot { rec ->
                    rec.id in deletedFinanceRecordIds ||
                    rec.appointmentId?.let { it in deletedAppointmentIds } == true ||
                    (deletedAdsExpenseDates.contains(rec.date) && (rec.source.contains("Google Ads", ignoreCase = true) || rec.id.startsWith("ads_")))
                }
                _financeRecords.value = filtered
                recalculateFinanceSummary(filtered)
            }
        }
        viewModelScope.launch {
            repository.getFinanceSummary().collect { sum ->
                if (_financeRecords.value.isNotEmpty()) {
                    recalculateFinanceSummary(_financeRecords.value)
                } else {
                    _financeSummary.value = sum
                }
            }
        }
        viewModelScope.launch {
            repository.getBankAccounts().collect { accs ->
                _bankAccounts.value = accs
            }
        }
        viewModelScope.launch {
            repository.getCatalogItems().collect { _catalogItems.value = it }
        }
        viewModelScope.launch {
            repository.getStockItems().collect { _stockItems.value = it }
        }
        viewModelScope.launch {
            repository.getStockMovements().collect { _stockMovements.value = it }
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

    fun login(password: String, rememberMe: Boolean = true) {
        if (password.isBlank()) {
            _loginError.value = "Şifre boş bırakılamaz."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            
            val result = repository.login(password, rememberMe)
            _isLoading.value = false
            
            result.onSuccess { token ->
                _authToken.value = token
                _isLoggedIn.value = true
                _loginError.value = null
                _currentRoute.value = "dashboard"
                repository.refreshAll()
                fetchAdsData()
            }.onFailure { error ->
                _isLoggedIn.value = false
                _loginError.value = error.message ?: "Giriş başarısız."
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authToken.value = null
            _isLoggedIn.value = false
        }
        _currentRoute.value = "login"
        _selectedModule.value = null
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun navigateTo(route: String, module: AdminModule? = null) {
        _currentRoute.value = route
        _selectedModule.value = module
        repository.refreshAll()
        if (route in listOf("ads", "google_ads", "googleads", "reklamlar")) {
            fetchAdsData()
        }
    }

    fun syncGoogleAdsSpendToFinance(customSpend: Double? = null, force: Boolean = false) {
        // Otomatik Google Ads gideri eklenmesi tamamen devre disi birakildi.
    }

    fun fetchAdsData(onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isAdsLoading.value = true
            _adsError.value = null

            val statsDeferred = async { repository.getAdsStats() }
            val campaignsDeferred = async { repository.getAdsCampaigns() }

            val statsResult = statsDeferred.await()
            val campaignsResult = campaignsDeferred.await()

            if (statsResult.isSuccess && campaignsResult.isSuccess) {
                val statsDto = statsResult.getOrNull() ?: com.example.data.remote.AdsStatsDto()
                val campList = campaignsResult.getOrDefault(emptyList())
                _adsStats.value = statsDto
                _adsCampaigns.value = campList
                _adsError.value = null
                onComplete?.invoke(true)
            } else {
                val err = statsResult.exceptionOrNull()?.message
                    ?: campaignsResult.exceptionOrNull()?.message
                    ?: "Google Ads verisi alınamadı."
                _adsError.value = err
                onComplete?.invoke(false)
            }
            _isAdsLoading.value = false
        }
    }

    fun toggleAdsCampaign(campaignId: String, onResult: (Boolean, String, String?) -> Unit) {
        viewModelScope.launch {
            _togglingCampaignId.value = campaignId
            val result = repository.toggleAdsCampaign(campaignId)
            if (result.isSuccess) {
                val newStatus = result.getOrNull() ?: "PAUSED"
                _adsCampaigns.value = _adsCampaigns.value.map { campaign ->
                    if (campaign.id == campaignId) campaign.copy(status = newStatus) else campaign
                }
                onResult(true, newStatus, null)
            } else {
                val errMessage = result.exceptionOrNull()?.message ?: "Kampanya durumu değiştirilemedi"
                onResult(false, "", errMessage)
            }
            _togglingCampaignId.value = null
        }
    }

    // Appointments Actions
    fun addAppointment(appointment: Appointment, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            onResult(repository.addAppointment(appointment))
        }
    }

    fun updateAppointment(appointment: Appointment, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            onResult(repository.updateAppointment(appointment))
        }
    }

    fun updateAppointmentStatus(id: String, status: AppointmentStatus) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(id, status)
        }
    }

    fun completeJob(appointmentId: String, jobReport: JobReport, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            onResult(repository.completeJob(appointmentId, jobReport))
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
            deletedAppointmentIds.add(id)
            _financeRecords.value = _financeRecords.value.filterNot { it.appointmentId == id }
            recalculateFinanceSummary(_financeRecords.value)
            val result = repository.deleteAppointment(id)
            if (result.isFailure) {
                deletedAppointmentIds.remove(id)
                repository.refreshAll()
            }
        }
    }

    // Customers Actions
    fun addCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.addCustomer(customer)
        }
    }

    fun addCustomers(customers: List<Customer>) {
        viewModelScope.launch {
            repository.addCustomers(customers)
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(id: String, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            onResult(repository.deleteCustomer(id))
        }
    }

    suspend fun getDeviceHistory(customerId: String): Result<com.example.data.remote.DeviceHistoryDto> {
        return repository.getDeviceHistory(customerId)
    }

    // Finance Actions
    fun addFinanceRecord(record: FinanceRecord) {
        viewModelScope.launch {
            deletedFinanceRecordIds.remove(record.id)
            if (record.source.contains("Google Ads", ignoreCase = true) || record.id.startsWith("ads_")) {
                deletedAdsExpenseDates.remove(record.date)
            }
            _financeRecords.value = listOf(record) + _financeRecords.value.filterNot { it.id == record.id }
            recalculateFinanceSummary(_financeRecords.value)
            repository.addFinanceRecord(record)
        }
    }

    fun deleteFinanceRecord(id: String) {
        viewModelScope.launch {
            val recToDelete = _financeRecords.value.find { it.id == id }
            if (recToDelete != null) {
                if (recToDelete.source.contains("Google Ads", ignoreCase = true) || recToDelete.id.startsWith("ads_")) {
                    deletedAdsExpenseDates.add(recToDelete.date)
                }
            }
            deletedFinanceRecordIds.add(id)

            _financeRecords.value = _financeRecords.value.filterNot { it.id == id || it.id in deletedFinanceRecordIds }
            recalculateFinanceSummary(_financeRecords.value)
            repository.deleteFinanceRecord(id)
        }
    }

    fun updateFinanceRecordStatus(
        id: String,
        status: String,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        if (status !in setOf("paid", "partial", "unpaid")) {
            onResult(Result.failure(IllegalArgumentException("Geçersiz finans durumu.")))
            return
        }

        viewModelScope.launch {
            onResult(repository.updateFinanceRecordStatus(id, status))
        }
    }

    fun updateBankAccounts(accounts: List<BankAccount>) {
        viewModelScope.launch {
            _bankAccounts.value = accounts
            repository.updateBankAccounts(accounts)
        }
    }

    fun saveCatalogItem(item: CatalogItem, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            onResult(repository.saveCatalogItem(item))
        }
    }

    fun saveStockItem(item: StockItem, onResult: (Result<StockItem>) -> Unit = {}) {
        viewModelScope.launch {
            onResult(repository.saveStockItem(item))
        }
    }

    fun createStockMovement(movement: StockMovement, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            onResult(repository.createStockMovement(movement))
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
