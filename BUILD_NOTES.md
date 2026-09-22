# 1.1.0 doğrulama notları

- Hedef: Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.153.0+26.2.
- Bütün mod kaynakları JDK 25 ile bu hedeflere karşı derlendi.
- `TierLogicChecks`: 354 kontrol başarılı. Tier sıralaması, API yanıtı okuma,
  isim eşleşmesi, etiket sırası, font/simge kaynakları, logo, mod metadatası ve
  eski ayarların aktarılması kontrol edildi.
- Mixin hedeflerinin isimleri ve parametreleri 26.2 sınıflarında doğrulandı.
- Normal Gradle/Loom çalıştırması bu geliştirme ortamında yerel Unix socket
  denetimi için izin hatası verdi. İzinler veya Loom değiştirilmedi. Dağıtılan
  JAR doğrudan `javac --release 25` ve JDK `jar` aracıyla üretildi; yalnızca
  projenin sınıfları ve kaynakları paketlendi. Minecraft bağımlılıkları JAR'a
  dahil edilmedi. Minecraft 26.2'nin official isim alanı kullanıldı.
- GitHub için standart Gradle projesi ve `verifyTierLogic` görevi kaynakta mevcut.
  Tam Gradle `build` bu ortamda doğrulanamadı.
- Gerçek Minecraft istemcisinde Mod Menu/TAB/nametag görsel testi yapılmadı.

## Oyun içi kısa kontrol listesi

1. Eski Tier Tagger JAR'ını çıkar, yalnızca 1.1.0 JAR'ını kur.
2. Mod Menu'de yeni tam adın ve kırmızı-siyah logonun göründüğünü kontrol et.
3. `/utier mode highest` ve `/utier refresh` kullan.
4. MCPvP kaydı olan bir oyuncunun TAB ve kafa üstü etiketini karşılaştır:
   oyun modu simgesi, MCPVP yazısı, ardından en iyi tier.
5. `/utier <oyuncu>` ile kit listesini karşılaştır; `/utier icons`, `tab`,
   `nametag` aç/kapat seçeneklerini dene.
6. Sorun olursa crash report veya `logs/latest.log` dosyasını paylaş.
