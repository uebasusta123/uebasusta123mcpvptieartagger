package dev.uebasusta.tiertagger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

		Path resources = Path.of("src/main/resources");
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
			check(Files.readString(legacy).equals(oldSettings), "legacy settings preserved");
			check(Files.isRegularFile(current), "new config created");
			Files.writeString(current, "{\"mode\":\"mace\",\"showIcons\":false}");
			TierSettings.load(configDir);
			check(TierSettings.get().mode.equals("mace") && !TierSettings.get().showIcons, "new settings take precedence");
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
		System.out.println("PASS: " + checks + " checks (ranking, API parsing, label layout, font resources, metadata/logo, settings migration)");
	}

	private static TierData data(Map<String, String> ranks) {
		return new TierData("TestPlayer", 1, 100, "EU", ranks);
	}

	private static void check(boolean success, String message) {
		checks++;
		if (!success) throw new AssertionError(message);
	}
}
