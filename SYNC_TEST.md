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

Bu bilgisayardaki ust klasor adinda Turkce karakter (`ı`) oldugu icin Gradle
test worker'i mevcut uzun yolda sorun cikartabiliyor. Yerel Android testleri
icin repoyu su tip bir ASCII yola klonlamak gerekir:

`C:\Dev\SancakKombiMobile\sancakkombi-admin-mobile`

Ardindan:

`.\gradlew.bat testDebugUnitTest`

`.\gradlew.bat assembleDebug`

APK cikisi:

`app\build\outputs\apk\debug\app-debug.apk`

Android Studio ile kurulum: projeyi Android Studio'da ac, `app` konfigurasyonunu
calistir ve bagli telefonu sec. Komut satirindan kurulum icin USB hata ayiklama
acik bir cihazla:

`adb install -r app\build\outputs\apk\debug\app-debug.apk`

Bu bilgisayarda son temiz dogrulama, kaynaklarin ASCII yola aynen kopyalandigi
gecici bir calisma klasorunde yapildi; `testDebugUnitTest` ve `assembleDebug`
basarili oldu. Turkce karakterli mevcut klasorde Gradle test worker'i yolu
bozabildigi icin ayni yontem tekrarlandiginda ASCII bir klon kullanilmalidir:

`C:\Dev\SancakKombiMobile\sancakkombi-admin-mobile\gradlew.bat clean testDebugUnitTest assembleDebug --no-daemon --no-configuration-cache`

Sadece APK uretmek icin mevcut klasorde de su komut yeterlidir:

`.\gradlew.bat assembleDebug --no-daemon --no-configuration-cache`

`secrets.properties` yerel kalmali, GitHub'a gonderilmemelidir. Gemini anahtari
mobil APK'ya konulmaz; sesli randevu parse islemi web sunucusundan yapilir.

### Kabul smoke listesi

- Gercek backend ile giris ve token yenileme/401 cikisi
- Randevu listeleme, yeni randevu ve tamamlanmis servis revizyonu
- Stokta giris/cikis, merkezi web stokla ayni miktar ve mutabakat
- Hizli IBAN ekraninda hesaplarin web backend'den gelmesi
- IBAN hesabi yoksa veya tutar bos/0 ise gonderim ve kopyalamanin engellenmesi
- Sesli randevuda bilinmeyen alanlarin uydurulmamasi
- WhatsApp baglanti durumunda tokenin istemciye donmemesi

AI Studio'ya guncel kodu almak icin once GitHub'a push edilmis commit gerekir.
GitHub push'u ayri bir yetkili adimdir; yerel APK testi bundan bagimsizdir.

Mobil repoya push yapildiginda `.github/workflows/android-debug.yml` workflow'u
Ubuntu ortaminda unit testleri ve debug APK build'ini otomatik calistirir.
Basarili calismanin APK'si GitHub Actions artifact'i olarak indirilebilir.

Son yerel dogrulama: `testDebugUnitTest` ve `assembleDebug` basarili.
