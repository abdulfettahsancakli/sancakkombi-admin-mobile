package com.example.data.repository

import com.example.data.model.Appointment
import com.example.data.model.AppointmentStatus
import com.example.data.model.BankAccount
import com.example.data.model.Customer
import com.example.data.model.DashboardStats
import com.example.data.model.FinanceRecord
import com.example.data.model.FinanceSummary
import com.example.data.model.FinanceType
import com.example.data.model.JobReport
import com.example.data.model.Proposal
import com.example.data.model.ProposalItem
import com.example.data.model.ProposalStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

import com.example.data.model.CustomerMessagingSettings
import com.example.data.model.MaintenanceRule
import com.example.data.model.MaintenanceStats
import com.example.data.model.MessageJob
import com.example.data.model.MessageLog
import com.example.data.model.MessageTemplate
import com.example.data.model.MessagingStats
import com.example.data.model.StaffMessagingSettings

class MockAdminRepositoryImpl : AdminRepository {

    private val initialCustomers = listOf(
        Customer(
            id = "c1",
            name = "Fettah Sancaklı",
            phone = "0541 328 06 98",
            district = "Bayrampaşa",
            address = "Yıldırım Mah. Ardıç Sokak",
            appointmentCount = 1,
            activeAppointmentCount = 1,
            notes = "Daha önce kombi arızası için aradı."
        ),
        Customer(
            id = "c2",
            name = "Fatih",
            phone = "0535 555 46 26",
            district = "Bayrampaşa",
            address = "Yıldırım Mah. Ardınç Sokağı 13",
            appointmentCount = 0,
            activeAppointmentCount = 0
        ),
        Customer(
            id = "c3",
            name = "Test Ad Soyad",
            phone = "0555 555 55 55",
            district = "Bayrampaşa",
            address = "Kartaltepe Mah. no:5",
            appointmentCount = 0,
            activeAppointmentCount = 0
        ),
        Customer(
            id = "c4",
            name = "Fatime",
            phone = "0537 691 73 61",
            district = "Bayrampaşa",
            address = "Muratpaşa Mah.",
            appointmentCount = 0,
            activeAppointmentCount = 0
        ),
        Customer(
            id = "c5",
            name = "Fatih Sancaklı",
            phone = "0539 267 77 00",
            district = "Bayrampaşa",
            address = "Cevatpaşa Mah.",
            appointmentCount = 0,
            activeAppointmentCount = 0
        ),
        Customer(
            id = "c6",
            name = "Ahmet Yılmaz",
            phone = "0212 581 75 74",
            district = "Bayrampaşa",
            address = "Kocatepe Mah. Şehir Parkı Cad.",
            appointmentCount = 2,
            activeAppointmentCount = 0
        )
    )

    private val initialAppointments = listOf(
        Appointment(
            id = "a1",
            customerId = "c1",
            customerName = "Fettah Sancaklı",
            phone = "0541 328 06 98",
            email = "",
            district = "Bayrampaşa",
            neighborhood = "Yıldırım Mah.",
            streetDoorNo = "Ardıç Sokak",
            date = "03.08.2026",
            timeSlot = "13:00 - 15:00",
            serviceType = "Kombi Bakım & Servis",
            status = AppointmentStatus.ONAYLANDI,
            addressDetail = "Yıldırım Mah. Ardıç Sokak",
            problemNote = "It doesnot work"
        )
    )

    private val initialBankAccounts = listOf(
        BankAccount(
            id = "b1",
            cardTitle = "Fatih Sancaklı",
            accountHolder = "Fatih Sancaklı",
            bankName = "YAPI KREDİ",
            iban = "TR12 0006 2000 0001 0000 0123 457"
        ),
        BankAccount(
            id = "b2",
            cardTitle = "Kart Başlığı",
            accountHolder = "Abdulfettah Sancaklı",
            bankName = "AKBANK",
            iban = "TR33 0001 5001 5008 0073 0298 7654"
        ),
        BankAccount(
            id = "b3",
            cardTitle = "Kart Başlığı",
            accountHolder = "Abdullah Sancaklı",
            bankName = "KUVEYT TÜRK",
            iban = "TR64 0010 0002 0000 0123 4567 890"
        )
    )

