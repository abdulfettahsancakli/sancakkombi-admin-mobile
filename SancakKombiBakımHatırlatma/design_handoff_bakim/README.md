# Handoff: Periyodik Bakım Takvimleri (Mobil + Tablet)

Kaynak (web): `abdulfettahsancakli/SancakKombiWebSite` · `sancakkombi-web/app/admin/admin-nav-links.tsx` (yönetim menüsü). Ekran dosyası (`app/admin/...`) bu pakete dahil edilmedi; uygulamadan önce doğrulanmalı.

## Genel Bakış
Web admin panelindeki **Periyodik Bakım Takvimleri** ekranının telefon ve tablete uyarlanmış hâli. Müşteri bazında oluşturulan yıllık bakım hatırlatmaları listelenir, WhatsApp ile hatırlatma gönderilir, randevu oluşturulur veya takvim silinir.

Kapsam:
- Başlık, takvim sayısı, geri butonu
- Özet kartları: Aktif Takvimler, 30 Gün İçinde, Gecikmiş / Acil, İleri Tarih / Pasif
- Arama ve filtre çipleri: Tümü · Acil / Gecikmiş · 30 Gün İçinde · İleri Tarihli · Pasif
- Takvim kartları: müşteri, telefon, ilçe, cihaz, servis ve periyot, hatırlatma tarihi ve kalan süre, takip durumu, eylemler
- Tablette aynı kartlar iki sütunlu ızgarada

Dil: Türkçe. Metinler web ekranıyla birebir aynıdır, değiştirmeyin.

## Tasarım Dosyaları Hakkında
`Maintenance Calendars Mobile Redesign.dc.html` bir **tasarım referansıdır**, üretim kodu değildir. İki görünüm içerir:
1. Telefon · Hatırlatma Listesi (390×844 referans)
2. Tablet · Kart Izgarası (820×1180 referans)

Dosyayı tarayıcıda açın (`support.js` aynı klasörde olmalı). Logo `sancakkombiservislogo.jpg` ile aynı klasörden okunur. Dosyadaki `_ds/...` bağlantıları bu pakette yoktur.

Kapsamda yalnızca tek telefon ve tek tablet görünümü var. Tablette yalnızca iki kart gösterilir; diğer kayıtlar aynı kart yapısıyla eklenecek.

## Doğruluk (Fidelity)
**High-fidelity.** Renkler, ölçüler, köşe yarıçapları ve metinler nihaidir. HTML px = Compose **dp**, font px = **sp**.

## Design Tokens (web admin)
**Yüzeyler:** sayfa zemini #EEF3F8 (telefon içi #F4F6F9) · yüzey #FFFFFF · yüzey2 #F8FAFC · kenarlık #E2E8F0 · kırmızı kenarlık #FECACA (Gecikmiş kartı)
**Metin:** başlık #0F172A · metin2 #5B6678 · muted #94A3B8 · bölüm başlığı #073368 (müşteri adı)
**Birincil:** #006ECE (Aktif Takipte rozeti) · lacivert #073368 (aktif çip, tablet menü seçili) · açık mavi zemin #E0F2FE, metin #0E7490 (kalan süre hapı)
**Yeşil (WhatsApp / ana eylem):** WhatsApp buton zemini #ECFDF3, kenarlık #BBF7D0, metin #15803D · ikon #25D366 · Yeni Bakım Takvimi butonu #16A34A
**Randevu butonu:** kenarlık #CFE0F5, metin #006ECE
**Sil butonu:** kenarlık #FECACA, ikon #DC2626
**Uyarı (Gecikmiş başlığı):** metin #B91C1C

**Tipografi:** Nunito Sans (başlık, müşteri adı, büyük sayılar, rozetler) · Roboto (gövde). Ekran başlığı 19 (telefon) / 24 (tablet) /800, kart adı 15/800, sayı kutusu 22/800, etiket 10.5/800 büyük harf, gövde 12–13.

**Köşe / boşluk:** özet kartı 14, takvim kartı 16 (tablet) / 16, arama ve menü 12, çip 999, buton 10–12. Dokunma alanı en az 44dp. Eylem butonları 34dp (telefon) / 36dp (tablet); Compose'ta dokunma alanı 44dp'ye tamamlanmalı.

## Ekranlar

