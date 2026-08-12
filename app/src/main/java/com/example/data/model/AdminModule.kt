package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.ui.graphics.vector.ImageVector

enum class AdminModule(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isPrimaryBottomNav: Boolean = false
) {
    MUSTERILER(
        id = "customers",
        title = "Müşteriler",
        description = "Müşteri kartları, geçmiş servisler ve iletişim notlarını yönetin.",
        icon = Icons.Default.People,
        isPrimaryBottomNav = true
    ),
    RANDEVULAR(
        id = "appointments",
        title = "Randevular",
        description = "Günlük plan, yeni talepler ve hızlı operasyon aksiyonları tek ekranda.",
        icon = Icons.Default.DateRange,
        isPrimaryBottomNav = true
    ),
    MESAJ_SISTEMI(
        id = "messages",
        title = "Mesaj Sistemi",
        description = "SMS ve WhatsApp kuyruğu, hata logları ve tekrar deneme akışları.",
        icon = Icons.AutoMirrored.Filled.Chat,
        isPrimaryBottomNav = false
    ),
    ISTATISTIKLER(
        id = "reports",
        title = "İstatistikler",
        description = "Aylık randevu trendleri, hizmet yoğunluğu ve müşteri tekrar oranları.",
        icon = Icons.Default.BarChart,
        isPrimaryBottomNav = false
    ),
    FINANS(
        id = "finance",
        title = "Finans",
        description = "Tahsilatlar, açık alacaklar ve manuel gider kayıtları burada tutulur.",
        icon = Icons.Default.AccountBalance,
        isPrimaryBottomNav = true
    ),
    TEKLIFLER(
        id = "quotes",
        title = "Teklifler",
        description = "Fiyat tekliflerini oluşturun, yazdırın ve durumlarını takip edin.",
        icon = Icons.Default.Description,
        isPrimaryBottomNav = false
    ),
    BAKIM_TAKVIMLERI(
        id = "maintenance",
        title = "Bakım Takvimleri",
        description = "Müşteri bazında otomatik oluşturulan yıllık bakım hatırlatma takvimlerini yönetin.",
        icon = Icons.Default.EventAvailable,
        isPrimaryBottomNav = false
    ),
    GOOGLE_ADS(
        id = "ads",
        title = "Google Ads",
        description = "Kampanya yönetimi, anahtar kelime optimizasyonu ve performans analizi.",
        icon = Icons.Default.Campaign,
        isPrimaryBottomNav = false
    ),
    WHATSAPP_CONNECT(
        id = "whatsapp",
        title = "WhatsApp Bağlantısı",
        description = "Meta Embedded Signup WhatsApp Business numara durumu ve bağlantısı.",
        icon = Icons.Default.PhonelinkSetup,
        isPrimaryBottomNav = false
    )
}
