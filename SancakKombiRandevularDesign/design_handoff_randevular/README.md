# Handoff: Randevular & Servis Fişi (Mobil + Tablet)

Repo: `abdulfettahsancakli/sancakkombi-admin-mobile` (Kotlin · Jetpack Compose · Material 3)
Web karşılığı: https://www.sancakkombi.com/admin/appointments

## Genel Bakış
Web admin panelindeki **Randevular** sayfası, **Hızlı Randevu Kaydı** formu ve **Servis Fişi Oluştur (Randevusuz · Acil / Yerinde İş)** akışının saha teknisyenleri için telefon ve tablete uyarlanmış hâli. Dashboard ve Müşteriler ekranlarında kurulan dil (sky-blue #0EA5E9, emoji ikonlar, bento kartlar, koyu/açık tema, sıcak Türkçe metinler) aynen devam eder.

## Tasarım Dosyaları Hakkında
Bu paketteki HTML dosyaları **tasarım referansıdır** — görünüm ve davranışı gösteren prototiplerdir, doğrudan kopyalanacak üretim kodu değildir. Görev, bu tasarımları mevcut Android uygulamasında **Jetpack Compose + Material 3** ile, repodaki mevcut desenleri (`ui/theme`, `ui/components`, `ui/screens`, mevcut ViewModel/Repository yapısı) kullanarak yeniden oluşturmaktır.

`Appointments Mobile Redesign.dc.html` dosyasını tarayıcıda açın (aynı klasördeki `support.js` ve `android-frame.jsx` ile birlikte). Sayfada solda telefon, sağda tablet vardır; hepsi tıklanabilir:
- Telefon üstü sekmeler: **☰ Liste / 📅 Hızlı Randevu / 🧾 Servis Fişi**
- Tablet üstü: **↔ Yatay / ↕ Dikey**; tablet içinde **☰ Liste / 🗓️ Takvim / 🗺️ Harita**
- ☀️/🌙 ile tema değişir.

`reference-kotlin/` klasörü önceki turda üretilen tema ve ortak bileşenlerdir (Color.kt, Theme.kt, TopHeaderBar.kt, BottomNavBar.kt) ve kart/filtre desenleri için örnek olarak CustomersScreen.kt. Renkleri ve bileşenleri buradan kullanın, yenisini icat etmeyin.

## Doğruluk (Fidelity)
**High-fidelity.** Renkler, ölçüler, köşe yarıçapları, metinler ve etkileşimler nihaidir. Ölçüler HTML px = Compose **dp** olarak alınmalı (font px = **sp**).

---

## Önerilen Dosya Yapısı
```
ui/screens/appointments/
  AppointmentsScreen.kt          // WindowSizeClass'a göre Phone / TabletLandscape / TabletPortrait seçer
  AppointmentsViewModel.kt       // liste, filtre, seçim, istatistik, durum güncelleme
  components/
    AppointmentCard.kt           // telefon kartı
    AppointmentRow.kt            // tablet liste satırı
    AppointmentDetailPane.kt     // tablet sağ bölme + BottomSheet içeriği (tek composable, iki yerde)
    StatusChip.kt, StatCard.kt, FilterChipsRow.kt
  calendar/WeekCalendarView.kt
  map/RouteMapView.kt
ui/screens/appointments/new/QuickAppointmentScreen.kt
ui/screens/receipt/ServiceReceiptFlow.kt (+ ReceiptViewModel.kt, SignaturePad.kt)
```
Navigasyon: telefonda `QuickAppointment` ve `ServiceReceipt` tam ekran route (alttan kayarak giriş). Tablette aynı composable'lar **Dialog** içinde (maks. 760dp genişlik, ekranın %92'si yükseklik, 24dp köşe) açılır — telefonu/başka ekranı değiştirmez.

Ekran sınıfı: `calculateWindowSizeClass()` → `Compact` = telefon; `Medium/Expanded` = tablet. Tablette yön `LocalConfiguration.current.orientation` ile.

---

## Design Tokens

