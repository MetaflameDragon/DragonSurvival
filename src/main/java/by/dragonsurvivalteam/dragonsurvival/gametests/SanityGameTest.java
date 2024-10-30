package by.dragonsurvivalteam.dragonsurvival.gametests;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(DragonSurvivalMod.MODID)
public class SanityGameTest {
    @PrefixGameTestTemplate(false)
    @GameTest(template = "test_templates/1x1air")
    public static void sanityGameTest(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 0, 0), Blocks.DIAMOND_BLOCK);
        helper.assertBlock(new BlockPos(0, 0, 0), block -> block == Blocks.DIAMOND_BLOCK, "Block should be diamond block!");
        helper.succeed();
    }
}
