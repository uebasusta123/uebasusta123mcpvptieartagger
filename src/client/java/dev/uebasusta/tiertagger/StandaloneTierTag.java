package dev.uebasusta.tiertagger;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/** Immutable data captured during extraction; no world or network access during submission. */
public record StandaloneTierTag(Component label, Vec3 attachment) {
	public void submit(PoseStack poses, SubmitNodeCollector collector, int light, CameraRenderState camera) {
		// false disables the through-walls name-tag pass without changing the entity's state.
		collector.submitNameTag(poses, attachment, 0, label, false, light, camera);
	}
}