### Koyu tema (varsayılan)
| Token | Değer |
|---|---|
| bg | #090D16 |
| surface | #131B2E |
| surface2 | #1E293B |
| border | #334155 |
| text | #F8FAFC |
| text2 | #94A3B8 |
| primary | #0EA5E9 · container rgba(14,165,233,0.16) |
| green | #22C55E · container rgba(34,197,94,0.14) |
| amber | #F59E0B · container rgba(245,158,11,0.14) |
| red | #F87171 · container rgba(248,113,113,0.14) |
| violet | #A78BFA |

### Açık tema
bg #F4F7FA · surface #FFFFFF · surface2 #EEF2F6 · border rgba(15,23,42,0.10) · text #0F172A · text2 #64748B · primary #0284C7 (c 0.10) · green #16A34A · amber #B45309 · red #DC2626 · violet #7C3AED

### Gradyanlar
- Primary CTA: linear 135° #0EA5E9 → #0369A1
- Servis Fişi CTA (telefon): linear 135° #F59E0B → #B45309

### Durum eşlemesi
| Durum | Etiket | Renk / Zemin |
|---|---|---|
| bekle | Bekliyor | amber / amberC |
| onay | Onaylandı | primary / primaryC |
| tamam | Tamamlandı | green / greenC |
| iptal | İptal | red / redC |

Hizmet noktası rengi: Petek → primary, Doğalgaz → violet, Gaz Kaçağı → red, diğer → text2.
Avatar paleti (id % 7): #F59E0B #14B8A6 #3B82F6 #8B5CF6 #22C55E #EC4899 #0EA5E9 — zemin = renk + %18 alfa.

### Tipografi (Roboto)
Ekran başlığı 18/800 (telefon), 24/900 (tablet, -0.4 letter-spacing) · Kart başlığı 14.5/800 · Bölüm başlığı 13.5/800 · Gövde 12–13.5/400–700 · Etiket 11.5/700 text2 · Mikro etiket 10–10.5/800, 0.4 letter-spacing, BÜYÜK HARF · Saat/telefon: Monospace · Toplam tutar 20/900 (alt bar), 32/900 (tahsilat kartı).

### Köşe / Boşluk
Kart 18 (telefon) · 20 (tablet panel) · Input 12 · Buton 11–14 · Chip 20 (pill) · Sheet üst 24 · Tablet frame 40.
Ekran padding 12 (telefon), 18–20 (tablet). Kart iç padding 14. Grid gap 6–10.
Min dokunma: input 48dp, ana buton 52dp, kart aksiyon 40dp.

### Gölge
Alt nav / toast: 0 8–10 24–30 rgba(0,0,0,.3–.35). Kartlarda gölge yok, 1dp border.

---

## Ekranlar

### 1. Telefon · Randevular Listesi
**Amaç:** Günün/ayın işlerini görmek, tek dokunuşla aramak, rota almak, işi tamamlamak.

