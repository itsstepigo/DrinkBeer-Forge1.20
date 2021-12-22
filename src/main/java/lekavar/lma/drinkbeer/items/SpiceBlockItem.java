package lekavar.lma.drinkbeer.items;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.managers.SpiceAndFlavorManager;
import lekavar.lma.drinkbeer.utils.ModCreativeTab;
import lekavar.lma.drinkbeer.utils.mixedbeer.Flavors;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

public class SpiceBlockItem extends BlockItem {
    public SpiceBlockItem(Block block, @Nullable MobEffectInstance statusEffectInstance, int hunger) {
        super(block, new Item.Properties().tab(ModCreativeTab.GENERAL).stacksTo(64)
                .food(statusEffectInstance != null
                        ? new FoodProperties.Builder().nutrition(hunger).effect(statusEffectInstance, 1).alwaysEat().build()
                        : new FoodProperties.Builder().nutrition(hunger).alwaysEat().build())
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        //Spice title
        tooltip.add(new TranslatableComponent(SpiceAndFlavorManager.getSpiceToolTipTranslationKey()).setStyle(Style.EMPTY.applyFormat(ChatFormatting.YELLOW)));
        //Flavor title
        tooltip.add(new TranslatableComponent(SpiceAndFlavorManager.getFlavorToolTipTranslationKey()).append(":").setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE)));
        //Flavor and tooltip
        Flavors flavor = Spices.byItem(this.asItem()).getFlavor();
        tooltip.add(new TranslatableComponent(SpiceAndFlavorManager.getFlavorTranslationKey(flavor.getId()))
                .append("(")
                .append(new TranslatableComponent(SpiceAndFlavorManager.getFlavorToolTipTranslationKey(flavor.getId())))
                .append(")")
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED)));
    }

}
