# Emma - Türkçe Çevrimdışı TTS (Text-to-Speech) Android Uygulaması

**Emma**, herhangi bir sunucuya, Python ortamına veya internet bağlantısına ihtiyaç duymadan, **doğrudan Android cihazınızın donanımında (%100 çevrimdışı)** çalışan bağımsız, modern ve gizlilik odaklı bir Türkçe seslendirme uygulamasıdır.

Telefonunuzdaki herhangi bir uygulamada (Chrome, WhatsApp, Twitter, PDF okuyucu vb.) bir metin seçtiğinizde, tek dokunuşla cihazınızın yerel ses motoruyla anında seslendirilir.

---

## 🌟 Öne Çıkan Özellikler

1. **%100 Sunucusuz ve Çevrimdışı (On-Device & Offline)**:
   - Ne bir bilgisayara ne de uzak bir API sunucusuna ihtiyaç vardır.
   - İnternet bağlantısı gerektirmez; uçak modunda veya çekmeyen yerlerde bile kesintisiz çalışır.
   - **Tam Gizlilik:** Okuttuğunuz hiçbir metin veya ses cihazınızdan dışarı çıkmaz.

2. **Hızlı Seçim Menüsü Entegrasyonu (`ACTION_PROCESS_TEXT`)**:
   - Herhangi bir uygulamada metni seçtiğinizde açılan menüde (*Kopyala*, *Kes* yanında) doğrudan **"Emma ile Oku"** butonu belirir.
   - Dokunduğunuz anda seçim yapılan uygulama terk edilmeden seslendirme başlar.

3. **Android Paylaşım Menüsü Entegrasyonu (`ACTION_SEND`)**:
   - Metni seçip **Paylaş (Share)** dediğinizde açılan listeden **Emma**'yı seçebilirsiniz.
   - Metin otomatik olarak Emma'ya aktarılır ve ayarlanmışsa hemen okunur.

4. **Modern Jetpack Compose Arayüzü**:
   - Dinamik ses dalgası (waveform) animasyonu.
   - Çalma, duraklatma, durdurma ve baştan seslendirme kontrolleri.
   - **Oynatma Hızı Ayarı:** 0.75x, 1.0x, 1.25x, 1.5x.
   - **Ses Tonu (Pitch) Ayarı:** 0.7x ile 1.4x arası ince ayar.
   - **Türkçe Ses Seçimi:** Cihazınızda yüklü farklı Türkçe sesler arasında geçiş yapabilme.
   - **Geçmiş (History):** Daha önce dinlenen son 30 metni tekrar dinleme ve yönetme.

---

## 📁 Proje Yapısı

```
Ema/
├── android/                         # Yerel (Native) Android Studio Projesi
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── AndroidManifest.xml  # TTS sorgusu, Paylaşım ve Bağlam menüsü filtreleri
│   │   │   └── java/com/emma/tts/
│   │   │       ├── MainActivity.kt  # Intent yakalama ve Compose UI entegrasyonu
│   │   │       ├── EmmaApp.kt       # Application sınıfı
│   │   │       ├── data/
│   │   │       │   └── PreferencesManager.kt # Ses hızı, tonu ve geçmiş yönetimi
│   │   │       ├── service/
│   │   │       │   └── NativeTtsManager.kt   # Çevrimdışı Cihaz İçi Android TTS Yöneticisi
│   │   │       └── ui/
│   │   │           ├── components/  # Dalga boyu görselleştiricisi
│   │   │           ├── screens/     # Ana ekran ve ayarlar (MainScreen.kt)
│   │   │           └── theme/       # Koyu tema ve modern renk paleti
│   │   └── build.gradle.kts
│   └── build.gradle.kts
│
├── Emma.apk                        # Hazır derlenmiş Android APK (15 MB)
├── build_apk.sh                    # Tek komutla APK derleme betiği
└── README.md
```

---

## 📱 APK Kurulumu ve Derleme

### ⚡ 1. Hazır APK (Hemen Yükleyin):
Çalışma dizininde derlenmiş ve kullanıma hazır **[`Emma.apk`](file:///Volumes/KINGSTON/Documents/Ema/Emma.apk)** dosyası bulunmaktadır.
Bu dosyayı telefonunuza USB, Bluetooth, Google Drive veya WhatsApp üzerinden atıp üzerine dokunarak hemen kurabilirsiniz.

### 🛠️ 2. Android Studio Olmadan Tek Komutla Yeniden Derleme:
Kaynak kodda bir değişiklik yaptığınızda Android Studio'ya gerek kalmadan terminalden tek komutla yeni APK üretebilirsiniz:
```bash
./build_apk.sh
```
Bu betik projeyi derler ve güncel APK'yı doğrudan ana dizindeki `Emma.apk` dosyasına yazar.

### 💻 3. Android Studio ile Açmak İsterseniz:
1. Android Studio'da `Open` diyerek `android/` klasörünü seçebilirsiniz.
2. `Build > Build Bundle(s) / APK(s) > Build APK(s)` ile de derleyebilirsiniz.

---

## 🎯 Nasıl Kullanılır?

1. Telefonunuzda herhangi bir metni seçin (web sitesi, WhatsApp, notlar vb.).
2. Çıkan bağlam menüsünden doğrudan **"Emma ile Oku"** seçeneğine dokunun (veya **Paylaş > Emma** deyin).
3. Metin anında cihaz içi seslendiriciyle Türkçe olarak okunmaya başlayacaktır!
4. İsterseniz uygulama içindeki **Ayarlar (Çark)** menüsünden ses tonunu değiştirebilir veya daha yüksek kaliteli Google ses paketlerini cihazınıza indirebilirsiniz.
# Emma-Turkish-TTS
# Emma-Turkish-TTS
# Emma-Turkish-TTS
# Emma-Turkish-TTS
# Emma-Turkish-TTS
