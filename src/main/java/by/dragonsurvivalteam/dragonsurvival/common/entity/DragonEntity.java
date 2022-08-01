package by.dragonsurvivalteam.dragonsurvival.common.entity;

import by.dragonsurvivalteam.dragonsurvival.client.emotes.Emote;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.AnimationTimer;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.CommonTraits;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.CustomTickAnimationController;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.EmoteCap;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.magic.common.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.ISecondAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.controller.AnimationController.IAnimationPredicate;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.resource.GeckoLibCache;

import java.util.ArrayList;
import java.util.stream.Stream;

public class DragonEntity extends LivingEntity implements IAnimatable, CommonTraits{
	public final ArrayList<Double> bodyYawAverage = new ArrayList<>();
	public final ArrayList<Double> headYawAverage = new ArrayList<>();
	public final ArrayList<Double> headPitchAverage = new ArrayList<>();
	public final ArrayList<Double> tailSideAverage = new ArrayList<>();
	public final ArrayList<Double> tailUpAverage = new ArrayList<>();
	/**
	 * This reference must be updated whenever player is remade, for example, when changing dimensions
	 */
	public volatile int player;
	public boolean neckLocked = false;
	public boolean tailLocked = false;
	public float prevZRot;
	public float prevXRot;

	//Molang queries
	public double lookYaw = 0;
	public double lookPitch = 0;

	public double flightY = 0;
	public double flightX = 0;
	public double tailMotionSide;
	public double tailMotionUp;
	public double body_yaw_change = 0;
	public double head_yaw_change = 0;
	public double head_pitch_change = 0;
	public double tail_motion_up = 0;
	public double tail_motion_side = 0;
	AnimationFactory animationFactory = new AnimationFactory(this);
	ActiveDragonAbility lastCast = null;
	boolean started, ended;
	AnimationTimer animationTimer = new AnimationTimer();
	Emote lastEmote;
	CustomTickAnimationController tailController;
	CustomTickAnimationController headController;
	CustomTickAnimationController biteAnimationController;
	CustomTickAnimationController dragonAnimationController;

	public DragonEntity(EntityType<? extends LivingEntity> type, Level worldIn){
		super(type, worldIn);
	}

	@Override
	public void registerControllers(AnimationData animationData){
		animationData.shouldPlayWhilePaused = true;

		for(int i = 0; i < EmoteCap.MAX_EMOTES; i++){
			int finalI = i;
			IAnimationPredicate<DragonEntity> predicate = (s) -> emotePredicate(finalI, s);
			animationData.addAnimationController(new CustomTickAnimationController(this, "2_" + i, 0, predicate));
		}

		animationData.addAnimationController(dragonAnimationController = new CustomTickAnimationController(this, "3", 2, this::predicate));
		animationData.addAnimationController(biteAnimationController = new CustomTickAnimationController(this, "4", 0, this::bitePredicate));
		animationData.addAnimationController(tailController = new CustomTickAnimationController(this, "5", 0, this::tailPredicate));
		animationData.addAnimationController(headController = new CustomTickAnimationController(this, "1", 0, this::headPredicate));
	}