### 1. Telefon · Hatırlatma Listesi
1. **Üst çubuk:** logo + `sancakkombi /admin`
2. **Yönetim menüsü:** yatay kaydırılabilir (web `admin-nav-links.tsx` sırası): Genel Bakış · Randevular · Teklifler · Fiyat Kataloğu (Usta rozeti) · Stok · Finans · Müşteriler · Kampanyalar. Bu ekranda seçili öğe yok.
3. **Başlık satırı:** Geri · "Bakım Takvimleri" + sayı hapı
4. **Özet:** 2×2 kart
5. **Arama** ve **filtre çipleri** (yatay kaydırma)
6. **Takvim kartları:** 
   - Üst satır: ad, telefon + ilçe hapı · sağda "Aktif Takipte" rozeti
   - İki sütun bilgi kutusu: Cihaz · Servis (Her N gün)
   - Alt satır: tarih + "N gün sonra" hapı · WhatsApp · Randevu · Sil

### 2. Tablet · Kart Izgarası
1. **Üst çubuk:** logo + `sancakkombi /admin` + yatay yönetim menüsü (Bakım Takvimleri bu menüde yok, sayfa başlığında geçer)
2. **Başlık satırı:** Geri · "Periyodik Bakım Takvimleri" + "17 Takvim" hapı · sağda **Yeni Bakım Takvimi** (yeşil)
3. **Özet:** dört sütun
4. **Arama ve çipler:** tek satır
5. **Kart ızgarası:** iki sütun, aynı telefon kartı yapısı (eylemler kart altında sağa yaslı)

## Etkileşimler
- **WhatsApp:** müşteriye bakım hatırlatması gönderir (web davranışı, uygulamada doğrulanmalı).
- **Randevu:** ilgili müşteri için randevu oluşturma akışını açar.
- **Sil:** takvimi silmeden önce onay ister (tasarımda onay ekranı yok, eklenmeli).
- **Telefon numarası:** dokunulduğunda arama başlatır.
- **Kart seçimi:** tasarımda yok; web'de satır detayı var mı, doğrulanmalı.
- **Filtre çipleri:** seçili çip dolu lacivert zemin alır, diğerleri beyaz.
- Basma: kart 0.98, buton 0.97, 120 ms.

## Veri (web mevcut uçlar)
Veri uçları bu pakette tanımlanmadı. Kaynak ekran dosyası doğrulandıktan sonra eklenecek. Kartta gösterilen alanlar: müşteri adı, telefon, ilçe, cihaz (marka/model), servis türü, periyot (gün), sonraki hatırlatma tarihi, kalan gün, takip durumu (Aktif / Pasif).

## Açık Noktalar (uygulamadan önce doğrulanmalı)
1. **Hedef platform:** Android (Compose) için yazıldıysa, bakım takvimi ekranı Android repoda yok; API uçları mobil için sözleşmeye eklenmeli.
2. **Ekran kaynağı:** Web ekran dosyası (`app/admin/...`) bu pakette değil. Alanlar ve eylemler ekran koduyla karşılaştırılmalı.
3. **Yönetim menüsü:** Android'de alt gezinme (Panel · Randevu · Müşteri · Finans · Menü) kullanılıyor. Bakım Takvimleri nereden açılacak, karar verilmeli.
4. **Silme onayı ve randevu akışı:** tasarımda yok, eklenmeli.
5. **Tablet sayfa düzeni:** iki sütun kart ızgarası; web'deki tablo sütunları (Müşteri, Cihaz, Servis, Hatırlatma, Durum, İşlemler) kart içine taşındı. Gerçek veriyle kontrol edin.

## Varlıklar
- Logo: `sancakkombiservislogo.jpg` (bu klasörde). Android'de `res/drawable` altına taşınmalı.
- İkonlar: web kaynağı Lucide kullanır (LayoutDashboard, Calendar, FileText, Tag, Boxes, Wallet, Users, Megaphone, Search, Phone, MessageCircle, CalendarPlus, Trash2, ChevronLeft, Plus, History). Android'de Material eşdeğerleri kullanılabilir. WhatsApp için resmi WhatsApp logosu kullanılmalı.

## Dosyalar
- `Maintenance Calendars Mobile Redesign.dc.html`: telefon ve tablet görünümleri (tasarım referansı).
- `sancakkombiservislogo.jpg`: logo.
- `support.js`: prototip çalışması için (uygulanmayacak).
