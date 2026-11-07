# 1.2.0 doğrulama notları

- Hedef: Minecraft 26.3, Java 25, Fabric Loader 0.19.5, Fabric API 0.165.2+26.3.
- Derleme araçları: Gradle 9.6.0 ve Loom 1.17.21.
- Kurulum dosyası: `uebasusta123mcpvptieartagger-1.2.0.jar`.
- Portun kapsamı Minecraft 26.3 desteğidir; TAB/kafa üstü etiketleri, komutlar ve
  önceki ayarlar korunur.
- Tam Gradle derlemesi, otomatik regresyon kontrolleri ve Minecraft 26.3 mixin
  hedefleri için doğrulama sonucu henüz kaydedilmedi.
- Minecraft 26.3 veya DonutSMP'de oyun içi görsel test henüz yapılmadı.

## 1.2.0 için oyun içi kontrol

1. Minecraft 26.3 için Fabric Loader ve Fabric API'yi kur; eski mod JAR'ını çıkarıp
   `uebasusta123mcpvptieartagger-1.2.0.jar` dosyasını koy ve oyunu yeniden başlat.
2. `/utier debug` ile sürümün 1.2.0 olduğunu ve önceki ayarlarının yüklendiğini doğrula.
3. Tieri bilinen bir oyuncuyu TAB'da ve kafa üstünde kontrol et; normal isimde tek etiket görünmeli.
4. Normal etiketi gizlenen, TAB'da olmayan yakındaki bir oyuncuda ayrı tier satırını kontrol et.
5. Ayrı satır duvar arkasında, eğilirken, görünmezken, 64 blok ve ötesinde veya F1 açıkken görünmemeli.
6. `/utier fallback` ve `/utier nametag` ile kapatmayı, simgeleri ve yeniden bağlanmayı kontrol et.

## Önceki sürüm: 1.1.2 doğrulama notları

- Hedef: Minecraft 26.2, Java 25, Fabric Loader 0.19.3, Fabric API 0.153.0+26.2.
- Yeni çizim vanilla isim etiketi null/boş olsa da bağımsız bir tier bileşeni kullanır.
  Sunucu adı ve scoreText değiştirilmez; ayrı veri render state içinde taşınır.
- `EntityRenderer.extractNameTags(...)` ve final `submitNameDisplay(..., int)` hedefleri
  gerçek Minecraft 26.2 bytecode'unda doğrulandı. AvatarRenderer'ın bu gönderim
  metodunu çağırdığı ve `submitNameTag(..., false, ...)` kullanımının yalnızca
  normal, derinlik testi olan çizim kolunu kullandığı kontrol edildi.
- Regresyon kapsamı: normal/null/boş isimler, çift etiket oluşturmama, seçili kit,
  eski ayarların taşınması, yeni ayarın kapanması, görünürlük/mesafe/görüş hattı koşulları,
  gönderim parametreleri ve HTTP hatalarının gerçek boş sonuçtan ayrılması.
- Yerel Gradle denemesi Loom eklentisini çözemedi. Kaynaklar mevcut JDK/bağımlılıklar
  ile ayrıca derlenip regresyon kontrolleri çalıştırılır; tam Gradle derlemesi
  GitHub build/release iş akışlarında zorunludur.
- DonutSMP veya gerçek oyun penceresinde görsel test yapılmadı. Birim kontrolleri,
  mixin hedef kontrolü ve derleme, sunucu üstündeki görsel testin yerine geçmez.

## Oyun içi kontrol

1. Eski mod JAR'ını çıkarıp 1.1.2'yi kur; oyunu yeniden başlat.
2. `/utier debug` ile sürümün 1.1.2, nametag ve fallback değerlerinin true olduğunu doğrula.
3. MCPvP tieri bilinen, TAB'da olmayan yakındaki görünür oyuncuyu incele.
4. `/utier debug <oyuncu>` ile profil, dünya/TAB ve MCPvP sonucunu karşılaştır.
5. Normal isimli bir oyuncuda tek etiket olduğunu kontrol et.
6. Ayrı etiket duvar arkasında/eğilirken/görünmezken, uzaklaşınca veya F1 ile görünmemeli.
7. `/utier fallback` ve `/utier nametag` ile kapatmayı, yeniden bağlanmayı kontrol et.
8. DonutSMP'deki sonuç için debug çıktısı ve kafa üstü ekran görüntüsünü paylaş.
