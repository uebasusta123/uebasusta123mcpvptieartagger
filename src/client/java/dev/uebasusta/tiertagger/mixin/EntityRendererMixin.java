package dev.uebasusta.tiertagger.mixin;

import dev.uebasusta.tiertagger.TierSettings;
import dev.uebasusta.tiertagger.TierTagFormatter;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
	@Inject(method = "extractNameTags(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FDD)V",
		at = @At("RETURN"))
	private void uebasusta123mcpvptieartagger$appendTier(Entity entity, EntityRenderState state,
			float partialTicks, double nameTagDistance, double belowNameDistance, CallbackInfo callback) {
		// Modify the final vanilla label even when a renderer overrides getNameTag.
		// Do not create labels for hidden names, invisible players, or custom holograms.
		if (state.nameTag != null && TierSettings.get().showAboveHead && entity instanceof Player player) {
			state.nameTag = TierTagFormatter.appendTag(state.nameTag, player.getGameProfile().name());
		}
	}
}
