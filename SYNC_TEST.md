# Sync Test - AI Studio & Local Senkronizasyon Testi

## Guncel mobil test akisi

AI Studio ile GitHub'dan mobil repoyu cekip `Install` ile telefona veya
tablete kurma yontemi gecerlidir. Bu akisin calismasi icin mobil repodaki
guncel degisikliklerin once GitHub'a push edilmis olmasi gerekir. Yerel APK
uretmek icin GitHub veya AI Studio beklemek gerekmez.

### AI Studio / telefon-tablet

1. AI Studio'da mobil GitHub reposunu ac ve son commit'i cek.
2. Projeyi yeniden build et.
3. `Install` ile APK'yi telefona veya tablete kur.
4. Gercek backend ile giris, randevu, stok hareketi, tamamlanmis servis
   revizyonu ve WhatsApp durumunu kontrol et.

### Windows'ta yerel Gradle ve APK kurulumu

Bu bilgisayardaki üst klasör adında Türkçe karakter (`ı`) olduğu için Gradle
test worker'ı doğrudan çalıştırıldığında yolu bozabiliyor. Repoyu kopyalamadan
test ve debug APK üretmek için kökteki doğrulama betiğini çalıştır:

`& .\scripts\verify-debug.ps1`

Betik gerekirse boş bir sürücü harfini geçici ASCII yol olarak eşler,
`testDebugUnitTest` ve `assembleDebug` görevlerini çalıştırır ve eşlemeyi her
durumda kaldırır.

APK cikisi:

`app\build\outputs\apk\debug\app-debug.apk`

Android Studio ile kurulum: projeyi Android Studio'da ac, `app` konfigurasyonunu
calistir ve bagli telefonu sec. Komut satirindan kurulum icin USB hata ayiklama
acik bir cihazla:

`adb install -r app\build\outputs\apk\debug\app-debug.apk`

`adb` PATH'te degilse Windows'ta Android SDK'nin tam yoluyla da
calistirilabilir:

`& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r .\app\build\outputs\apk\debug\app-debug.apk`

Bagli cihazlari kontrol etmek icin:

`& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices`

Sadece APK üretmek için mevcut klasörde şu komut da kullanılabilir:

`.\gradlew.bat assembleDebug --no-daemon --no-configuration-cache`

`secrets.properties` yerel kalmali, GitHub'a gonderilmemelidir. Gemini anahtari
mobil APK'ya konulmaz; sesli randevu parse islemi web sunucusundan yapilir.

### Beni hatırla kabul testi

- `Beni hatırla` seçiliyken ilk başarılı girişten sonra uygulamayı tamamen kapatıp
  yeniden aç: parola sormadan panele dönmeli.
- `Beni hatırla` seçimini kaldırarak giriş yap: sonraki uygulama açılışında yeni
  parola girişi istenmeli; çıkış yapınca kayıtlı oturum da silinmeli.

### Kabul smoke listesi

- Gercek backend ile giris ve token yenileme/401 cikisi
- Randevu listeleme, yeni randevu ve tamamlanmis servis revizyonu
- Stokta giris/cikis, merkezi web stokla ayni miktar ve mutabakat
- Google Code Scanner ile düz barkod ve eski JSON QR etiketi okuma
- Bilinen kodda doğru stok detayının, bilinmeyen kodda dolu yeni ürün formunun açılması
- Ürün görselinin stok kartında gösterilmesi ve hata durumunda fallback ikonunun görünmesi
- Hizli IBAN ekraninda hesaplarin web backend'den gelmesi
- IBAN hesabi yoksa veya tutar bos/0 ise gonderim ve kopyalamanin engellenmesi
- Sesli randevuda bilinmeyen alanlarin uydurulmamasi
- WhatsApp baglanti durumunda tokenin istemciye donmemesi

AI Studio'ya guncel kodu almak icin once GitHub'a push edilmis commit gerekir.
GitHub push'u ayri bir yetkili adimdir; yerel APK testi bundan bagimsizdir.

Mobil repoya push yapildiginda `.github/workflows/android-debug.yml` workflow'u
Ubuntu ortaminda unit testleri ve debug APK build'ini otomatik calistirir.
Basarili calismanin APK'si GitHub Actions artifact'i olarak indirilebilir.

Son remote CI kaniti: `c1d3987` commit'i icin run `33446051627` basarili oldu;
Android SDK kurulumu, `testDebugUnitTest`, `assembleDebug` ve APK artifact
yuklemesi tamamlandi.

Son yerel doğrulama: `& .\scripts\verify-debug.ps1` ile `testDebugUnitTest` ve
`assembleDebug` başarılı. Üretilen dosya:
`app\build\outputs\apk\debug\app-debug.apk`.
