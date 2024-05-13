package by.dragonsurvivalteam.dragonsurvival.common.dragon_types.bodies;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import net.minecraft.nbt.CompoundTag;

public class NorthBodyType extends AbstractDragonBody {

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northJumpBonus", comment = "The jump bonus given to North-type dragons")
	public static Double northJumpBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northStepBonus", comment = "The step bonus given to North-type dragons")
	public static Double northStepBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northDamageBonus", comment = "The damage bonus given to North-type dragons")
	public static Double northDamageBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northArmorBonus", comment = "The armor bonus given to North-type dragons")
	public static Double northArmorBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northManaBonus", comment = "The mana bonus given to North-type dragons")
	public static Double northManaBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northSwimSpeedBonus", comment = "The swimSpeed bonus given to North-type dragons")
	public static Double northSwimSpeedBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northHealthBonus", comment = "The health bonus given to North-type dragons")
	public static Double northHealthBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northKnockbackBonus", comment = "The knockback bonus given to North-type dragons")
	public static Double northKnockbackBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northRunMult", comment = "The run speed multiplier given to North-type dragons")
	public static Double northRunMult = 1.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northDamageMult", comment = "The damage multiplier given to North-type dragons")
	public static Double northDamageMult = 1.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northExpMult", comment = "The exp multiplier given to North-type dragons")
	public static Double northExpMult = 1.0;

	@ConfigRange(min = 0.0, max = 10)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northFlightMult", comment = "The flight multiplier given to North-type dragons")
	public static Double northFlightMult = 1.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northFlightStaminaMult", comment = "The flightStamina multiplier given to North-type dragons")
	public static Double northFlightStaminaMult = 1.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northHealthMult", comment = "The health multiplier given to North-type dragons")
	public static Double northHealthMult = 1.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "north"}, key = "northGravityMult", comment = "The gravity multiplier given to North-type dragons")
	public static Double northGravityMult = 1.0;

	@Override
	public CompoundTag writeNBT() {
		CompoundTag tag = new CompoundTag();
		return tag;
	}

	@Override
	public void readNBT(CompoundTag base) {}

	@Override
	public String getBodyName() {
		return "north";
	}

	public Double getHeightMult() {
		return 0.55;
	}

	public Double getEyeHeightMult() {
		return 0.55;
	}

	public Boolean isSquish() {
		return AbstractDragonBody.bodyAffectsHitbox;
	}

	@Override
	public void onPlayerUpdate() {}

	@Override
	public void onPlayerDeath() {}

	public Double getJumpBonus() {
		return northJumpBonus;
	}
	public Double getStepBonus() {
		return northStepBonus;
	}
	public Double getDamageBonus() {
		return northDamageBonus;
	}
	public Double getArmorBonus() {
		return northArmorBonus;
	}
	public Double getManaBonus() {
		return northManaBonus;
	}
	public Double getSwimSpeedBonus() {
		return northSwimSpeedBonus;
	}
	public Double getHealthBonus() {
		return northHealthBonus;
	}
	public Double getKnockbackBonus() {
		return northKnockbackBonus;
	}
	public Double getRunMult() {
		return northRunMult;
	}
	public Double getDamageMult() {
		return northDamageMult;
	}
	public Double getExpMult() {
		return northExpMult;
	}
	public Double getFlightMult() {
		return northFlightMult;
	}
	public Double getFlightStaminaMult() {
		return northFlightStaminaMult;
	}
	public Double getHealthMult() {
		return northHealthMult;
	}
	public Double getGravityMult() {
		return northGravityMult;
	}
}