Yukarıdan aşağı (dikey `LazyColumn`, padding 12, gap 10, alt boşluk 96 — yüzen nav için):
1. **TopHeaderBar** (mevcut bileşen, 60dp): SK logo, `sancakkombi/admin`, tema butonu.
2. **Başlık satırı:** "Randevular" 18/800 + alt satır "Saha ekibi anlık senkron 🟢" 11.5 text2. Sağda pill: nokta + "{n} Randevu" (primaryC zemin, primary metin).
3. **2'li CTA grid:** "📅 Yeni Randevu / Telefonla gelen iş" (mavi gradyan) · "🧾 Servis Fişi Kes / Randevusuz · Acil / Yerinde" (amber gradyan). 16 radius, padding 12, emoji 30dp beyaz %20 daire içinde. Basınca scale 0.97.
4. **4'lü istatistik:** Toplam / Sahada (onay) / Bitti (tamam) / İptal. Değer 19/900 durum renginde, etiket 9.5/700, altta 3dp progress (yüzde = sayı/toplam).
5. **Arama:** 🔎 + "Müşteri, telefon, mahalle ara..." — ad+telefon+adres+ilçe+not içinde, `tr` locale küçük harf ile arar.
6. **Durum chip'leri** (yatay kaydırma): Tümü, Bekliyor, Onaylandı, Tamamlandı, İptal — her birinde renkli nokta + sayı. Seçili: primary zemin, beyaz metin.
7. **Tarih segmenti:** Bugün · Yarın · Hafta (±6 gün) · Bu Ay · Tümü. Varsayılan **Bu Ay**.
8. **Gün grupları:** başlık "BUGÜN · 29 EYLÜL SALI" / "YARIN · …" / "23 EYLÜL ÇARŞAMBA" + çizgi + "{n} iş". Yeni tarih üstte.
9. **AppointmentCard:**
   - Sol 54dp saat kutusu (surface2): başlangıç 15/900 mono, bitiş 10 mono.
   - Ad 14.5/800 (tek satır, ellipsis), altında "📍 İlçe" + etiket rozeti (Yeni / Eski müşteri).
   - Sağ üst durum pill'i.
   - İç kutu (surface2, 12 radius): hizmet (renk noktası + 12/800), adres, not varsa "⚠️ not" amber.
   - Aksiyon grid'i `1fr 1fr 1fr 1.3fr`, 40dp: **📞 Ara** (primaryC) · **💬 WA** (greenC) · **🧭 Rota** (surface2) · ana aksiyon: açık işlerde **✓ Tamamla** (green, beyaz), bitmiş işlerde **🧾 Fiş** (surface2).
   - Kart üst kısmına dokununca genişler (border primary olur): kesikli ayraç + **✏️ Düzenle · ❌ İptal Et · 🗑️ Sil** (36dp).
10. **Boş durum:** "☕ Bu aralıkta randevu yok. Filtreyi değiştir ya da yeni randevu ekle."
11. **BottomNavBar** (mevcut, yüzen, 26 radius): Ana Sayfa · **İşler (aktif)** · Müşteriler · Finans · Modüller.

### 2. Telefon · Hızlı Randevu Kaydı (tam ekran)
Üst bar: ✕ (40dp) + "Hızlı Randevu Kaydı" / "Telefonla gelen işi saniyeler içinde kaydet".
Kartlar:
- **👤 Müşteri & İletişim:** Ad Soyad* · Telefon* (mono 15/700) + yanında "📇 Rehber" butonu (ContactsContract picker) · İlçe* (dropdown) + Mahalle* · Cadde/Sokak* (2fr) + No/Daire (1fr) · Bizi Nereden Buldu? + Durum* (Onaylandı/Bekliyor).
- **📅 Tarih & Saat:** 3 hızlı buton **Bugün / Yarın / +2 Gün** (altında "29 Eyl Sal"), tarih seçici, Hizmet Türü (Genel Servis, Kombi Bakım & Servis, Petek Temizliği, Doğalgaz Tesisatı, Gaz Kaçağı Tespiti). **Saat aralığı** 3 sütunlu grid, 09:00–24:00 arası 15 slot, 44dp. O tarihte iptal olmayan randevusu olan saat "HH:00 dolu" gösterilir ve seçilemez. Seçili slot sağ üstte yazılır.
- **🔧 Arıza & Müşteri Şikâyeti:** "Dokun, nota eklensin" — chip'ler: 🔥 Sıcak Su Gelmiyor, ❄️ Petekler Isınmıyor, 💧 Su Akıtıyor, 📉 Basınç Düşüyor, ⚡ Ateşleme / F1 / E01, 🔊 Ses / Gürültü, 🛠️ Yıllık Bakım, ⚠️ Gaz Kokusu (Acil — kırmızı). Dokununca metin nota virgülle eklenir; zaten varsa chip seçili görünür.
Sabit alt bar: **Vazgeç** (outline) + **✓ Randevuyu Kaydet** (flex 1, 52dp).
**Doğrulama:** ad + telefon + slot yoksa buton yarı saydam; basınca toast "👀 Ad, telefon ve saat aralığını doldur". Kayıtta durum `onay`, liste filtresi Tümü'ye döner, yeni kayıt seçilir, toast "✅ Randevu kaydedildi, ekibe iletildi".

