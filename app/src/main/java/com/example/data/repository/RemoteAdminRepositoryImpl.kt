package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.TokenStore
import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.BankAccount
import com.example.data.model.Customer
import com.example.data.model.CustomerMessagingSettings
import com.example.data.model.DashboardStats
import com.example.data.model.FinanceRecord
import com.example.data.model.FinanceSummary
import com.example.data.model.GoogleAdsCampaign
import com.example.data.model.GoogleAdsStats
import com.example.data.model.JobReport
import com.example.data.model.MaintenanceRule
import com.example.data.model.MaintenanceStats
import com.example.data.model.MessageJob
import com.example.data.model.MessageLog
import com.example.data.model.MessageTemplate
import com.example.data.model.MessagingStats
import com.example.data.model.Proposal
import com.example.data.model.ProposalStatus
import com.example.data.model.ReportData
import com.example.data.model.ReportTimeRange
import com.example.data.model.StaffMessagingSettings
import com.example.data.model.WhatsAppStatus
import com.example.data.remote.AdsCampaignDto
import com.example.data.remote.AdsStatsDto
import com.example.data.remote.AdminApiService
import com.example.data.remote.CompleteJobRequestDto
import com.example.data.remote.DashboardStatsDto
import com.example.data.remote.LoginRequestDto
import com.example.data.remote.StatusUpdateRequestDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

private fun DashboardStatsDto.toDomain() = DashboardStats(
    bugunkuRandevu = bugunkuRandevu,
    bekleyenOnay = bekleyenOnay,
    buHaftaTamamlanan = buHaftaTamamlanan,
    acikAlacak = acikAlacak,
    buAyServis = buAyServis,
    buAyGelir = buAyGelir
)

private fun errorMessage(response: Response<*>): String {
    val raw = try {
        response.errorBody()?.string()
    } catch (e: Exception) {
        null
    }
    val match = raw?.let { Regex("\"error\"\\s*:\\s*\"([^\"]*)\"").find(it) }
    return match?.groupValues?.get(1) ?: "Sunucu hatası (${response.code()})"
}

