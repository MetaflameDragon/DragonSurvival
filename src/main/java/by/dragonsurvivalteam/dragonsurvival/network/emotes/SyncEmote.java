package by.dragonsurvivalteam.dragonsurvival.network.emotes;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;
import static by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler.DRAGON_HANDLER;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncEmote implements IMessage<SyncEmote.Data> {

	public static void handleServer(final SyncEmote.Data message, final IPayloadContext context) {
		context.enqueueWork(() -> {
			Player sender = context.player();
			DragonStateHandler handler = sender.getData(DRAGON_HANDLER);
			handler.getEmoteData().deserializeNBT(context.player().registryAccess(), message.nbt);
		});
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), message);
	}

	public static void handleClient(final SyncEmote.Data message, final IPayloadContext context) {
		Player sender = context.player();
		DragonStateHandler handler = sender.getData(DRAGON_HANDLER);
		handler.getEmoteData().deserializeNBT(context.player().registryAccess(), message.nbt);
	}

	public record Data(int playerId, CompoundTag nbt) implements CustomPacketPayload {
		public static final Type<Data> TYPE = new Type<>(new ResourceLocation(MODID, "emote"));

		public static final StreamCodec<FriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT,
			Data::playerId,
			ByteBufCodecs.COMPOUND_TAG,
			Data::nbt,
			Data::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}