### 3. Telefon · Servis Fişi Kes (4 adım)
Üst bar: ✕ + "Servis Fişi Kes" + "YENİ İŞ" rozeti (amber) + alt metin "Randevusuz acil / yerinde iş · fiş + gelir kaydı". Altında 4 segmentli ilerleme: **👤 Müşteri · 🛠️ İş & Cihaz · 💰 Tahsilat · ✍️ İmza** (4dp çubuk, geçilenler primary; segmente dokunarak adıma atlanır).

- **Adım 1 — Müşteri:** Müşteri & Adres kartı (Ad*, Telefon*, İlçe*, Mahalle, Cadde/Sokak, Kapı/Daire) + Hizmet & Zaman kartı (İş Tarihi* = bugün, Saat* dropdown "Şu an" dahil, Müşteri Beyanı textarea).
- **Adım 2 — İş & Cihaz:**
  - **🔥 Marka** 3 sütun grid, 42dp, beyaz zemin + marka renkli metin; seçilince marka rengi zemin: DemirDöküm #1D4ED8, Baymak #0F766E, E.C.A. #0369A1, Vaillant #15803D, BOSCH #DC2626, Buderus #1E3A8A, ARISTON #BE123C, Viessmann #EA580C, Protherm #F97316, Airfel #0284C7, Ferroli #B91C1C. Altında model input.
  - **🛠️ Yapılan İş*** şablon chip'leri (Yıllık Periyodik Bakım, NTC Sensör Değişti, Genleşme Tankı Basıldı (1 bar), Eşanjör Kireç Temizliği, Doldurma Musluğu Yenilendi, Fan Motoru Temizlendi, Sirkülasyon Pompası Revize, 3 Yollu Vana Motoru Değişti, Ateşleme / İyonizasyon) → textarea'ya ekler.
  - **🔩 Parçalar & Kalemler** "Stoktan otomatik düşer": kaynak filtresi (Tümü / 🏢 Dükkan Depo / 🏷️ İlhan Isı / 🛒 Paşa Market), checkbox satırları (ad, kaynak, fiyat yeşil). "＋ Özel parça satırı ekle" (kesikli). İşçilik (₺) + Diğer (₺) sayı alanları.
  - **🛡️ Garanti & Test:** Yok / 1 Ay / 3 Ay / 6 Ay / 1 Yıl / 2 Yıl (seçili yeşil) + "Cihaz test edildi, çalışır durumda teslim edildi" checkbox.
  - **👷 Usta & Bildirim:** usta dropdown + "📲 Fişi WhatsApp ile gönder" switch (varsayılan açık).
- **Adım 3 — Tahsilat:** mavi gradyan kart "CANLI TAHSİLAT DÖKÜMÜ" toplam 32/900 + Parça / İşçilik / Diğer + "🔒 Tutar iş kalemlerinden otomatik hesaplandı". **💳 Ödeme:** 💵 Nakit · 💳 Kart / POS · 🏦 Havale (66dp kutular). **💵 Tahsilat Durumu** radyo satırları: ✅ Tahsil Edildi (Ödendi) green · ⏳ Kısmi Tahsilat amber · ❌ Ödeme Alınmadı (Veresiye) red + kasa notu. "Bu iş için gider kaydı ekle" checkbox.
- **Adım 4 — İmza & Foto:** **✍️ Müşteri İmzası** (140dp) ve **Firma Yetkilisi İmzası** (110dp) — kesikli çerçeve, "👆 Parmağınla imzala" placeholder, "🧽 Temizle". Compose'da `Canvas` + `pointerInput(detectDragGestures)` ile Path; kalem 2.5dp, round cap, tema text renginde; PNG olarak kaydedilir. **📸 Servis Fotoğrafları** 3 sütun kare grid, maks. 5, "📷 Çek / Yükle" (kamera + galeri).
- **Sabit alt bar (her adımda):** sol "GENEL TOPLAM" + tutar 20/900 green · ← (önceki, adım>1) · ileri butonu: "İş & Cihaz →", "Tahsilata İlerle →", "İmza & Foto →", son adımda **"✓ Fişi Kes"** (green). Bitince liste'ye döner, toast "🧾 Fiş kesildi · ₺X (· WhatsApp gönderildi)".
- Tutar biçimi: `₺` + tr-TR, 2 ondalık (₺1.950,00). `Toplam = seçili parçalar + işçilik + diğer`.

