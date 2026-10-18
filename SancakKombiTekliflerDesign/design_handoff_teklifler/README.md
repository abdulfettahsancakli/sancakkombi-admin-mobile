# Handoff: Fiyat Teklifleri (Mobil + Tablet)

Repo: `abdulfettahsancakli/sancakkombi-admin-mobile` (Kotlin · Jetpack Compose · Material 3)
Web karşılığı: https://www.sancakkombi.com/admin (Teklifler)

## Genel Bakış
Web admin panelindeki **Fiyat Teklifleri** modülünün telefon ve tablete uyarlanmış hâli. Kapsam:
- Teklif listesi (özet kartları, durum filtreleri, arama, teklif kartları)
- Teklif detayı (PDF paylaşımı, müşteri onayı, kâğıt görünümünde teklif belgesi)
- Yeni teklif formu (telefonda tam ekran, tablette dialog)

Dil, renk ve bileşenler Randevular ve Müşteriler paketleriyle aynıdır: sky-blue #0EA5E9, emoji ikonlar, koyu/açık tema, Türkçe metinler.

## Tasarım Dosyaları Hakkında
`Quotes Mobile Redesign.dc.html` bir **tasarım referansıdır**, üretim kodu değildir. Görevi, bu tasarımları repodaki mevcut Compose yapısıyla (`ui/theme`, `ui/components`, `ui/screens`, mevcut ViewModel ve model sınıfları) yeniden oluşturmaktır.

Dosyayı tarayıcıda açın (`support.js` ve `android-frame.jsx` aynı klasörde olmalı). Ekranda:
- Telefon üstü sekmeler: **📋 Liste / ✍️ Yeni Teklif**. Liste → kartın kendisine dokunarak detay açılır.
- Tablet: yatay, sol sütun liste, sağ sütun detay. **+ Yeni Teklif** dialog açar.
- ☀️/🌙 ile tema değişir.

`reference-kotlin/` içinde Color.kt, Theme.kt, TopHeaderBar.kt ve BottomNavBar.kt bulunur. Renk ve bileşenleri buradan kullanın.

## Kaynak Kotlin Dosyaları (mevcut, değiştirilecek)
- `app/src/main/java/com/example/ui/screens/ProposalsScreen.kt`: liste ve filtreler
- `app/src/main/java/com/example/ui/screens/NewProposalScreen.kt`: yeni teklif formu
- `app/src/main/java/com/example/ui/screens/ProposalDetailScreen.kt`: detay, PDF paylaşımı, kâğıt belge

## Doğruluk (Fidelity)
**High-fidelity.** Renkler, ölçüler, köşe yarıçapları ve metinler nihaidir. HTML px = Compose **dp**, font px = **sp**.

---

## Önerilen Dosya Yapısı
```
ui/screens/proposals/
  ProposalsScreen.kt            // WindowSizeClass → Phone / Tablet (liste + detay yan yana)
  ProposalsViewModel.kt         // liste, filtre, seçim, istatistik, durum güncelleme, silme
  components/
    ProposalCard.kt             // telefon kartı (Ara / WhatsApp / PDF)
    ProposalRow.kt              // tablet liste satırı
    ProposalStatCard.kt, StatusFilterRow.kt
  detail/
    ProposalDetailPane.kt       // telefon ekranı ve tablet bölmesinde ortak
    ProposalDocumentCard.kt     // kâğıt görünümlü teklif belgesi
  new/
    NewProposalForm.kt          // telefonda tam ekran, tablette Dialog
    PaymentTermsSection.kt      // tablet: peşinat, ödeme şekli, geçerlilik, not
```
Navigasyon: telefonda `ProposalDetail` ve `NewProposal` ayrı route. Tablette detay sağ bölmede açılır. Yeni teklif (560dp) tablette **Dialog** olarak açılır ve arka ekranı değiştirmez.

---

## Design Tokens
Randevu ve Müşteri paketleriyle aynı (Color.kt).