    private val initialFinanceRecords = listOf(
        FinanceRecord(
            id = "f1",
            date = "5.06.2026",
            type = FinanceType.GELIR,
            amount = 1000.0,
            status = "Ödendi",
            source = "Parça Satışı",
            receiptNo = "SK-202606-6A6F7A"
        ),
        FinanceRecord(
            id = "f2",
            date = "28.05.2026",
            type = FinanceType.GELIR,
            amount = 1370.0,
            status = "Ödendi",
            source = "-",
            note = "Ateşleyici elektrodu + O-Ring takımı + Servis Bedeli",
            receiptNo = "SK-202605-28A6F1"
        ),
        FinanceRecord(
            id = "f3",
            date = "25.05.2026",
            type = FinanceType.GELIR,
            amount = 1000.0,
            status = "Ödendi",
            source = "Fettah Sancaklı (Servis)",
            receiptNo = "SK-202605-25B3C2"
        ),
        FinanceRecord(
            id = "f4",
            date = "18.05.2026",
            type = FinanceType.GELIR,
            amount = 1000.0,
            status = "Ödendi",
            source = "Fettah Sancaklı (Servis)",
            receiptNo = "SK-202605-18D4E5"
        ),
        FinanceRecord(
            id = "f5",
            date = "18.05.2026",
            type = FinanceType.GELIR,
            amount = 1000.0,
            status = "Ödendi",
            source = "Fettah Sancaklı (Servis)",
            receiptNo = "SK-202605-18F6G7"
        ),
        FinanceRecord(
            id = "f6",
            date = "18.05.2026",
            type = FinanceType.GELIR,
            amount = 500.0,
            status = "Ödendi",
            source = "Fettah Sancaklı (Servis)",
            receiptNo = "SK-202605-18H8I9"
        ),
        FinanceRecord(
            id = "f7",
            date = "18.05.2026",
            type = FinanceType.GIDER,
            amount = 500.0,
            status = "Ödendi",
            source = "Fettah Sancaklı (Servis Malzeme Alımı)",
            receiptNo = "SK-202605-18J0K1"
        ),
        FinanceRecord(
            id = "f8",
            date = "18.05.2026",
            type = FinanceType.GELIR,
            amount = 1000.0,
            status = "Ödendi",
            source = "Fettah Sancaklı (Servis)",
            receiptNo = "SK-202605-18L2M3"
        ),
        FinanceRecord(
            id = "f9",
            date = "18.05.2026",
            type = FinanceType.GELIR,
            amount = 500.0,
            status = "Kısmi",
            source = "Fettah Sancaklı",
            totalAmount = 1000.0,
            collectedAmount = 500.0,
            receiptNo = "SK-202605-18N4O5"
        )
    )

    private val initialProposals = listOf(
        Proposal(
            id = "TF-202608-024EFE",
            customerName = "Ömer Yazan",
            customerPhone = "0546 254 77 43",
            customerEmail = "",
            customerDistrict = "Bayrampaşa",
            customerAddress = "Bayrampaşa, Kocatepe Mahallesi 6.Sokak No: 19 Daire : 1 Bayrampaşa/İstanbul",
            deviceBrand = "Demirdöküm",
            deviceModel = "Nitromix P24",
            date = "4 Ağustos 2026",
            validUntilDate = "18.08.2026",
            preparedBy = "Fatih Sancaklı",
            status = ProposalStatus.PENDING,
            items = listOf(
                ProposalItem("p1", "Doğalgaz İç Tesisatı ve Proje Hazırlanması", 1, 20000.0),
                ProposalItem("p2", "Kalorifer Tesisatı (1400 cm + 1200 cm + 1000 cm Panel Radyatör ve 50*70 Havlupan Dahil)", 1, 18000.0),
                ProposalItem("p3", "Komple Revizyonlu 2. El Kombi (Montaj Dahil)", 1, 15000.0),
                ProposalItem("p4", "Su (Sıcak/Soğuk) Tesisatı", 1, 5000.0),
                ProposalItem("p5", "Su Sayaç Bağlantı Takımı ve Küresel Vanalar", 1, 2000.0),
                ProposalItem("p6", "Kombi Baca Çıkışı İçin Cam Delimi ve Montajı", 1, 0.0),
                ProposalItem("p7", "Mini Lavabo ve Musluk Montajı", 1, 0.0),
                ProposalItem("p8", "Kombi Elektrik Besleme Hattı ve Sigorta Montajı", 1, 0.0)
            ),
            downPayment = 25000.0,
            remainingPaymentType = "Kredi Kartı (Tek Çekim)"
        )
    )

    private val _customers = MutableStateFlow(initialCustomers)
    private val _appointments = MutableStateFlow(initialAppointments)
    private val _bankAccounts = MutableStateFlow(initialBankAccounts)
    private val _financeRecords = MutableStateFlow(initialFinanceRecords)
    private val _proposals = MutableStateFlow(initialProposals)

