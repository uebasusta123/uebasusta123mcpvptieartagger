# uebasusta123mcpvptieartagger

Minecraft Java **26.2** için açık kaynak, yalnızca istemcide çalışan Fabric modu.
TAB listesinde ve oyuncunun kafa üstünde şu biçimde tier etiketi ekler:

`OyuncuAdı [oyun modu simgesi MCPVP HT2]`

## 1.1.2 yenilikleri

1.1.1 yalnızca mevcut Minecraft isim etiketine tier ekliyordu. Sunucu normal etiketi
boşaltıp/gizleyerek kendi hologramını çizdiğinde veri alınsa bile kafa üstünde tier görünmüyordu.

- Normal isim etiketi yoksa, görünür oyuncunun üzerinde **ayrı bir tier satırı** çizilir.
  Sunucunun hologramı ve oyuncu adı değiştirilmez; normal isim etiketi varsa tier yine sonuna eklenir.
- Yeni satır yalnızca yakındaki (64 bloktan yakın), görüş hattındaki, görünür, yaşayan
  oyunculara eklenir. Eğilmiş oyuncu, spectator, kendi oyuncun ve F1 ile gizlenmiş HUD hariçtir.
  Bu satırda duvar arkasından çizim kapalıdır.
- `/utier fallback` bu ek desteği açar/kapatır; varsayılan açık, eski ayarlarda otomatik etkinleşir.
- `/utier debug` sürümü, ayarları ve en yakın görünür oyuncuların sorgu durumunu yerel sohbette gösterir.
  `/utier debug <oyuncu>` ayrıca dünya/TAB varlığını, isim görünürlüğünü ve yeni API sorgusunu gösterir.
- HTTP/bağlantı hatası artık yanlış biçimde “kayıt yok” mesajı olarak gösterilmez.
- 1.1.1'deki TAB'dan bağımsız, yüklü oyuncu profillerini saniyede yaklaşık bir kez sorgulama korunur.

### DonutSMP testi

Bu sürüm, özel isim etiketleri kullanıldığında vanilla etikete bağımlılığı kaldırır.
**DonutSMP'de canlı oyun testi yapılmadı; kullanıcı 1.1.1'in yeterli olmadığını bildirdi.**
MCPvP kaydı gerçek profil adıyla bulunmalı, oyuncu istemci dünyasında yüklü olmalıdır.
Nick/disguise arkasındaki kimlik veya sunucunun göndermediği oyuncular tahmin edilmez.
Özel hologramın yüksekliği farklı sunucularda değişebilir; ek tier satırının konumu oyun içinde
kontrol edilmelidir. Başka bir mod etiket çizimini tamamen iptal ediyorsa ek inceleme gerekebilir.

Güncellemeden sonra `/utier debug` sürüm satırında `1.1.2` göründüğünü kontrol et.
Sorun devam ederse `/utier debug <tierini_bildiğin_oyuncu>` çıktısını ve o oyuncunun
kafa üstünü gösteren bir ekran görüntüsünü paylaş.

## 1.1.0'dan korunan özellikler

- Mod Menu adı, mod kimliği, dosya adı ve yardım mesajları: `uebasusta123mcpvptieartagger`.
- Mod Menu listesi ve detayları için kullanıcının sağladığı kırmızı-siyah MCPVP / uebasusta123 logosu.
- En iyi tier otomatik seçilir; seçilen kitin Minecraft eşya simgesi gösterilir.
- Etiket sırası: **kit simgesi → MCPVP → tier**. MC beyaz, PVP kırmızı; tier kendi renginde.
- Sunucunun oyuncu adı, takımı ve önekleri korunur; etiket sadece sonuna eklenir.
- Eşit tierlerde etiketin değişip durmaması için sabit kit önceliği kullanılır.
- Eski ayarlar otomatik aktarılır; ilk aktarımda `highest` ve simgeler açılır.
- Geçici API hataları 30 dakika boyunca “kayıt yok” diye saklanmaz.

## Kurulum / güncelleme

