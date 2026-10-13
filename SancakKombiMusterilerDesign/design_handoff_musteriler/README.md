# Handoff: Müşteri Yönetim Merkezi (Mobil + Tablet)

Repo: `abdulfettahsancakli/sancakkombi-admin-mobile` (Kotlin · Jetpack Compose · Material 3)
Web karşılığı: https://www.sancakkombi.com/admin/customers

## Genel Bakış
Web admin panelindeki **Müşteri Yönetim Merkezi** (Müşteri Rehberi + müşteri detayı: Profil & Adres, Kombi & Cihaz, Randevular, Finans & Tahsilat, WhatsApp Gönderimlerimiz), **Yeni Müşteri Kaydı Ekle** formu ve yeni **Tüm Rehberi Aktar** akışının telefon ve tablete uyarlanmış hâli. Randevular / Dashboard ekranlarındaki dil aynen devam eder: sky-blue #0EA5E9, emoji ikonlar, koyu/açık tema, sıcak Türkçe metinler.

## Tasarım Dosyaları Hakkında
Bu paketteki HTML dosyaları **tasarım referansıdır** — görünüm ve davranışı gösteren prototiplerdir, doğrudan kopyalanacak üretim kodu değildir. Görev, bu tasarımları mevcut Android uygulamasında **Jetpack Compose + Material 3** ile, repodaki mevcut desenleri (`ui/theme`, `ui/components`, `ui/screens`, mevcut ViewModel/Repository) kullanarak yeniden oluşturmaktır.

`Customers Mobile Redesign.dc.html` dosyasını tarayıcıda açın (`support.js` ve `android-frame.jsx` aynı klasörde olmalı). Solda telefon, sağda tablet; hepsi tıklanabilir:
- Telefon üstü sekmeler: **☰ Liste / 👤 Müşteri Kartı / 🙋 Yeni Müşteri / 📇 Rehberi Aktar**
- Tablet üstü: **↔ Yatay / ↕ Dikey**
- ☀️/🌙 ile tema değişir.

`reference-kotlin/` önceki turlardan gelen tema ve ortak bileşenlerdir (Color.kt, Theme.kt, TopHeaderBar.kt, BottomNavBar.kt) ve mevcut **CustomersScreen.kt** (bu tasarımla değiştirilecek). Renk ve bileşenleri buradan kullanın.

## Doğruluk (Fidelity)
**High-fidelity.** Renkler, ölçüler, köşe yarıçapları, metinler ve etkileşimler nihaidir. HTML px = Compose **dp**, font px = **sp**.

---

## Önerilen Dosya Yapısı
```
ui/screens/customers/
  CustomersScreen.kt            // WindowSizeClass → Phone / TabletLandscape / TabletPortrait
  CustomersViewModel.kt         // liste, arama, ilçe, filtre, seçim, istatistik, düzenleme
  components/
    CustomerCard.kt             // telefon liste kartı (📞 / 💬 hızlı butonlar)
    CustomerRow.kt              // tablet liste satırı (yatay: tek sütun, dikey: 2 sütun)
    CustomerStatCard.kt, FilterChipsRow.kt, DistrictDropdown.kt
  detail/
    CustomerDetailPane.kt       // hero + aksiyonlar + sekmeler; telefon ekranı, tablet bölmesi ve BottomSheet'te ortak
    ProfileTab.kt, DeviceTab.kt, AppointmentsTab.kt, FinanceTab.kt, WhatsAppTab.kt
  new/NewCustomerForm.kt        // telefonda tam ekran, tablette Dialog
  import/ContactImportFlow.kt   // + ContactImportViewModel.kt
```
Navigasyon: telefonda `CustomerDetail`, `NewCustomer`, `ContactImport` ayrı route. Tablette yatayda detay sağ bölmede; dikeyde `ModalBottomSheet` (%86 yükseklik). Yeni Müşteri (600dp) ve Rehber Aktar (680×640dp) tablette **Dialog** olarak açılır, arka ekranı değiştirmez.

---

## Design Tokens
Randevular paketindekiyle aynı (Color.kt):

**Koyu (varsayılan):** bg #090D16 · surface #131B2E · surface2 #1E293B · border #334155 · text #F8FAFC · text2 #94A3B8 · primary #0EA5E9 (c rgba(14,165,233,.16)) · green #22C55E (c .14) · amber #F59E0B (c .14) · red #F87171 (c .14) · violet #A78BFA (c rgba(167,139,250,.14))

