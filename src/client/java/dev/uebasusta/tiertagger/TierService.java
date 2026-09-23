package dev.uebasusta.tiertagger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class TierService {
	private static final TierService INSTANCE = new TierService();
	private static final String API_URL = "https://www.mcpvp.com/tiers/search?version=beta&kit=overall&include_retired=1&q=";
	private static final long CACHE_TIME_MS = Duration.ofMinutes(30).toMillis();
	private static final long ERROR_RETRY_MS = Duration.ofMinutes(1).toMillis();
	private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");

	private final HttpClient httpClient = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(8))
		.followRedirects(HttpClient.Redirect.NORMAL)
		.build();
	private final ExecutorService workers = Executors.newFixedThreadPool(2, runnable -> {
		Thread thread = new Thread(runnable, McpvpTierTaggerClient.MOD_ID + "-HTTP");
		thread.setDaemon(true);
		return thread;
	});
	private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
	private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

	private TierService() {
	}

	public static TierService get() {
		return INSTANCE;
	}

	public TierData getOrQueue(String playerName) {
		if (!isValidPlayerName(playerName)) {
			return null;
		}
		String key = normalize(playerName);
		CacheEntry cached = cache.get(key);
		long now = System.currentTimeMillis();
		if (cached != null && cached.expiresAt() > now) {
			return cached.data();
		}

		queue(playerName, key);
		return cached == null ? null : cached.data();
	}

	public void lookup(String playerName, Consumer<Optional<TierData>> callback) {
		workers.execute(() -> callback.accept(Optional.ofNullable(fetchAndCache(playerName))));
	}

	public void clearCache() {
		cache.clear();
	}

	private void queue(String playerName, String key) {
		if (!inFlight.add(key)) {
			return;
		}

		workers.execute(() -> {
			try {
				fetchAndCache(playerName);
			} finally {
				inFlight.remove(key);
				try {
					Thread.sleep(100L);
				} catch (InterruptedException interruptedException) {
					Thread.currentThread().interrupt();
				}
			}
		});
	}

	private TierData fetchAndCache(String playerName) {
		String key = normalize(playerName);
		TierData tierData = null;
		boolean success = false;
		try {
			String encodedName = URLEncoder.encode(playerName, StandardCharsets.UTF_8);
			HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL + encodedName))
				.timeout(Duration.ofSeconds(12))
				.header("Accept", "application/json")
				.header("User-Agent", McpvpTierTaggerClient.MOD_ID + "/1.1.1")
				.GET()
				.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			if (response.statusCode() == 200) {
				tierData = parseExactPlayer(response.body(), playerName);
				success = true;
			}
		} catch (IOException | InterruptedException | RuntimeException exception) {
			if (exception instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
		}

		if (!success) {
			CacheEntry previous = cache.get(key);
			tierData = previous == null ? null : previous.data();
		}
		cache.put(key, new CacheEntry(tierData, System.currentTimeMillis() + (success ? CACHE_TIME_MS : ERROR_RETRY_MS)));
		return tierData;
	}

	static TierData parseExactPlayer(String json, String requestedName) {
		JsonObject root = JsonParser.parseString(json).getAsJsonObject();
		JsonElement playersElement = root.get("players");
		if (playersElement == null || !playersElement.isJsonArray()) {
			throw new IllegalArgumentException("Missing players array in MCPvP response");
		}

		for (JsonElement playerElement : playersElement.getAsJsonArray()) {
			if (!playerElement.isJsonObject()) {
				continue;
			}
			JsonObject player = playerElement.getAsJsonObject();
			String name = stringValue(player, "name", "");
			if (!name.equalsIgnoreCase(requestedName)) {
				continue;
			}

			Map<String, String> kitRanks = new LinkedHashMap<>();
			JsonElement kitRanksElement = player.get("kitRanks");
			if (kitRanksElement != null && kitRanksElement.isJsonObject()) {
				for (Map.Entry<String, JsonElement> entry : kitRanksElement.getAsJsonObject().entrySet()) {
					if (entry.getValue().isJsonPrimitive()) {
						kitRanks.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue().getAsString().toUpperCase(Locale.ROOT));
					}
				}
			}

			int rank = player.has("rank") && !player.get("rank").isJsonNull() ? player.get("rank").getAsInt() : -1;
			double points = player.has("points") && !player.get("points").isJsonNull() ? player.get("points").getAsDouble() : 0.0;
			return new TierData(name, rank, points, stringValue(player, "region", "?"), kitRanks);
		}

		return null;
	}

	private static String stringValue(JsonObject object, String key, String fallback) {
		JsonElement value = object.get(key);
		return value == null || value.isJsonNull() ? fallback : value.getAsString();
	}

	private static String normalize(String name) {
		return name.toLowerCase(Locale.ROOT);
	}

	static boolean isValidPlayerName(String name) {
		return name != null && PLAYER_NAME.matcher(name).matches();
	}

	private record CacheEntry(TierData data, long expiresAt) {
	}
}
