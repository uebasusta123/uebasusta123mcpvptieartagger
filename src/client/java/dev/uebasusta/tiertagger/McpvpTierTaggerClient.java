package dev.uebasusta.tiertagger;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class McpvpTierTaggerClient implements ClientModInitializer {
	public static final String MOD_ID = "uebasusta123mcpvptieartagger";

	@Override
	public void onInitializeClient() {
		TierSettings.load();
		registerCommands();
		PlayerTierPrefetcher prefetcher = new PlayerTierPrefetcher();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			var level = client.level;
			TierSettings settings = TierSettings.get();
			prefetcher.tick(level, settings.enabled && settings.showAboveHead,
				() -> level.players(), player -> player.getGameProfile().name(),
				TierService.get()::getOrQueue);
		});
	}

	private static void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			var command = dispatcher.register(ClientCommands.literal(MOD_ID)
				.executes(context -> {
					send(Component.literal(MOD_ID + ": /utier <oyuncu>, /utier mode highest, /utier tab, /utier nametag, /utier icons, /utier refresh")
						.withStyle(ChatFormatting.AQUA));
					return 1;
				})
				.then(ClientCommands.literal("mode")
					.then(ClientCommands.argument("mode", StringArgumentType.word())
						.suggests((context, builder) -> {
							TierSettings.MODES.forEach(builder::suggest);
							return builder.buildFuture();
						})
						.executes(context -> {
							String mode = StringArgumentType.getString(context, "mode").toLowerCase(Locale.ROOT);
							if (!TierSettings.MODES.contains(mode)) {
								send(Component.literal("Bilinmeyen mod: " + mode).withStyle(ChatFormatting.RED));
								return 0;
							}
							TierSettings.get().mode = mode;
							TierSettings.save();
							send(Component.literal("Tier modu: " + mode).withStyle(ChatFormatting.GREEN));
							return 1;
						})))
				.then(ClientCommands.literal("tab").executes(context -> {
					TierSettings.get().showInTab = !TierSettings.get().showInTab;
					TierSettings.save();
					send(toggleMessage("TAB", TierSettings.get().showInTab));
					return 1;
				}))
				.then(ClientCommands.literal("nametag").executes(context -> {
					TierSettings.get().showAboveHead = !TierSettings.get().showAboveHead;
					TierSettings.save();
					send(toggleMessage("Nametag", TierSettings.get().showAboveHead));
					return 1;
				}))
				.then(ClientCommands.literal("icons").executes(context -> {
					TierSettings.get().showIcons = !TierSettings.get().showIcons;
					TierSettings.save();
					send(toggleMessage("Oyun modu simgeleri", TierSettings.get().showIcons));
					return 1;
				}))
				.then(ClientCommands.literal("refresh").executes(context -> {
					TierService.get().clearCache();
					send(Component.literal("Tier cache temizlendi.").withStyle(ChatFormatting.GREEN));
					return 1;
				}))
				.then(ClientCommands.argument("player", StringArgumentType.word()).executes(context -> {
					String playerName = StringArgumentType.getString(context, "player");
					if (!TierService.isValidPlayerName(playerName)) {
						send(Component.literal("Geçersiz oyuncu adı.").withStyle(ChatFormatting.RED));
						return 0;
					}

					send(Component.literal(playerName + " aranıyor...").withStyle(ChatFormatting.GRAY));
					TierService.get().lookup(playerName, result -> Minecraft.getInstance().execute(() -> {
						if (result.isEmpty()) {
							send(Component.literal(playerName + " için MCPvP tier kaydı bulunamadı.").withStyle(ChatFormatting.YELLOW));
							return;
						}
						send(TierTagFormatter.fullCard(result.get()));
					}));
					return 1;
				}))
			);
			dispatcher.register(ClientCommands.literal("utier").executes(command.getCommand()).redirect(command));
		});
	}

	private static Component toggleMessage(String label, boolean enabled) {
		return Component.literal(label + ": " + (enabled ? "açık" : "kapalı"))
			.withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
	}

	private static void send(Component message) {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null) {
			client.player.sendSystemMessage(message);
		}
	}
}