**Açık:** bg #F4F7FA · surface #FFFFFF · surface2 #EEF2F6 · border rgba(15,23,42,.10) · text #0F172A · text2 #64748B · primary #0284C7 (c .10) · green #16A34A · amber #B45309 · red #DC2626 · violet #7C3AED

**Gradyan:** Primary CTA linear 135° #0EA5E9 → #0369A1 (Yeni Müşteri, Randevu, Kaydet, Aktar).

**Avatar:** yuvarlatılmış kare (telefon kart 46dp/14r, detay 58dp/16r, tablet satır 42dp/12r), düz renk zemin + beyaz baş harfler 900. Palet (id % 7): #0EA5E9 #14B8A6 #8B5CF6 #F59E0B #22C55E #EC4899 #3B82F6. Baş harfler: ilk iki kelimenin ilk harfi, `uppercase(Locale("tr"))` (İbrahim Soydan → "İS").

**Tipografi (Roboto):** ekran başlığı 18/800 (telefon), 24/900 −0.4 (tablet) · müşteri adı: kart 14.5/800, detay 19–20/900 · bölüm başlığı 13.5/800 · etiket 11.5/700 text2 · mikro etiket 10/800 +0.4 BÜYÜK HARF · telefon numarası **Monospace** 12.5/700 (liste), 16/800 (detay).

**Köşe / boşluk:** kart 18 (telefon), panel 20 (tablet), input 12, buton 12–16, chip 20 (pill), sheet üst 28, dialog 24. Ekran padding 12 (telefon), 18–20 (tablet). Min dokunma: input 48dp, ana buton 52dp, satır aksiyon 42dp.

**Rozetler:** ⚡ Aktif (green/greenC) · 🔥 Marka (amber/amberC) · 📍 İlçe (text2, 1dp border) · ✓ Tanımlı Cihaz (green metin).

---

## Ekranlar

