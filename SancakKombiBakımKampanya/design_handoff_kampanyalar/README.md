# Handoff: Bakım Kampanyaları (Mobil + Tablet)

Kaynak (web): `abdulfettahsancakli/SancakKombiWebSite` · `sancakkombi-web/app/admin/campaigns/` (`page.tsx`, `campaigns-dashboard-client.tsx`) · `sancakkombi-web/app/api/admin/campaigns/*`
Web karşılığı: https://www.sancakkombi.com/admin/campaigns

## Genel Bakış
Web admin panelindeki **Bakım Kampanyaları** ekranının telefon ve tablete uyarlanmış hâli. Müşteriler seçilir, kişiye özel WhatsApp bakım daveti önizlenir ve tek tıkla gönderilir. Kapsam:
- Başlık ve özet sayıları (Kayıtlı Müşteri, Kampanyaya Uygun, Seçili Alıcı)
- 3 adımlı rehber (Alıcıları Belirleyin → Önizlemeyi İnceleyin → Tek Tıkla Başlatın)
- Alıcı seçimi: filtre çipleri, arama, toplu izin, müşteri satırları
- Canlı WhatsApp önizlemesi, kampanya başlığı, gönderim kartı
- Rehberden müşteri yükleme (.vcf / .csv)
- Kampanya geçmişi ve raporlar

Dil: Türkçe. Metinler web kaynağıyla birebir aynıdır, değiştirmeyin.

## Tasarım Dosyaları Hakkında
`Campaigns Mobile Redesign.dc.html` bir **tasarım referansıdır**, üretim kodu değildir. Üç görünüm içerir:
1. Telefon · Alıcılar (390×844 referans)
2. Telefon · Önizleme ve Gönderim (390×844)
3. Tablet (820×1180): solda alıcı listesi, sağda önizleme ve gönderim, altta geçmiş

Dosyayı tarayıcıda açın (`support.js` aynı klasörde olmalı). Logo `sancakkombiservislogo.jpg` ile aynı klasörden okunur. Dosyadaki `_ds/...` bağlantıları bu pakette yoktur; tokenlar aşağıda listelenmiştir.

Kaynak olarak web bileşeni okunmalıdır. Davranış (seçim, önizleme geçişi, gönderim partileri, rehber ayrıştırma) `campaigns-dashboard-client.tsx` içindedir.

## Doğruluk (Fidelity)
**High-fidelity.** Renkler, ölçüler, köşe yarıçapları ve metinler nihaidir. HTML px = Compose **dp**, font px = **sp**.

## Design Tokens (web admin)
Kampanyalar ekranı web admin paletini kullanır. Sancak Kombi tasarım sistemiyle karışmasın diye değerler kaynak koddan alınmıştır.

**Açık tema (tasarım referansı):** sayfa zemini #F4F6F9 · yüzey #FFFFFF · yüzey2 #F8FAFC · kenarlık #E2E8F0 · metin #0F172A · metin2 #5B6678 · muted #94A3B8
- Vurgu (admin yeşili): #16A34A · vurgu zemin rgba(22,163,74,.12) · vurgu kenarlık rgba(34,197,94,.25) · seçili satır zemin rgba(22,163,74,.08)
- Ana buton (Tüm İzinlileri Seç, Önizle): #073368 / gradient #144983 → #006ECE
- WhatsApp: ikon #25D366 · doğrulama rozeti #25D366 · gönderim butonu (pasif) #A7E8C0 · başlık çubuğu #008069 · sohbet zemini #EFEAE2 · balon #FFFFFF · balon metni #111B21 · saat #667781 · tik #53BDEB · buton metni #0284C7 · tarih hapı rgba(255,255,255,.85) metin #54656F
- Uyarı: zemin #FFF7E6 · kenarlık #FDE68A · metin #A16207
- Hata: zemin rgba(239,68,68,.1) · kenarlık rgba(239,68,68,.22) · metin #EF4444

