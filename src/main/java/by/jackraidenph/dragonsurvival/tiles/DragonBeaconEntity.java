package by.jackraidenph.dragonsurvival.tiles;

import by.jackraidenph.dragonsurvival.registration.TileEntityTypesInit;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;

public class DragonBeaconEntity extends BaseBlockEntity implements ITickableTileEntity {
    public Type type = Type.NONE;

    public enum Type {
        PEACE,
        MAGIC,
        VETO,
        NONE
    }

    public DragonBeaconEntity() {
        super(TileEntityTypesInit.dragonBeacon);
    }

    @Override
    public void tick() {

    }

    @Override
    public CompoundNBT save(CompoundNBT compoundNBT) {
        compoundNBT.putString("Type", type.name());
        return super.save(compoundNBT);
    }

    @Override
    public void load(BlockState p_230337_1_, CompoundNBT compoundNBT) {
        super.load(p_230337_1_, compoundNBT);
        type = Type.valueOf(compoundNBT.getString("Type"));
    }
}