### 1. Telefon · Müşteri Listesi
`LazyColumn`, padding 12, gap 10, alt boşluk 110 (FAB + yüzen nav).
1. **TopHeaderBar** (mevcut).
2. Başlık "Müşteriler" 18/800 + "Portföyün büyüyor, harika gidiyorsun 💪"; sağda pill "● {toplam} Müşteri".
3. **4'lü istatistik** (surface kart, 14r): 300 Portföy 👥 (text) · 4 Aktif 🛠️ (green) · 0 Sadık 💜 (violet) · 6 Kombi 🔥 (amber). Değer 19/900, etiket 9.5/700.
4. **📇 Tüm Rehberi Aktar** kartı: primaryC zemin, 1dp primary border, 16r; alt metin "Telefondaki kişileri tek seferde portföye ekle", sağda ›. → Ekran 7.
5. **Arama + ilçe:** 🔎 "Ad, telefon, ilçe, kombi markası..." (ad + telefon (boşluklu/boşluksuz) + ilçe + marka içinde, `tr` küçük harf). Yanında 📍 ilçe dropdown: "Tüm İlçeler", "Bayrampaşa (10)"…
6. **Filtre chip'leri** (yatay kaydırma, `flex:none`): Tümü 300 · Aktif Randevu 4 (green nokta) · Sadık (2+) 0 (violet) · Kombi Bilgisi 6 (amber). Seçili: primaryC zemin + primary border/metin.
7. **CustomerCard** (surface, 18r, padding 12): avatar 46 · ad 14.5/800 + ⚡ Aktif · telefon mono · "📍 İlçe · 🔥 Marka · N servis" · sağda **📞** (primaryC) ve **💬** (greenC) 42dp kare butonlar (tıklama karta yayılmaz). Karta dokununca → Müşteri Kartı (Profil sekmesi). Basınca scale 0.98.
8. Alt bilgi: "12 / 300 müşteri gösteriliyor · kaydırdıkça yüklenir" → **sayfalı yükleme** (Paging 3).
9. Boş durum: "🙈 Bu filtrede müşteri yok. Aramayı temizle ya da yeni müşteri ekle."
10. **FAB** "🙋 Yeni Müşteri" (sağ alt, nav'ın 96dp üstü, 54dp, 18r, gradyan, gölge 0 10 24 rgba(14,165,233,.4)).
11. **BottomNavBar**: Panel · Randevu · **Müşteri (aktif)** · Finans · Menü.

### 2. Telefon · Müşteri Kartı
Üst bar: ‹ geri (40dp) · "Müşteri Kartı" · "🗄️ Arşivle" (outline).
- **Hero kart:** avatar 58 · ad 19/900 · rozetler (📍 ilçe, 🔥 marka, ⚡ Aktif). Telefon kutusu (surface2): numara 16/800 mono · "📋 Kopyala" · "· Kayıt 21.09.2026". **4'lü aksiyon** 58dp: 📞 Ara (primaryC) · 💬 WhatsApp (greenC) · 🧭 Harita (surface2) · 📅 Randevu (gradyan).
- **Sekmeler** (yatay kaydırma, 38dp, 12r): 👤 Profil · 🔥 Kombi ✓ · 📅 Randevu {n} · 💳 Finans {n} · 💬 Gönderimler {n}. Seçili primaryC + primary border.
- **Profil:** Ad Soyad* · Telefon (mono) · İlçe (dropdown) + Bina/Kat/Zil · Açık Adres (textarea 2 satır) · **⚠️ Müşteri Özel Servis Notu** (amberC zemin/border). "💬 Otomatik WhatsApp Bildirimleri" switch kartı (50×30, açık = green). Sabit alt bar: **✓ Değişiklikleri Kaydet** (52dp, gradyan) — sadece Profil sekmesinde.
- **Kombi:** cihaz kartı: 48dp 🔥 kutusu (amberC) · marka pill (primaryC) + "✓ Tanımlı Cihaz" · model başlığı (yoksa "Model ve kapasite detayı girilmemiş") · MARKA / MODEL / KAPASİTE kutuları · "ℹ️ MONTAJ, BACA & GARANTİ NOTLARI" · "✏️ Kombiyi Düzenle / Değiştir". Marka yoksa kesikli boş durum + "+ Kombi Bilgisi Ekle". Altında **🛡️ Servis & Garanti Geçmişi** kayıtları (başlık + "✓ 12 Ay Servis Garantisi" rozeti + açıklama).
- **Randevular:** satırlar (54dp gün kutusu "21 / Eyl", hizmet 13.5/800, saat mono, durum pill: Onaylandı primary, Tamamlandı green). Boş: "📭 Bu müşterinin randevusu yok." · "📅 Yeni Randevu Oluştur".
- **Finans:** 3 kutu TOPLAM / TAHSİL (green) / KALAN (bakiye > 0 ise amber) · fiş satırları (🧾, başlık, "SF-2026-0147 · tarih" mono, tutar 900, durum ✓ Ödendi / Kısmi · ₺X / Bekliyor) · bakiye varsa **"💵 ₺X Tahsilat Al"** (green) → ödendi yapar.
- **Gönderimler (WhatsApp):** bkz. Ekran 3.

### 3. WhatsApp Gönderimlerimiz sekmesi (telefon + tablet)
Sadece **bizim gönderdiğimiz** mesajların kaydı (müşteri cevapları gösterilmez).
- Üst satır: "📤 {n} gönderim · {a} otomatik · {m} manuel" + sağda "● Otomatik bildirimler açık/kapalı" (green / text2; Profil'deki switch'e bağlı).
- Balonlar sağa hizalı, greenC zemin (başarısızsa redC), köşe 16 16 4 16, maks. %88 (tablet %72): başlık 10.5–11/800 green + tür rozeti **⚙️ Otomatik** (surface2/text2) veya **✍️ Manuel** (primaryC/primary) · metin · alt satır tarih + durum:
  - `sent` ✓ Gönderildi (text2) · `delivered` ✓✓ İletildi (text2) · `read` ✓✓ Okundu (primary) · `failed` ⚠️ Başarısız · tekrar dene (red).
- **"💬 Yeni Mesaj Gönder"** → altında şablon paneli açılır (buton "✕ Şablonları Kapat" olur). "HAZIR ŞABLON SEÇ": ⏰ Randevu Hatırlatma · 🚐 Usta Yolda · 💳 Ödeme Hatırlatma · 🔧 Yıllık Bakım Zamanı. Metinler müşterinin adı ve bakiyesiyle doldurulur. "Gönder ›" → listeye `manual/sent` olarak eklenir, toast "💬 "Şablon" gönderildi". Durum güncellemeleri gerçekte WhatsApp Business API webhook'larından gelir (prototipte 1.5 sn → İletildi, 4 sn → Okundu simüle edilir).
- Otomatik mesaj örnekleri: Hoş geldiniz (kayıtta), Randevu onayı, Servis tamamlandı.

### 4. Telefon · Yeni Müşteri Kaydı (tam ekran)
Üst: ✕ · "Yeni Müşteri Kaydı" / "Portföye ekle ya da rehberden aktar".
- **📇 Rehberden Seç** (56dp, 1.5dp kesikli primary, primaryC) → sistem kişi seçici (`ActivityResultContracts.PickContact`), ad + telefonu doldurur.
- **👤 Kişi & Adres:** Ad Soyad* ("Örn. Fatime Yılmaz") · Cep Telefonu* (mono, "05xx xxx xx xx") · İlçe (dropdown) + Bina / Kapı Notu · Açık Adres (textarea).
- **🔥 Kombi** "İsteğe bağlı": marka chip'leri E.C.A., Demirdöküm, Buderus, Vaillant, Baymak, Bosch (tek seçim, tekrar dokununca kalkar) + Model input.
- Sabit alt: **Vazgeç** + **🙋 Müşteri Kaydını Oluştur**. Doğrulama: ad veya telefon boşsa toast "⚠️ Ad soyad ve telefon zorunlu". Kayıtta listenin başına eklenir, seçilir, Müşteri Kartı açılır, toast "🎉 {ad} portföye eklendi". Telefon numarası mükerrerse kayıt engellenmeli (backend).

### 5. Tablet · Yatay (1194×834)
- **NavigationRail** 84dp (mevcut): Panel · Randevu · **Müşteri** · Finans · Menü · altta tema.
- Üst satır: "Ana Panel / Müşteri CRM" + "Müşteri Yönetim Merkezi" 24/900 · **📇 Tüm Rehberi Aktar** (surface2, 46dp) · **🙋 Yeni Müşteri Ekle** (gradyan, 46dp).
- **4 istatistik kartı:** MÜŞTERİ PORTFÖYÜ 👥 300 "Kayıtlı" · AKTİF SERVİSTE 🛠️ 4 "İşlem / Beklemede" · SADIK (2+ SERVİS) 💜 0 "%0 sadakat" · KOMBİ KAYITLI 🔥 6 "%2 cihaz kartı". Değer 26/900.
- `380dp | 1fr` iki bölme (gap 14):
  - **Sol:** arama + ilçe dropdown, chip'ler (wrap), satırlar (seçili: primaryC zemin + primary border), altta "12 / 300 müşteri gösteriliyor".
  - **Sağ CustomerDetailPane:** hero (avatar 56, ad 20/900, rozetler, telefon mono + Kopyala + Kayıt tarihi, sağ üstte 🗄️ Arşivle 42dp ikon butonu) · aksiyon satırı **tek satır, taşarsa yatay kaydırma** (42dp): 💬 WhatsApp · 📞 Ara · 🧭 Harita · 🧾 Son Fiş (fiş varsa, amberC) · 📅 Yeni Randevu (gradyan) · sekme çubuğu (bg zemin, seçili sekme surface + border; kısa etiketler Profil / Kombi / Randevu / Finans / Gönderimler) · içerik (Profil formu **2 sütun**) · Profil'de alt bar sağda **✓ Değişiklikleri Kaydet** (50dp).

### 6. Tablet · Dikey (834×1194)
Aynı rail/başlık/istatistik. Liste tam genişlik, **2 sütunlu satır grid'i** (surface2 kartlar). Satıra dokununca **ModalBottomSheet**: %86 yükseklik, 28dp üst köşe, 44×5 tutamaç, gölge 0 −20 60 rgba(0,0,0,.4), arkada rgba(2,6,23,.55) scrim (dokununca kapanır). Hero'da 🗄️ yanında ✕ kapat.

### 7. Tüm Rehberi Aktar (telefon tam ekran · tablet Dialog 680×640)
Üst: 📇 42dp kutu · "Tüm Rehberi Aktar" · alt metin (seçim adımında "Rehberde 14 kişi bulundu · 11 aktarılabilir") · ✕.
1. **İzin:** 84dp 📇 · "Rehbere erişim izni" 19/900 · açıklama · 3 bilgi satırı (🔁 Aynı numara iki kez eklenmez · 👪 Anne, kardeş gibi kişisel kayıtlar seçilmeden gelir · ☎️ Sabit hatlar ayrıca işaretlenir) · **Rehbere Eriş** (52dp gradyan) → `READ_CONTACTS` runtime izni. Reddedilirse: açıklama + "Ayarlara git".
2. **Tarama:** 🔍 "Rehber taranıyor… / Kayıtlı müşterilerle karşılaştırılıyor". `ContactsContract.CommonDataKinds.Phone` sorgusu (IO dispatcher).
3. **Seçim:**
   - Segment: **Aktarılabilir (11)** / **Zaten Kayıtlı (3)** · arama · "Tümünü seç" kutusu + "{n} seçili".
   - Satır: 24dp checkbox (seçili primary zemin, beyaz ✓) · 40dp avatar · ad · telefon mono · tür rozeti: **Yeni** (green) · **👪 Kişisel olabilir** (violet) · **☎️ Sabit hat** (amber) · **✓ Zaten kayıtlı** (surface2, %55 opaklık, seçilemez). Seçili satır primaryC zemin. Tablette 2 sütun.
   - Varsayılan seçim: yalnızca **Yeni** olanlar. Kişisel/sabit hat seçilmemiş gelir ama seçilebilir.
   - Alt panel: "📍 Varsayılan ilçe" dropdown (eklenenlerin ilçesi) · "💬 Otomatik WhatsApp bildirimleri" switch (varsayılan açık) · **📥 {n} Kişiyi Aktar** (seçim yoksa surface2/text2, toast "☝️ Önce en az bir kişi seç").
4. **Aktarım:** 📥 · "{n} kişi" · 8dp progress (1.3 sn ease-out) · "Portföye ekleniyor, mükerrer kontrolü yapılıyor…". Gerçekte toplu `POST` (batch), ilerleme gerçek sayıya bağlanmalı.
5. **Sonuç:** 🎉 · "{n} yeni müşteri eklendi" · "Portföyün büyüyor, eline sağlık!" · 3 kutu **Eklendi** (greenC) / **Zaten kayıtlı** / **Seçilmedi** · **Müşterilere Dön**. Eklenenler listenin başına düşer, toplam ve chip sayıları güncellenir.

**Eşleştirme kuralları:**
- Telefon normalizasyonu: yalnızca rakamlar; `+90` / `90` / baştaki `0` farkları eşitlenir (ör. 05301234567 ≡ +905301234567). Mükerrer = normalize numara mevcut müşteride var.
- Sabit hat: normalize numara `02`/`03`/`04` ile başlıyorsa (cep `05`).
- Kişisel olabilir: ad; anne, baba, abi, abla, kardeş, eşim, dayı, teyze, hala, amca, ev, iş… anahtar kelimelerinden birini içeriyor ya da emoji barındırıyor. (Prototip kural kümesi örnektir; listeyi yapılandırılabilir tutun.)
- Aynı kişide birden fazla numara varsa önce cep numarası alınır.

---

## Etkileşimler & Animasyonlar
- Giriş: fadeUp (opacity 0→1, translateY 10→0, 300–500ms ease). Tam ekran form / sheet: translateY 40→0 + fade, 350ms. Dialog: scale .96→1 + fade, 250ms.
- Basma: kart 0.98, FAB 0.95, 120ms. Tema geçişi 300ms.
- Toast: üstte ortada, text zemin / bg metin, 14r, 2.2 sn.
- 📞 → `ACTION_DIAL tel:` · 💬 → `https://wa.me/90XXXXXXXXXX` · 🧭 → `geo:0,0?q=<adres, ilçe>` · 📋 Kopyala → ClipboardManager + toast "📋 {numara} kopyalandı".
- 🗄️ Arşivle → onay dialog'u, sonra listeden çıkar (soft delete).
- Profil alanları anlık state'e yazılır; **Kaydet** ile backend'e `PATCH`, toast "✅ {ad} kaydedildi".
- Filtre/arama/ilçe birlikte çalışır; sayılar StateFlow'dan türetilir.
- Tüm yatay kaydırılan satırlar (`LazyRow`) kaydırma çubuğu göstermez.

## State (ViewModel)
```
CustomersUiState(
  customers: List<Customer>, total: Int,            // total = sunucudaki toplam (sayfalı)
  query: String, district: String? = null,
  filter: All|Active|Loyal|HasDevice = All,
  selectedId: Long?, detailTab: Profile|Device|Appointments|Finance|WhatsApp = Profile,
  sheetOpen: Boolean, waTemplatesOpen: Boolean
)
Customer(id, name, phone, district, address, gateNote, serviceNote, autoWhatsApp: Boolean,
         brand?, model?, deviceNote?, registeredAt, activeAppointment: Boolean, serviceCount: Int,
         history: List<ServiceRecord(title, warranty, text)>,
         billed: BigDecimal, paid: BigDecimal)
  isLoyal = serviceCount >= 2 ; hasDevice = brand != null ; balance = billed - paid

WhatsAppMessage(id, customerId, title, text, kind: Auto|Manual,
                status: Sent|Delivered|Read|Failed, sentAt)

NewCustomerState(name, phone, district="Bayrampaşa", gate, address, brand?, model)
  canSave = name.isNotBlank() && phone.isNotBlank()

ContactImportState(step: Permission|Scanning|Pick|Importing|Done,
                   contacts: List<DeviceContact(id, name, phone, kind: New|Personal|Landline|Duplicate)>,
                   selected: Set<Long>, tab: Importable|Duplicate, query,
                   defaultDistrict, autoWhatsApp=true, result: (added, duplicate, skipped)?)
```
Veri kaynağı web paneliyle aynı backend. Randevu / fiş / mesaj sekmeleri müşteri id'si ile ilgili modüllerden (Appointments, Finance, WhatsApp log) çekilir.

## Varlıklar
- İkonlar tamamen emoji. Ek ikon seti yok.
- Harita / rota Google Maps intent'i ile açılır.
- İzinler: `READ_CONTACTS` (rehber aktarma), `CALL_PHONE` gerekmez (ACTION_DIAL).

## Ekran Görüntüleri (`screenshots/`, 2x)
**Telefon**
- `01-telefon-liste.png` — Liste: istatistik, Rehberi Aktar kartı, arama + ilçe, filtreler, kartlar, FAB
- `02-telefon-musteri-karti-profil.png` — Müşteri kartı: hero, aksiyonlar, Profil formu, Kaydet
- `03-telefon-kombi-cihaz.png` — Kombi & Cihaz + Servis & Garanti Geçmişi
- `04-telefon-randevular.png` — Randevular sekmesi
- `05-telefon-finans.png` — Finans & Tahsilat
- `06-telefon-whatsapp-gonderimler.png` — Gönderimler: otomatik/manuel, durumlar, şablon paneli
- `07-telefon-yeni-musteri.png` — Yeni Müşteri Kaydı
- `08-telefon-rehber-izin.png` — Rehberi Aktar: izin
- `09-telefon-rehber-secim.png` — Rehberi Aktar: seçim listesi ve ayarlar
- `10-telefon-rehber-sonuc.png` — Rehberi Aktar: sonuç özeti
- `18-telefon-acik-tema.png` — Açık tema

**Tablet**
- `11-tablet-yatay-liste-profil.png` — Yatay: liste + detay (Profil)
- `12-tablet-yatay-kombi.png` — Yatay: Kombi & Cihaz
- `13-tablet-yatay-whatsapp-gonderimler.png` — Yatay: Gönderimler + şablonlar
- `14-tablet-yatay-yeni-musteri-dialog.png` — Yeni Müşteri dialog'u
- `15-tablet-yatay-rehber-aktar-dialog.png` — Rehberi Aktar dialog'u (2 sütun)
- `16-tablet-dikey-liste.png` — Dikey: 2 sütun liste
- `17-tablet-dikey-detay-sheet.png` — Dikey: alttan açılan detay
- `19-tablet-yatay-acik-tema.png` — Açık tema

Not: Bazı görüntülerde "İS" baş harfleri "İs" gibi görünebilir (görüntü aracı kaynaklı); uygulamada `uppercase(Locale("tr"))` kullanın.

## Dosyalar
- `Customers Mobile Redesign.dc.html` — tüm ekranların interaktif prototipi. Mantık dosyanın altındaki `class Component` içinde: filtreler, mükerrer/rehber sınıflandırma (`CONTACTS`, `digits`), WhatsApp şablonları ve durum akışı, form doğrulama.
- `android-frame.jsx`, `support.js` — prototipin çalışması için (uygulanmayacak).
- `reference-kotlin/` — mevcut tema ve ortak bileşenler.

## Claude Code'a verilecek örnek istek
> "design_handoff_musteriler/README.md ve prototipi incele. sancakkombi-admin-mobile reposunda Müşteriler ekranını (liste, müşteri kartı ve 5 sekme, Yeni Müşteri formu, Tüm Rehberi Aktar akışı) Jetpack Compose ile uygula; mevcut CustomersScreen.kt'yi bu tasarımla değiştir. Color.kt/Theme.kt ve TopHeaderBar/BottomNavBar'ı kullan. Sıra: telefon listesi → müşteri kartı sekmeleri → Yeni Müşteri → Rehber Aktar (READ_CONTACTS izni, numara normalizasyonu, mükerrer kontrolü) → tablet (WindowSizeClass, yatay bölme / dikey BottomSheet, Dialog'lar). Her adımda derlenebilir kal."