    private val initialMessageTemplates = listOf(
        MessageTemplate(
            id = "t1",
            title = "Randevu Onay Mesajı",
            tag = "Onay SMS",
            category = "MUSTERI",
            templateText = "Sancak Kombi\nMerhaba {musteri_adi},\nRandevu bilgileriniz aşağıdaki gibidir.\nHizmet: {hizmet}\nTarih: {tarih}\nSaat: {saat}\nİlçe: {ilce}\nAdres: {adres}\nBilgi: 0539 267 77 00\nKonum için aşağıdaki linke tıklayın:\nhttps://maps.google.com/?q={konum}"
        ),
        MessageTemplate(
            id = "t2",
            title = "Randevu Güncelleme Mesajı",
            tag = "Güncelleme Bilgisi",
            category = "MUSTERI",
            templateText = "Sancak Kombi\nMerhaba {musteri_adi}, randevu bilgileriniz güncellenmiştir.\nHizmet: {hizmet}\nYeni Tarih: {tarih} - {saat}"
        ),
        MessageTemplate(
            id = "t3",
            title = "Randevu Hatırlatma Mesajı",
            tag = "Randevu Hatırlatması",
            category = "MUSTERI",
            templateText = "Sancak Kombi\nSayın {musteri_adi}, bugün {saat} saatleri arasındaki randevunuzu hatırlatmak isteriz."
        ),
        MessageTemplate(
            id = "t4",
            title = "Randevu İptal Mesajı",
            tag = "İptal Bilgisi",
            category = "MUSTERI",
            templateText = "Sancak Kombi\nSayın {musteri_adi}, {tarih} tarihli randevunuz iptal edilmiştir."
        ),
        MessageTemplate(
            id = "t5",
            title = "Randevu Tamamlandı Mesajı",
            tag = "Tamamlandı Bilgisi",
            category = "MUSTERI",
            templateText = "Sancak Kombi\nSayın {musteri_adi}, servisimiz tamamlanmıştır. Bizi tercih ettiğiniz için teşekkür ederiz."
        ),
        MessageTemplate(
            id = "t6",
            title = "Havale / EFT Ödeme Bilgisi",
            tag = "Havale / EFT Bilgisi",
            category = "MUSTERI",
            templateText = "Sancak Kombi\nIBAN: TR12 0006 2000 0001 0000 0123 457\nAlıcı: Sancak Kombi Isı Sistemleri"
        ),
        MessageTemplate(
            id = "t7",
            title = "Değerlendirme Mesajı",
            tag = "Değerlendirme Mesajı",
            category = "MUSTERI",
            templateText = "Sancak Kombi\nHizmetimizi değerlendirmek için lütfen bağlantıya tıklayın: https://sancakkombi.com/degerlendir"
        ),
        MessageTemplate(
            id = "t8",
            title = "Bakım Hatırlatma Mesajı",
            tag = "Bakım Hatırlatması",
            category = "MUSTERI",
            templateText = "Sancak Kombi\nSayın {musteri_adi}, cihazınızın periyodik bakım zamanı gelmiştir. Randevu almak için bize ulaşabilirsiniz."
        ),
        MessageTemplate(
            id = "t9",
            title = "Yeni Randevu Bildirimi",
            tag = "Yeni İş",
            category = "USTA",
            templateText = "Sancak Kombi - Usta Bildirimi\nYeni Randevu Atandı:\nMüşteri: {musteri_adi}\nTelefon: {telefon}\nAdres: {adres}\nHizmet: {hizmet}\nTarih: {tarih} {saat}"
        ),
        MessageTemplate(
            id = "t10",
            title = "Usta Randevu Hatırlatması",
            tag = "Hatırlatma",
            category = "USTA",
            templateText = "Hatırlatma: Bugün saat {saat} için {musteri_adi} adresine randevunuz bulunmaktadır."
        ),
        MessageTemplate(
            id = "t11",
            title = "Günlük Randevu Özeti",
            tag = "Günlük Özet",
            category = "USTA",
            templateText = "Sancak Kombi - Bugün için tanımlı toplam {randevu_sayisi} randevunuz bulunmaktadır."
        )
    )

    private val initialMessageJobs = listOf(
        MessageJob(
            id = "j1",
            template = "Randevu Hatırlatması",
            channel = "WHATSAPP",
            time = "05.08.2026 10:15",
            status = "FAILED",
            error = "Zaman aşımı / Sunucu hatası",
            recipient = "0541 328 06 98"
        )
    )

    private val initialMessageLogs = listOf(
        MessageLog(
            id = "l1",
            time = "05.08.2026 12:38",
            channel = "WHATSAPP",
            template = "Günlük Randevu Özeti",
            recipient = "0539 267 77 00",
            status = "sent",
            provider = "meta_whatsapp_cloud"
        ),
        MessageLog(
            id = "l2",
            time = "05.08.2026 09:00",
            channel = "SMS",
            template = "Randevu Onay Mesajı",
            recipient = "0541 328 06 98",
            status = "sent",
            provider = "verimor_sms"
        ),
        MessageLog(
            id = "l3",
            time = "04.08.2026 16:20",
            channel = "WHATSAPP",
            template = "Bakım Hatırlatması",
            recipient = "0535 555 46 26",
            status = "skipped",
            provider = "meta_whatsapp_cloud"
        )
    )

