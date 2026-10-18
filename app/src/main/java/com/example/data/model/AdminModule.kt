package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

enum class ModuleGroup(val title: String) {
    OPERATION("Operasyon"),
    FINANCE_PRODUCT("Finans ve Ürün"),
    COMMUNICATION("İletişim"),
    ANALYSIS("Analiz"),
    SETTINGS("Ayarlar")
}

enum class AdminModule(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val group: ModuleGroup,
    val isPrimaryBottomNav: Boolean = false
) {
    RANDEVULAR("appointments", "Randevular", "Günlük plan ve servis işleri", Icons.Default.DateRange, ModuleGroup.OPERATION, true),
    MUSTERILER("customers", "Müşteriler", "Müşteri kartları ve servis geçmişi", Icons.Default.People, ModuleGroup.OPERATION, true),
    TEKLIFLER("quotes", "Teklifler", "Teklif oluşturma ve durum takibi", Icons.Default.Description, ModuleGroup.OPERATION),

    FINANS("finance", "Finans", "Tahsilatlar, alacaklar ve giderler", Icons.Default.AccountBalance, ModuleGroup.FINANCE_PRODUCT, true),
    HIZLI_IBAN("quick_iban", "Hızlı IBAN", "IBAN ve ödeme bilgisi gönderimi", Icons.Default.Bolt, ModuleGroup.FINANCE_PRODUCT),
    FIYAT_KATALOGU("catalog", "Fiyat Kataloğu", "Ürün ve hizmet fiyatları", Icons.Default.LocalOffer, ModuleGroup.FINANCE_PRODUCT),
    STOK("stock", "Stok", "Parça ve stok hareketleri", Icons.Default.Inventory2, ModuleGroup.FINANCE_PRODUCT),

    MESAJ_SISTEMI("messages", "Mesajlar", "WhatsApp bildirimleri ve şablonları", Icons.AutoMirrored.Filled.Chat, ModuleGroup.COMMUNICATION),
    BAKIM_TAKVIMLERI("maintenance", "Bakım Takvimleri", "Yıllık bakım hatırlatmaları", Icons.Default.EventAvailable, ModuleGroup.COMMUNICATION),
    KAMPANYALAR("campaigns", "Kampanyalar", "Müşteri kampanyaları ve gönderimler", Icons.Default.Notifications, ModuleGroup.COMMUNICATION),

    ISTATISTIKLER("reports", "İstatistikler", "Operasyon ve gelir raporları", Icons.Default.BarChart, ModuleGroup.ANALYSIS),
    GOOGLE_ADS("ads", "Google Ads", "Reklam performansı ve kampanyalar", Icons.Default.Campaign, ModuleGroup.ANALYSIS),

    WHATSAPP_DURUMU("whatsapp_status", "WhatsApp bağlantı durumu", "Cloud API bağlantısını kontrol edin", Icons.Default.Link, ModuleGroup.SETTINGS)
}
