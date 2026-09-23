package dev.uebasusta.tiertagger;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Polls loaded world players, not the server's (possibly filtered) TAB list. */
final class PlayerTierPrefetcher {
	static final int SCAN_INTERVAL_TICKS = 20;
	private Object world;
	private int ticksUntilScan;

	/** Suppliers are evaluated only on a scan tick; lookup must enqueue, not block. */
	<T> void tick(Object currentWorld, boolean enabled, Supplier<? extends Iterable<T>> players,
			Function<T, String> profileName, Consumer<String> lookup) {
		if (currentWorld == null || !enabled) {
			world = null;
			ticksUntilScan = 0;
			return;
		}
		if (world != currentWorld) {
			world = currentWorld;
			ticksUntilScan = 0;
		}
		if (ticksUntilScan > 0) {
			ticksUntilScan--;
			return;
		}
		ticksUntilScan = SCAN_INTERVAL_TICKS - 1;

		Set<String> seen = new HashSet<>();
		for (T player : players.get()) {
			String name = profileName.apply(player);
			if (TierService.isValidPlayerName(name) && seen.add(name.toLowerCase(Locale.ROOT))) {
				// TierService handles cache expiry and deduplicates in-flight HTTP requests.
				lookup.accept(name);
			}
		}
	}
}
