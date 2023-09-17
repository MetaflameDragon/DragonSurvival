package by.dragonsurvivalteam.dragonsurvival.client.gui;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.magic.ClientMagicHUDHandler;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SkinCap;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Supplier;

public class SkinsScreen extends Screen{
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/skin_interface.png");

	private static final ResourceLocation UNCHECKED = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/unchecked.png");
	private static final ResourceLocation CHECKED = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/dragon_claws_checked.png");

	private static final ResourceLocation DISCORD = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/discord_button.png");
	private static final ResourceLocation WIKI = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/wiki_button.png");
	private static final ResourceLocation HELP = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/skin_help.png");

	private static final String DISCORD_URL = "https://discord.gg/8SsB8ar";
	private static final String WIKI_URL = "https://github.com/DragonSurvivalTeam/DragonSurvival/wiki/3.-Customization";
	private static final ArrayList<String> seenSkins = new ArrayList<>();
	private final DragonStateHandler handler = new DragonStateHandler();
	public static ResourceLocation skinTexture = null;
	public static ResourceLocation glowTexture = null;
	private static String playerName = null;
	private static String lastPlayerName = null;
	private static DragonLevel level = DragonLevel.ADULT;
	private static boolean noSkin = false;
	private static boolean loading = false;
	public Screen sourceScreen;
	private int guiLeft;
	private int guiTop;
	private float yRot = -3;
	private float xRot = -5;
	private float zoom = 0;
	private URI clickedLink;
	protected int imageWidth = 164;
	protected int imageHeight = 128;

	// To avoid having to retrieve the player capabilities every render tick
	private boolean renderNewBorn;
	private boolean renderYoung;
	private boolean renderAdult;

	public SkinsScreen(Screen sourceScreen){
		super(Component.empty());
		this.sourceScreen = sourceScreen;

		LocalPlayer localPlayer = sourceScreen.getMinecraft().player;
		SkinCap skinData = DragonUtils.getHandler(localPlayer).getSkinData();
		renderNewBorn = skinData.renderNewborn;
		renderYoung = skinData.renderYoung;
		renderAdult = skinData.renderAdult;
	}