    private val initialMaintenanceRules = listOf(
        MaintenanceRule(
            id = "m1",
            customerName = "Fettah Sancaklı",
            serviceType = "Genel Servis",
            status = "Aktif",
            nextReminderDate = "13.04.2027",
            intervalMonths = 12,
            channel = "WhatsApp"
        ),
        MaintenanceRule(
            id = "m2",
            customerName = "Fettah Sancaklı",
            serviceType = "doğalgaz-tesisatı",
            status = "Aktif",
            nextReminderDate = "13.04.2027",
            intervalMonths = 12,
            channel = "WhatsApp"
        ),
        MaintenanceRule(
            id = "m3",
            customerName = "Fettah Sancaklı",
            serviceType = "Kombi Bakımı",
            status = "Aktif",
            nextReminderDate = "20.04.2027",
            intervalMonths = 12,
            channel = "WhatsApp"
        )
    )

    private val _messagingStats = MutableStateFlow(MessagingStats(gonderildiCount = 142, bekleyenCount = 0, basarisizCount = 1))
    private val _customerMessagingSettings = MutableStateFlow(CustomerMessagingSettings())
    private val _staffMessagingSettings = MutableStateFlow(StaffMessagingSettings())
    private val _messageTemplates = MutableStateFlow(initialMessageTemplates)
    private val _messageJobs = MutableStateFlow(initialMessageJobs)
    private val _messageLogs = MutableStateFlow(initialMessageLogs)

    private val _maintenanceStats = MutableStateFlow(MaintenanceStats(activeRulesCount = 3, within30DaysCount = 0, overdueCount = 0))
    private val _maintenanceRules = MutableStateFlow(initialMaintenanceRules)

    private val _stats = MutableStateFlow(
        DashboardStats(
            bugunkuRandevu = 1,
            bekleyenOnay = 0,
            buHaftaTamamlanan = 0,
            acikAlacak = "₺500,00",
            buAyServis = 1,
            buAyGelir = "₺8.870,00"
        )
    )

    private val _whatsAppConnected = MutableStateFlow(true)

    private fun recalculateStats() {
        val list = _appointments.value
        val todayCount = list.count { it.status == AppointmentStatus.ONAYLANDI || it.status == AppointmentStatus.BEKLIYOR }
        val pendingCount = list.count { it.status == AppointmentStatus.BEKLIYOR }
        val completedCount = list.count { it.status == AppointmentStatus.TAMAMLANDI }
        
        val totalRevenue = _financeRecords.value
            .filter { it.type == FinanceType.GELIR }
            .sumOf { it.amount }

        _stats.value = DashboardStats(
            bugunkuRandevu = todayCount,
            bekleyenOnay = pendingCount,
            buHaftaTamamlanan = completedCount,
            acikAlacak = "₺500,00",
            buAyServis = list.size,
            buAyGelir = "₺%.2f".format(totalRevenue).replace(".", ",")
        )
    }

    override suspend fun login(password: String): Result<String> {
        delay(300)
        val mockToken = "sk_token_" + UUID.randomUUID().toString().take(16)
        return Result.success(mockToken)
    }

    override suspend fun logout() {}

    override fun refreshAll() {}

    override fun getDashboardStats(): Flow<DashboardStats> = _stats.asStateFlow()
    override fun getWhatsAppConnected(): Flow<Boolean> = _whatsAppConnected.asStateFlow()

    override fun getAppointments(): Flow<List<Appointment>> = _appointments.asStateFlow()

    override suspend fun addAppointment(appointment: Appointment): Result<Unit> {
        delay(300)
        val newList = _appointments.value.toMutableList()
        newList.add(0, appointment)
        _appointments.value = newList

        // Update active appointment count for customer
        val custs = _customers.value.toMutableList()
        val index = custs.indexOfFirst { it.id == appointment.customerId || it.name.equals(appointment.customerName, ignoreCase = true) }
        if (index != -1) {
            val c = custs[index]
            custs[index] = c.copy(
                appointmentCount = c.appointmentCount + 1,
                activeAppointmentCount = c.activeAppointmentCount + 1
            )
            _customers.value = custs
        } else {
            // Auto create customer if not found
            val newC = Customer(
                id = UUID.randomUUID().toString(),
                name = appointment.customerName,
                phone = appointment.phone,
                district = appointment.district,
                address = appointment.addressDetail,
                appointmentCount = 1,
                activeAppointmentCount = 1
            )
            custs.add(0, newC)
            _customers.value = custs
        }

        recalculateStats()
        return Result.success(Unit)
    }