**Filtre çipleri:** İzinli #15803D (zemin #ECFDF3, kenarlık #BBF7D0) · Belirsiz #A16207 (zemin #FFF7E6) · İstemiyor #B42318 (zemin #FDECEA, kenarlık #FBC4BD)

**Kampanya durumu rozetleri:** Tamamlandı #16A34A · Kısmi / Hatalı #D97706 · Gönderiliyor #0284C7 · Başarısız #DC2626 · Taslak metin2

**Tipografi:** Nunito Sans (başlıklar, butonlar, etiketler) · Roboto (gövde). Ekran başlığı 22–26/800, bölüm başlığı 16/800, gövde 12–13, etiket 10.5–11/700–800, mikro 10.

**Köşe / boşluk:** panel 16–18 (telefon 14–18), hero 18–20, input 12, buton 10–14, çip 999, WhatsApp balonu 10. Dokunma alanı en az 44dp. Ana gönderim butonu 50dp.

**Gölgeler:** panel 0 12 32 rgba(2,6,23,.04) · telefon çerçevesi 0 16 36 rgba(0,0,0,.12) · balon 0 1 3 rgba(0,0,0,.12).

## Ekranlar

### 1. Telefon · Alıcılar
Kaydırılabilir tek sütun. Üstte logo + `sancakkombi /admin` ve yatay kaydırılabilir yönetim menüsü (Kampanyalar aktif).
1. **Hero kartı:** eyebrow (WhatsApp ikonu + "0212 581 75 74 · Kurumsal WhatsApp Servisi"), başlık "Bakım Kampanyaları", açıklama, 3'lü sayı kutusu.
2. **Rehber adım 1:** "Alıcıları Belirleyin" ve alt metin.
3. **Alıcıları Seç paneli:**
   - Başlık + "N seçili" hapı
   - Filtre çipleri (yatay kaydırma): Tümü (N) · İzinli (N) · Belirsiz (N) · İstemiyor (N)
   - Arama: "İsim, telefon veya ilçe ile ara..."
   - Butonlar: **Tüm İzinlileri Seç (N)** (birincil) · **Görünenleri Seç** (ikincil)
   - Toplu İzin kutusu: **Seçilenlere İzin Ver** · **Kampanya İstemiyor** (seçim yoksa pasif)
   - "Tümünü İşaretle (N müşteri)" ve "Satıra tıklayarak sağda önizleyebilirsiniz" (telefonda "Önizle" butonuna dönüşür)
   - Müşteri satırı: onay kutusu · avatar (ad baş harfi) · ad + "Önizleniyor" hapı · telefon · ilçe · sağda izin rozeti ve **Son Servis** tarihi · **Önizle** butonu
   - Önizlenen satır: primary sol kenarlık ve açık vurgu zemin

