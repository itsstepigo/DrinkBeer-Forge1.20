package lekavar.lma.drinkbeer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.blocks.TradeboxBlock;
import lekavar.lma.drinkbeer.managers.TradeBoxManager;
import lekavar.lma.drinkbeer.networking.NetWorking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.awt.*;


public class TradeBoxContainerScreen extends AbstractContainerScreen<TradeBoxContainer> {
    private static final ResourceLocation TRADE_BOX_GUI = new ResourceLocation(DrinkBeer.MOD_ID, "textures/gui/container/trade_box.png");
    private final int textureWidth = 176;
    private final int textureHeight = 166;
    private Inventory inventory;
    TradeBoxContainer container;

    public TradeBoxContainerScreen(TradeBoxContainer screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
        this.imageWidth = textureWidth;
        this.imageHeight = textureHeight;

        this.inventory = inv;
        this.container = screenContainer;
    }

    @Override
    protected void renderBg(GuiGraphics stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TRADE_BOX_GUI);
        int backgroundWidth = this.getXSize();
        int backgroundHeight = this.getYSize();
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        stack.blit(TRADE_BOX_GUI, x, y, 0, 0, backgroundWidth, backgroundHeight);
        if (container.isCooling()) {
            stack.blit(TRADE_BOX_GUI,x + 84, y + 25, 178, 38, 72, 36);
            String timeStr = convertTickToTime(container.getCoolingTime());
            stack.drawString(font, timeStr, x + 114, y + 39, new Color(64, 64, 64, 255).getRGB());
        } else if (container.isTrading()) {
            if (isHovering(157, 6, 13, 13, (double) mouseX, (double) mouseY)) {
                stack.blit(TRADE_BOX_GUI, x + 155, y + 4, 178, 19, 16, 16);
            } else {
                stack.blit(TRADE_BOX_GUI, x + 155, y + 4, 178, 0, 16, 16);
            }
        }
        if (!container.isCooling()) {
            Language language = Language.getInstance();
            String youStr = language.getOrDefault("drinkbeer.resident.you");
            stack.drawString(font, youStr, x + 85, y + 16, new Color(64, 64, 64, 255).getRGB());
            String locationAndResidentStr =
                    language.getOrDefault(TradeBoxManager.getLocationTranslationKey(container.getLocationId()))
                            + "-" +
                            language.getOrDefault(TradeBoxManager.getResidentTranslationKey(container.getResidentId()));
            stack.drawString(font, locationAndResidentStr, x + 85, y + 63, new Color(64, 64, 64, 255).getRGB());
        }
    }

    public String convertTickToTime(int tick) {
        String result;
        if (tick > 0) {
            double time = (double) tick / 20;
            int m = (int) (time / 60);
            int s = (int) (time % 60);
            result = m + ":" + s;
        } else result = "";
        return result;
    }

    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected boolean isHovering(int xPosition, int yPosition, int width, int height, double pointX, double pointY) {
        return super.isHovering(xPosition, yPosition, width, height, pointX, pointY);
    }

    @Override
    protected void init() {
        int x = (width - getXSize()) / 2;
        int y = (height - getYSize()) / 2;
        this.addRenderableWidget(new ImageButton(x + 156, y + 5, 15, 15, 210, 0, 0, TRADE_BOX_GUI, (buttonWidget) -> {
            if(container.isTrading()) {
                BlockPos pos = getHitTradeBoxBlockPos();
                if (pos != null)
                    NetWorking.sendRefreshTradebox(pos);
            }
        }));
        super.init();
    }

    private BlockPos getHitTradeBoxBlockPos() {
        Minecraft client = Minecraft.getInstance();
        HitResult hit = client.hitResult;
        if (hit.getType().equals(HitResult.Type.BLOCK)) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos blockPos = blockHit.getBlockPos();
            BlockState blockState = client.level.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (block instanceof TradeboxBlock) {
                return blockPos;
            }
        }
        return null;
    }
}