**Koyu (varsayılan):** bg #090D16 · surface #131B2E · surface2 #1E293B · border #334155 · text #F8FAFC · text2 #94A3B8 · primary #0EA5E9 (c #0F2233) · green #4ADE80 (c rgba(74,222,128,.14)) · amber #FBBF24 (c rgba(251,191,36,.14)) · red #F87171 (c rgba(248,113,113,.14))

**Açık:** bg #F4F7FA · surface #FFFFFF · surface2 #E7EEF2 · border rgba(0,0,0,.08) · text #1A1A1A · text2 #757575 · primary #0284C7 (c #E0F2FE) · green #16A34A · amber #B45309 · red #B91C1C

**Durum rozetleri** (`ProposalsScreen.kt` renkleri, koyu ve açık temada aynı):
- Beklemede: metin #B45309, zemin #FEF3C7, sol kenar #F59E0B
- Kabul Edildi: metin #065F46, zemin #D1FAE5, sol kenar #22C55E
- Reddedildi: metin #991B1B, zemin #FEE2E2, sol kenar #EF4444
- Süresi Doldu: metin #374151, zemin #F3F4F6, sol kenar border rengi

**Aksiyon renkleri:** Yeni Teklif, Teklifi Oluştur ve Kabul Edildi için #10B981 (Kotlin kaynağındaki yeşil); PDF ve birincil aksiyon için tema primary. Ara ve Görüntüle için primary; WhatsApp için #25D366; Sil ve Reddedildi için #EF4444.

**Belge (kâğıt) renkleri:** zemin #FFFFFF, kenarlık #D1D5DB, bilgi kutusu #F9FAFB / #E5E7EB, başlık kırmızısı #DC2626, toplam #10B981. Belge her zaman açık temadadır.

**Tipografi (Roboto):** ekran başlığı 18/800 (telefon), 22/900 −0.4 (tablet) · teklif no 11–14 monospace primary · müşteri adı kart 13.5/800, detay 22/900 · tutar kart 14/900, detay 20/900 · bölüm başlığı 13/800 · etiket 9.5–10/700 text2 · mikro etiket 8.5/800 · belge içi metin 9–11.

**Köşe / boşluk:** kart 14–16 (telefon), panel 16 (tablet), input 10–12, buton 12–16, rozet 8–9, dialog 20. Ekran padding 12 (telefon), 24–28 (tablet). Min dokunma: input 48dp, ana buton 52dp, satır aksiyon 42dp.

---

## Ekranlar

