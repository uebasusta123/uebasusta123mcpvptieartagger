package dev.uebasusta.tiertagger.mixin;

import dev.uebasusta.tiertagger.TierSettings;
import dev.uebasusta.tiertagger.TierTagFormatter;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
	@Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
	private void uebasusta123mcpvptieartagger$appendTier(PlayerInfo info, CallbackInfoReturnable<Component> callback) {
		if (TierSettings.get().showInTab) {
			callback.setReturnValue(TierTagFormatter.appendTag(callback.getReturnValue(), info.getProfile().name()));
		}
	}
}
