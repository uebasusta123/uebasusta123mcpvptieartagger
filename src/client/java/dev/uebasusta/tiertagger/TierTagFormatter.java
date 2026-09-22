package dev.uebasusta.tiertagger;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.util.Map;

public final class TierTagFormatter {
	private static final FontDescription.Resource ICON_FONT = new FontDescription.Resource(
		Identifier.fromNamespaceAndPath(McpvpTierTaggerClient.MOD_ID, "kits"));
	// Explicitly reset formatting so server team colours/fonts don't leak into tags.
	private static final Style BASE = Style.EMPTY.withFont(FontDescription.DEFAULT)
		.withColor(ChatFormatting.WHITE).withBold(false).withItalic(false)
		.withUnderlined(false).withStrikethrough(false).withObfuscated(false);

	private TierTagFormatter() {
	}

	public static Component appendTag(Component original, String playerName) {
		TierSettings settings = TierSettings.get();
		if (!settings.enabled) {
			return original;
		}
		TierData data = TierService.get().getOrQueue(playerName);
		if (data == null) {
			return original;
		}
		return data.select(settings.mode)
			.<Component>map(selection -> original.copy().append(tag(selection, settings.showKit, settings.showIcons)))
			.orElse(original);
	}

	/** The same component renders in TAB and nametags: icon, MCPVP, tier. */
	static Component tag(TierData.TierSelection selection, boolean showKit, boolean showIcons) {
		MutableComponent result = Component.literal(" [").setStyle(BASE.withColor(ChatFormatting.DARK_GRAY));
		if (showKit) {
			result.append(showIcons ? icon(selection.kit())
				: Component.literal(KitCatalog.label(selection.kit())).setStyle(BASE));
			result.append(Component.literal(" ").setStyle(BASE));
		}
		result.append(Component.literal("MC").setStyle(BASE.withBold(true)));
		result.append(Component.literal("PVP").setStyle(BASE.withColor(ChatFormatting.RED).withBold(true)));
		result.append(Component.literal(" " + selection.tier()).setStyle(BASE.withColor(tierColor(selection.tier())).withBold(true)));
		result.append(Component.literal("]").setStyle(BASE.withColor(ChatFormatting.DARK_GRAY)));
		return result.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(
			Component.literal(KitCatalog.label(selection.kit()) + " • " + selection.tier()))));
	}

	private static Component icon(String kit) {
		return Component.literal(KitCatalog.get(kit).glyph()).setStyle(BASE.withFont(ICON_FONT));
	}

	public static Component fullCard(TierData data) {
		MutableComponent message = Component.literal(data.name()).setStyle(BASE.withColor(ChatFormatting.AQUA).withBold(true))
			.append(Component.literal("  •  #" + data.overallRank() + "  •  " + data.points() + " puan  •  " + data.region())
				.setStyle(BASE.withColor(ChatFormatting.GRAY)));
		data.select("highest").ifPresent(best -> message
			.append(Component.literal("\nEn iyi:").setStyle(BASE))
			.append(tag(best, true, TierSettings.get().showIcons)));
		for (Map.Entry<String, String> entry : data.kitRanks().entrySet()) {
			message.append(Component.literal("\n").setStyle(BASE));
			if (TierSettings.get().showIcons) {
				message.append(icon(entry.getKey()));
			}
			message.append(Component.literal(" " + KitCatalog.label(entry.getKey()) + ": ").setStyle(BASE.withColor(ChatFormatting.GRAY)));
			message.append(Component.literal(entry.getValue()).setStyle(BASE.withColor(tierColor(entry.getValue())).withBold(true)));
		}
		return message;
	}

	private static ChatFormatting tierColor(String tier) {
		if (tier == null || tier.length() != 3) {
			return ChatFormatting.WHITE;
		}
		return switch (tier.charAt(2)) {
			case '1' -> ChatFormatting.LIGHT_PURPLE;
			case '2' -> ChatFormatting.AQUA;
			case '3' -> ChatFormatting.GOLD;
			case '4' -> ChatFormatting.GRAY;
			case '5' -> ChatFormatting.DARK_GREEN;
			default -> ChatFormatting.WHITE;
		};
	}
}