1. Minecraft 26.2, Java 25 ve Fabric Loader **0.19.3 veya üstü** kullan.
2. Minecraft 26.2 ile uyumlu Fabric API **0.153.0+26.2 veya üstünü** kur.
3. Önceki Tier Tagger JAR'ını `mods` klasöründen çıkar. Eski ve yeni sürümü birlikte kurma.
4. [Releases](https://github.com/uebasusta123/uebasusta123mcpvptieartagger/releases)
   bölümündeki `uebasusta123mcpvptieartagger-1.1.2.jar` dosyasını `mods` klasörüne koy.
5. Mod listesini/logoyu görmek için Minecraft sürümünle uyumlu Mod Menu kurulu olmalı.
   Mod Menu, tier etiketlerinin çalışması için zorunlu değildir.

Sunucu eklentisi, sunucu resource pack'i veya başka oyuncuların bu modu kurması gerekmez.
Etiketleri yalnızca modu kullanan kişi görür. Mod veya resource pack çakışmaları olabilir;
oyun içindeki görünüm ilgili mod kombinasyonunda ayrıca kontrol edilmelidir.

## En iyi tier nasıl seçilir?

Önce seviye karşılaştırılır: T1, T2'den; T2, T3'ten iyidir.
Aynı seviyede **H > M > L** kullanılır; API'de MT değeri varsa bu da hesaba katılır.
Örnek: `HT1 > MT1 > LT1 > HT2 > MT2 > LT2 > … > LT5`.
Sword HT3, Mace LT2 olan oyuncuda **mace simgesi + MCPVP LT2** görünür.

Eşit tierlerde sabit öncelik: sword, shield, pot, early-game, end-game, mace,
late-game, spear, diamond-smp, netherite-pot, creeper, cart, bow, smp, crystal.
Yeni/bilinmeyen kitler aynı tierde bilinenlerden sonra alfabetik sıralanır;
bunlar için genel bir simge kullanılır. Kayıt bulunmayan oyunculara tier uydurulmaz.

Veriler MCPvP'nin herkese açık arama uç noktasındaki isim eşleşmesinden gelir.
Başka tier siteleri, sunucu içi rütbeler veya nick/disguise adları doğrulanmaz.
Normal sonuçlar 30 dakika önbellekte tutulur; HTTPS isteği render iş parçacığını bekletmez.

## Komutlar

Kısa komut `/utier` korunur; tam adla `/uebasusta123mcpvptieartagger` da kullanılabilir.

- `/utier <oyuncu>`: Bütün kit tierlerini ve en iyisini yerel sohbette gösterir.
- `/utier mode highest`: Otomatik en iyi tier (varsayılan).
- `/utier mode <kit>`: İstersen belirli bir kit seç.
- `/utier tab`: TAB etiketini aç/kapat.
- `/utier nametag`: Kafa üstü etiketini aç/kapat.
- `/utier fallback`: Normal etiketi olmayan oyuncular için ayrı tier satırını aç/kapat.
- `/utier debug [oyuncu]`: Yerel teşhis; oyuncu verilirse MCPvP sorgusunu da yeniler.
- `/utier icons`: Simgeleri aç/kapat; kapalıyken kit adı yazılır.
- `/utier refresh`: Önbelleği temizle.

Ayar dosyası: `.minecraft/config/uebasusta123mcpvptieartagger.json`.
Eski `universal-tier-tagger.json` ilk açılışta okunur; eski dosya silinmez.

## Görseller ve kaynaklar

Logo: `src/main/resources/assets/uebasusta123mcpvptieartagger/icon.png` (128×128 PNG).
Oyun modu simgeleri özel `kits` fontuyla çizilir. Font tanımları Minecraft'ın kurulu
eşya dokularına referans verir; Minecraft görsel dosyaları bu projeye kopyalanmaz.
Ayrı sunucu resource pack'i gerekmez; kullandığın istemci resource pack'i bu eşya dokularını değiştirebilir.

## Gizlilik ve sunucu kuralları

Modun tier sorgusu oyuncu adını `https://www.mcpvp.com/tiers/search` adresine gönderir;
bu hizmet normal HTTPS bağlantısındaki IP bilgisini de görebilir.
Minecraft hesabının parolası/token'ı okunmaz veya gönderilmez. Minecraft sunucusuna
tier sorgusu için ek komut, özel paket veya hareket gönderilmez.
Yerel mod olması her sunucunun izin verdiği anlamına gelmez; sıfır ban riski garanti edilemez.
Sunucunun izin verilen mod kurallarını kontrol et.

## Kaynaktan derleme ve test

JDK 25 ile Linux/macOS:

```bash
./gradlew build
```

Windows:

```bat
gradlew.bat build
```

Çıktı `build/libs/` içindedir. Kaynak JAR'ını (`-sources.jar`) oyuna kurma.
`build`, ayrıca `verifyTierLogic` doğrulamalarını çalıştırır:
bütün tier çiftleri, eşitlik, eksik/bozuk kayıtlar, tam isim eşleşmesi,
etiket sırası, simge-font eşleşmeleri, logo, sürüm gereksinimleri, TAB dışı oyuncu
taraması, özel etiket planı, görünürlük koşulları, duvar arkası çizimini kapatan gönderim,
HTTP hata ayrımı ve gerçek Minecraft sınıflarındaki mixin hedefleri.
Bu kontroller bir oyun istemcisi açmaz; oyun içi uçtan uca testin yerine geçmez.

### GitHub sürüm yayımlama

`build` iş akışı push/PR üzerinde derleme ve test yapar. `release` iş akışı
`release/<sürüm>` dalına push veya Actions ekranından elle çalıştırma ile derler,
testler geçerse yeni GitHub Release'e JAR ve SHA-256 dosyasını ekler.
Sürüm `gradle.properties` ve `docs/releases/<sürüm>.md` ile eşleşmelidir.
Var olan sürümler veya dosyaları üzerine yazılmaz; yayımlama için GitHub'ın geçici
iş akışı token'ı kullanılır, kişisel token gerekmez.

## Lisans

Proje kodu MIT lisanslıdır; [LICENSE](LICENSE) dosyasını koruyarak GitHub'da paylaşılabilir.
Bağımlılıklar kendi lisanslarına tabidir. Logo kaynak görseli kullanıcı tarafından sağlanmıştır.
Bu proje MCPvP, Mojang veya Microsoft'un resmî/onaylanmış bir ürünü değildir.
