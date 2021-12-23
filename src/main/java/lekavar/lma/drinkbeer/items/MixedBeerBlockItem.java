package lekavar.lma.drinkbeer.items;

import lekavar.lma.drinkbeer.blocks.MixedBeerBlock;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.managers.SpiceAndFlavorManager;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.utils.ModCreativeTab;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import lekavar.lma.drinkbeer.utils.mixedbeer.Flavors;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class MixedBeerBlockItem extends BeerBlockItem {
    public MixedBeerBlockItem(Block block) {
        super(block, new Item.Properties().tab(ModCreativeTab.BEER).stacksTo(1)
                .food(new FoodProperties.Builder().alwaysEat().build()));
    }

    public void appendMixedBeerTooltip(ItemStack stack, List<Component> tooltip) {
        //Base title
        tooltip.add(new TranslatableComponent(MixedBeerManager.getBaseBeerToolTipTranslationKey()).append(":").setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE)));
        //Base beer
        int beerId = MixedBeerManager.getBeerId(stack);
        Item beerItem = Beers.byId(beerId).getBeerItem();
        String beerName = beerId > Beers.EMPTY_BEER_ID ? beerItem.toString()
                : MixedBeerManager.getUnmixedToolTipTranslationKey();
        tooltip.add(new TranslatableComponent(beerName).setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE)));
        //Base status effect tooltip
        if (beerId > Beers.EMPTY_BEER_ID) {
            String name = beerItem.asItem().toString();
            if (Beers.byId(beerId).getHasStatusEffectTooltip()) {
                tooltip.add(new TranslatableComponent("item.drinkbeer." + name + ".tooltip").setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE)));
            }
        }
        //Base food level
        if (beerId > Beers.EMPTY_BEER_ID) {
            String hunger = Integer.toString(beerItem.getFoodProperties().getNutrition());
            tooltip.add(new TranslatableComponent("drinkbeer.restores_hunger").setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE)).append(hunger));
        }

        //Flavor title
        tooltip.add(new TranslatableComponent(SpiceAndFlavorManager.getFlavorToolTipTranslationKey()).append(":").setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE)));
        //Flavor
        List<Integer> spiceList = MixedBeerManager.getSpiceList(stack);
        if (!spiceList.isEmpty()) {
            for (int spiceId : spiceList) {
                Flavors flavor = Spices.byId(spiceId).getFlavor();
                tooltip.add(new TranslatableComponent(SpiceAndFlavorManager.getFlavorTranslationKey(flavor.getId()))
                        .append("(")
                        .append(new TranslatableComponent(SpiceAndFlavorManager.getFlavorToolTipTranslationKey(flavor.getId())))
                        .append(")")
                        .setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED)));
            }
        } else {
            tooltip.add(new TranslatableComponent(SpiceAndFlavorManager.getNoFlavorToolTipTranslationKey()).setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED)));
        }
        //Flavor combination(if exists)
        Flavors combinedFlavor = SpiceAndFlavorManager.getCombinedFlavor(spiceList);
        if (combinedFlavor != null) {
            tooltip.add(new TranslatableComponent("")
                    .append("\"")
                    .append(new TranslatableComponent(SpiceAndFlavorManager.getFlavorTranslationKey(combinedFlavor.getId())))
                    .append("\"")
                    .setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_RED)));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return getMixedBeerName(stack);
    }


    public Component getMixedBeerName(ItemStack stack) {
        int beerId = MixedBeerManager.getBeerId(stack);
        Item beerItem = Beers.byId(beerId).getBeerItem();
        String beerName = beerId > Beers.EMPTY_BEER_ID ? "item.drinkbeer." + beerItem.asItem().toString(): "block.drinkbeer.empty_beer_mug";
        Component name = new TranslatableComponent(beerName).append(new TranslatableComponent(MixedBeerManager.getMixedBeerTranslationKey())).setStyle(Style.EMPTY.applyFormat(ChatFormatting.YELLOW));
        return name;
    }


    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        if(world != null && world.isClientSide()) {
            appendMixedBeerTooltip(stack, tooltip);
        }
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        ItemStack stack = context.getItemInHand();
        ((MixedBeerBlock) state.getBlock()).setBeerId(MixedBeerManager.getBeerId(stack));
        ((MixedBeerBlock) state.getBlock()).setSpiceList(MixedBeerManager.getSpiceList(stack));
        return super.placeBlock(context, state);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        if (context.getClickLocation().distanceTo(context.getPlayer().position()) > MAX_PLACE_DISTANCE)
            return false;
        else {
            return super.canPlace(context, state);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        //Apply mixed beer
        if(!world.isClientSide()) {
            MixedBeerManager.useMixedBeer(stack, world, user);
        }
        //Give empty mug back
        giveEmptyMugBack(user);

        return super.finishUsingItem(stack, world, user);
    }
}
