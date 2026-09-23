package dev.uebasusta.tiertagger.mixin;

import dev.uebasusta.tiertagger.StandaloneTierTag;
import dev.uebasusta.tiertagger.TierNameTagState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements TierNameTagState {
	@Unique
	private StandaloneTierTag uebasusta123$standaloneTierTag;

	@Override
	public StandaloneTierTag uebasusta123$getStandaloneTierTag() {
		return uebasusta123$standaloneTierTag;
	}

	@Override
	public void uebasusta123$setStandaloneTierTag(StandaloneTierTag tag) {
		uebasusta123$standaloneTierTag = tag;
	}
}
