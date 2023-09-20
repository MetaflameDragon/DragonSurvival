package by.dragonsurvivalteam.dragonsurvival.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.RawAnimation;

public class AnimationUtils {
    public static RawAnimation createAnimation(@Nullable final RawAnimation builder, @NotNull final RawAnimation staticAnimation) {
        if (builder == null) {
            return staticAnimation;
        }

        assert staticAnimation.getAnimationStages().size() == 1;
        RawAnimation.Stage stage = staticAnimation.getAnimationStages().get(0);
        builder.then(stage.animationName(), stage.loopType());

        return builder;
    }
}