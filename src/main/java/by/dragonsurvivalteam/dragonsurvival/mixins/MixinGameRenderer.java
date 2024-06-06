package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin( GameRenderer.class )
public class MixinGameRenderer{
	@ModifyReturnValue( method = "getNightVisionScale", at = @At( value = "RETURN"))
	private static float modifyNightVisionScale(float original) {
		return ClientConfig.stableNightVision ? 1f : original;
	}
}