package com.example.data.remote

import com.example.data.model.Appointment
import com.example.data.model.BankAccount
import com.example.data.model.Customer
import com.example.data.model.CustomerMessagingSettings
import com.example.data.model.FinanceRecord
import com.example.data.model.FinanceSummary
import com.example.data.model.GoogleAdsCampaign
import com.example.data.model.GoogleAdsStats
import com.example.data.model.MaintenanceRule
import com.example.data.model.MaintenanceStats
import com.example.data.model.MessageJob
import com.example.data.model.MessageLog
import com.example.data.model.MessageTemplate
import com.example.data.model.MessagingStats
import com.example.data.model.Proposal
import com.example.data.model.ReportData
import com.example.data.model.StaffMessagingSettings
import com.example.data.model.WhatsAppStatus
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApiService {
    @POST("api/admin/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @GET("api/admin/dashboard")
    suspend fun getDashboardStats(@Header("Authorization") auth: String): Response<DashboardStatsDto>

    @Multipart
    @POST("api/admin/upload")
    suspend fun uploadFile(
        @Header("Authorization") auth: String,
        @Part file: MultipartBody.Part,
        @Part("folder") folder: RequestBody
    ): Response<UploadResponseDto>

    // Randevular
    @GET("api/admin/appointments")
    suspend fun getAppointments(@Header("Authorization") auth: String): Response<List<Appointment>>

    @POST("api/admin/appointments")
    suspend fun addAppointment(@Header("Authorization") auth: String, @Body appointment: Appointment): Response<SuccessResponseDto>

    @PATCH("api/admin/appointments/{id}")
    suspend fun updateAppointment(@Header("Authorization") auth: String, @Path("id") id: String, @Body appointment: Appointment): Response<SuccessResponseDto>

    @PATCH("api/admin/appointments/{id}/status")
    suspend fun updateAppointmentStatus(@Header("Authorization") auth: String, @Path("id") id: String, @Body body: StatusUpdateRequestDto): Response<SuccessResponseDto>

    @POST("api/admin/appointments/{id}/complete")
    suspend fun completeJob(@Header("Authorization") auth: String, @Path("id") id: String, @Body body: CompleteJobRequestDto): Response<SuccessResponseDto>

    @POST("api/admin/appointments/{id}/send-bank-transfer")
    suspend fun sendBankTransfer(
        @Header("Authorization") auth: String,
        @Path("id") id: String,
        @Body body: SendBankTransferRequestDto
    ): Response<SuccessResponseDto>

    @GET("api/admin/appointments/available-slots")
    suspend fun getAvailableSlots(
        @Header("Authorization") auth: String,
        @Query("date") date: String
    ): Response<List<String>>

    @DELETE("api/admin/appointments/{id}")
    suspend fun deleteAppointment(@Header("Authorization") auth: String, @Path("id") id: String): Response<SuccessResponseDto>

    // Müşteriler
    @GET("api/admin/customers")
    suspend fun getCustomers(@Header("Authorization") auth: String, @Query("search") search: String? = null): Response<List<Customer>>

    @POST("api/admin/customers")
    suspend fun addCustomer(@Header("Authorization") auth: String, @Body customer: Customer): Response<SuccessResponseDto>

    @PATCH("api/admin/customers/{id}")
    suspend fun updateCustomer(@Header("Authorization") auth: String, @Path("id") id: String, @Body customer: Customer): Response<SuccessResponseDto>

    @GET("api/admin/customers/{id}/device-history")
    suspend fun getDeviceHistory(@Header("Authorization") auth: String, @Path("id") id: String): Response<DeviceHistoryDto>

    // Finans
    @GET("api/admin/finance/records")
    suspend fun getFinanceRecords(@Header("Authorization") auth: String): Response<List<FinanceRecord>>

    @GET("api/admin/finance/receipt/{entryId}")
    suspend fun getReceiptDetail(
        @Header("Authorization") auth: String,
        @Path("entryId") entryId: String
    ): Response<ReceiptDetailDto>

    @POST("api/admin/finance/records")
    suspend fun addFinanceRecord(@Header("Authorization") auth: String, @Body record: FinanceRecord): Response<FinanceRecordCreateResponseDto>

    @DELETE("api/admin/finance/records/{id}")
    suspend fun deleteFinanceRecord(@Header("Authorization") auth: String, @Path("id") id: String): Response<SuccessResponseDto>

    @GET("api/admin/finance/summary")
    suspend fun getFinanceSummary(@Header("Authorization") auth: String): Response<FinanceSummary>

    @GET("api/admin/finance/bank-accounts")
    suspend fun getBankAccounts(@Header("Authorization") auth: String): Response<List<BankAccount>>

    @PUT("api/admin/finance/bank-accounts")
    suspend fun updateBankAccounts(@Header("Authorization") auth: String, @Body accounts: List<BankAccount>): Response<List<BankAccount>>

    // Teklifler
    @GET("api/admin/quotes")
    suspend fun getProposals(@Header("Authorization") auth: String): Response<List<Proposal>>

    @POST("api/admin/quotes")
    suspend fun addProposal(@Header("Authorization") auth: String, @Body proposal: Proposal): Response<ProposalCreateResponseDto>

    @PATCH("api/admin/quotes/{id}/status")
    suspend fun updateProposalStatus(@Header("Authorization") auth: String, @Path("id") id: String, @Body body: StatusUpdateRequestDto): Response<ProposalCreateResponseDto>

    @DELETE("api/admin/quotes/{id}")
    suspend fun deleteProposal(@Header("Authorization") auth: String, @Path("id") id: String): Response<SuccessResponseDto>

    // Mesajlaşma
    @GET("api/admin/messages/stats")
    suspend fun getMessagingStats(@Header("Authorization") auth: String): Response<MessagingStats>

    @GET("api/admin/messages/settings/customer")
    suspend fun getCustomerMessagingSettings(@Header("Authorization") auth: String): Response<CustomerMessagingSettings>

    @PUT("api/admin/messages/settings/customer")
    suspend fun updateCustomerMessagingSettings(@Header("Authorization") auth: String, @Body settings: CustomerMessagingSettings): Response<CustomerMessagingSettings>

    @GET("api/admin/messages/settings/staff")
    suspend fun getStaffMessagingSettings(@Header("Authorization") auth: String): Response<StaffMessagingSettings>

    @PUT("api/admin/messages/settings/staff")
    suspend fun updateStaffMessagingSettings(@Header("Authorization") auth: String, @Body settings: StaffMessagingSettings): Response<StaffMessagingSettings>

    @GET("api/admin/messages/templates")
    suspend fun getMessageTemplates(@Header("Authorization") auth: String): Response<List<MessageTemplate>>

    @PUT("api/admin/messages/templates/{id}")
    suspend fun updateMessageTemplate(@Header("Authorization") auth: String, @Path("id") id: String, @Body template: MessageTemplate): Response<MessageTemplate>

    @GET("api/admin/messages/jobs")
    suspend fun getMessageJobs(@Header("Authorization") auth: String): Response<List<MessageJob>>

    @POST("api/admin/messages/jobs/{id}/retry")
    suspend fun retryMessageJob(@Header("Authorization") auth: String, @Path("id") id: String): Response<SuccessResponseDto>

    @GET("api/admin/messages/logs")
    suspend fun getMessageLogs(@Header("Authorization") auth: String): Response<List<MessageLog>>

    // Bakım
    @GET("api/admin/maintenance/stats")
    suspend fun getMaintenanceStats(@Header("Authorization") auth: String): Response<MaintenanceStats>

    @GET("api/admin/maintenance/rules")
    suspend fun getMaintenanceRules(@Header("Authorization") auth: String): Response<List<MaintenanceRule>>

    @POST("api/admin/maintenance/rules")
    suspend fun addMaintenanceRule(@Header("Authorization") auth: String, @Body rule: MaintenanceRule): Response<MaintenanceRuleCreateResponseDto>

    @PATCH("api/admin/maintenance/rules/{id}")
    suspend fun updateMaintenanceRule(@Header("Authorization") auth: String, @Path("id") id: String, @Body rule: MaintenanceRule): Response<SuccessResponseDto>

    @DELETE("api/admin/maintenance/rules/{id}")
    suspend fun deleteMaintenanceRule(@Header("Authorization") auth: String, @Path("id") id: String): Response<SuccessResponseDto>

    @POST("api/admin/maintenance/rules/{id}/toggle")
    suspend fun toggleMaintenanceRuleStatus(@Header("Authorization") auth: String, @Path("id") id: String): Response<SuccessResponseDto>

    // Raporlar
    @GET("api/admin/reports")
    suspend fun getReportData(@Header("Authorization") auth: String, @Query("range") range: String): Response<ReportData>

    // Google Ads
    @GET("api/admin/ads/stats")
    suspend fun getAdsStats(@Header("Authorization") auth: String): Response<AdsStatsDto>

    @GET("api/admin/ads/campaigns")
    suspend fun getAdsCampaigns(@Header("Authorization") auth: String): Response<List<AdsCampaignDto>>

    @POST("api/admin/ads/campaigns/{id}/toggle")
    suspend fun toggleAdsCampaign(@Header("Authorization") auth: String, @Path("id") campaignId: String): Response<AdsToggleResponseDto>

    @GET("api/admin/ads/stats")
    suspend fun getGoogleAdsStats(@Header("Authorization") auth: String): Response<GoogleAdsStats>

    @GET("api/admin/ads/campaigns")
    suspend fun getGoogleAdsCampaigns(@Header("Authorization") auth: String): Response<List<GoogleAdsCampaign>>

    @POST("api/admin/ads/campaigns/{id}/toggle")
    suspend fun toggleCampaignStatus(@Header("Authorization") auth: String, @Path("id") id: String): Response<SuccessResponseDto>

    // WhatsApp
    @GET("api/admin/whatsapp/status")
    suspend fun getWhatsAppStatus(@Header("Authorization") auth: String): Response<WhatsAppStatus>
}
