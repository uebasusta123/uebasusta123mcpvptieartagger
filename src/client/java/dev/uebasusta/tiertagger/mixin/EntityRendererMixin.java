package dev.uebasusta.tiertagger.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.uebasusta.tiertagger.NameTagPresentation;
import dev.uebasusta.tiertagger.StandaloneTierTag;
import dev.uebasusta.tiertagger.TierNameTagState;
import dev.uebasusta.tiertagger.TierService;
import dev.uebasusta.tiertagger.TierSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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
		TierNameTagState extra = (TierNameTagState) state;
		// Render states may be reused. Never leave a previous player's tag behind.
		extra.uebasusta123$setStandaloneTierTag(null);
		TierSettings settings = TierSettings.get();
		if (!settings.enabled || !settings.showAboveHead || !(entity instanceof Player player)) {
			return;
		}
		var data = TierService.get().getOrQueue(player.getGameProfile().name());
		if (data == null) {
			return;
		}
		boolean fallbackAllowed = false;
		if (settings.showFallbackTags && (state.nameTag == null || state.nameTag.getString().isBlank())) {
			Minecraft client = Minecraft.getInstance();
			if (client.player != null && client.level == player.level()) {
				fallbackAllowed = NameTagPresentation.permitsFallback(
					!client.gui.hud.isHidden(), player != client.player && player != client.getCameraEntity(),
					!player.isInvisible() && !player.isInvisibleTo(client.player),
					player.isAlive() && !player.isRemoved(), player.isSpectator(), player.isDiscrete(),
					true, state.distanceToCameraSq) && client.player.hasLineOfSight(player);
			}
		}
		NameTagPresentation presentation = NameTagPresentation.plan(state.nameTag, data, settings, fallbackAllowed);
		state.nameTag = presentation.vanilla();
		if (presentation.standalone() != null) {
			Vec3 attachment = player.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, player.getYRot(partialTicks));
			if (attachment == null) {
				attachment = new Vec3(0, player.getBbHeight(), 0);
			}
			// Keep clear of the server's own hologram; do not copy or replace its text.
			extra.uebasusta123$setStandaloneTierTag(new StandaloneTierTag(
				presentation.standalone(), attachment.add(0, 0.55, 0)));
		}
	}

	@Inject(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
		at = @At("RETURN"))
	private void uebasusta123mcpvptieartagger$submitTier(EntityRenderState state, PoseStack poses,
			SubmitNodeCollector collector, CameraRenderState camera, int nameOffset, CallbackInfo callback) {
		StandaloneTierTag tag = ((TierNameTagState) state).uebasusta123$getStandaloneTierTag();
		if (tag != null) {
			tag.submit(poses, collector, state.lightCoords, camera);
		}
	}
}