	@Override
	public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (minecraft == null) {
			return;
		}

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0F, 0F, -500);
		renderBackground(guiGraphics);
		guiGraphics.pose().popPose();

		int startX = guiLeft;
		int startY = guiTop;

		final CoreGeoBone neckandHead = ClientDragonRender.dragonModel.getAnimationProcessor().getBone("Neck");

		if(neckandHead != null){
			neckandHead.setHidden(false);
		}

		DragonEntity dragon = FakeClientPlayerUtils.getFakeDragon(0, handler);
		EntityRenderer<? super DragonEntity> dragonRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(dragon);

		if(noSkin && Objects.equals(playerName, minecraft.player.getGameProfile().getName())){
			ClientDragonRender.dragonModel.setCurrentTexture(null);
			((DragonRenderer)dragonRenderer).glowTexture = null;
		}else{
			ClientDragonRender.dragonModel.setCurrentTexture(skinTexture);
			((DragonRenderer)dragonRenderer).glowTexture = glowTexture;
		}

		float scale = zoom;

		if(!loading){
			handler.setHasWings(true);
			handler.setSize(level.size);
			handler.setType(DragonUtils.getDragonType(minecraft.player));

			handler.getSkinData().skinPreset.initDefaults(handler);

			if(noSkin && Objects.equals(playerName, minecraft.player.getGameProfile().getName())){
				handler.getSkinData().skinPreset.readNBT(DragonUtils.getHandler(minecraft.player).getSkinData().skinPreset.writeNBT());
			}else{
				handler.getSkinData().skinPreset.skinAges.get(level).get().defaultSkin = true;
			}

			FakeClientPlayerUtils.getFakePlayer(0, handler).animationSupplier = () -> "fly";

			guiGraphics.pose().pushPose();
			guiGraphics.pose().scale(scale, scale, scale);
			guiGraphics.pose().translate(0, 0, 100);
			ClientDragonRender.renderEntityInInventory(dragon, startX + 15, startY + 70, scale, xRot, yRot);
			guiGraphics.pose().popPose();
		}

		((DragonRenderer)dragonRenderer).glowTexture = null;

		guiGraphics.pose().popPose();

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0F, 0F, 400);

		RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
		guiGraphics.blit(BACKGROUND_TEXTURE, startX + 128, startY, 0, 0, 164, 256);

		drawNonShadowString(guiGraphics, minecraft.font, Component.translatable("ds.gui.skins").withStyle(ChatFormatting.BLACK), startX + 128 + imageWidth / 2, startY + 7, -1);
		guiGraphics.drawCenteredString(minecraft.font, Component.translatable("ds.gui.skins.toggle"), startX + 128 + imageWidth / 2, startY + 30, -1);
		drawNonShadowString(guiGraphics, minecraft.font, Component.empty().append(playerName + " - " + level.getName()).withStyle(ChatFormatting.GRAY), startX + 15, startY - 15, -1);

		if(!loading){
			if(noSkin){
				if(playerName.equals(minecraft.player.getGameProfile().getName())){
					drawNonShadowLineBreak(guiGraphics, minecraft.font, Component.translatable("ds.gui.skins.noskin.yours").withStyle(ChatFormatting.DARK_GRAY), startX + 40, startY + imageHeight - 20, -1);
				}else{
					drawNonShadowLineBreak(guiGraphics, minecraft.font, Component.translatable("ds.gui.skins.noskin").withStyle(ChatFormatting.DARK_GRAY), startX + 65, startY + imageHeight - 20, -1);
				}
			}
		}

		super.render(guiGraphics, mouseX, mouseY, partialTick);

		guiGraphics.pose().popPose();
	}

	public static void drawNonShadowString(@NotNull final GuiGraphics guiGraphics, final Font font, final Component component, int x, int y, int color) {
		guiGraphics.drawString(font, Language.getInstance().getVisualOrder(component), x - font.width(component) / 2, y, color, false);
	}

	public static void drawNonShadowLineBreak(@NotNull final GuiGraphics guiGraphics, final Font font, final Component component, int x, int y, int color) {
		List<FormattedText> wrappedLine = font.getSplitter().splitLines(component, 150, Style.EMPTY);

		for (int i = 0; i < wrappedLine.size(); i++) {
			FormattedText properties = wrappedLine.get(i);
			guiGraphics.drawString(font, Language.getInstance().getVisualOrder(properties), x - font.width(component.getVisualOrderText()) / 2, y + i * 9, color, false);
		}
	}

	@Override
	public void init(){
		super.init();

		guiLeft = (width - 256) / 2;
		guiTop = (height - 128) / 2;

		int startX = guiLeft;
		int startY = guiTop;

		if(playerName == null){
			playerName = minecraft.player.getGameProfile().getName();
		}

		setTextures();

		addRenderableWidget(new TabButton(startX + 128 + 4, startY - 26, 0, this));
		addRenderableWidget(new TabButton(startX + 128 + 33, startY - 26, 1, this));
		addRenderableWidget(new TabButton(startX + 128 + 62, startY - 26, 2, this));
		addRenderableWidget(new TabButton(startX + 128 + 91, startY - 28, 3, this));

		// Button to enable / disable rendering of the newborn dragon skin
		addRenderableWidget(new Button(startX + 128, startY + 45, imageWidth, 20, Component.translatable("ds.level.newborn"), button -> {
			DragonStateHandler handler = DragonUtils.getHandler(getMinecraft().player);
			boolean newValue = !handler.getSkinData().renderNewborn;

			handler.getSkinData().renderNewborn = newValue;
			renderNewBorn = newValue;
			ConfigHandler.updateConfigValue("renderNewbornSkin", newValue);

			NetworkHandler.CHANNEL.sendToServer(new SyncDragonSkinSettings(getMinecraft().player.getId(), handler.getSkinData().renderNewborn, handler.getSkinData().renderYoung, handler.getSkinData().renderAdult));
			setTextures();
		}, Supplier::get) {
			@Override
			protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
				guiGraphics.blit(renderNewBorn ? UNCHECKED : CHECKED, getX() + 3, getY() + 3, 0, 0, 13, 13, 13, 13 );
			}
		});

		// Button to enable / disable rendering of the young dragon skin
		addRenderableWidget(new Button(startX + 128, startY + 45 + 23, imageWidth, 20, Component.translatable("ds.level.young"), button -> {
			DragonStateHandler handler = DragonUtils.getHandler(getMinecraft().player);
			boolean newValue = !handler.getSkinData().renderYoung;

			handler.getSkinData().renderYoung = newValue;
			renderYoung = newValue;
			ConfigHandler.updateConfigValue("renderYoungSkin", handler.getSkinData().renderYoung);

			NetworkHandler.CHANNEL.sendToServer(new SyncDragonSkinSettings(getMinecraft().player.getId(), handler.getSkinData().renderNewborn, handler.getSkinData().renderYoung, handler.getSkinData().renderAdult));
			setTextures();
		}, Supplier::get) {
			@Override
			protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
				guiGraphics.blit(renderYoung ? UNCHECKED : CHECKED, getX() + 3, getY() + 3, 0, 0, 13, 13, 13, 13);
			}
		});

		// Button to enable / disable rendering of the adult dragon skin
		addRenderableWidget(new Button(startX + 128, startY + 45 + 46, imageWidth, 20, Component.translatable("ds.level.adult"), button -> {
			DragonStateHandler handler = DragonUtils.getHandler(getMinecraft().player);
			boolean newValue = !handler.getSkinData().renderAdult;

			handler.getSkinData().renderAdult = newValue;
			renderAdult = newValue;
			ConfigHandler.updateConfigValue("renderAdultSkin", handler.getSkinData().renderAdult);

			NetworkHandler.CHANNEL.sendToServer(new SyncDragonSkinSettings(getMinecraft().player.getId(), handler.getSkinData().renderNewborn, handler.getSkinData().renderYoung, handler.getSkinData().renderAdult));
			setTextures();
		}, Supplier::get) {
			@Override
			protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
				guiGraphics.blit(renderAdult ? UNCHECKED : CHECKED, getX() + 3, getY() + 3, 0, 0, 13, 13, 13, 13);
			}
		});

		// Button to enable / disable the rendering of customized skins (of other players)
		addRenderableWidget(new Button(startX + 128, startY + 128, imageWidth, 20, Component.translatable("ds.gui.skins.other_skins"), button -> {
			ConfigHandler.updateConfigValue("renderOtherPlayerSkins", !ClientDragonRender.renderOtherPlayerSkins);
			setTextures();
		}, Supplier::get) {
			@Override
			protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
				guiGraphics.blit(ClientDragonRender.renderOtherPlayerSkins ? CHECKED : UNCHECKED, getX() + 3, getY() + 3, 0, 0, 13, 13, 13, 13);
			}
		});

		addRenderableWidget(new Button(startX + 128 + imageWidth / 2 - 8 - 25, startY + 128 + 30, 16, 16, Component.empty(), button -> {
			try{
				URI uri = new URI(DISCORD_URL);
				clickedLink = uri;
				minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, DISCORD_URL, false));
			}catch(URISyntaxException urisyntaxexception){
				urisyntaxexception.printStackTrace();
			}
		}, Supplier::get) {
			@Override
			protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				guiGraphics.blit(DISCORD, getX(), getY(), 0, 0, 16, 16, 16, 16);

				if (isHovered()) {
					guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("ds.gui.skins.tooltip.discord"), mouseX, mouseY);
				}
			}
		});

		addRenderableWidget(new Button(startX + 128 + imageWidth / 2 - 8 + 25, startY + 128 + 30, 16, 16, Component.empty(), button -> {
			try{
				URI uri = new URI(WIKI_URL);
				clickedLink = uri;
				minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, WIKI_URL, false));
			}catch(URISyntaxException urisyntaxexception){
				urisyntaxexception.printStackTrace();
			}
		}, Supplier::get) {
			@Override
			protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				guiGraphics.blit(WIKI, getX(), getY(), 0, 0, 16, 16, 16, 16);

				if (isHovered()) {
					guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("ds.gui.skins.tooltip.wiki"), mouseX, mouseY);
				}
			}
		});

		addRenderableWidget(new HelpButton(startX + 128 + imageWidth / 2 - 8, startY + 128 + 30, 16, 16,  "ds.gui.skins.tooltip.help", 1));

		addRenderableWidget(Button.builder(Component.translatable("ds.gui.skins.yours"), button -> {
			playerName = minecraft.player.getGameProfile().getName();
			setTextures();
		}).bounds(startX - 60, startY + 128, 90, 20).tooltip(Tooltip.create(Component.translatable("ds.gui.skins.tooltip.yours"))).build());

		addRenderableWidget(Button.builder(Component.translatable("ds.gui.skins.random"), button -> {
			ArrayList<Pair<DragonLevel, String>> skins = new ArrayList<>();
			HashSet<String> users = new HashSet<>();
			Random random = new Random();

			for (Map.Entry<DragonLevel, HashMap<String, DragonSkins.SkinObject>> ent : DragonSkins.SKIN_USERS.entrySet()){
				for (Map.Entry<String,DragonSkins.SkinObject> user : ent.getValue().entrySet()){
					if (!user.getValue().glow){
						skins.add(Pair.of(ent.getKey(), user.getKey()));
						users.add(user.getKey());
					}
				}
			}

			skins.removeIf(c -> seenSkins.contains(c.second));
			if(!skins.isEmpty()){
				Pair<DragonLevel, String> skin = skins.get(random.nextInt(skins.size()));

				if(skin != null){
					level = skin.first;
					playerName = skin.second;

					seenSkins.add(skin.second);

					if(seenSkins.size() >= users.size() / 2){
						seenSkins.remove(0);
					}

					setTextures();
				}
			}
		}).bounds(startX + 35, startY + 128, 60, 20).tooltip(Tooltip.create(Component.translatable("ds.gui.skins.tooltip.random"))).build());

		addRenderableWidget(new Button(startX + 90, startY + 10, 11, 17, Component.empty(), button -> {
			int pos = Mth.clamp(level.ordinal() + 1, 0, DragonLevel.values().length - 1);
			level = DragonLevel.values()[pos];
			setTextures();
		}, Supplier::get) {
			@Override
			protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				if (isHoveredOrFocused()) {
					guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, getX(), getY(), (float) 66 / 2, (float) 222 / 2, 11, 17, 128, 128);
				} else {
					guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, getX(), getY(), (float) 44 / 2, (float) 222 / 2, 11, 17, 128, 128);
				}
			}
		});

		addRenderableWidget(new Button(startX - 70, startY + 10, 11, 17, Component.empty(), button -> {
			int pos = Mth.clamp(level.ordinal() - 1, 0, DragonLevel.values().length - 1);
			level = DragonLevel.values()[pos];
			setTextures();
		}, Supplier::get) {
			@Override
			protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				if (isHoveredOrFocused()) {
					guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, getX(), getY(), (float) 22 / 2, (float) 222 / 2, 11, 17, 128, 128);
				}else{
					guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, getX(), getY(), 0, (float) 222 / 2, 11, 17, 128, 128);
				}
			}
		});
	}

	public void setTextures(){
		loading = true;

		ResourceLocation skinTexture = DragonSkins.getPlayerSkin(playerName, level);
		ResourceLocation glowTexture = null;
		boolean defaultSkin = false;

		if(!DragonSkins.renderStage(minecraft.player, level) && playerName == minecraft.player.getGameProfile().getName() || skinTexture == null){
			skinTexture = null;
			defaultSkin = true;
		}

		if(skinTexture != null){
			glowTexture = DragonSkins.getPlayerGlow(playerName, level);
		}

		SkinsScreen.glowTexture = glowTexture;
		SkinsScreen.skinTexture = skinTexture;

		if(Objects.equals(lastPlayerName, playerName) || lastPlayerName == null){
			zoom = level.size;
		}

		noSkin = defaultSkin;
		loading = false;
		lastPlayerName = playerName;
	}

	private void confirmLink(boolean p_231162_1_){
		if(p_231162_1_){
			openLink(clickedLink);
		}

		clickedLink = null;
		minecraft.setScreen(this);
	}

	private void openLink(URI p_231156_1_){
		Util.getPlatform().openUri(p_231156_1_);
	}

	@Override
	public boolean isPauseScreen(){
		return false;
	}

	@Override
	public boolean mouseDragged(double x1, double y1, int p_231045_5_, double x2, double y2){
		xRot -= (float) (x2 / 5);
		yRot -= (float) (y2 / 5);

		return super.mouseDragged(x1, y1, p_231045_5_, x2, y2);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount){
		zoom += (float)amount;
		zoom = Mth.clamp(zoom, 10, 80);

		return true;
	}
}