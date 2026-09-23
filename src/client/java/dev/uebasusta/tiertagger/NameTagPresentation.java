package dev.uebasusta.tiertagger;

import net.minecraft.network.chat.Component;

/** Keeps server names intact; a missing vanilla label can use a separate tier-only line. */
public record NameTagPresentation(Component vanilla, Component standalone) {
	public static NameTagPresentation plan(Component original, TierData data, TierSettings settings,
			boolean fallbackAllowed) {
		if (!settings.enabled || !settings.showAboveHead || data == null) {
			return new NameTagPresentation(original, null);
		}
		if (original != null && !original.getString().isBlank()) {
			return new NameTagPresentation(TierTagFormatter.appendTag(original, data, settings), null);
		}
		Component fallback = settings.showFallbackTags && fallbackAllowed
			? data.select(settings.mode).map(selection -> TierTagFormatter.tag(selection,
				settings.showKit, settings.showIcons)).orElse(null) : null;
		return new NameTagPresentation(original, fallback);
	}

	/** Standalone tags must not reveal a hidden, sneaking, occluded, or distant player. */
	public static boolean permitsFallback(boolean hudVisible, boolean otherPlayer, boolean visible,
			boolean alive, boolean spectator, boolean discrete, boolean lineOfSight, double distanceSquared) {
		return hudVisible && otherPlayer && visible && alive && !spectator && !discrete
			&& lineOfSight && distanceSquared >= 0 && distanceSquared < 64.0 * 64.0;
	}
}