### 4. Tablet · Yatay (1194×834 referans)
- **Sol NavigationRail** 84dp: SK logo, 5 öğe (44×32 pill aktif göstergesi), en altta tema butonu.
- **Üst satır** (flex-wrap): breadcrumb "Ana Panel / Saha Randevuları" + başlık "Randevu & Servis Yönetimi" 24/900 · görünüm segmenti **☰ Liste / 🗓️ Takvim / 🗺️ Harita** · "🧾 Servis Fişi / Randevusuz · Acil" (amberC) · "📅 Yeni Randevu" (mavi gradyan, 46dp).
- **5 istatistik kartı:** TOPLAM RANDEVU 📅 · ONAY BEKLİYOR ⏳ · ONAYLANDI · SAHADA 🚐 · TAMAMLANDI ✅ · İPTAL ORANI ❌ — değer 26/900 + alt metin (%… aktif / başarılı / fire) + 4dp progress.
- **Liste görünümü:** `440dp | 1fr` iki bölme.
  - Sol: arama + durum chip'leri (wrap) + satırlar (tarih kısa + saat mono · 38dp avatar baş harfler · ad / "hizmet · ilçe" · durum pill). Seçili satır primaryC zemin + primary border.
  - Sağ **AppointmentDetailPane:** 54dp avatar, ad 19/900 + etiket, telefon mono, durum pill · 3 büyük aksiyon 54dp (📞 Ara, 💬 WhatsApp, 🧭 Rota) · bilgi kutuları (📅 TARİH & SAAT, 🔧 HİZMET, 📍 HİZMET ADRESİ · İLÇE, ⚠️ ARIZA & NOTLAR amberC — not yoksa "Özel not girilmedi") · mini harita alanı · alt bar: ✏️ Düzenle · ❌ İptal · (boşluk) · 🧾 Servis Fişi · ana aksiyon (✓ Tamamla / 🧾 Fiş).

### 5. Tablet · Dikey (834×1194 referans)
Aynı rail, başlık ve istatistikler (başlık satırı alt satıra kayar). **Liste görünümü:** arama + chip'ler, altında **2 sütunlu kart grid'i** (avatar, ad, "tarih · saat" mono, durum, hizmet, 📍 adres). Karta dokununca **ModalBottomSheet** ile AppointmentDetailPane açılır (tam genişlik, maks. %78 yükseklik, 24dp üst köşe, tutamaç çubuğu 44×5).

### 6. Tablet · Takvim (her iki yön)
- Araç çubuğu: ‹ › (38dp) · hafta etiketi "21 – 27 Eylül 2026" (ay değişirse "28 Eyl – 4 Eki 2026") · "{n} randevu" · sağda **Bu Hafta**.
- Gün başlıkları: **PZT SAL ÇAR PER CUM CMT PAZ** (10.5/800) + gün numarası 30dp daire; bugün primary daire, beyaz metin; bugünün sütunu primaryC zemin.
- Izgara: 52dp saat sütunu (09:00–19:00, 11 satır) + 7 gün. Satır yüksekliği **yatay 46dp, dikey 70dp**. Randevu bloğu: top = (saat−9)×satır + 2, yükseklik satır−4, durum container zemini + 3dp sol border durum renginde, içinde saat (mono 10/800), ad 11/800, hizmet 10. Blok → BottomSheet detay.
- **Şimdi çizgisi:** bugünün sütununda 2dp red çizgi + 8dp nokta, her dakika güncellenir.
- Açılışta prototipte geçen hafta gösteriliyor (örnek veri o haftada); gerçek uygulamada **bu hafta** açılmalı.

