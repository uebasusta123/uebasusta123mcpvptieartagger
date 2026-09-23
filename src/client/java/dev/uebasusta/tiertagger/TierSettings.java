package dev.uebasusta.tiertagger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TierSettings {
	public static final List<String> MODES = KitCatalog.modes();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static TierSettings instance = new TierSettings();

	public boolean enabled = true;
	public boolean showInTab = true;
	public boolean showAboveHead = true;
	public boolean showFallbackTags = true;
	public boolean showKit = true;
	public boolean showIcons = true;
	public String mode = "highest";

	private TierSettings() {
	}

	public static TierSettings get() {
		return instance;
	}

	public static void load() {
		load(FabricLoader.getInstance().getConfigDir());
	}

	static void load(Path configDir) {
		instance = new TierSettings();
		Path path = configDir.resolve(McpvpTierTaggerClient.MOD_ID + ".json");
		Path legacyPath = configDir.resolve("universal-tier-tagger.json");
		boolean migrate = !Files.isRegularFile(path) && Files.isRegularFile(legacyPath);
		Path source = migrate ? legacyPath : path;
		if (!Files.isRegularFile(source)) {
			save(configDir);
			return;
		}

		try (Reader reader = Files.newBufferedReader(source)) {
			TierSettings loaded = GSON.fromJson(reader, TierSettings.class);
			if (loaded != null) {
				instance = loaded;
			}
		} catch (IOException | RuntimeException ignored) {
			instance = new TierSettings();
		}

		if (migrate || instance.mode == null || !MODES.contains(instance.mode)) {
			instance.mode = "highest";
		}
		if (migrate) {
			instance.showIcons = true;
			instance.showKit = true;
			save(configDir);
		}
	}

	public static void save() {
		save(FabricLoader.getInstance().getConfigDir());
	}

	private static void save(Path configDir) {
		Path path = configDir.resolve(McpvpTierTaggerClient.MOD_ID + ".json");
		try {
			Files.createDirectories(configDir);
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException ignored) {
		}
	}
}
