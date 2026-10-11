# Google Play yeniden gönderim notları

## Uygulamadaki düzeltmeler

- Medya erişiminin amacı, işlenen veriler, yerel saklama ve paylaşım durumu uygulama içinde açıklanır.
- Kullanıcı açıklamayı kabul etmeden ana ekranlar ve medya tarama ViewModel nesneleri oluşturulmaz.
- Android medya izinleri yalnızca “Kabul et ve devam et” işleminden sonra istenir.
- “Şimdi değil”, geri veya ana ekran tuşu onay sayılmaz.
- Önceki kurulumda Android izni verilmiş olsa bile yeni açıklama için açık onay gerekir.
- MANAGE_EXTERNAL_STORAGE birleştirilmiş manifestten çıkarılır. Medya erişimi Android izinleriyle sınırlıdır; mevcut MediaStore işlemleri korunur.
- INTERNET izni yoktur. Medya ve tarama verilerinin Android bulut yedeği ve cihaz aktarımı kapatılmıştır.

## Play Console üzerinde tamamlanması gerekenler

1. Yeni sürümü mevcut upload anahtarıyla imzala. Projedeki versionCode 2; Play Console bu kodu daha önce kullandıysa daha yüksek bir kod seç.
2. Tüm dosyalara erişim beyanını yeni paketin izinleriyle uyumlu güncelle. Eski, uyumsuz sürümlerin yayın kanallarında aktif olmadığını kontrol et.
3. Data safety formunu ve gizlilik politikasını yeni paketin gerçek davranışıyla karşılaştır. Fotoğraf/video/ses işleme cihazda gerçekleşir; yerel tarama önbelleği ve çöp kutusu saklamasını açıkla. Diğer veri türlerini ayrıca doğrula.
4. Gerçek cihazda temiz kurulum ve eski sürümden yükseltmeyi dene: onay öncesinde tarama yok; ret/geri sonrası erişim yok; kabul sonrası Android izin penceresi; Android izni reddedilince çökme yok; seçilen medyanın analizi ve silme onayı çalışıyor.
5. İnceleme için açıklama ekranını, kabulü ve Android izin penceresini gösteren kısa video hazırla.
6. İmzalı AAB paketini yükle ve Publishing overview üzerinden incelemeye gönder.

## İnceleme notu taslağı (cihaz testi sonrası kullan)

We added an in-app prominent disclosure explaining access to photos, videos, audio and file metadata, the purposes of local media analysis, local scan caching and trash storage, and that these data are not uploaded or shared. Explicit acceptance is required before media permissions are requested or media-reading screens are initialized. Declining or navigating away does not grant consent. Existing installations also receive the new disclosure. MANAGE_EXTERNAL_STORAGE is absent from the merged release manifest; the app uses scoped media access and existing MediaStore operations. Android cloud backup and device transfer of app data have been disabled.

## Doğrulama sınırı

Kaynak kod düzeltmesi veya başarılı derleme Google Play kabulü anlamına gelmez. İmzalama, Console beyanları ve gerçek cihaz akışı ayrıca doğrulanmalıdır.