### 2. Telefon · Önizleme ve Gönderim
1. **Canlı Mesaj Önizlemesi** paneli:
   - "Önizlenen Müşteri:" + ad + telefon · **İsmi Değiştir** butonu (test adı girişi açılır)
   - Adım göstergesi (önceki / sonraki, 2+ alıcı seçiliyken): ‹ 1 / N ›
   - WhatsApp çerçevesi: başlık çubuğu (‹, logo avatarı, "Sancak Kombi" + doğrulama rozeti, "Resmi İşletme Hesabı · 0212 581 75 74", video/telefon/menü ikonları), "BUGÜN" hapı, mesaj balonu, saat + tik, üç hızlı yanıt butonu (📅 Randevu Al · 📞 Beni Arayın · 🔕 İlgilenmiyorum)
   - Mesaj metni `{{customer_name}}` yerine seçili müşterinin adını koyar; ad kalın ve vurgulu (#DCF8C6 zemin) gösterilir.
2. **Gönderim kartı:**
   - "Kampanya Başlığı (Takip İçin)" input (varsayılan: `2026 Sonbahar Bakım Kampanyası`)
   - Üç özet kutusu: **Hazır Alıcı** (sayı) · **Kişiye Özel** / Tekil WhatsApp · **Meta Onaylı** / Resmi Şablon
   - Ana buton: seçili izinli müşteri varsa `N Müşteriye WhatsApp Gönder`, yoksa `Gönderim İçin Müşteri Seçin` (pasif). Gönderim sırasında spinner ve ilerleme metni.
   - Uyarı (seçim yoksa): "Sol listeden en az 1 **izinli** müşteri seçmelisiniz." + **Tüm İzinlileri Seç (N)** bağlantısı
   - Güvenlik notu: "Meta onaylı `sonbahar_bakim_kampanyasi` şablonuyla iletilir…"
3. **Rehberden Müşteri Yükle** (katlanabilir): dosya seçimi (.vcf / .csv), "N kişi bulundu" önizlemesi, onay kutusu, **Rehberi Panele Aktar**.

### 3. Tablet (820×1180 referans)
- Üst çubuk: logo + `sancakkombi /admin` + yatay yönetim menüsü (Kampanyalar aktif). Menü kaydırılabilir kalır.
- Hero: solda eyebrow / başlık / açıklama, sağda üç sayı kutusu.
- Rehber: üç adım yan yana.
- İki sütun (1.3fr / 1fr): sol **Alıcıları Seç**, sağ **Canlı Mesaj Önizlemesi** + gönderim kartı. Telefondaki bölümlerin aynısı; önizleme kutusu tek satır mesaj gösterir ve tam mesaj ile kaydırılabilir.
- Altta **Kampanya Geçmişi & Raporlar**: dört sütunlu satırlar (kampanya adı + tarih/özet · Alıcı · Gönderim zamanı · Durum rozeti).

### 4. Kampanya Geçmişi (tüm boyutlar)
Satır: kampanya adı (kalın) + "gg Ay yyyy · N/N gönderildi · N okundu · N dönüş" · Alıcı · Telefon · Gönderim zamanı · Durum rozeti (Okundu / Teslim edildi / Yanıtladı) · Tamamlandı etiketi. Durum "sending" veya "partial" ise **Devam et** butonu görünür.

## Etkileşimler
- Müşteri satırına dokunmak seçimi değiştirir ve önizlemeyi o müşteriye çevirir (web davranışı).
- **Önizle** yalnızca önizlemeyi değiştirir, seçimi değiştirmez.
- Toplu izin: seçilen müşteriler için `granted` veya `rejected` kaydedilir; başarıda "N müşterinin iletişim tercihi güncellendi." bildirimi.
- Gönderim: önce kampanya oluşturulur (yalnızca izinli seçili müşteriler), ardından parti parti gönderilir. Her partide ilerleme metni güncellenir: "Gönderim sürüyor · parti N", "N mesaj kuyrukta", "Gönderim tamamlandı". Güvenlik sınırı (200 parti) aşılırsa "panelden devam edebilirsiniz" uyarısı gelir.
- Önizleme geçişi (‹ ›) seçili izinli müşteriler arasında döngüseldir.
- Rehber aktarımında ilk 1000 kişi alınır.
- Bildirimler (`sk-alert`): başarı yeşil, hata kırmızı; telefonda üstte, tablette panel üstünde.
- Basma: kart 0.98, buton 0.97, 120 ms.

## State (ViewModel)
```
CampaignsUiState(
  customers: List<CampaignCustomer>,      // id, name, phoneRaw, district, consent, lastServiceDate, totalAppointments
  campaigns: List<CampaignHistoryItem>,   // id, name, createdAt, recipientCount, counts{sent,delivered,read,responded,optedOut}, status
  search: String = "",
  consentFilter: All | Granted | Unknown | Rejected = All,
  selectedIds: Set<String>,
  previewCustomerId: String?,
  customPreviewName: String?,
  campaignName: String = "${year} Sonbahar Bakım Kampanyası",
  importedContacts: List<ImportedContact>,
  importConsent: Boolean,
  busy: Boolean,
  progress: String,
  notice: Notice?,                        // tone: OK | ERROR, text
  migrationRequired: Boolean
)
Derived: grantedCount, unknownCount, rejectedCount, filtered, eligibleSelected (= selected ∩ granted),
         currentPreviewCustomer (önce previewCustomerId, sonra eligibleSelected[0], selected[0], filtered[0])
CampaignStatus: DRAFT | SENDING | PARTIAL | COMPLETED | FAILED
```

## Veri ve API (web mevcut uçlar)
- Liste: `getCampaignDashboard()` (`lib/operations/service`) → `customers`, `campaigns`, `migrationRequired`
- İzin: `POST /api/admin/campaigns/consent` · body `{ customerIds, status, source: "campaign_panel_admin_confirmation" }`
- Kampanya oluşturma: `POST /api/admin/campaigns` · body `{ name, customerIds }`
- Parti gönderimi: `POST /api/admin/campaigns/{id}/send` · yanıt `remaining`
- Rehber aktarımı: `POST /api/admin/customers/import` · body `{ contacts, marketingConsentConfirmed }` · yanıt `imported`, `matched`
- Mesaj şablonu: `DEFAULT_MAINTENANCE_CAMPAIGN_MESSAGE` (`{{customer_name}}` yer tutucusu içerir)
- Telefon gösterimi: `normalizePhoneForDisplay`

## Açık Noktalar (uygulamadan önce doğrulanmalı)
1. **Hedef platform:** Kaynak yalnızca web admin paneli. Bu paket Android (Compose) için yazıldıysa, Kampanyalar ekranı Android repoda henüz yok ve API uçları mobil için sözleşmeye eklenmeli (`BACKEND_CONTRACT.md`).
2. **Yönetim menüsü:** Android'de alt gezinme (Panel · Randevu · Müşteri · Finans · Menü) kullanılıyor. Kampanyalar telefonda Menü altına mı yoksa alt çubuğa mı girecek, karar verilmeli.
3. **Mesaj içeriği:** Web önizlemesindeki metin ve emojiler ürün metnidir. Şablon Meta tarafından onaylı olduğu için değiştirilmemeli.
4. **Hızlı yanıt butonları:** Randevu Al / Beni Arayın / İlgilenmiyorum yalnızca görsel. Bu yanıtlar sunucuda nasıl işlenecek, belirlenmeli.
5. **Tablet geçmişi:** Web'de geçmiş tablo olarak görünüyor; tabletteki dört sütunlu düzen, telefondaki kart düzeni kadar sade olmayabilir. Gerçek veri ile kontrol edin.
6. **Migration:** `20261006120000_whatsapp_marketing_campaigns.sql` çalıştırılmamışsa gönderim devre dışı kalır (`migrationRequired`). Mobil tarafta da aynı uyarı gösterilmeli.

## Varlıklar
- Logo: `sancakkombiservislogo.jpg` (bu klasörde). Android'de `res/drawable` altına taşınmalı.
- İkonlar: web kaynağı Lucide kullanır (Search, ChevronLeft, ShieldCheck, Eye, Info, Users, Send, Clock3, CheckCircle2, Video, Phone, MoreVertical, CheckCheck). Android'de Material eşdeğerleri kullanılabilir. WhatsApp markası için kaynaktaki SVG yolu (`whatsapp-icon.tsx`) korunmalı.

## Dosyalar
- `Campaigns Mobile Redesign.dc.html`: telefon ve tablet görünümleri (tasarım referansı).
- `sancakkombiservislogo.jpg`: logo.
- `support.js`: prototip çalışması için (uygulanmayacak).

## Claude Code'a verilecek örnek istek
> "design_handoff_kampanyalar/README.md ve prototipi incele. Bakım Kampanyaları ekranını Jetpack Compose ile uygula. Metinler birebir kalsın; renk ve ölçüler README'deki değerler. Açık Noktalar 1–6 maddelerini önce bana sor. Sıra: telefon alıcılar → önizleme ve gönderim → tablet (WindowSizeClass, iki sütun) → kampanya geçmişi. Veri katmanı mevcut API uçlarını kullanmalı. Her adımda derlenebilir kal."
