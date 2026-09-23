package dev.uebasusta.tiertagger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;

import javax.imageio.ImageIO;
import java.lang.classfile.ClassFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** Run with ./gradlew verifyTierLogic (also part of build). Never connects to a server. */
public final class TierLogicChecks {
	private static int checks;

	public static void main(String[] args) throws Exception {
		List<String> tiers = new ArrayList<>();
		for (int level = 1; level <= 5; level++) {
			for (String position : List.of("H", "M", "L")) {
				tiers.add(position + "T" + level);
			}
		}
		for (int i = 0; i < tiers.size(); i++) {
			for (int j = 0; j < tiers.size(); j++) {
				TierData data = data(Map.of("sword", tiers.get(i), "mace", tiers.get(j)));
				check(data.select("highest").orElseThrow().tier().equals(tiers.get(Math.min(i, j))), "tier order " + i + "," + j);
			}
		}
		check(data(Map.of("sword", "HT2", "mace", "LT1")).select("highest").orElseThrow().kit().equals("mace"), "LT1 beats HT2");
		check(data(Map.of("sword", "MT2", "mace", "LT2")).select("highest").orElseThrow().kit().equals("sword"), "middle tier supported");
		Map<String, String> tied = new LinkedHashMap<>();
		tied.put("mace", "HT2");
		tied.put("sword", "HT2");
		check(data(tied).select("highest").orElseThrow().kit().equals("sword"), "stable tie independent of API order");
		check(data(Map.of(" SWORD ", " ht2 ")).select("SWORD").orElseThrow().tier().equals("HT2"), "normalization");
		check(data(Map.of("sword", "HT2")).select(null).orElseThrow().tier().equals("HT2"), "null selects highest");
		check(data(Map.of()).select("highest").isEmpty(), "unranked has no tag");
		check(data(Map.of("sword", "HT2")).select("mace").isEmpty(), "missing selected kit");
		check(data(Map.of("sword", "T1", "mace", "HT0", "pot", "LT6")).select("highest").isEmpty(), "invalid tiers ignored");
		Map<String, String> mutable = new HashMap<>();
		mutable.put("sword", "HT2");
		mutable.put("mace", null);
		TierData copied = data(mutable);
		mutable.put("sword", "LT5");
		check(copied.select("highest").orElseThrow().tier().equals("HT2"), "defensive copy / null rank");

		String json = """
			{"players":[null,{"name":"TestPlayerExtra","kitRanks":{"mace":"HT1"}},
			{"name":"TestPlayer","rank":null,"points":null,"region":"EU","kitRanks":{"sword":"HT3","mace":"LT2","pot":null,"bow":"invalid"}}]}
			""";
		TierData found = TierService.parseExactPlayer(json, "testplayer");
		check(found != null && found.name().equals("TestPlayer"), "exact match, case insensitive");
		check(found.select("highest").orElseThrow().kit().equals("mace"), "best kit from API data");
		check(found.kitRanks().size() == 2, "bad ranks discarded");
		check(found.overallRank() == -1 && found.points() == 0, "nullable metadata");
		check(TierService.parseExactPlayer(json, "Test") == null, "no partial matches");
		check(TierService.parseExactPlayer("{\"players\":[]}", "TestPlayer") == null, "no result");
		try {
			TierService.parseExactPlayer("{}", "TestPlayer");
			throw new AssertionError("invalid API schema accepted");
		} catch (IllegalArgumentException expected) {
			checks++;
		}

		TierData.TierSelection selection = new TierData.TierSelection("sword", "HT2");
		Component tag = TierTagFormatter.tag(selection, true, true);
		check(tag.getString().equals(" [\uE000 MCPVP HT2]"), "icon -> MCPVP -> tier");
		check(TierTagFormatter.tag(selection, true, false).getString().equals(" [SWORD MCPVP HT2]"), "text fallback");
		check(TierTagFormatter.tag(selection, false, true).getString().equals(" [MCPVP HT2]"), "no kit option");
		Component icon = tag.getSiblings().getFirst();
		check(icon.getStyle().getFont() instanceof FontDescription.Resource font
			&& font.id().toString().equals("uebasusta123mcpvptieartagger:kits"), "custom icon font");
		check(!icon.getStyle().isBold() && !icon.getStyle().isItalic(), "icon not distorted by text style");
		checkWorldPlayerPrefetch();
		checkNameTagFormatting();
		checkStandaloneTags();
		checkApiDiagnostics();
		checkMinecraftTargets();

		Path resources = Path.of("src/main/resources");
		java.util.Properties properties = new java.util.Properties();
		try (var reader = Files.newBufferedReader(Path.of("gradle.properties"))) {
			properties.load(reader);
		}
		check(McpvpTierTaggerClient.VERSION.equals(properties.getProperty("version")), "command/API version matches release");
		JsonObject metadata = JsonParser.parseString(Files.readString(resources.resolve("fabric.mod.json"))).getAsJsonObject();
		check(metadata.get("id").getAsString().equals("uebasusta123mcpvptieartagger"), "mod identity");
		check(metadata.get("name").getAsString().equals(metadata.get("id").getAsString()), "display name");
		check(metadata.get("environment").getAsString().equals("client"), "client-only");
		check(metadata.getAsJsonObject("depends").get("fabricloader").getAsString().equals(">=0.19.3"), "loader baseline");
		check(metadata.getAsJsonObject("depends").get("fabric-api").getAsString().equals(">=0.153.0+26.2"), "API baseline");
		var image = ImageIO.read(resources.resolve(metadata.get("icon").getAsString()).toFile());
		check(image != null && image.getWidth() == 128 && image.getHeight() == 128, "Mod Menu logo 128x128 PNG");
		JsonObject fonts = JsonParser.parseString(Files.readString(resources.resolve("assets/uebasusta123mcpvptieartagger/font/kits.json"))).getAsJsonObject();
		Map<String, String> glyphFiles = new HashMap<>();
		fonts.getAsJsonArray("providers").forEach(element -> {
			JsonObject provider = element.getAsJsonObject();
			check(provider.get("ascent").getAsInt() <= provider.get("height").getAsInt(), "font bounds");
			String glyph = provider.getAsJsonArray("chars").get(0).getAsString();
			check(glyph.codePointCount(0, glyph.length()) == 1, "single icon glyph");
			check(glyphFiles.put(glyph, provider.get("file").getAsString()) == null, "unique glyph");
		});
		Set<String> kitNames = new HashSet<>();
		for (KitCatalog.Kit kit : KitCatalog.KITS) {
			check(kitNames.add(kit.key()), "unique kit");
			check(kit.texture().equals(glyphFiles.get(kit.glyph())), "matching icon: " + kit.key());
			String resource = "assets/" + kit.texture().replace(":", "/textures/");
			check(TierLogicChecks.class.getClassLoader().getResource(resource) != null, "vanilla texture exists: " + kit.key());
		}
		check(glyphFiles.containsKey(KitCatalog.get("future-mode").glyph()), "fallback icon");
		Path configDir = Files.createTempDirectory("tier-tagger-check-");
		Path legacy = configDir.resolve("universal-tier-tagger.json");
		Path current = configDir.resolve("uebasusta123mcpvptieartagger.json");
		String oldSettings = "{\"mode\":\"sword\",\"showInTab\":false,\"showAboveHead\":true,\"showKit\":false}";
		try {
			Files.writeString(legacy, oldSettings);
			TierSettings.load(configDir);
			check(TierSettings.get().mode.equals("highest"), "migration defaults to best tier");
			check(TierSettings.get().showIcons && TierSettings.get().showKit, "migration enables kit icons");
			check(!TierSettings.get().showInTab && TierSettings.get().showAboveHead, "display toggles preserved");
			check(TierSettings.get().showFallbackTags, "old configs gain standalone tag support");
			check(Files.readString(legacy).equals(oldSettings), "legacy settings preserved");
			check(Files.isRegularFile(current), "new config created");
			Files.writeString(current, "{\"mode\":\"mace\",\"showIcons\":false}");
			TierSettings.load(configDir);
			check(TierSettings.get().mode.equals("mace") && !TierSettings.get().showIcons, "new settings take precedence");
			Files.writeString(current, "{\"showFallbackTags\":false}");
			TierSettings.load(configDir);
			check(!TierSettings.get().showFallbackTags, "explicit standalone opt-out preserved");
			Files.writeString(current, "{\"mode\":null}");
			TierSettings.load(configDir);
			check(TierSettings.get().mode.equals("highest"), "null mode fallback");
			Files.writeString(current, "invalid json");
			TierSettings.load(configDir);
			check(TierSettings.get().mode.equals("highest") && TierSettings.get().showIcons, "damaged settings fallback");
		} finally {
			Files.deleteIfExists(current);
			Files.deleteIfExists(legacy);
			Files.deleteIfExists(configDir);
		}
		System.out.println("PASS: " + checks + " checks (ranking, API parsing, world-player prefetch, nametags, Minecraft targets, fonts, metadata, settings)");
	}