    override suspend fun updateAppointment(appointment: Appointment): Result<Unit> {
        delay(300)
        val newList = _appointments.value.toMutableList()
        val index = newList.indexOfFirst { it.id == appointment.id }
        if (index != -1) {
            newList[index] = appointment
            _appointments.value = newList
            recalculateStats()
            return Result.success(Unit)
        }
        return Result.failure(IllegalArgumentException("Randevu bulunamadı."))
    }

    override suspend fun updateAppointmentStatus(id: String, status: AppointmentStatus): Result<Unit> {
        delay(200)
        val newList = _appointments.value.toMutableList()
        val index = newList.indexOfFirst { it.id == id }
        if (index != -1) {
            newList[index] = newList[index].copy(status = status)
            _appointments.value = newList
            recalculateStats()
            return Result.success(Unit)
        }
        return Result.failure(IllegalArgumentException("Randevu bulunamadı."))
    }

    override suspend fun completeJob(appointmentId: String, jobReport: JobReport): Result<Unit> {
        delay(500)
        val newList = _appointments.value.toMutableList()
        val index = newList.indexOfFirst { it.id == appointmentId }
        if (index != -1) {
            val updated = newList[index].copy(
                status = AppointmentStatus.TAMAMLANDI,
                jobReport = jobReport
            )
            newList[index] = updated
            _appointments.value = newList

            // Update active count on customer
            val custs = _customers.value.toMutableList()
            val cIndex = custs.indexOfFirst { it.id == updated.customerId || it.name.equals(updated.customerName, ignoreCase = true) }
            if (cIndex != -1) {
                val c = custs[cIndex]
                custs[cIndex] = c.copy(
                    activeAppointmentCount = (c.activeAppointmentCount - 1).coerceAtLeast(0)
                )
                _customers.value = custs
            }

            recalculateStats()
            return Result.success(Unit)
        }
        return Result.failure(IllegalArgumentException("Randevu bulunamadı."))
    }

    override suspend fun sendBankTransferMessage(
        appointmentId: String,
        paymentAccountKey: String,
        amount: Double?,
        promisedPaymentDate: String?
    ): Result<String> {
        delay(300)
        return Result.success("whatsapp")
    }

    override suspend fun getAvailableSlots(dateIso: String): Result<List<String>> {
        delay(200)
        return Result.success(listOf("09:00 - 11:00", "13:00 - 15:00", "17:00 - 19:00"))
    }

    override suspend fun deleteAppointment(id: String): Result<Unit> {
        delay(300)
        val newList = _appointments.value.toMutableList()
        newList.removeAll { it.id == id }
        _appointments.value = newList
        recalculateStats()
        return Result.success(Unit)
    }

    override fun getCustomers(): Flow<List<Customer>> = _customers.asStateFlow()

    override suspend fun addCustomer(customer: Customer): Result<Unit> {
        delay(300)
        val newList = _customers.value.toMutableList()
        newList.add(0, customer)
        _customers.value = newList
        return Result.success(Unit)
    }

    override suspend fun updateCustomer(customer: Customer): Result<Unit> {
        delay(300)
        val newList = _customers.value.toMutableList()
        val index = newList.indexOfFirst { it.id == customer.id }
        if (index != -1) {
            newList[index] = customer
            _customers.value = newList
            return Result.success(Unit)
        }
        return Result.failure(IllegalArgumentException("Müşteri bulunamadı."))
    }

    // Finance Implementations
    override fun getFinanceRecords(): Flow<List<FinanceRecord>> = _financeRecords.asStateFlow()

    override fun getFinanceSummary(): Flow<FinanceSummary> {
        val totalInc = _financeRecords.value.filter { it.type == FinanceType.GELIR }.sumOf { it.amount }
        val totalExp = _financeRecords.value.filter { it.type == FinanceType.GIDER }.sumOf { it.amount }
        return MutableStateFlow(
            FinanceSummary(
                totalIncome = if (totalInc == 0.0) 8870.0 else totalInc,
                totalExpense = if (totalExp == 0.0) 500.0 else totalExp,
                outstandingReceivable = 500.0
            )
        ).asStateFlow()
    }

    override fun getBankAccounts(): Flow<List<BankAccount>> = _bankAccounts.asStateFlow()

    override suspend fun addFinanceRecord(record: FinanceRecord): Result<Unit> {
        delay(300)
        val newList = _financeRecords.value.toMutableList()
        newList.add(0, record)
        _financeRecords.value = newList
        recalculateStats()
        return Result.success(Unit)
    }

    override suspend fun updateBankAccounts(accounts: List<BankAccount>): Result<Unit> {
        delay(300)
        _bankAccounts.value = accounts
        return Result.success(Unit)
    }

