package dev.uebasusta.tiertagger;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.function.Consumer;

/** User-requested local chat output, never uploaded or sent to the game server. */
final class TierDiagnostics {
	private TierDiagnostics() {}

	static void show(String name, Consumer<Component> output) {
		Minecraft client = Minecraft.getInstance();
		TierSettings settings = TierSettings.get();
		output.accept(Component.literal("Tier Tagger " + McpvpTierTaggerClient.VERSION + " | mod=" + settings.enabled
			+ " | nametag=" + settings.showAboveHead + " | fallback=" + settings.showFallbackTags
			+ " | mode=" + settings.mode).withStyle(ChatFormatting.AQUA));
		if (client.level == null || client.player == null) {
			output.accept(Component.literal("Dünya yüklü değil.").withStyle(ChatFormatting.YELLOW));
			return;
		}
		if (name != null) {
			Player target = client.level.players().stream()
				.filter(player -> player.getGameProfile().name().equalsIgnoreCase(name)).findFirst().orElse(null);
			describe(name, target, client, output);
		} else {
			output.accept(Component.literal("Yüklü oyuncu: " + client.level.players().size()
				+ " | HUD gizli: " + client.gui.hud.isHidden() + " | En yakın 5 görünür oyuncu:"));
			client.level.players().stream()
				.filter(player -> player != client.player && !player.isInvisible() && player.isAlive())
				.sorted(Comparator.comparingDouble(player -> player.distanceToSqr(client.player)))
				.limit(5).forEach(player -> describe(player.getGameProfile().name(), player, client, output));
			output.accept(Component.literal("Tek oyuncuyu sorgulamak için: /utier debug <oyuncu>").withStyle(ChatFormatting.GRAY));
		}
	}

	private static void describe(String name, Player target, Minecraft client, Consumer<Component> output) {
		boolean listed = client.getConnection() != null && client.getConnection().getListedOnlinePlayers().stream()
			.anyMatch(info -> info.getProfile().name().equalsIgnoreCase(name));
		String context = target == null ? "dünyada yok" : "dünyada var"
			+ ", isim görünürlüğü=" + (target.getTeam() == null ? "varsayılan" : target.getTeam().getNameTagVisibility())
			+ ", görüş=" + client.player.hasLineOfSight(target) + ", eğilmiş=" + target.isDiscrete();
		output.accept(Component.literal(name + " | " + context + " | TAB=" + listed
			+ " | " + TierService.get().status(name)).withStyle(ChatFormatting.GRAY));
	}
}