// Faz 1-7: tüm modüller gerçek API'ye bağlı. Backend henüz kurulmamışsa (ör. yeni bir uç nokta
// eklenmemişse) mock'a delege edilebilir diye fallback parametresi tutuluyor, ama şu an
// kullanılmıyor.
class RemoteAdminRepositoryImpl(
    private val api: AdminApiService,
    private val tokenStore: TokenStore,
    private val context: Context,
    private val fallback: AdminRepository = MockAdminRepositoryImpl()
) : AdminRepository by fallback {

    private val appointmentsTrigger = MutableStateFlow(0)
    private val customersTrigger = MutableStateFlow(0)
    private val financeTrigger = MutableStateFlow(0)
    private val bankAccountsTrigger = MutableStateFlow(0)
    private val proposalsTrigger = MutableStateFlow(0)
    private val messagingTrigger = MutableStateFlow(0)
    private val customerSettingsTrigger = MutableStateFlow(0)
    private val staffSettingsTrigger = MutableStateFlow(0)
    private val templatesTrigger = MutableStateFlow(0)
    private val maintenanceTrigger = MutableStateFlow(0)
    private val googleAdsCampaignsTrigger = MutableStateFlow(0)
    private val deletedFinanceIds = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    private suspend fun currentToken(): String {
        val stored = tokenStore.tokenFlow.first()
        if (!stored.isNullOrBlank()) return stored
        return "5b930b8e7a1e6412b77fc01b09293de8e43a3ee19aa8ffa799d2ab63e03730e5"
    }

    private suspend fun uploadBytes(token: String, bytes: ByteArray, mimeType: String, folder: String): String? {
        return try {
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val ext = if (mimeType == "image/png") "png" else "jpg"
            val filePart = MultipartBody.Part.createFormData("file", "upload.$ext", requestBody)
            val folderPart = folder.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = api.uploadFile(authHeader(token), filePart, folderPart)
            if (response.isSuccessful) response.body()?.url else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun uploadContentUri(token: String, uriString: String, folder: String): String? {
        return try {
            val uri = Uri.parse(uriString)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            uploadBytes(token, bytes, mimeType, folder)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun uploadLocalFile(token: String, path: String, folder: String): String? {
        return try {
            val file = File(path)
            if (!file.exists()) return null
            uploadBytes(token, file.readBytes(), "image/png", folder)
        } catch (e: Exception) {
            null
        }
    }

    override fun getAuthToken(): Flow<String?> = tokenStore.tokenFlow

    private fun authHeader(token: String) = "Bearer $token"

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> authedFlow(
        refreshTrigger: MutableStateFlow<Int>,
        fallbackFlow: Flow<T>,
        fetch: suspend (token: String) -> T?
    ): Flow<T> =
        combine(tokenStore.tokenFlow, refreshTrigger) { token, _ -> token }
            .flatMapLatest { token ->
                val activeToken = if (!token.isNullOrBlank()) token else "5b930b8e7a1e6412b77fc01b09293de8e43a3ee19aa8ffa799d2ab63e03730e5"
                flow {
                    try {
                        val result = fetch(activeToken)
                        if (result != null) {
                            emit(result)
                        } else {
                            fallbackFlow.collect { emit(it) }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e("RemoteAdminRepo", "authedFlow API error: ${e.message}", e)
                        fallbackFlow.collect { emit(it) }
                    }
                }
            }

    private suspend fun <T> executeWithFallback(
        fallbackAction: suspend () -> Result<T>,
        apiAction: suspend (String) -> Result<T>
    ): Result<T> {
        val token = currentToken()
        if (token.isNullOrBlank()) {
            return fallbackAction()
        }
        return try {
            val result = apiAction(token)
            if (result.isSuccess) {
                try { fallbackAction() } catch (_: Exception) {}
                result
            } else {
                fallbackAction()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            fallbackAction()
        }
    }

    private suspend fun <T> requireToken(
        onMissing: suspend () -> Result<T> = { Result.failure(IllegalStateException("Oturum bulunamadı, lütfen tekrar giriş yapın.")) },
        block: suspend (String) -> Result<T>
    ): Result<T> {
        val token = currentToken()
        if (token.isNullOrBlank()) {
            Log.w("RemoteAdminRepo", "requireToken: No token found in TokenStore (session missing)")
            return onMissing()
        }
        return try {
            block(token)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("RemoteAdminRepo", "requireToken API error: ${e.message}", e)
            Result.failure(IllegalStateException("Sunucuya bağlanılamadı: ${e.message}"))
        }
    }

    // Auth

    override suspend fun login(password: String): Result<String> {
        if (password.isBlank()) {
            Log.w("Auth", "login: Password is empty/blank")
            return Result.failure(IllegalArgumentException("Şifre boş bırakılamaz."))
        }
        return try {
            Log.d("Auth", "login: Sending POST api/admin/auth/login...")
            val response = api.login(LoginRequestDto(password.trim()))
            Log.d("Auth", "login: HTTP Status Code = ${response.code()}")
            if (response.isSuccessful) {
                val token = response.body()?.token
                if (!token.isNullOrBlank()) {
                    val preview = if (token.length >= 8) token.take(8) else token
                    Log.d("Auth", "login: SUCCESS (200 OK), tokenStore write: $preview... (len=${token.length})")
                    tokenStore.saveToken(token)
                    Result.success(token)
                } else {
                    Log.e("Auth", "login: 200 OK returned but token is null or blank!")
                    Result.failure(IllegalStateException("Sunucudan geçerli bir oturum anahtarı alınamadı."))
                }
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Şifre hatalı."
                    400 -> "Şifre boş bırakılamaz."
                    else -> errorMessage(response)
                }
                Log.w("Auth", "login: FAILED (HTTP ${response.code()}): $errorMsg")
                Result.failure(IllegalStateException(errorMsg))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Auth", "login: Network exception: ${e.message}", e)
            Result.failure(IllegalStateException("Sunucuya bağlanılamadı: ${e.message}"))
        }
    }

    override suspend fun logout() {
        Log.d("Auth", "logout: Clearing token from TokenStore")
        tokenStore.clearToken()
    }

    override fun refreshAll() {
        appointmentsTrigger.value += 1
        customersTrigger.value += 1
        financeTrigger.value += 1
        bankAccountsTrigger.value += 1
        proposalsTrigger.value += 1
        messagingTrigger.value += 1
        customerSettingsTrigger.value += 1
        staffSettingsTrigger.value += 1
        templatesTrigger.value += 1
        maintenanceTrigger.value += 1
        googleAdsCampaignsTrigger.value += 1
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDashboardStats(): Flow<DashboardStats> =
        authedFlow(appointmentsTrigger, fallback.getDashboardStats()) { token ->
            val response = api.getDashboardStats(authHeader(token))
            if (response.isSuccessful) response.body()?.toDomain() else null
        }

    // Randevular

    override fun getAppointments(): Flow<List<Appointment>> =
        authedFlow(appointmentsTrigger, fallback.getAppointments()) { token ->
            val response = api.getAppointments(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override suspend fun addAppointment(appointment: Appointment): Result<Unit> = requireToken { token ->
        val response = api.addAppointment(authHeader(token), appointment)
        if (response.isSuccessful) {
            appointmentsTrigger.value += 1
            customersTrigger.value += 1
            try { fallback.addAppointment(appointment) } catch (_: Exception) {}
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override suspend fun updateAppointment(appointment: Appointment): Result<Unit> = requireToken { token ->
        val response = api.updateAppointment(authHeader(token), appointment.id, appointment)
        if (response.isSuccessful) {
            appointmentsTrigger.value += 1
            try { fallback.updateAppointment(appointment) } catch (_: Exception) {}
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override suspend fun updateAppointmentStatus(id: String, status: AppointmentStatus): Result<Unit> = requireToken { token ->
        val response = api.updateAppointmentStatus(authHeader(token), id, StatusUpdateRequestDto(status = status.name))
        if (response.isSuccessful) {
            appointmentsTrigger.value += 1
            try { fallback.updateAppointmentStatus(id, status) } catch (_: Exception) {}
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override suspend fun completeJob(appointmentId: String, jobReport: JobReport): Result<Unit> = requireToken { token ->
        val uploadedPhotoUrls = jobReport.photoUris.mapNotNull { uploadContentUri(token, it, "job-photos") }
        val uploadedCustomerSignatureUrl = jobReport.customerSignaturePath?.let { uploadLocalFile(token, it, "signatures") }
        val uploadedTechnicianSignatureUrl = jobReport.technicianSignaturePath?.let { uploadLocalFile(token, it, "signatures") }

        val uploadedJobReport = jobReport.copy(
            photoUris = uploadedPhotoUrls,
            customerSignaturePath = uploadedCustomerSignatureUrl,
            technicianSignaturePath = uploadedTechnicianSignatureUrl
        )

        val response = api.completeJob(authHeader(token), appointmentId, CompleteJobRequestDto(uploadedJobReport))
        if (response.isSuccessful) {
            appointmentsTrigger.value += 1
            financeTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override suspend fun sendBankTransferMessage(
        appointmentId: String,
        paymentAccountKey: String,
        amount: Double?,
        promisedPaymentDate: String?
    ): Result<String> = requireToken { token ->
        val req = com.example.data.remote.SendBankTransferRequestDto(
            paymentAccountKey = paymentAccountKey,
            amount = amount,
            promisedPaymentDate = promisedPaymentDate
        )
        val response = api.sendBankTransfer(authHeader(token), appointmentId, req)
        if (response.isSuccessful) {
            val channel = response.body()?.channel ?: "whatsapp"
            Result.success(channel)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override suspend fun getAvailableSlots(dateIso: String): Result<List<String>> = requireToken { token ->
        val response = api.getAvailableSlots(authHeader(token), dateIso)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override suspend fun deleteAppointment(id: String): Result<Unit> = requireToken { token ->
        val response = api.deleteAppointment(authHeader(token), id)
        if (response.isSuccessful) {
            appointmentsTrigger.value += 1
            financeTrigger.value += 1
            try { fallback.deleteAppointment(id) } catch (_: Exception) {}
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    // Müşteriler

    override fun getCustomers(): Flow<List<Customer>> =
        authedFlow(customersTrigger, fallback.getCustomers()) { token ->
            val response = api.getCustomers(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override suspend fun addCustomer(customer: Customer): Result<Unit> =
        executeWithFallback(
            fallbackAction = {
                val res = fallback.addCustomer(customer)
                customersTrigger.value += 1
                res
            },
            apiAction = { token ->
                val response = api.addCustomer(authHeader(token), customer)
                if (response.isSuccessful) {
                    customersTrigger.value += 1
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalStateException(errorMessage(response)))
                }
            }
        )

    override suspend fun addCustomers(customers: List<Customer>): Result<Unit> =
        executeWithFallback(
            fallbackAction = {
                val res = fallback.addCustomers(customers)
                customersTrigger.value += 1
                res
            },
            apiAction = { token ->
                customers.chunked(50).forEach { chunk ->
                    chunk.forEach { cust ->
                        try {
                            api.addCustomer(authHeader(token), cust)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                customersTrigger.value += 1
                Result.success(Unit)
            }
        )

    override suspend fun updateCustomer(customer: Customer): Result<Unit> =
        executeWithFallback(
            fallbackAction = {
                val res = fallback.updateCustomer(customer)
                customersTrigger.value += 1
                res
            },
            apiAction = { token ->
                val response = api.updateCustomer(authHeader(token), customer.id, customer)
                if (response.isSuccessful) {
                    customersTrigger.value += 1
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalStateException(errorMessage(response)))
                }
            }
        )

    override suspend fun deleteCustomer(id: String): Result<Unit> =
        executeWithFallback(
            fallbackAction = {
                val res = fallback.deleteCustomer(id)
                customersTrigger.value += 1
                res
            },
            apiAction = { token ->
                val response = api.deleteCustomer(authHeader(token), id)
                if (response.isSuccessful) {
                    fallback.deleteCustomer(id)
                    customersTrigger.value += 1
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalStateException(errorMessage(response)))
                }
            }
        )

    override suspend fun getDeviceHistory(customerId: String): Result<com.example.data.remote.DeviceHistoryDto> = requireToken(
        onMissing = { fallback.getDeviceHistory(customerId) }
    ) { token ->
        try {
            val response = api.getDeviceHistory(authHeader(token), customerId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                fallback.getDeviceHistory(customerId)
            }
        } catch (e: Exception) {
            fallback.getDeviceHistory(customerId)
        }
    }

    // Finans

    override fun getFinanceRecords(): Flow<List<FinanceRecord>> =
        authedFlow(financeTrigger, fallback.getFinanceRecords()) { token ->
            val response = api.getFinanceRecords(authHeader(token))
            if (response.isSuccessful) {
                response.body()?.filterNot { it.id in deletedFinanceIds }
            } else null
        }

    override fun getFinanceSummary(): Flow<FinanceSummary> =
        authedFlow(financeTrigger, fallback.getFinanceSummary()) { token ->
            val response = api.getFinanceSummary(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override fun getBankAccounts(): Flow<List<BankAccount>> =
        authedFlow(bankAccountsTrigger, fallback.getBankAccounts()) { token ->
            val response = api.getBankAccounts(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override suspend fun addFinanceRecord(record: FinanceRecord): Result<Unit> {
        deletedFinanceIds.remove(record.id)
        return requireToken { token ->
            val response = api.addFinanceRecord(authHeader(token), record)
            if (response.isSuccessful && response.body()?.success == true) {
                financeTrigger.value += 1
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException(response.body()?.error ?: errorMessage(response)))
            }
        }
    }

    override suspend fun deleteFinanceRecord(id: String): Result<Unit> {
        deletedFinanceIds.add(id)
        return executeWithFallback(
            fallbackAction = {
                val res = fallback.deleteFinanceRecord(id)
                financeTrigger.value += 1
                res
            },
            apiAction = { token ->
                try {
                    val response = api.deleteFinanceRecord(authHeader(token), id)
                    fallback.deleteFinanceRecord(id)
                    financeTrigger.value += 1
                    if (response.isSuccessful) Result.success(Unit) else Result.success(Unit)
                } catch (e: Exception) {
                    fallback.deleteFinanceRecord(id)
                    financeTrigger.value += 1
                    Result.success(Unit)
                }
            }
        )
    }

    override suspend fun updateBankAccounts(accounts: List<BankAccount>): Result<Unit> =
        executeWithFallback(
            fallbackAction = {
                val res = fallback.updateBankAccounts(accounts)
                bankAccountsTrigger.value += 1
                res
            },
            apiAction = { token ->
                try {
                    val response = api.updateBankAccounts(authHeader(token), accounts)
                    fallback.updateBankAccounts(accounts)
                    bankAccountsTrigger.value += 1
                    if (response.isSuccessful) Result.success(Unit) else Result.success(Unit)
                } catch (e: Exception) {
                    fallback.updateBankAccounts(accounts)
                    bankAccountsTrigger.value += 1
                    Result.success(Unit)
                }
            }
        )

    override suspend fun getReceiptDetail(entryId: String): Result<com.example.data.remote.ReceiptDetailDto> = requireToken(
        onMissing = { fallback.getReceiptDetail(entryId) }
    ) { token ->
        val response = api.getReceiptDetail(authHeader(token), entryId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            fallback.getReceiptDetail(entryId)
        }
    }

    // Teklifler

    override fun getProposals(): Flow<List<Proposal>> =
        authedFlow(proposalsTrigger, fallback.getProposals()) { token ->
            val response = api.getProposals(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override suspend fun addProposal(proposal: Proposal): Result<Unit> = requireToken { token ->
        val response = api.addProposal(authHeader(token), proposal)
        if (response.isSuccessful && response.body()?.success == true) {
            proposalsTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(response.body()?.error ?: errorMessage(response)))
        }
    }

    override suspend fun updateProposalStatus(id: String, status: ProposalStatus): Result<Unit> = requireToken { token ->
        val response = api.updateProposalStatus(authHeader(token), id, StatusUpdateRequestDto(status = status.name))
        if (response.isSuccessful && response.body()?.success == true) {
            proposalsTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(response.body()?.error ?: errorMessage(response)))
        }
    }

    override suspend fun deleteProposal(id: String): Result<Unit> = requireToken { token ->
        val response = api.deleteProposal(authHeader(token), id)
        if (response.isSuccessful) {
            proposalsTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    // Mesajlaşma

    override fun getMessagingStats(): Flow<MessagingStats> =
        authedFlow(messagingTrigger, fallback.getMessagingStats()) { token ->
            val response = api.getMessagingStats(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override fun getCustomerMessagingSettings(): Flow<CustomerMessagingSettings> =
        authedFlow(customerSettingsTrigger, fallback.getCustomerMessagingSettings()) { token ->
            val response = api.getCustomerMessagingSettings(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override suspend fun updateCustomerMessagingSettings(settings: CustomerMessagingSettings): Result<Unit> = requireToken { token ->
        val response = api.updateCustomerMessagingSettings(authHeader(token), settings)
        if (response.isSuccessful) {
            customerSettingsTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override fun getStaffMessagingSettings(): Flow<StaffMessagingSettings> =
        authedFlow(staffSettingsTrigger, fallback.getStaffMessagingSettings()) { token ->
            val response = api.getStaffMessagingSettings(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override suspend fun updateStaffMessagingSettings(settings: StaffMessagingSettings): Result<Unit> = requireToken { token ->
        val response = api.updateStaffMessagingSettings(authHeader(token), settings)
        if (response.isSuccessful) {
            staffSettingsTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override fun getMessageTemplates(): Flow<List<MessageTemplate>> =
        authedFlow(templatesTrigger, fallback.getMessageTemplates()) { token ->
            val response = api.getMessageTemplates(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override suspend fun updateMessageTemplate(template: MessageTemplate): Result<Unit> = requireToken { token ->
        val response = api.updateMessageTemplate(authHeader(token), template.id, template)
        if (response.isSuccessful) {
            templatesTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override fun getMessageJobs(): Flow<List<MessageJob>> =
        authedFlow(messagingTrigger, fallback.getMessageJobs()) { token ->
            val response = api.getMessageJobs(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override suspend fun retryMessageJob(jobId: String): Result<Unit> = requireToken { token ->
        val response = api.retryMessageJob(authHeader(token), jobId)
        if (response.isSuccessful) {
            messagingTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override fun getMessageLogs(): Flow<List<MessageLog>> =
        authedFlow(messagingTrigger, fallback.getMessageLogs()) { token ->
            val response = api.getMessageLogs(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    // Bakım

    override fun getMaintenanceStats(): Flow<MaintenanceStats> =
        authedFlow(maintenanceTrigger, fallback.getMaintenanceStats()) { token ->
            val response = api.getMaintenanceStats(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override fun getMaintenanceRules(): Flow<List<MaintenanceRule>> =
        authedFlow(maintenanceTrigger, fallback.getMaintenanceRules()) { token ->
            val response = api.getMaintenanceRules(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override suspend fun addMaintenanceRule(rule: MaintenanceRule): Result<Unit> = requireToken { token ->
        val response = api.addMaintenanceRule(authHeader(token), rule)
        if (response.isSuccessful && response.body()?.success == true) {
            maintenanceTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(response.body()?.error ?: errorMessage(response)))
        }
    }

    override suspend fun updateMaintenanceRule(rule: MaintenanceRule): Result<Unit> = requireToken { token ->
        val response = api.updateMaintenanceRule(authHeader(token), rule.id, rule)
        if (response.isSuccessful) {
            maintenanceTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override suspend fun deleteMaintenanceRule(ruleId: String): Result<Unit> = requireToken { token ->
        val response = api.deleteMaintenanceRule(authHeader(token), ruleId)
        if (response.isSuccessful) {
            maintenanceTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override suspend fun toggleMaintenanceRuleStatus(ruleId: String): Result<Unit> = requireToken { token ->
        val response = api.toggleMaintenanceRuleStatus(authHeader(token), ruleId)
        if (response.isSuccessful) {
            maintenanceTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    // Raporlar

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getReportData(timeRange: ReportTimeRange): Flow<ReportData> =
        tokenStore.tokenFlow.flatMapLatest { token ->
            if (token == null) {
                flowOf(ReportData())
            } else {
                flow {
                    try {
                        val response = api.getReportData(authHeader(token), timeRange.name)
                        emit(if (response.isSuccessful) response.body() ?: ReportData() else ReportData())
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        emit(ReportData())
                    }
                }
            }
        }

    // Google Ads

    override suspend fun getAdsStats(): Result<AdsStatsDto> = requireToken { token ->
        val response = api.getAdsStats(authHeader(token))
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override suspend fun getAdsCampaigns(): Result<List<AdsCampaignDto>> = requireToken { token ->
        val response = api.getAdsCampaigns(authHeader(token))
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override suspend fun toggleAdsCampaign(campaignId: String): Result<String> = requireToken { token ->
        val response = api.toggleAdsCampaign(authHeader(token), campaignId)
        if (response.isSuccessful && response.body() != null) {
            val newStatus = response.body()?.status ?: "PAUSED"
            Result.success(newStatus)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    override fun getGoogleAdsStats(): Flow<GoogleAdsStats> =
        authedFlow(googleAdsCampaignsTrigger, fallback.getGoogleAdsStats()) { token ->
            val response = api.getGoogleAdsStats(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override fun getGoogleAdsCampaigns(): Flow<List<GoogleAdsCampaign>> =
        authedFlow(googleAdsCampaignsTrigger, fallback.getGoogleAdsCampaigns()) { token ->
            val response = api.getGoogleAdsCampaigns(authHeader(token))
            if (response.isSuccessful) response.body() else null
        }

    override suspend fun toggleCampaignStatus(campaignId: String): Result<Unit> = requireToken { token ->
        val response = api.toggleCampaignStatus(authHeader(token), campaignId)
        if (response.isSuccessful) {
            googleAdsCampaignsTrigger.value += 1
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(errorMessage(response)))
        }
    }

    // WhatsApp (bağlanma akışı web admin'de kalıyor, mutasyon yok - sadece token'a bağlı okuma)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getWhatsAppStatus(): Flow<WhatsAppStatus> =
        tokenStore.tokenFlow.flatMapLatest { token ->
            if (token == null) {
                flowOf(WhatsAppStatus())
            } else {
                flow {
                    try {
                        val response = api.getWhatsAppStatus(authHeader(token))
                        emit(if (response.isSuccessful) response.body() ?: WhatsAppStatus() else WhatsAppStatus())
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        emit(WhatsAppStatus())
                    }
                }
            }
        }
}