    override suspend fun getReceiptDetail(entryId: String): Result<com.example.data.remote.ReceiptDetailDto> {
        delay(200)
        val rec = _financeRecords.value.find { it.id == entryId }
        val detail = com.example.data.remote.ReceiptDetailDto(
            entryId = entryId,
            receiptNo = rec?.receiptNo ?: if (entryId.isNotBlank()) "SK-202608-${entryId.take(6).uppercase()}" else "SK-202608-6A6F7A",
            date = rec?.date ?: "10.08.2026",
            amount = rec?.amount ?: 1000.0,
            paymentMethod = "Nakit",
            status = if (rec?.status == "Ödendi") "paid" else "unpaid",
            customerName = rec?.source ?: "Fettah Sancaklı",
            customerPhone = "0537 691 73 61",
            customerAddress = "Kocatepe Mah. 6. Sokak No: 19 D:1",
            customerDistrict = "Bayrampaşa",
            deviceBrand = "Demirdöküm",
            deviceModel = "Nitromix P24",
            deviceTested = true,
            workDescription = if (rec?.note?.isNotBlank() == true) rec.note else "Kombi genleşme tankı hava basıncı kontrol edildi. Ateşleyici elektrot temizliği ve O-ring conta değişimi yapıldı. Sızdırmazlık testi başarıyla tamamlandı.",
            warrantyMonths = 12,
            serviceTitle = "Kombi Periyodik Bakım & Arıza Onarım Servis Fişi"
        )
        return Result.success(detail)
    }

    // Proposal Implementations
    override fun getProposals(): Flow<List<Proposal>> = _proposals.asStateFlow()

    override suspend fun addProposal(proposal: Proposal): Result<Unit> {
        delay(300)
        val newList = _proposals.value.toMutableList()
        newList.add(0, proposal)
        _proposals.value = newList
        return Result.success(Unit)
    }

    override suspend fun updateProposalStatus(id: String, status: ProposalStatus): Result<Unit> {
        delay(200)
        val newList = _proposals.value.toMutableList()
        val index = newList.indexOfFirst { it.id == id }
        if (index != -1) {
            newList[index] = newList[index].copy(status = status)
            _proposals.value = newList
            return Result.success(Unit)
        }
        return Result.failure(IllegalArgumentException("Teklif bulunamadı."))
    }

    override suspend fun deleteProposal(id: String): Result<Unit> {
        delay(300)
        val newList = _proposals.value.toMutableList()
        newList.removeAll { it.id == id }
        _proposals.value = newList
        return Result.success(Unit)
    }

    // Messaging Implementations
    override fun getMessagingStats(): Flow<MessagingStats> = _messagingStats.asStateFlow()
    override fun getCustomerMessagingSettings(): Flow<CustomerMessagingSettings> = _customerMessagingSettings.asStateFlow()
    override suspend fun updateCustomerMessagingSettings(settings: CustomerMessagingSettings): Result<Unit> {
        delay(200)
        _customerMessagingSettings.value = settings
        return Result.success(Unit)
    }
    override fun getStaffMessagingSettings(): Flow<StaffMessagingSettings> = _staffMessagingSettings.asStateFlow()
    override suspend fun updateStaffMessagingSettings(settings: StaffMessagingSettings): Result<Unit> {
        delay(200)
        _staffMessagingSettings.value = settings
        return Result.success(Unit)
    }
    override fun getMessageTemplates(): Flow<List<MessageTemplate>> = _messageTemplates.asStateFlow()
    override suspend fun updateMessageTemplate(template: MessageTemplate): Result<Unit> {
        delay(200)
        val list = _messageTemplates.value.toMutableList()
        val index = list.indexOfFirst { it.id == template.id }
        if (index != -1) {
            list[index] = template
            _messageTemplates.value = list
        }
        return Result.success(Unit)
    }
    override fun getMessageJobs(): Flow<List<MessageJob>> = _messageJobs.asStateFlow()
    override suspend fun retryMessageJob(jobId: String): Result<Unit> {
        delay(300)
        val list = _messageJobs.value.toMutableList()
        val index = list.indexOfFirst { it.id == jobId }
        if (index != -1) {
            list[index] = list[index].copy(status = "SUCCESS", error = null, isRetrying = false)
            _messageJobs.value = list
            _messagingStats.value = _messagingStats.value.copy(
                basarisizCount = (_messagingStats.value.basarisizCount - 1).coerceAtLeast(0),
                gonderildiCount = _messagingStats.value.gonderildiCount + 1
            )
        }
        return Result.success(Unit)
    }
    override fun getMessageLogs(): Flow<List<MessageLog>> = _messageLogs.asStateFlow()

