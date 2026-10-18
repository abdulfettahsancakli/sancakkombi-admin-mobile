# Handoff: Modül Merkezi (Mobil + Tablet)

Repo: `abdulfettahsancakli/sancakkombi-admin-mobile` (Kotlin · Jetpack Compose · Material 3)
Web karşılığı: https://www.sancakkombi.com/admin (Modüller)

## Genel Bakış
Admin uygulamasındaki modül listesinin telefon ve tablet tasarımıdır. Kapsam:
- Modül listesi (2 sütunlu kartlar, aktif/kapalı durumu, özet sayısı)
- Modül detayı (aç/kapa anahtarı, KPI'lar, hızlı işlemler, son hareketler)
- Tablet görünümü: sol sütun liste, sağ sütun detay

Dil, renk ve bileşenler Randevular, Müşteriler ve Teklifler paketleriyle aynıdır: sky-blue #0EA5E9, emoji ikonlar, koyu/açık tema, Türkçe metinler.

## Tasarım Dosyaları Hakkında
`Modules Mobile Redesign.dc.html` bir **tasarım referansıdır**, üretim kodu değildir. Görevi, bu tasarımı repodaki mevcut Compose yapısıyla (`ui/theme`, `ui/components`, `ui/screens`) yeniden oluşturmaktır.

Dosyayı tarayıcıda açın (`support.js` ve `android-frame.jsx` aynı klasörde olmalı). Ekranda:
- Telefon üstü sekmeler: **📱 Liste / 📱 Detay**. Karta dokunmak detayı açar.
- Tablet: yatay, 1040×640 referans. Soldan bir modül seçilir, sağda detay görünür.
- ☀️/🌙 ile tema değişir.
- Modül detayında Teklifler işlemi `Quotes Mobile Redesign.dc.html` dosyasını açar. Diğer hızlı işlemler yalnızca bilgi mesajı (toast) gösterir.

`reference-kotlin/` içinde Color.kt, Theme.kt, TopHeaderBar.kt ve BottomNavBar.kt bulunur.

## Önerilen Dosya Yapısı
Kaynak Kotlin dosyası bu pakette yoktur. Aşağıdaki yapı öneridir ve repoya göre uyarlanmalıdır:
```
ui/screens/modules/
  ModulesScreen.kt          // WindowSizeClass → Phone (liste → detay route) / Tablet (yatay bölme)
  ModulesViewModel.kt       // modül listesi, aktiflik durumu, seçili modül
  components/
    ModuleCard.kt           // telefon kartı (2 sütun grid)
    ModuleRow.kt            // tablet liste satırı
    ModuleToggleRow.kt      // Modül açık / kapalı anahtarı
  detail/
    ModuleDetailPane.kt     // telefon ekranı ve tablet bölmesinde ortak
    ModuleKpiRow.kt         // 3 KPI kutusu
```
Navigasyon: telefonda detay ayrı route. Tablette detay sağ bölmede açılır.

---

## Design Tokens
Randevu ve Müşteri paketleriyle aynı Color.kt yapısı kullanılır. Bu ekran için değerler:

**Koyu (varsayılan):** bg #090D16 · surface #131B2E · surface2 #1E293B · border #334155 · text #F8FAFC · text2 #94A3B8 · primary #0EA5E9 · primaryC #0F2233 · green #22C55E (c rgba(34,197,94,.14)) · amber #F59E0B (c rgba(245,158,11,.14))

**Açık:** bg #F4F7FA · surface #FFFFFF · surface2 #E7EEF2 · border rgba(0,0,0,.08) · text #1A1A1A · text2 #757575 · primary #0284C7 · primaryC #E0F2FE · green #16A34A (c rgba(22,163,74,.10)) · amber #B45309 (c rgba(217,119,6,.12))

> Not: Teklif paketi koyu temada yeşil #4ADE80 ve amber #FBBF24 kullanıyor. Bu ekran #22C55E ve #F59E0B kullanıyor. Hangisinin Color.kt ile eşleşeceği Açık Noktalar bölümünde.

**Durum rozetleri** (tüm modüllerde ortak):
- Aktif: metin green, zemin greenC
- Dikkat: metin amber, zemin amberC
- Kapalı: metin text2, zemin surface2

**Tipografi (Roboto):** ekran başlığı 18/800 (telefon), 18/800 (tablet sol bölme) · detay adı 20/900 (telefon), 24/900 −0.4 (tablet) · modül adı kart 14.5/800, tablet satır 13.5/800 · açıklama 11 text2 (2 satır sınırlı) · KPI değeri 16/900 (telefon), 20/900 (tablet) · KPI etiketi 9.5/700 (telefon), 11/700 (tablet) · bölüm başlığı 11/800 text2, harf aralığı .4 · hızlı işlem 13.5/700 · toast 13/700.

**Köşe / boşluk:** modül kartı 18 (telefon), 16 (tablet satır 16); ikon kutusu 14 (telefon kart), 13 (tablet satır); detay ikonu 18 (telefon), 20 (tablet); KPI kutusu 14 (telefon), 16 (tablet); hızlı işlem 14; toast 14; alt yüzen nav 28. Ekran padding 12 (telefon liste), 14 (detay), 28/32 (tablet detay). Kart arası boşluk 10 (telefon grid), 8 (tablet liste).

**Dokunma alanı:** kart tamamı dokunulabilir. Anahtar 48×28dp. Hızlı işlem satırı en az 48dp yüksekliğinde.

---

## Ekranlar

### 1. Telefon · Modül Listesi
`LazyVerticalGrid` (2 sütun), padding 12, gap 10. Alt boşluk 110dp (yüzen nav için).
1. **TopHeaderBar** (mevcut): logo, Çıkış, tema değiştirici.
2. Başlık "Modüller" 18/800. Altında özet metni: "{n} / {toplam} modül açık".
3. **Modül kartları** (surface, 18r, 1dp border):
   - Üst satır: 44dp ikon kutusu (primaryC zemin, emoji 20) · sağda durum rozeti (10/800, 8r).
   - Alt blok: modül adı 14.5/800 · açıklama 11 text2, en fazla 2 satır.
   - Karta dokunmak detayı açar.
4. **BottomNavBar** (mevcut). Bu ekranda "Menü" sekmesi aktiftir.

### 2. Telefon · Modül Detayı
Üst bar: ‹ "Modüller" geri (38dp) · Çıkış · tema değiştirici.
1. Başlık satırı: 60dp ikon kutusu (primaryC, emoji 28) · modül adı 20/900 · açıklama 12 text2 · durum rozeti.
2. **Modül açık / kapalı** kartı (surface, 16r, 14dp padding): solda etiket 13.5/700, sağda 48×28 anahtar. Açıkken primary zemin ve knob sağda, kapalıyken surface2 zemin ve knob solda.
3. **KPI** 3 sütun grid: değer 16/900 · etiket 9.5/700 text2 (surface, 14r).
4. **HIZLI İŞLEMLER** başlığı, ardından her işlem için satır (surface, 14r, 13/14 padding, sağda ›).
5. **SON HAREKETLER** başlığı, ardından log kutuları (surface2, 12r).

### 3. Tablet · Yatay (1040×640 referans)
- Üst bar: logo, Çıkış, tema değiştirici (60dp).
- **Sol bölme (340dp, sağda border):** başlık "Modüller", özet metni, modül satırları. Satır: 40dp ikon · ad 13.5/800 · açıklama tek satır (kesilir) · sağda durum rozeti. Seçili satır: primaryC zemin ve primary kenarlık. Diğer satırlar: surface zemin ve border kenarlık.
- **Sağ bölme (flex 1, padding 28/32):**
  - Başlık satırı: 68dp ikon · modül adı 24/900 · açıklama · sağda durum rozeti.
  - Yatay satır: anahtar kartı (flex 1) + 3 KPI kutusu (flex 1 her biri).
  - İki sütun: **HIZLI İŞLEMLER** (sol) · **SON HAREKETLER** (sağ).
- Toast: alt ortada, 20dp yukarıdan.

---

## Etkileşimler
- Giriş: fadeUp (opacity 0→1, translateY 10→0, 400ms). Başlık için 500ms.
- Basma: modül kartı ve hızlı işlem satırı scale 0.98, 120ms.
- Anahtar: track rengi ve knob konumu 200ms geçişle değişir.
- Tema geçişi 300ms.
- Toast: 1.8 sn görünür, sonra kaybolur. Telefonda üstte, tablette altta ortada.
- Modül açık/kapalı durumu anlık değişir. Kapalı modül kartında durum "Kapalı" olur ve sayaç güncellenir.
- Teklifler hızlı işlemi → Teklifler ekranı (`Quotes Mobile Redesign.dc.html`). Uygulamada bu, Teklifler route'una gidiştir.
- Diğer hızlı işlemler: uygulamada ilgili modül ekranına gider. Prototipte toast gösterir.

## State (ViewModel)
```
ModulesUiState(
  modules: List<AdminModule>,
  selectedId: String?,
  isDark: Boolean,          // tema, uygulama genelinde
  toast: String?            // yalnızca prototip
)
AdminModule(id, name, emoji, description, status: ModuleStatus, enabled: Boolean,
            kpis: List<Kpi>, quickActions: List<QuickAction>, recentLog: List<String>)
ModuleStatus: OK | WARNING   // Kapalı, enabled=false'tan türetilir
  effectiveStatus = if (!enabled) OFF else status
  sayaç = modules.count { it.enabled }  → "{n} / {toplam} modül açık"
```

## Modül Listesi (örnek veri)
| id | Ad | Emoji | Varsayılan durum |
|---|---|---|---|
| musteriler | Müşteriler | 👥 | Aktif |
| randevular | Randevular | 📅 | Aktif |
| iban | Hızlı IBAN At | ⚡ | Aktif |
| mesaj | Mesaj Sistemi | 💬 | Aktif |
| finans | Finans | 💳 | Dikkat |
| istatistik | İstatistikler | 📊 | Aktif |
| teklif | Teklifler | 📝 | Aktif |
| bakim | Bakım Takvimleri | 🔧 | Aktif |
| ads | Google Ads | 📣 | Aktif (bkz. açık nokta 1) |
| stok | Stok | 📦 | Dikkat |

Örnek KPI ve hareket metinleri `Modules Mobile Redesign.dc.html` dosyasındaki `MODS` dizisindedir. Üretimde gerçek veriyle değiştirilmelidir.

## Açık Noktalar (uygulamadan önce doğrulanmalı)
1. **Google Ads varsayılanı:** Prototipte Google Ads'in başlangıçta kapalı olması amaçlanmıştı. Kod `m.id !== 'google-ads'` kontrolü yapıyor, ancak modülün id'si `ads`. Bu yüzden Google Ads açık görünüyor. Hangi davranışın istendiği netleştirilmeli.
2. **Renk paleti:** Koyu temada yeşil ve amber değerleri Teklif paketinden farklıdır (#22C55E / #F59E0B ve #4ADE80 / #FBBF24). Tek bir Color.kt değeri seçilmeli.
3. **Modül kaynağı:** Modül listesi prototipte sabit veridir. Repoda modül listesinin nereden geldiği (sabit liste mi, yetki/ayar mı) doğrulanmalı.
4. **Aktiflik kalıcılığı:** Anahtar durumunun kaydedilip kaydedilmeyeceği ve hangi ekranlara yansıyacağı belirsizdir.
5. **Dikkat durumu:** "Dikkat" durumu hangi veriye göre hesaplanacak (Finans: bekleyen ödeme, Stok: azalan parça) belirlenmeli.
6. **Alt navigasyon:** Prototipte alt bar Ana Sayfa, İşler, Müşteriler, Finans, Menü şeklindedir. Teklif paketinde Panel, Randevu, Müşteri, Finans, Menü kullanılır. Repodaki BottomNavBar.kt ile eşleşmelidir.
7. **Hızlı işlem hedefleri:** Teklifler dışındaki işlemler şimdilik yalnızca toast gösterir. Hedef ekranlar belirlenmeli.

## Varlıklar
- İkonlar emoji. Ek ikon seti yok. Kotlin tarafında Material ikonları korunabilir.
- Logo: `app/src/main/res/drawable/logo_sancakkombi.jpg`.

## Dosyalar
- `Modules Mobile Redesign.dc.html`: tüm ekranların interaktif prototipi. Veri `MODS`, mantık `class Component` içindedir.
- `android-frame.jsx`, `support.js`: prototipin çalışması için (uygulanmayacak).
- `reference-kotlin/`: Color.kt, Theme.kt, TopHeaderBar.kt, BottomNavBar.kt.

## Claude Code'a verilecek örnek istek
> "design_handoff_moduller/README.md ve prototipi incele. sancakkombi-admin-mobile reposunda Modül Merkezi ekranını Jetpack Compose ile uygula. Telefon liste → detay, tablet yatay bölme (WindowSizeClass). Açık Noktalar bölümündeki 1–7 maddelerini önce bana sor. Teklifler işlemi mevcut Teklifler route'una gitsin. Her adımda derlenebilir kal."
