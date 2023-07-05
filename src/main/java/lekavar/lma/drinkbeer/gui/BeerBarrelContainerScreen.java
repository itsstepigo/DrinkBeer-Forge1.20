package lekavar.lma.drinkbeer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;

public class BeerBarrelContainerScreen extends AbstractContainerScreen<BeerBarrelContainer> {

    private final ResourceLocation BEER_BARREL_CONTAINER_RESOURCE = new ResourceLocation(DrinkBeer.MOD_ID, "textures/gui/container/beer_barrel.png");
    private final int textureWidth = 176;
    private final int textureHeight = 166;
    private Inventory inventory;

    public BeerBarrelContainerScreen(BeerBarrelContainer screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
        this.imageWidth = textureWidth;
        this.imageHeight = textureHeight;

        this.inventory = inv;
    }

    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics stack, float partialTicks, int mouseX, int mouseY) {
        renderBackground(stack);
        RenderSystem.setShaderTexture(0, BEER_BARREL_CONTAINER_RESOURCE);
        int i = (this.width - this.getXSize()) / 2;
        int j = (this.height - this.getYSize()) / 2;
        blit(stack, i, j, 0, 0, imageWidth, imageHeight);
    }

    protected void renderLabels(GuiGraphics stack, int x, int y) {
        stack.drawCenteredString(this.font, this.title, (int) this.textureWidth / 2, (int) this.titleLabelY, 4210752);
        stack.drawString(this.font, this.inventory.getDisplayName(), this.inventoryLabelX, this.inventoryLabelY, 4210752);
        String str = menu.getIsBrewing() ? convertTickToTime(menu.getRemainingBrewingTime()) : convertTickToTime(menu.getStandardBrewingTime());
        stack.drawString(this.font, str, 128, 54, new Color(64, 64, 64, 255).getRGB());
    }

    public String convertTickToTime(int tick) {
        String result;
        if (tick > 0) {
            double time = tick / 20;
            int m = (int) (time / 60);
            int s = (int) (time % 60);
            result = m + ":" + s;
        } else result = "";
        return result;
    }
}
