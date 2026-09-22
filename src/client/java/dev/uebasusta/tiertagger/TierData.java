package dev.uebasusta.tiertagger;

import java.util.Comparator;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

public record TierData(String name, int overallRank, double points, String region, Map<String, String> kitRanks) {
	private static final Pattern VALID_TIER = Pattern.compile("[HML]T[1-5]");
	private static final Comparator<TierSelection> BEST_TIER = Comparator
		.comparingInt((TierSelection selection) -> tierWeight(selection.tier()))
		.thenComparingInt(selection -> KitCatalog.priority(selection.kit()))
		.thenComparing(TierSelection::kit);

	public TierData {
		Map<String, String> normalized = new TreeMap<>();
		kitRanks.forEach((kit, tier) -> {
			if (kit != null && tier != null) {
				String value = tier.trim().toUpperCase(Locale.ROOT);
				if (VALID_TIER.matcher(value).matches()) {
					normalized.put(kit.trim().toLowerCase(Locale.ROOT), value);
				}
			}
		});
		kitRanks = Collections.unmodifiableMap(normalized);
	}

	public Optional<TierSelection> select(String mode) {
		String selectedMode = mode == null ? "highest" : mode.trim().toLowerCase(Locale.ROOT);
		if ("highest".equals(selectedMode)) {
			return kitRanks.entrySet().stream()
				.map(entry -> new TierSelection(entry.getKey(), entry.getValue()))
				.filter(selection -> isTier(selection.tier()))
				.min(BEST_TIER);
		}

		String tier = kitRanks.get(selectedMode);
		return tier == null ? Optional.empty() : Optional.of(new TierSelection(selectedMode, tier));
	}

	private static boolean isTier(String tier) {
		return tier != null && VALID_TIER.matcher(tier).matches();
	}

	private static int tierWeight(String tier) {
		String normalized = tier.toUpperCase(Locale.ROOT);
		int level = normalized.charAt(2) - '0';
		int position = switch (normalized.charAt(0)) {
			case 'H' -> 0;
			case 'M' -> 1;
			default -> 2;
		};
		return level * 10 + position;
	}

	public record TierSelection(String kit, String tier) {
	}
}
