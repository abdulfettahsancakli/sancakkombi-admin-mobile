# Randevular: Mobil ve Tablet Güncellemesi (v2)

Kaynak tasarım: `Appointments Mobile Redesign.dc.html` (bu klasördeki kopya). Web ekranındaki yeni özelliklere göre güncellendi.

## Varsayılanlar
- Tarih aralığı: Tüm Zamanlar
- Durum filtresi: Tümü
- Liste: ilk 10 kayıt, "Daha fazla göster" ile 10'ar artar

## Eklenenler
- Kapanmamış iş uyarı bandı: tarihi geçmiş "Onay" veya "Bekliyor" işler varsa üst kısımda gösterilir. (Mobil ve tablet)
- Tarih aralığı: Bugün, Yarın, Bu Hafta, Bu Ay, Tüm Zamanlar, Özel. Özel seçilince başlangıç ve bitiş tarihi alanları açılır. (Mobil)
- 5 istatistik kartı: Bekliyor, Sahada, Tamamlandı ve diğerleri, her biri yüzde metniyle. (Mobil ve tablet)
- Durum "Bekliyor" olarak eklendi. Kartta "Onayla" aksiyonu görünür.
- Hızlı aksiyonlar:
  - Onayla / Tamamla / Fiş (ana aksiyon, duruma göre değişir)
  - Beklet / Devam (ikincil aksiyon)
- Telefon kopyalama: numaraya dokunulunca panoya kopyalanır ve bildirim gösterilir. (Mobil kart, tablet detay paneli)
- Servis kaydı satırı (mobil kart):
  - Fiş kesildiyse yeşil "✓" ile "Servis fişi kesildi"
  - Fiş kesilmediyse amber "!" ile "Servis fişi kesilmedi"
- Servis fotoğrafları altında "Fişi WhatsApp ile gönder" anahtarı. (Servis akışı)

## Kaldırılanlar
- Bölge ve iş türü filtre çipleri (Tümü, Bayrampaşa, Güngören, Eyüpsultan vb.)
- Kartlardaki "Sil" butonu, yerine "Beklet" geldi
- Kartlardaki ve tablet detay panelindeki WhatsApp (WA) aksiyonları

## Açık noktalar
- Tablet satırlarında telefon kopyalama ve Beklet/Devam yok; sadece detay panelinde var.
- Tablet detay panelinde aksiyon satırı üç sütunlu; iki buton olduğunda sağda boşluk kalabilir.
- Kontrol, alt menünün kaydırılan liste başlığıyla çakıştığını bildirdi; ekran görüntüsüyle doğrulanmadı.
