package by.dragonsurvivalteam.dragonsurvival.network.container;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestOpenInventory implements IMessage<RequestOpenInventory.Data> {

	public static void handleServer(final RequestOpenInventory.Data message, final IPayloadContext context) {
		Player sender = context.player();
		context.enqueueWork(() -> {
            sender.containerMenu.removed(sender);

            InventoryMenu inventory = sender.inventoryMenu;
			sender.openMenu((MenuProvider) inventory);
			//sender.initMenu(inventory);
		});
	}

	public record Data() implements CustomPacketPayload {

		public static final Type<RequestOpenInventory.Data> TYPE = new Type<>(new ResourceLocation(MODID, "open_inventory"));

		public static final StreamCodec<ByteBuf, RequestOpenInventory.Data> STREAM_CODEC = new StreamCodec<>(){
			@Override
			public void encode(ByteBuf pBuffer, RequestOpenInventory.Data pValue) {}

			@Override
			public RequestOpenInventory.Data decode(ByteBuf pBuffer) { return new RequestOpenInventory.Data(); }
		};

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}