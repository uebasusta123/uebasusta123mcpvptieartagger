# 1.1.1 doğrulama notları

- Hedef: Minecraft 26.2, Java 25, Fabric Loader 0.19.3, Fabric API 0.153.0+26.2.
- Bütün mod kaynakları JDK 25 ile yerelde bu hedeflere karşı derlendi.
- `TierLogicChecks`: 386 kontrol başarılı. Önceki 354 kontrole ek olarak TAB'da
  listelenmeyen dünya oyuncuları, profil/display adı ayrımı, tekilleştirme, bozuk
  adlar, tarama aralığı, dünya değişimi, yeniden bağlanma, devre dışı bırakma,
  orijinal isim stilini koruma ve gizli etiketi zorla göstermeme kontrol edildi.
- `extractNameTags(Entity, EntityRenderState, float, double, double)` ve
  `ClientLevel.players()` hedefleri gerçek 26.2 bytecode'u üzerinde kontrol edildi.
- Tam Gradle/Loom derlemesi ve aynı kontroller GitHub `build` ve `release`
  iş akışlarında çalışır. Release iş akışı yalnızca `./gradlew build` başarılı
  olduktan sonra JAR yayımlar; mevcut 1.1.0 sürümünü değiştirmez.
- Gerçek Minecraft istemcisinde veya DonutSMP'de canlı görsel/uçtan uca test yapılmadı.
  Bu testler Mixin'in oyun içinde uygulanmasının ve sunucuya özel eklentilerin
  görsel kontrolünün yerine geçmez.

## Oyun içi kısa kontrol listesi

1. Eski Tier Tagger JAR'ını çıkar, yalnızca 1.1.1 JAR'ını kur.
2. `/utier mode highest` ve `/utier refresh` kullan; kafa üstü etiketleri açık olsun.
3. MCPvP kaydı olan, yakında duran fakat TAB'da listelenmeyen bir oyuncuyu kontrol et.
   TAB'ı açmadan birkaç saniye bekle; internet/API yoğunluğu süreyi uzatabilir.
4. `/utier <gerçek_profil_adı>` sonucu ile kafa üstü tierini karşılaştır.
5. `/utier tab` ile TAB etiketlerini kapat; kafa üstü etiketleri çalışmaya devam etmeli.
6. Sunucudan çıkıp yeniden gir; `/utier nametag` ile kafa üstü etiketlerini aç/kapat.
7. API'de olmayan oyuncuya tier eklenmediğini, sunucunun öneklerinin korunduğunu kontrol et.
8. Sunucu normal isimleri gizleyip özel hologram çiziyorsa veya nick/disguise kullanıyorsa
   README'deki sınırlamalara bak. Sorun sürerse oyun sürümü, mod listesi, ekran görüntüsü
   ve `logs/latest.log` dosyasını paylaş; hesap parolası/token'ı paylaşma.
