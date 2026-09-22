package dev.uebasusta.tiertagger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Stable kit order also makes tied tiers deterministic across frames/restarts. */
public final class KitCatalog {
	public static final List<Kit> KITS = List.of(
		new Kit("sword", "SWORD", "\uE000", "minecraft:item/diamond_sword.png"),
		new Kit("shield", "SHIELD", "\uE001", "minecraft:gui/sprites/container/slot/shield.png"),
		new Kit("pot", "POT", "\uE002", "minecraft:item/splash_potion.png"),
		new Kit("early-game", "EARLY", "\uE003", "minecraft:item/iron_chestplate.png"),
		new Kit("end-game", "CRYSTAL", "\uE004", "minecraft:item/end_crystal.png"),
		new Kit("mace", "MACE", "\uE005", "minecraft:item/mace.png"),
		new Kit("late-game", "LATE", "\uE006", "minecraft:item/netherite_chestplate.png"),
		new Kit("spear", "SPEAR", "\uE007", "minecraft:item/diamond_spear.png"),
		new Kit("diamond-smp", "DSMP", "\uE008", "minecraft:item/diamond_chestplate.png"),
		new Kit("netherite-pot", "NPOT", "\uE009", "minecraft:item/netherite_sword.png"),
		new Kit("creeper", "CREEPER", "\uE00A", "minecraft:item/gunpowder.png"),
		new Kit("cart", "CART", "\uE00B", "minecraft:item/tnt_minecart.png"),
		new Kit("bow", "BOW", "\uE00C", "minecraft:item/bow.png"),
		new Kit("smp", "SMP", "\uE00D", "minecraft:item/totem_of_undying.png"),
		new Kit("crystal", "CRYSTAL", "\uE00E", "minecraft:item/end_crystal.png")
	);
	private static final Kit UNKNOWN = new Kit("unknown", "?", "\uE00F", "minecraft:item/emerald.png");

	private KitCatalog() {
	}

	public static Kit get(String key) {
		return KITS.stream().filter(kit -> kit.key().equals(key)).findFirst().orElse(UNKNOWN);
	}

	public static String label(String key) {
		Kit kit = get(key);
		return kit == UNKNOWN ? key.toUpperCase(Locale.ROOT) : kit.label();
	}

	public static int priority(String key) {
		int index = KITS.indexOf(get(key));
		return index < 0 ? KITS.size() : index;
	}

	public static List<String> modes() {
		List<String> modes = new ArrayList<>();
		modes.add("highest");
		KITS.forEach(kit -> modes.add(kit.key()));
		return List.copyOf(modes);
	}

	public record Kit(String key, String label, String glyph, String texture) {
	}
}
