package com.example.data.repository

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
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    suspend fun login(password: String): Result<String>
    suspend fun logout()

    // Ekranlar arası geçişte çağrılır - web'den yapılan değişikliklerin mobile
    // yansıması için tüm listeleri arka planda tazeler.
    fun refreshAll()

    fun getDashboardStats(): Flow<DashboardStats>
    fun getWhatsAppConnected(): Flow<Boolean>

    // Appointments CRUD
    fun getAppointments(): Flow<List<Appointment>>
    suspend fun addAppointment(appointment: Appointment): Result<Unit>
    suspend fun updateAppointment(appointment: Appointment): Result<Unit>
    suspend fun updateAppointmentStatus(id: String, status: AppointmentStatus): Result<Unit>
    suspend fun completeJob(appointmentId: String, jobReport: JobReport): Result<Unit>
    suspend fun sendBankTransferMessage(
        appointmentId: String,
        paymentAccountKey: String,
        amount: Double? = null,
        promisedPaymentDate: String? = null
    ): Result<String>
    suspend fun getAvailableSlots(dateIso: String): Result<List<String>>
    suspend fun deleteAppointment(id: String): Result<Unit>

    // Customers CRUD
    fun getCustomers(): Flow<List<Customer>>
    suspend fun addCustomer(customer: Customer): Result<Unit>
    suspend fun updateCustomer(customer: Customer): Result<Unit>
    suspend fun getDeviceHistory(customerId: String): Result<com.example.data.remote.DeviceHistoryDto>

    // Finance
    fun getFinanceRecords(): Flow<List<FinanceRecord>>
    fun getFinanceSummary(): Flow<FinanceSummary>
    fun getBankAccounts(): Flow<List<BankAccount>>
    suspend fun addFinanceRecord(record: FinanceRecord): Result<Unit>
    suspend fun updateBankAccounts(accounts: List<BankAccount>): Result<Unit>
    suspend fun getReceiptDetail(entryId: String): Result<com.example.data.remote.ReceiptDetailDto>

    // Proposals
    fun getProposals(): Flow<List<Proposal>>
    suspend fun addProposal(proposal: Proposal): Result<Unit>
    suspend fun updateProposalStatus(id: String, status: ProposalStatus): Result<Unit>
    suspend fun deleteProposal(id: String): Result<Unit>

    // Messaging
    fun getMessagingStats(): Flow<MessagingStats>
    fun getCustomerMessagingSettings(): Flow<CustomerMessagingSettings>
    suspend fun updateCustomerMessagingSettings(settings: CustomerMessagingSettings): Result<Unit>
    fun getStaffMessagingSettings(): Flow<StaffMessagingSettings>
    suspend fun updateStaffMessagingSettings(settings: StaffMessagingSettings): Result<Unit>
    fun getMessageTemplates(): Flow<List<MessageTemplate>>
    suspend fun updateMessageTemplate(template: MessageTemplate): Result<Unit>
    fun getMessageJobs(): Flow<List<MessageJob>>
    suspend fun retryMessageJob(jobId: String): Result<Unit>
    fun getMessageLogs(): Flow<List<MessageLog>>

    // Maintenance
    fun getMaintenanceStats(): Flow<MaintenanceStats>
    fun getMaintenanceRules(): Flow<List<MaintenanceRule>>
    suspend fun addMaintenanceRule(rule: MaintenanceRule): Result<Unit>
    suspend fun updateMaintenanceRule(rule: MaintenanceRule): Result<Unit>
    suspend fun deleteMaintenanceRule(ruleId: String): Result<Unit>
    suspend fun toggleMaintenanceRuleStatus(ruleId: String): Result<Unit>

    // Reports
    fun getReportData(timeRange: com.example.data.model.ReportTimeRange): Flow<com.example.data.model.ReportData>

    // Google Ads
    fun getGoogleAdsStats(): Flow<com.example.data.model.GoogleAdsStats>
    fun getGoogleAdsCampaigns(): Flow<List<com.example.data.model.GoogleAdsCampaign>>
    suspend fun toggleCampaignStatus(campaignId: String): Result<Unit>

    // WhatsApp Status
    fun getWhatsAppStatus(): Flow<com.example.data.model.WhatsAppStatus>
}