### 7. Tablet · Harita (her iki yön)
- Yatay: `360dp liste | 1fr harita`. Dikey: harita üstte 440dp, liste altta.
- Liste kartı: "🧭 Günün Rotası" + "{n} durak · {m} tamamlandı"; gün chip'leri (randevusu olan son 8 gün, "Bugün" / "2 Eyl · 3"). Duraklar saate göre sıralı; iptaller hariç: numara dairesi (tamamlandıysa green, değilse primary), ad, saat mono, 📍 ilçe · adres, **🧭 Yol Tarifi** ve **Detay** butonları, durum pill'i. Altta "🗺️ Tüm rotayı Google Maps'te aç" (mavi gradyan, 46dp) → `https://www.google.com/maps/dir/?api=1&waypoints=...` intent.
- Harita: **Google Maps Compose** (`com.google.maps.android:maps-compose`). Numaralı özel marker (34dp, seçili 44dp, 3dp beyaz kenar, gölge), seçili marker üstünde "Ad · saat" balonu. Listedeki satır ↔ marker seçimi senkron. Prototipteki konumlar ilçeye göre yaklaşıktır; gerçekte adresler **Geocoder** ile koordinata çevrilip kaydedilmeli.

---

## Etkileşimler & Animasyonlar
- Ekran girişleri: fadeUp — opacity 0→1, translateY 10→0, 400–500ms ease, kartlar 50ms kademeli.
- Tam ekran form / sheet: translateY 40→0 + fade, 300–350ms ease.
- Basma: CTA scale 0.97, aksiyon butonları 0.95, 120–150ms.
- Tema geçişi: arka plan 300ms.
- Toast: üstte, text renk zemin / bg renk metin, 14 radius, 2.6 sn sonra kaybolur.
- 📞 Ara → `ACTION_DIAL`, 💬 WA → `https://wa.me/90XXXXXXXXXX`, 🧭 Rota → `google.navigation:q=<adres>`.
- ✓ Tamamla → durum `tamam` + toast "🎉 {ad} işi tamamlandı". ❌ İptal → `iptal` + "❌ Randevu iptal edildi". 🗑️ Sil → onay dialog'u ister.
- İstatistikler, chip sayıları ve takvim sayısı filtrelerle canlı güncellenir (StateFlow'dan türetilmiş).

## State (ViewModel)
```
AppointmentsUiState(
  appointments: List<Appointment>,
  statusFilter: All|Bekliyor|Onay|Tamam|Iptal = All,
  range: Today|Tomorrow|Week|Month|All = Month,
  query: String,
  expandedId: Long?,            // telefon kart genişletme
  selectedId: Long?,            // tablet detay
  tabletView: List|Calendar|Map,
  sheetOpen: Boolean,
  weekOffset: Int = 0,
  mapDate: LocalDate?           // null → en yoğun gün
)
Appointment(id, date, timeSlot "13:00 – 14:00", name, phone, district, neighborhood,
            address, tag Yeni|Eski, service, note, status)

QuickAppointmentState(name, phone, district, neighborhood, street, doorNo, source,
                      status, date, service, slot?, note)  // canSave = name && phone && slot

ReceiptState(step 1..4, customer…, brand?, model, jobText, selectedPartIds: Set,
             partSource, labor: BigDecimal, other: BigDecimal, warranty, tested,
             sendWhatsApp=true, technician?, payMethod Nakit|Kart|Havale,
             collection Paid|Partial|None, cashNote, addExpense,
             signatureCustomer: Bitmap?, signatureCompany: Bitmap?, photos: List<Uri> max 5)
  total = sum(parts) + labor + other
```
Veri kaynağı: web paneliyle aynı backend (mevcut Repository). Parça listesi Stok modülünden, kaynak alanıyla. Fiş kesilince: gelir kaydı + stoktan düşme + (açıksa) WhatsApp mesajı.

## Varlıklar
- İkonlar: tamamen emoji (📅🧾📞💬🧭✏️❌🗑️🔎📍⚠️🔥🛠️🔩🛡️👷📲💵💳🏦✍️📸 …). Ek ikon seti yok.
- Marka logoları: şimdilik renkli metin; gerçek logolar gelirse `res/drawable` altına.
- Harita: Google Maps API anahtarı gerekir (`local.properties` → `MAPS_API_KEY`).

## Ekran Görüntüleri (`screenshots/`, 2x)
**Telefon**
- `01-telefon-liste.png` — Liste üst kısmı: CTA'lar, istatistik, arama, filtreler, ilk kart
- `02-telefon-liste-kart-acik.png` — Kart genişletilmiş (Düzenle / İptal Et / Sil)
- `03-telefon-hizli-randevu-musteri.png` — Hızlı Randevu: müşteri & adres, hızlı tarih
- `04-telefon-hizli-randevu-saat.png` — Saat slot grid'i + arıza chip'leri (nota eklenmiş)
- `05-telefon-fis-adim1-musteri.png` — Servis Fişi adım 1
- `06-telefon-fis-adim2-cihaz-is.png` — Adım 2: marka + yapılan iş şablonları
- `07-telefon-fis-adim2-parcalar.png` — Adım 2: parça seçimi, işçilik, canlı toplam
- `08-telefon-fis-adim2-garanti-usta.png` — Adım 2: garanti, test, usta, WhatsApp
- `09-telefon-fis-adim3-tahsilat.png` — Adım 3: tahsilat dökümü, ödeme, durum
- `10-telefon-fis-adim4-imza-foto.png` — Adım 4: imzalar + fotoğraflar, "Fişi Kes"
- `20-telefon-acik-tema.png` — Açık tema

**Tablet**
- `11-tablet-yatay-liste-detay.png` — Yatay: liste + detay bölmesi
- `12-tablet-yatay-takvim.png` — Yatay: haftalık takvim
- `13-tablet-yatay-harita.png` — Yatay: günün rotası + harita
- `14-tablet-yatay-yeni-randevu-dialog.png` — Yeni Randevu dialog'u
- `15-tablet-yatay-servis-fisi-dialog.png` — Servis Fişi dialog'u (adım 2)
- `16-tablet-dikey-liste.png` — Dikey: 2 sütun kart grid'i
- `17-tablet-dikey-detay-sheet.png` — Dikey: alttan açılan detay paneli
- `18-tablet-dikey-takvim.png` — Dikey: takvim (70dp satır)
- `19-tablet-dikey-harita.png` — Dikey: harita üstte, liste altta
- `21-tablet-yatay-acik-tema.png` — Açık tema

Not: Bazı görüntülerde avatar baş harflerindeki "İ/Ç" küçük harf gibi görünüyor; bu ekran görüntüsü aracından kaynaklı. Uygulamada `uppercase(Locale("tr"))` ile büyük harf gösterilmeli.

## Dosyalar
- `Appointments Mobile Redesign.dc.html` — tüm ekranların interaktif prototipi (telefon + tablet yatay/dikey, liste/takvim/harita, hızlı randevu, 4 adımlı servis fişi). Mantık dosyanın altındaki `class Component` içinde: filtreler, slot doluluğu, toplam hesabı, imza çizimi.
- `android-frame.jsx`, `support.js` — prototipin çalışması için gerekli (uygulanmayacak).
- `reference-kotlin/` — mevcut tema ve ortak bileşenler (Color.kt, Theme.kt, TopHeaderBar.kt, BottomNavBar.kt, CustomersScreen.kt).

## Claude Code'a verilecek örnek istek
> "design_handoff_randevular/README.md ve prototipi incele. sancakkombi-admin-mobile reposunda Randevular ekranını, Hızlı Randevu ve Servis Fişi akışlarını Jetpack Compose ile uygula. Mevcut Color.kt/Theme.kt ve TopHeaderBar/BottomNavBar'ı kullan. Önce telefon listesi, sonra formlar, sonra tablet (WindowSizeClass) görünümleri. Her adımda derlenebilir kal."
