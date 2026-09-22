package dev.uebasusta.tiertagger.mixin;

import dev.uebasusta.tiertagger.TierSettings;
import dev.uebasusta.tiertagger.TierTagFormatter;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
	@Inject(method = "getNameTag", at = @At("RETURN"), cancellable = true)
	private void uebasusta123mcpvptieartagger$appendTier(Entity entity, CallbackInfoReturnable<Component> callback) {
		Component original = callback.getReturnValue();
		if (original != null && TierSettings.get().showAboveHead && entity instanceof Player player) {
			callback.setReturnValue(TierTagFormatter.appendTag(original, player.getGameProfile().name()));
		}
	}
}