	private <E extends IAnimatable> PlayState tailPredicate(AnimationEvent<E> animationEvent){
		if(!tailLocked || !ClientConfig.enableTailPhysics){
			animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation("tail_turn", true));
			return PlayState.CONTINUE;
		}else{
			animationEvent.getController().setAnimation(null);
			animationEvent.getController().clearAnimationCache();
			return PlayState.STOP;
		}
	}

	private <E extends IAnimatable> PlayState headPredicate(AnimationEvent<E> animationEvent){
		if(!neckLocked){
			animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation("head_turn", true));
			return PlayState.CONTINUE;
		}else{
			animationEvent.getController().setAnimation(null);
			animationEvent.getController().clearAnimationCache();
			return PlayState.STOP;
		}
	}

	private <E extends IAnimatable> PlayState bitePredicate(AnimationEvent<E> animationEvent){
		Player player = getPlayer();
		DragonStateHandler handler = DragonUtils.getHandler(player);
		AnimationBuilder builder = new AnimationBuilder();

		ActiveDragonAbility curCast = handler.getMagic().getCurrentlyCasting();

		if(curCast instanceof ISecondAnimation || lastCast instanceof ISecondAnimation)
			renderAbility(builder, curCast);


		if(!ClientDragonRender.renderItemsInMouth && animationExists("use_item") && (player.isUsingItem() || (handler.getMovementData().bite || handler.getMovementData().dig) && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()))){
			builder.addAnimation("use_item", true);
			handler.getMovementData().bite = false;
		}else if(!ClientDragonRender.renderItemsInMouth && animationExists("eat_item_right") && player.isUsingItem() && player.getMainHandItem().isEdible() || animationTimer.getDuration("eat_item_right") > 0){
			if(animationTimer.getDuration("eat_item_right") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("eat_item_right", 0.32 * 20, builder);
			}

			builder.addAnimation("eat_item_right", true);
		}else if(!ClientDragonRender.renderItemsInMouth && animationExists("eat_item_left") && player.isUsingItem() && player.getOffhandItem().isEdible() || animationTimer.getDuration("eat_item_right") > 0){
			if(animationTimer.getDuration("eat_item_left") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("eat_item_left", 0.32 * 20, builder);
			}

			builder.addAnimation("eat_item_left", true);
		}else if(!ClientDragonRender.renderItemsInMouth && animationExists("use_item_right") && (!player.getMainHandItem().isEmpty()) && (handler.getMovementData().bite && player.getMainArm() == HumanoidArm.RIGHT) || animationTimer.getDuration("use_item_right") > 0){
			if(animationTimer.getDuration("use_item_right") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("use_item_right", 0.32 * 20, builder);
			}

			builder.addAnimation("use_item_right", true);
		}else if(!ClientDragonRender.renderItemsInMouth && animationExists("use_item_left") && (!player.getOffhandItem().isEmpty() && handler.getMovementData().bite && player.getMainArm() == HumanoidArm.LEFT) || animationTimer.getDuration("use_item_left") > 0){
			if(animationTimer.getDuration("use_item_left") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("use_item_left", 0.32 * 20, builder);
			}

			builder.addAnimation("use_item_left", true);
		}else if(handler.getMovementData().bite && !handler.getMovementData().dig || animationTimer.getDuration("bite") > 0){
			builder.addAnimation("bite", true);
			if(animationTimer.getDuration("bite") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("bite", 0.44 * 20, builder);
			}
		}

		if(builder.getRawAnimationList().size() > 0){
			animationEvent.getController().setAnimation(builder);
			return PlayState.CONTINUE;
		}

		return PlayState.STOP;
	}

	public static boolean animationExists(String key){
		Animation animation = GeckoLibCache.getInstance().getAnimations().get(ClientDragonRender.dragonModel.getAnimationFileLocation(ClientDragonRender.dragonArmor)).getAnimation(key);

		return animation != null;
	}

	private <E extends IAnimatable> PlayState emotePredicate(int num, AnimationEvent<E> animationEvent){
		final Player player = getPlayer();
		DragonStateHandler handler = DragonUtils.getHandler(player);

		if(handler.getEmotes().currentEmotes[num] != null){
			Emote emote = handler.getEmotes().currentEmotes[num];

			neckLocked = emote.locksHead;
			tailLocked = emote.locksTail;

			dragonAnimationController.speed = emote.speed;

			if(emote.animation != null && !emote.animation.isEmpty()){
				animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation(emote.animation, emote.loops));
				lastEmote = emote;
				return PlayState.CONTINUE;
			}
		}

		return PlayState.STOP;
	}

	public Player getPlayer(){
		return (Player)level.getEntity(player);
	}

	@Override
	public AnimationFactory getFactory(){
		return animationFactory;
	}

	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> animationEvent){
		Player player = getPlayer();
		AnimationController animationController = animationEvent.getController();
		DragonStateHandler playerStateHandler = DragonUtils.getHandler(player);

		AnimationBuilder builder = new AnimationBuilder();

		dragonAnimationController.speed = 1;

		if(player == null || Stream.of(playerStateHandler.getEmotes().currentEmotes).anyMatch((s) -> s != null && !s.blend && s.animation != null && !s.animation.isBlank())){
			animationEvent.getController().setAnimation(null);
			animationEvent.getController().clearAnimationCache();
			return PlayState.STOP;
		}

		neckLocked = false;
		tailLocked = false;


		ActiveDragonAbility curCast = playerStateHandler.getMagic().getCurrentlyCasting();

		if(!(curCast instanceof ISecondAnimation) && !(lastCast instanceof ISecondAnimation)){
			renderAbility(builder, curCast);
		}

		Vec3 motio = new Vec3(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo);
		boolean isMovingHorizontal = Math.sqrt(Math.pow(motio.x, 2) + Math.pow(motio.z, 2)) > 0.005;

		if(playerStateHandler.getMagic().onMagicSource){
			neckLocked = true;
			tailLocked = true;
			builder.addAnimation("sit_on_magic_source", true);
		}else if(player.isSleeping() || playerStateHandler.treasureResting){
			neckLocked = true;
			tailLocked = true;
			builder.addAnimation("sleep", true);
		}else if(player.isPassenger()){
			tailLocked = true;
			builder.addAnimation("sit", true);
		}else if(player.getAbilities().flying || ServerFlightHandler.isFlying(player)){
			double preLandDuration = 1;
			double hoverLand = ServerFlightHandler.getLandTime(player, (2.24 + preLandDuration) * 20);
			double fullLand = ServerFlightHandler.getLandTime(player, 2.24 * 20);

			if(player.isCrouching() && fullLand != -1 && player.getDeltaMovement().length() < 4){
				neckLocked = true;

				builder.addAnimation("fly_land_end");
			}else if(player.isCrouching() && hoverLand != -1 && player.getDeltaMovement().length() < 4){
				neckLocked = true;
				builder.addAnimation("fly_land", true);
			}

			if(ServerFlightHandler.isGliding(player)){
				neckLocked = true;
				if(ServerFlightHandler.isSpin(player)){
					tailLocked = true;

					builder.addAnimation("fly_spin_fast", true);
				}else if(player.getDeltaMovement().y < -1){
					builder.addAnimation("fly_dive_alt", true);
				}else if(player.getDeltaMovement().y < -0.25){
					builder.addAnimation("fly_dive", true);
				}else if(player.getDeltaMovement().y > 0.25){
					dragonAnimationController.speed = 1 + player.getDeltaMovement().y / 2 / 5;
					builder.addAnimation("fly_fast", true);
				}else{
					builder.addAnimation("fly_soaring", true);
				}
			}else{
				if(ServerFlightHandler.isSpin(player)){
					neckLocked = true;
					tailLocked = true;
					builder.addAnimation("fly_spin", true);
				}else if(player.getDeltaMovement().y > 0.25){
					dragonAnimationController.speed = 1 + player.getDeltaMovement().y / 2 / 5;
					builder.addAnimation("fly_fast", true);
				}else{
					builder.addAnimation("fly", true);
				}
			}
		}else if(player.getPose() == Pose.SWIMMING){
			if(ServerFlightHandler.isSpin(player)){
				neckLocked = true;
				tailLocked = true;
				builder.addAnimation("fly_spin_fast", true);
			}else{
				dragonAnimationController.speed = 1 + (double)Mth.sqrt((float)(player.getDeltaMovement().x * player.getDeltaMovement().x + player.getDeltaMovement().z * player.getDeltaMovement().z)) / 10;
				builder.addAnimation("swim_fast", true);
			}
		}else if((player.isInLava() || player.isInWaterOrBubble()) && !player.isOnGround()){
			if(ServerFlightHandler.isSpin(player)){
				neckLocked = true;
				tailLocked = true;
				builder.addAnimation("fly_spin_fast", true);
			}else{
				dragonAnimationController.speed = 1 + (double)Mth.sqrt((float)(player.getDeltaMovement().x * player.getDeltaMovement().x + player.getDeltaMovement().z * player.getDeltaMovement().z)) / 10;
				builder.addAnimation("swim", true);
			}
		}else if(!player.isOnGround() && motio.y() < 0){
			if(player.fallDistance <= 4 && !player.onClimbable()){
				builder.addAnimation("land", true);
			}
		}else if(ClientEvents.dragonsJumpingTicks.getOrDefault(this.player, 0) > 0){
			builder.addAnimation("jump", false);
		}else if(player.isShiftKeyDown() || !DragonSizeHandler.canPoseFit(player, Pose.STANDING) && DragonSizeHandler.canPoseFit(player, Pose.CROUCHING)){
			// Player is Sneaking
			if(isMovingHorizontal && player.animationSpeed != 0f){
				builder.addAnimation("sneak_walk", true);
			}else if(playerStateHandler.getMovementData().dig){
				builder.addAnimation("dig_sneak", true);
			}else{
				builder.addAnimation("sneak", true);
			}
		}else if(player.isSprinting()){
			dragonAnimationController.speed = 1 + (double)Mth.sqrt((float)(player.getDeltaMovement().x * player.getDeltaMovement().x + player.getDeltaMovement().z * player.getDeltaMovement().z)) / 10;
			builder.addAnimation("run", true);
		}else if(isMovingHorizontal && player.animationSpeed != 0f){
			dragonAnimationController.speed = 1 + (double)Mth.sqrt((float)(player.getDeltaMovement().x * player.getDeltaMovement().x + player.getDeltaMovement().z * player.getDeltaMovement().z)) / 10;
			builder.addAnimation("walk", true);
		}else if(playerStateHandler.getMovementData().dig){
			builder.addAnimation("dig", true);
		}

		if(animationEvent.getController().getCurrentAnimation() == null || builder.getRawAnimationList().size() <= 0){
			builder.addAnimation("idle", true);
		}

		animationController.setAnimation(builder);
		return PlayState.CONTINUE;
	}

	private void renderAbility(AnimationBuilder builder, ActiveDragonAbility curCast){
		if(curCast != null && lastCast == null){
			if(curCast.getStartingAnimation() != null){
				AbilityAnimation starAni = curCast.getStartingAnimation();
				neckLocked = starAni.locksNeck;
				tailLocked = starAni.locksTail;

				if(!started){
					animationTimer.putAnimation(starAni.animationKey, starAni.duration, builder);
					started = true;
				}

				builder.addAnimation(starAni.animationKey);

				if(animationTimer.getDuration(starAni.animationKey) <= 0){
					lastCast = curCast;
					started = false;
				}
			}else if(curCast.getLoopingAnimation() != null){
				AbilityAnimation loopingAni = curCast.getLoopingAnimation();
				neckLocked = loopingAni.locksNeck;
				tailLocked = loopingAni.locksTail;

				lastCast = curCast;
				builder.addAnimation(loopingAni.animationKey, true);
			}
		}else if(curCast != null){
			lastCast = curCast;

			if(curCast.getLoopingAnimation() != null){
				AbilityAnimation loopingAni = curCast.getLoopingAnimation();
				neckLocked = loopingAni.locksNeck;
				tailLocked = loopingAni.locksTail;

				builder.addAnimation(loopingAni.animationKey, true);
			}
		}else if(lastCast != null){
			if(lastCast.getStoppingAnimation() != null){
				AbilityAnimation stopAni = lastCast.getStoppingAnimation();
				neckLocked = stopAni.locksNeck;
				tailLocked = stopAni.locksTail;

				if(!ended){
					animationTimer.putAnimation(stopAni.animationKey, stopAni.duration, builder);
					ended = true;
				}

				builder.addAnimation(stopAni.animationKey);

				if(animationTimer.getDuration(stopAni.animationKey) <= 0){
					lastCast = null;
					ended = false;
				}
			}else{
				lastCast = null;
			}
		}
	}

	@Override
	public Iterable<ItemStack> getArmorSlots(){
		return getPlayer().getArmorSlots();
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slotIn){
		return getPlayer().getItemBySlot(slotIn);
	}

	@Override
	public void setItemSlot(EquipmentSlot slotIn, ItemStack stack){
		getPlayer().setItemSlot(slotIn, stack);
	}

	@Override
	public HumanoidArm getMainArm(){
		return getPlayer().getMainArm();
	}
}