	private static void checkStandaloneTags() {
		TierSettings settings = TierSettings.get();
		TierData ranked = data(Map.of("sword", "HT2"));
		var hidden = NameTagPresentation.plan(null, ranked, settings, true);
		check(hidden.vanilla() == null, "server-hidden vanilla name stays hidden");
		check(hidden.standalone() != null && hidden.standalone().getString().equals(" [\uE000 MCPVP HT2]"),
			"ranked world-only player gets a separate tier even with a null vanilla name");
		Component blank = Component.empty();
		var empty = NameTagPresentation.plan(blank, ranked, settings, true);
		check(empty.vanilla() == blank && empty.standalone() != null, "empty custom vanilla name also uses separate tier");
		Component original = Component.literal("[VIP] TestPlayer").withStyle(net.minecraft.ChatFormatting.GREEN);
		var ordinary = NameTagPresentation.plan(original, ranked, settings, true);
		check(ordinary.standalone() == null && ordinary.vanilla().getString().equals("[VIP] TestPlayer [\uE000 MCPVP HT2]"),
			"ordinary names are augmented once without duplicate standalone label");
		check(original.getString().equals("[VIP] TestPlayer") && ordinary.vanilla().getStyle().equals(original.getStyle()),
			"standalone planning preserves server text and style");
		check(NameTagPresentation.plan(null, ranked, settings, false).standalone() == null, "occluded or otherwise ineligible player has no fallback");
		check(NameTagPresentation.plan(null, null, settings, true).standalone() == null, "pending lookup never makes an empty label");
		check(NameTagPresentation.plan(null, data(Map.of()), settings, true).standalone() == null, "no fake tier for unranked players");
		try {
			settings.showFallbackTags = false;
			check(NameTagPresentation.plan(null, ranked, settings, true).standalone() == null, "standalone toggle disables fallback");
			check(NameTagPresentation.plan(original, ranked, settings, true).vanilla().getString().contains("HT2"), "fallback toggle preserves ordinary nametags");
			settings.showFallbackTags = true;
			settings.showAboveHead = false;
			check(NameTagPresentation.plan(null, ranked, settings, true).standalone() == null, "nametag toggle disables fallback too");
			settings.showAboveHead = true;
			settings.enabled = false;
			check(NameTagPresentation.plan(null, ranked, settings, true).standalone() == null, "disabled mod creates no standalone tags");
			settings.enabled = true;
			settings.mode = "mace";
			check(NameTagPresentation.plan(null, ranked, settings, true).standalone() == null, "no fallback tier in an unranked selected kit");
		} finally {
			settings.showFallbackTags = true;
			settings.showAboveHead = true;
			settings.enabled = true;
			settings.mode = "highest";
		}
		check(NameTagPresentation.permitsFallback(true, true, true, true, false, false, true, 16), "nearby visible player allowed");
		check(!NameTagPresentation.permitsFallback(false, true, true, true, false, false, true, 16), "F1 hides fallback");
		check(!NameTagPresentation.permitsFallback(true, false, true, true, false, false, true, 16), "no fallback on self/camera");
		check(!NameTagPresentation.permitsFallback(true, true, false, true, false, false, true, 16), "invisible player hidden");
		check(!NameTagPresentation.permitsFallback(true, true, true, false, false, false, true, 16), "dead/removed player hidden");
		check(!NameTagPresentation.permitsFallback(true, true, true, true, true, false, true, 16), "spectator hidden");
		check(!NameTagPresentation.permitsFallback(true, true, true, true, false, true, true, 16), "sneaking player hidden");
		check(!NameTagPresentation.permitsFallback(true, true, true, true, false, false, false, 16), "player behind wall hidden");
		for (double distance : new double[] {4096, 10000, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
			check(!NameTagPresentation.permitsFallback(true, true, true, true, false, false, true, distance), "invalid/outside name range: " + distance);
		}
		var calls = new ArrayList<Object[]>();
		var collector = (net.minecraft.client.renderer.SubmitNodeCollector) java.lang.reflect.Proxy.newProxyInstance(
			TierLogicChecks.class.getClassLoader(), new Class<?>[] {net.minecraft.client.renderer.SubmitNodeCollector.class},
			(proxy, method, arguments) -> {
				if (!method.getName().equals("submitNameTag")) throw new AssertionError("unexpected renderer call: " + method);
				calls.add(arguments);
				return null;
			});
		var anchor = new net.minecraft.world.phys.Vec3(0, 2.35, 0);
		new StandaloneTierTag(hidden.standalone(), anchor).submit(null, collector, 123, null);
		check(calls.size() == 1, "standalone tag submitted exactly once");
		check(calls.getFirst()[1] == anchor && calls.getFirst()[3] == hidden.standalone(), "standalone submission retains label and anchor");
		check(Boolean.FALSE.equals(calls.getFirst()[4]), "standalone submission never requests through-wall rendering");
		check(Integer.valueOf(123).equals(calls.getFirst()[5]), "standalone submission preserves light");
	}

	private static void checkApiDiagnostics() {
		var missing = TierService.parseResponse(200, "{\"players\":[]}", "TestPlayer");
		check(missing.successful() && missing.data() == null, "valid empty API result is not a network error");
		for (int status : new int[] {403, 404, 429, 500, 503}) {
			var error = TierService.parseResponse(status, "<html>error</html>", "TestPlayer");
			check(!error.successful() && error.error().equals("HTTP " + status), "HTTP errors are not reported as no rank: " + status);
		}
		for (String body : List.of("{}", "not json", "{\"players\":{}}")) {
			check(!TierService.parseResponse(200, body, "TestPlayer").successful(), "malformed API response has diagnostic error");
		}
		var found = TierService.parseResponse(200, "{\"players\":[{\"name\":\"TestPlayer\",\"kitRanks\":{\"sword\":\"HT2\"}}]}", "TestPlayer");
		check(found.successful() && found.data().select("highest").orElseThrow().tier().equals("HT2"), "valid API tier remains successful");
	}

	private static void checkWorldPlayerPrefetch() {
		PlayerTierPrefetcher prefetcher = new PlayerTierPrefetcher();
		Object world = new Object();
		List<String> queued = new ArrayList<>();
		Supplier<Iterable<String>> unavailable = () -> {
			throw new AssertionError("players read while disconnected, disabled, or throttled");
		};
		prefetcher.tick(null, true, unavailable, name -> name, queued::add);
		prefetcher.tick(world, false, unavailable, name -> name, queued::add);
		check(queued.isEmpty(), "no prefetch outside an enabled world");

		// A server may omit WorldOnly from TAB and decorate its display name.
		// Neither field is an identity source: only the loaded player's profile is used.
		record LoadedPlayer(String profile, String display, boolean listedInTab) {}
		List<LoadedPlayer> players = List.of(
			new LoadedPlayer("WorldOnly", "[VIP] SomeoneElse", false),
			new LoadedPlayer("TabVisible", "TabVisible", true),
			new LoadedPlayer("worldonly", "duplicate", false),
			new LoadedPlayer("[NPC] Fake", "Fake", true),
			new LoadedPlayer(null, "Missing profile", false));
		prefetcher.tick(world, true, () -> players, LoadedPlayer::profile, queued::add);
		check(queued.equals(List.of("WorldOnly", "TabVisible")), "unlisted world player queried by profile, duplicates and bad names ignored");
		queued.clear();
		for (int tick = 1; tick < PlayerTierPrefetcher.SCAN_INTERVAL_TICKS; tick++) {
			prefetcher.tick(world, true, unavailable, name -> name, queued::add);
		}
		check(queued.isEmpty(), "no per-frame scanning");
		prefetcher.tick(world, true, () -> List.of("WorldOnly", "NewArrival"), name -> name, queued::add);
		check(queued.equals(List.of("WorldOnly", "NewArrival")), "rescan retries cache lookups, discovers arrivals, forgets departed players");
		queued.clear();
		prefetcher.tick(new Object(), true, () -> List.of("OtherWorld"), name -> name, queued::add);
		check(queued.equals(List.of("OtherWorld")), "dimension/server change scans immediately");
		queued.clear();
		prefetcher.tick(null, true, unavailable, name -> name, queued::add);
		prefetcher.tick(world, true, () -> List.of("Rejoined"), name -> name, queued::add);
		check(queued.equals(List.of("Rejoined")), "disconnect resets scanner");
		queued.clear();
		prefetcher.tick(world, false, unavailable, name -> name, queued::add);
		prefetcher.tick(world, true, () -> List.of("Reenabled"), name -> name, queued::add);
		check(queued.equals(List.of("Reenabled")), "reenabling nametags scans immediately");
		queued.clear();
		prefetcher.tick(new Object(), true, List::<String>of, name -> name, queued::add);
		check(queued.isEmpty(), "empty world is safe");

		for (String valid : List.of("Ab_", "0123456789abcdef", "TestPlayer", "testplayer")) {
			check(TierService.isValidPlayerName(valid), "valid profile name: " + valid);
		}
		for (String invalid : new String[] {null, "", "ab", "0123456789abcdefg", "[VIP] TestPlayer", "Player One", " Oyuncu", ".Bedrock"}) {
			check(!TierService.isValidPlayerName(invalid), "reject invalid profile name: " + invalid);
		}
	}

	private static void checkNameTagFormatting() {
		TierSettings settings = TierSettings.get();
		Component original = Component.literal("[VIP] WorldOnly").withStyle(net.minecraft.ChatFormatting.GREEN);
		Component result = TierTagFormatter.appendTag(original, data(Map.of("sword", "HT2")), settings);
		check(result.getString().equals("[VIP] WorldOnly [\uE000 MCPVP HT2]"), "world-only player receives tag without TAB component");
		check(original.getString().equals("[VIP] WorldOnly"), "server name component not mutated");
		check(result.getStyle().equals(original.getStyle()), "server prefix style preserved");
		check(TierTagFormatter.appendTag(original, (TierData) null, settings) == original, "pending/failed lookup preserves name");
		check(TierTagFormatter.appendTag(original, data(Map.of()), settings) == original, "no fabricated rank for unranked player");
		check(TierTagFormatter.appendTag(null, data(Map.of("sword", "HT2")), settings) == null, "hidden nametag not forced visible");
		check(TierTagFormatter.appendTag(null, "WorldOnly") == null, "hidden nametag does not enqueue HTTP request");
		settings.enabled = false;
		try {
			check(TierTagFormatter.appendTag(original, data(Map.of("sword", "HT2")), settings) == original, "disabled mod preserves name");
		} finally {
			settings.enabled = true;
		}
	}

	private static void checkMinecraftTargets() throws Exception {
		// Inspect actual target bytecode without booting the game or loading renderer natives.
		String renderer = "net/minecraft/client/renderer/entity/EntityRenderer.class";
		try (var stream = TierLogicChecks.class.getClassLoader().getResourceAsStream(renderer)) {
			check(stream != null, "Minecraft renderer bytecode available");
			var model = ClassFile.of().parse(stream.readAllBytes());
			check(model.methods().stream().anyMatch(method -> method.methodName().equalsString("extractNameTags")
				&& method.methodType().equalsString("(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FDD)V")),
				"exact 26.2 nametag mixin target exists");
			check(model.methods().stream().anyMatch(method -> method.methodName().equalsString("submitNameDisplay")
				&& method.methodType().equalsString("(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V")),
				"exact standalone tag submission mixin target exists");
		}
		try (var stream = TierLogicChecks.class.getClassLoader().getResourceAsStream("net/minecraft/client/multiplayer/ClientLevel.class")) {
			check(stream != null, "Minecraft client world bytecode available");
			check(ClassFile.of().parse(stream.readAllBytes()).methods().stream().anyMatch(method -> method.methodName().equalsString("players")
				&& method.methodType().equalsString("()Ljava/util/List;")), "loaded-world player source exists independently of TAB");
		}
	}

	private static TierData data(Map<String, String> ranks) {
		return new TierData("TestPlayer", 1, 100, "EU", ranks);
	}

	private static void check(boolean success, String message) {
		checks++;
		if (!success) throw new AssertionError(message);
	}
}