    // Maintenance Implementations
    override fun getMaintenanceStats(): Flow<MaintenanceStats> = _maintenanceStats.asStateFlow()
    override fun getMaintenanceRules(): Flow<List<MaintenanceRule>> = _maintenanceRules.asStateFlow()
    override suspend fun addMaintenanceRule(rule: MaintenanceRule): Result<Unit> {
        delay(200)
        val list = _maintenanceRules.value.toMutableList()
        list.add(0, rule)
        _maintenanceRules.value = list
        _maintenanceStats.value = _maintenanceStats.value.copy(activeRulesCount = list.count { it.status == "Aktif" })
        return Result.success(Unit)
    }
    override suspend fun updateMaintenanceRule(rule: MaintenanceRule): Result<Unit> {
        delay(200)
        val list = _maintenanceRules.value.toMutableList()
        val index = list.indexOfFirst { it.id == rule.id }
        if (index != -1) {
            list[index] = rule
            _maintenanceRules.value = list
            _maintenanceStats.value = _maintenanceStats.value.copy(activeRulesCount = list.count { it.status == "Aktif" })
        }
        return Result.success(Unit)
    }
    override suspend fun deleteMaintenanceRule(ruleId: String): Result<Unit> {
        delay(200)
        val list = _maintenanceRules.value.toMutableList()
        list.removeAll { it.id == ruleId }
        _maintenanceRules.value = list
        _maintenanceStats.value = _maintenanceStats.value.copy(activeRulesCount = list.count { it.status == "Aktif" })
        return Result.success(Unit)
    }
    override suspend fun toggleMaintenanceRuleStatus(ruleId: String): Result<Unit> {
        delay(200)
        val list = _maintenanceRules.value.toMutableList()
        val index = list.indexOfFirst { it.id == ruleId }
        if (index != -1) {
            val curr = list[index]
            val newStatus = if (curr.status == "Aktif") "Pasif" else "Aktif"
            list[index] = curr.copy(status = newStatus)
            _maintenanceRules.value = list
            _maintenanceStats.value = _maintenanceStats.value.copy(activeRulesCount = list.count { it.status == "Aktif" })
        }
        return Result.success(Unit)
    }

    // Reports Implementation
    override fun getReportData(timeRange: com.example.data.model.ReportTimeRange): Flow<com.example.data.model.ReportData> {
        val data = when (timeRange) {
            com.example.data.model.ReportTimeRange.WEEK -> com.example.data.model.ReportData(
                timeRange = timeRange,
                appointments = com.example.data.model.AppointmentReportData(totalAppointments = 18, pendingCount = 3, approvedCount = 5, completedCount = 9, cancelledCount = 1),
                finance = com.example.data.model.FinanceReportData(totalIncome = 148500.0, totalExpense = 32400.0, netProfit = 116100.0),
                popularServices = listOf(
                    com.example.data.model.PopularServiceItem("Kombi Bakım & Servis", 12, 65, 0xFF3B82F6),
                    com.example.data.model.PopularServiceItem("Doğalgaz Tesisatı", 4, 22, 0xFF10B981),
                    com.example.data.model.PopularServiceItem("Petek Temizliği", 2, 13, 0xFFF59E0B)
                ),
                appointmentTrends = listOf(
                    com.example.data.model.TrendBarData("Pzt", 4),
                    com.example.data.model.TrendBarData("Sal", 2),
                    com.example.data.model.TrendBarData("Çar", 5),
                    com.example.data.model.TrendBarData("Per", 3),
                    com.example.data.model.TrendBarData("Cum", 4),
                    com.example.data.model.TrendBarData("Cmt", 2),
                    com.example.data.model.TrendBarData("Paz", 0)
                )
            )
            com.example.data.model.ReportTimeRange.MONTH -> com.example.data.model.ReportData(
                timeRange = timeRange,
                appointments = com.example.data.model.AppointmentReportData(totalAppointments = 74, pendingCount = 8, approvedCount = 14, completedCount = 48, cancelledCount = 4),
                finance = com.example.data.model.FinanceReportData(totalIncome = 520000.0, totalExpense = 112000.0, netProfit = 408000.0),
                popularServices = listOf(
                    com.example.data.model.PopularServiceItem("Kombi Bakım & Servis", 45, 61, 0xFF3B82F6),
                    com.example.data.model.PopularServiceItem("Doğalgaz Tesisatı", 18, 24, 0xFF10B981),
                    com.example.data.model.PopularServiceItem("Petek Temizliği", 11, 15, 0xFFF59E0B)
                ),
                appointmentTrends = listOf(
                    com.example.data.model.TrendBarData("1. Hafta", 16),
                    com.example.data.model.TrendBarData("2. Hafta", 21),
                    com.example.data.model.TrendBarData("3. Hafta", 18),
                    com.example.data.model.TrendBarData("4. Hafta", 19)
                )
            )
            com.example.data.model.ReportTimeRange.ALL_TIME -> com.example.data.model.ReportData(
                timeRange = timeRange,
                appointments = com.example.data.model.AppointmentReportData(totalAppointments = 412, pendingCount = 8, approvedCount = 14, completedCount = 375, cancelledCount = 15),
                finance = com.example.data.model.FinanceReportData(totalIncome = 2850000.0, totalExpense = 640000.0, netProfit = 2210000.0),
                popularServices = listOf(
                    com.example.data.model.PopularServiceItem("Kombi Bakım & Servis", 240, 58, 0xFF3B82F6),
                    com.example.data.model.PopularServiceItem("Doğalgaz Tesisatı", 110, 27, 0xFF10B981),
                    com.example.data.model.PopularServiceItem("Petek Temizliği", 62, 15, 0xFFF59E0B)
                ),
                appointmentTrends = listOf(
                    com.example.data.model.TrendBarData("Oca", 32),
                    com.example.data.model.TrendBarData("Şub", 38),
                    com.example.data.model.TrendBarData("Mar", 45),
                    com.example.data.model.TrendBarData("Nis", 40),
                    com.example.data.model.TrendBarData("May", 42),
                    com.example.data.model.TrendBarData("Haz", 55),
                    com.example.data.model.TrendBarData("Tem", 50),
                    com.example.data.model.TrendBarData("Ağu", 48)
                )
            )
        }
        return kotlinx.coroutines.flow.flowOf(data)
    }