### 1. Telefon · Teklif Listesi
`LazyColumn`, padding 12, gap 10–12. Alt boşluk 110dp (FAB + yüzen nav).
1. **TopHeaderBar** (mevcut): logo, Çıkış, tema değiştirici.
2. Başlık "Fiyat Teklifleri" 18/800 + sayı rozeti ("{n} kayıt"). Altında 1 satır açıklama text2 11.5.
3. **2×2 özet kartları** (surface, 14r): TOPLAM TEKLİF HACMİ · BEKLEYEN (amber vurgu) · KABUL EDİLEN · RED / SÜRESİ DOLAN. Etiket 8.5/800, değer 15/900, alt metin 9.5.
4. **Arama alanı**: 🔍 "Müşteri, telefon, teklif no ara…" (surface, 14r).
5. **Durum chip'leri** (yatay kaydırma, `flex:none`): Tümü · Beklemede · Kabul Edildi · Reddedildi · Süresi Doldu, her birinde sayı. Seçili: primary zemin, onPrimary metin.
6. **Teklif kartı** (surface, 16r, sol 3dp kenarlık durum rengi, padding 12):
   - Üst satır: 36dp marka kutusu (2 harf, surface2) · marka + model 13/800 tek satır · teklif no mono primary 10 · sağda durum rozeti.
   - Alt satır: müşteri adı 13.5/800 · "telefon · tarih" 10.5 · sağda TOPLAM etiketi + tutar 14/900 primary.
   - Buton sırası: **WhatsApp** (greenC zemin, #25D366 metin) · **Ara** (primaryC) · **PDF** (surface2). Her biri 10r, 8dp dikey padding, eşit genişlik.
   - Karta dokunmak detayı açar (`ProposalDetail`).
7. Boş durum: "Bu kritere uygun teklif kaydı bulunamadı." (Kotlin kaynağındaki metin).
8. **FAB** "+ Yeni Teklif Oluştur": alt sabit, nav üstünde, 16r, primary zemin, gölge 0 8 24 rgba(0,0,0,.3).
9. **BottomNavBar** (mevcut): Panel · Randevu · Müşteri · Finans · Menü. Bu ekranda Teklifler, Menü altındaki modüllerden açılır.

### 2. Telefon · Teklif Detayı
Üst bar: ‹ "Teklifler" geri (38dp) · tema değiştirici.
1. **📄 Fiyat Teklifi PDF** (primary, tam genişlik, 14r). Tıklanınca indirme göstergesi "İndiriliyor..." olur (Kotlin davranışı).
2. **Müşteri Onayı** kartı (surface, 12r): solda etiket, sağda **✓ Kabul Edildi** (#10B981) ve **✕ Reddedildi** (#EF4444), 8r, 7dp/10dp padding.
3. **Teklif belgesi** (beyaz kâğıt, 12r, gölge 0 4 14 rgba(0,0,0,.25)):
   - Üst: "SANCAK KOMBİ" 16/900 kırmızı · "TEKNİK SERVİS" · "GÜVENLİK • KALİTE • HİZMET". Sağda "FİYAT TEKLİFİ", "Teklif No", "Tarih" ve "Hazırlayan".
   - Ayırıcı çizgi, ardından müşteri kutusu: "TEKLİF VERİLEN MÜŞTERİ BİLGİLERİ", "Sayın {ad}", Tel, Adres (varsa), Cihaz (varsa).
   - "TEKLİF DETAYLARI": numaralı satırlar, çift satırda #F9FAFB zemin.
   - Genel toplam kutusu: "GENEL TOPLAM (KDV DAHİL)" ve tutar, 15/900 yeşil.
   - "ÖDEME VE TEKLİF ÖZETİ": ara toplam, varsa iskonto (kırmızı), peşinat (nakit), kalan tutar, kalan ödeme şekli.
   - "TEKLİF ŞARTLARI": "Teklifimiz {geçerlilik tarihi} tarihine kadar geçerlidir.", "Montaj ve işçilik Sancak Kombi güvencesiyle 1 yıl garantilidir.", varsa not.
   - Alt satır: firma adresi ve telefonu, 9sp Gri, ortalı.

### 3. Telefon · Yeni Teklif (tam ekran)
Üst bar: ‹ "Teklifler" · tema değiştirici.
Başlık "Yeni Fiyat Teklifi" 18/800 + açıklama "Müşteriye verilecek fiyat teklifini oluşturun." Bölümler surface kart (14r, padding 14), her biri başlık 13/800:
- **Müşteri Bilgileri:** Ad Soyad* · Telefon / E-posta (yan yana) · İlçe · Adres (çok satır). Kotlin kaynağında ayrıca Cihaz Marka ve Cihaz Model alanları vardır (yan yana).
- **Teklif Kalemleri:** "Hazır hizmet / ürün fiyatları" (katalog kısayolları, en fazla 4 buton, 2 satırlı etiket), kalem kartları (Ürün / Hizmet, Adet, Birim Fiyat), "+ Kalem Ekle" (kesikli çerçeve), Ara Toplam, **İskonto** (sağda 100dp input), Genel Toplam (yeşil).
- **Ödeme Planı:** Peşinat Tutarı · Kalan Ödeme Şekli.
- **Ek Bilgiler:** Geçerlilik Tarihi (gg.aa.yyyy) · Teklifi Hazırlayan · Not / Şartlar.

Sabit alt buton: **Teklifi Oluştur** (52dp, primary, 16r). Kotlin davranışı: müşteri adı ve en az bir geçerli kalem yoksa buton pasif kalır. Toast "Müşteri ve en az bir geçerli ürün/hizmet girin". Başarıda toast "Yeni Teklif Oluşturuldu" ve liste açılır.

### 4. Tablet · Yatay (1040×640 referans)
- Üst bar: logo, Çıkış, tema değiştirici (60dp).
- **Sol bölme (380dp, sağda border):** başlık "Fiyat Teklifleri" + sayı rozeti + sağda **+ Yeni Teklif** (primary, 10r). 2×2 özet kartları (küçük). Durum chip'leri (wrap). Teklif satırları: sol sütun müşteri adı + teklif no, sağ sütun tutar + durum rozeti. Seçili satır: primaryC zemin, primary kenarlık.
- **Sağ bölme (flex 1, padding 24–28):** teklif no mono, müşteri adı 22/900, "telefon · tarih · marka model" 12. Sağda durum rozeti ve toplam 20/900.
  - **İŞ KAPSAMI** kartı: her kalem surface2 satır.
  - **DURUM** kartı: Beklemede / Kabul / Red seçenekleri (seçili: durum rengi zemin ve kenarlık).
  - Aksiyon ızgarası 3 sütun: WhatsApp Teklifi · Ara · PDF.
  - Alt ızgara 2 sütun: ✏️ Düzenle · 🗑️ Sil (Sil kırmızı metin).
- **Dialog · Yeni Teklif (560dp genişlik, %92 yükseklik):** üstte ✕, içerikte telefon formuyla aynı bölümler, ek olarak **Ödeme Koşulları & Teklif Notları** bölümü:
  - Peşinat Tutarı (TL): hazır seçenekler ₺1000, ₺1500, ₺2000, ₺2500, ₺3000, ₺4000, ₺5000 ve **Sıfırla** (kırmızı). Altta serbest tutar alanı.
  - Kalan Ödeme Şekli: kart seçimi **Nakit** (Peşin tahsilat), **Kredi Kartı** (Taksitli ödeme), **Havale / EFT** (IBAN ile). Seçili kart primary kenarlık ve primaryC zemin.
  - Teklif Geçerlilik Süresi: **7 Gün · 15 Gün · 30 Gün** (seçili 15 Gün).
  - Teklifi Hazırlayan, Not / Şartlar (çok satır) ve hızlı ekleme chip'leri: + Garanti Notu, + Ödeme Notu, + İşçilik Notu.
  - Alt bar: **İptal** (surface2) ve **Teklifi Oluştur** (primary, 1.4 pay).

---

## Etkileşimler
- Giriş: fadeUp (opacity 0→1, translateY 10→0, 300ms). Dialog: scale .96→1 + fade, 250ms.
- Basma: kart 0.98, FAB 0.95, 120ms. Tema geçişi 300ms.
- Toast: telefonda üstte, tablette altta ortada. Text zemin / bg metin, 14r, 1.8 sn.
- **PDF** → `downloadAndShareQuotePdf` (mevcut): `https://www.sancakkombi.com/api/quote/{id}` indirilir, `FileProvider` ile paylaşım sayfası açılır.
- **Ara** → `ACTION_DIAL tel:`. **WhatsApp** → `https://wa.me/90XXXXXXXXXX`.
- Kabul / Red → `ProposalStatus.APPROVED` / `REJECTED` güncellenir, toast ile onaylanır.
- Sil → onay dialog'u, sonra listeden çıkar.
- Filtre ve durum sayıları `StateFlow`'dan türetilir.

## State (ViewModel)
```
ProposalsUiState(
  proposals: List<Proposal>,
  query: String = "",
  filter: All|Pending|Approved|Rejected|Expired = All,
  selectedId: String?,
  isDownloadingPdf: Boolean,
  newProposalOpen: Boolean
)
Proposal(id, quoteNumber, customerName, customerPhone, customerEmail, customerDistrict,
         customerAddress, deviceBrand, deviceModel, date, validUntilDate, preparedBy, note,
         status: ProposalStatus, items: List<ProposalItem>, downPayment, remainingPaymentType, discount)
  subtotal = items.sum(totalPrice); grandTotal = subtotal - discount; remainingAmount = grandTotal - downPayment
ProposalStatus: DRAFT | PENDING | APPROVED | REJECTED   (label: Beklemede, Kabul Edildi, Reddedildi, Taslak)
```

## Açık Noktalar (uygulamadan önce doğrulanmalı)
1. **Durum filtresi:** Kotlin `ProposalsScreen.kt` 4 sekme (Tümü, Beklemede, Kabul Edildi, Reddedildi) kullanıyor. Tasarımdaki **Süresi Doldu** sekmesi Kotlin'de yok. Ekleyecekseniz `ProposalStatus`'a `EXPIRED` eklenmesi ya da tarihe göre hesaplanması gerekir.
2. **Beklemede sayısı:** Web yönetim panelinde 4, kartlarda 3 görünüyor. Kaynağın hangisi olduğu doğrulanmalı.
3. **Detay aksiyonları:** Ara, WhatsApp, Düzenle, Sil ve durum seçici tasarımda eklendi. Kotlin detay ekranında yalnızca PDF ve Kabul / Red var. Bu aksiyonlar onaylanırsa uygulanır.
4. **Liste aksiyonları:** Kotlin'de kartta Görüntüle ve Sil var. Tasarımda Ara / WhatsApp / PDF var.
5. **Tablet dialog alanları:** Peşinat seçenekleri (₺1000–₺5000), Kalan Ödeme Şekli kartları (Nakit / Kredi Kartı / Havale / EFT), 7/15/30 gün geçerlilik ve not chip'leri tasarımın ürettiği değerlerdir. Uygulamanın gerçek değerleriyle eşleştirilmeli.
6. **İskonto:** Kotlin formunda var, telefon tasarımında da var. Tablet dialogunda yok.
7. **Ek alanlar:** Kotlin formunda E-posta ve Cihaz Marka / Model alanları var. Tasarımda bu alanlar telefonda bulunuyor, tablet dialogunda yok.
8. **Teklif no formatı:** Kotlin `TF-yyyyMM-XXXXXX` üretiyor. Örnek verilerde aynı format kullanıldı.

## Varlıklar
- İkonlar emoji (📄 ✏️ 🗑️ 🔍 ☀️ 🌙). Ek ikon seti yok. Kotlin kaynağındaki Material ikonları (Add, Close, Check, Share, Description, Delete) korunabilir.
- Logo: `app/src/main/res/drawable/logo_sancakkombi.jpg`.
- Ekran görüntüsü klasörü bu pakette yoktur. Tasarım dosyasını tarayıcıda açarak inceleyin.

## Dosyalar
- `Quotes Mobile Redesign.dc.html`: tüm ekranların interaktif prototipi. Mantık dosyanın altındaki `class Component` içinde (`QUOTES` örnek verisi, filtreler, durum etiketleri, PDF ve onay aksiyonları).
- `android-frame.jsx`, `support.js`: prototipin çalışması için (uygulanmayacak).
- `reference-kotlin/`: Color.kt, Theme.kt, TopHeaderBar.kt, BottomNavBar.kt.

## Claude Code'a verilecek örnek istek
> "design_handoff_teklifler/README.md ve prototipi incele. sancakkombi-admin-mobile reposunda Teklifler ekranını Jetpack Compose ile uygula; mevcut ProposalsScreen.kt, NewProposalScreen.kt ve ProposalDetailScreen.kt'yi bu tasarımla değiştir. Açık Noktalar bölümündeki 1–8 maddelerini önce bana sor. Sıra: telefon liste → detay (PDF ve onay) → yeni teklif formu → tablet (WindowSizeClass, yatay bölme, dialog). PDF indirme mevcut downloadAndShareQuotePdf fonksiyonunu kullanmalı. Her adımda derlenebilir kal."