    // Google Ads Mock Implementation
    private val _googleAdsStats = MutableStateFlow(
        com.example.data.model.GoogleAdsStats(
            totalSpend = 14250.00,
            totalClicks = 3120,
            totalConversions = 184,
            avgCpa = 77.44,
            conversionRate = 5.90
        )
    )

    private val _googleAdsCampaigns = MutableStateFlow(
        listOf(
            com.example.data.model.GoogleAdsCampaign(
                id = "c1",
                name = "Arama - Bayrampaşa Kombi Servisi",
                status = "ACTIVE",
                dailyBudget = 250.0,
                spend = 5420.0,
                clicks = 1240,
                conversions = 78,
                cpa = 69.48
            ),
            com.example.data.model.GoogleAdsCampaign(
                id = "c2",
                name = "Arama - Acil Kombi Tamiri",
                status = "ACTIVE",
                dailyBudget = 180.0,
                spend = 3850.0,
                clicks = 890,
                conversions = 52,
                cpa = 74.03
            ),
            com.example.data.model.GoogleAdsCampaign(
                id = "c3",
                name = "Arama - Petek Temizleme Kampanyası",
                status = "PAUSED",
                dailyBudget = 120.0,
                spend = 2100.0,
                clicks = 450,
                conversions = 24,
                cpa = 87.50
            ),
            com.example.data.model.GoogleAdsCampaign(
                id = "c4",
                name = "Haritalar / Yerel - Gaziosmanpaşa & Bayrampaşa",
                status = "ACTIVE",
                dailyBudget = 150.0,
                spend = 2880.0,
                clicks = 540,
                conversions = 30,
                cpa = 96.00
            )
        )
    )

    override fun getGoogleAdsStats(): Flow<com.example.data.model.GoogleAdsStats> = _googleAdsStats.asStateFlow()

    override fun getGoogleAdsCampaigns(): Flow<List<com.example.data.model.GoogleAdsCampaign>> = _googleAdsCampaigns.asStateFlow()

    override suspend fun toggleCampaignStatus(campaignId: String): Result<Unit> {
        val current = _googleAdsCampaigns.value
        _googleAdsCampaigns.value = current.map { camp ->
            if (camp.id == campaignId) {
                camp.copy(status = if (camp.status == "ACTIVE") "PAUSED" else "ACTIVE")
            } else camp
        }
        return Result.success(Unit)
    }

    // WhatsApp Status Implementation
    private val _whatsAppStatus = MutableStateFlow(
        com.example.data.model.WhatsAppStatus(
            isConnected = true,
            phoneNumber = "+90 532 123 45 67",
            businessName = "Sancak Kombi & Doğalgaz Servisi",
            wabaAccountId = "WABA_982401827401",
            connectedAt = "12 Ocak 2025 - 14:30",
            qualityRating = "Yüksek (Yeşil)",
            messagingLimit = "1.000 Müşteri / 24 Saat",
            displayPhoneNumberStatus = "Onaylandı (APPROVED)",
            webhookStatus = "Etkin (Aktif)"
        )
    )

    override fun getWhatsAppStatus(): Flow<com.example.data.model.WhatsAppStatus> = _whatsAppStatus.asStateFlow()
}
