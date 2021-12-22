package lekavar.lma.drinkbeer.managers;

import lekavar.lma.drinkbeer.effects.DrunkStatusEffect;
import lekavar.lma.drinkbeer.effects.NightHowlStatusEffect;
import lekavar.lma.drinkbeer.entities.damages.AlcoholDamage;
import lekavar.lma.drinkbeer.items.MixedBeerBlockItem;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import lekavar.lma.drinkbeer.utils.mixedbeer.Flavors;
import lekavar.lma.drinkbeer.utils.mixedbeer.MixedBeerOnUsing;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MixedBeerManager {
    public static final int MAX_SPICES_COUNT = 3;

    public static ItemStack genMixedBeerItemStack(int beerId, int... spiceIds) {
        List<Integer> spiceList = new ArrayList<>();
        for (int spiceId : spiceIds) {
            spiceList.add(spiceId);
        }
        return genMixedBeerItemStack(beerId, spiceList);
    }

    public static ItemStack genMixedBeerItemStack(int beerId, List<Integer> spiceList) {
        ItemStack resultStack = new ItemStack(ItemRegistry.MIXED_BEER.get(), 1);

        spiceList = removeIllegalSpiceId(spiceList);

        /* TODO load data
        NbtCompound compoundTag = resultStack.getOrCreateSubNbt("BlockEntityTag");
        NbtCompound tag = new NbtCompound();
        compoundTag.put("MixedBeer", tag);
        tag.putInt("BeerId", beerId);

        NbtList listTag = genSpiceListTag(spiceList);
        if (listTag != null) {
            tag.put("Spices", listTag);
        }

        resultStack.writeNbt(tag);

         */
        return resultStack;
    }

    // TODO
    public static NbtList genSpiceListTag(List<Integer> spiceList) {
        NbtList listTag = null;
        if (!spiceList.isEmpty()) {
            listTag = new NbtList();
            for (int i = 0; i < MixedBeerManager.MAX_SPICES_COUNT && i < spiceList.size(); i++) {
                NbtCompound tag2 = new NbtCompound();
                tag2.putInt("SpiceId", spiceList.get(i));
                listTag.add(tag2);
            }
        }
        return listTag;
    }

    private static List<Integer> removeIllegalSpiceId(List<Integer> spiceList) {
        return spiceList.stream()
                .filter(value -> (value.compareTo(Spices.EMPTY_SPICE_ID) > 0) && (value.compareTo(Spices.size()) <= 0))
                .collect(Collectors.toList());
    }

    // TODO
    public static int getBeerId(ItemStack itemStack) {
        int beerId = Beers.EMPTY_BEER_ID;
        if (itemStack.getItem() instanceof MixedBeerBlockItem) {
            NbtCompound compoundTag = itemStack.getSubNbt("BlockEntityTag");
            if (compoundTag != null && compoundTag.contains("MixedBeer")) {
                NbtCompound tag = compoundTag.getCompound("MixedBeer");
                beerId = tag.getInt("BeerId");
            }
        }

        return beerId;
    }

    // TODO
    public static List<Integer> getSpiceList(ItemStack itemStack) {
        List<Integer> spiceList = new ArrayList<>();
        NbtCompound compoundTag = itemStack.getSubNbt("BlockEntityTag");
        if (compoundTag != null && compoundTag.contains("MixedBeer")) {
            NbtCompound tag = compoundTag.getCompound("MixedBeer");
            NbtList listTag = tag.getList("Spices", 10);
            for (int i = 0; i < listTag.size() && i < MixedBeerManager.MAX_SPICES_COUNT; ++i) {
                NbtCompound tag2 = listTag.getCompound(i);
                int spiceId = tag2.getInt("SpiceId");
                spiceList.add(spiceId);
            }
        }
        return spiceList;
    }

    public static String getMixedBeerTranslationKey() {
        return ItemRegistry.MIXED_BEER.get().asItem().toString();
    }

    public static String getBaseBeerToolTipTranslationKey() {
        return "item.drinkbeer.mixed_beer.tooltip_base";
    }

    public static String getUnmixedToolTipTranslationKey() {
        return "item.drinkbeer.mixed_beer.tooltip_unmixed";
    }

    public static void useMixedBeer(ItemStack stack, Level world, LivingEntity user) {
        /*Initialize properties!*/
        /*------------------------------------------------------------------------------------------------------------------*/
        MixedBeerOnUsing mixedBeerOnUsing = new MixedBeerOnUsing();
        //Initialize beer
        mixedBeerOnUsing.setBeer(Beers.byId(getBeerId(stack)));
        //Initialize food level
        mixedBeerOnUsing.addHunger(Objects.requireNonNull(mixedBeerOnUsing.getBeerItem().getFoodProperties().getNutrition()));
        //Initialize spices and flavors
        List<Integer> spiceList = getSpiceList(stack);
        mixedBeerOnUsing.setSpiceList(spiceList);
        Flavors combinedFlavor = SpiceAndFlavorManager.getCombinedFlavor(spiceList);
        if (combinedFlavor != null) {
            mixedBeerOnUsing.addFlavor(combinedFlavor);
        }

        /*Deal with properties!*/
        /*------------------------------------------------------------------------------------------------------------------*/
        //Add base beer status effect
        mixedBeerOnUsing.addStatusEffect(getBeerStatusEffectList(mixedBeerOnUsing.getBeerItem(), world));
        //Deal with flavors
        SpiceAndFlavorManager.applyFlavorValue(mixedBeerOnUsing);

        /*Apply properties!*/
        /*------------------------------------------------------------------------------------------------------------------*/
        //Apply Drunk status effect
        DrunkStatusEffect.addStatusEffect(user, mixedBeerOnUsing.getDrunkValue());
        //Apply beer's special actions
        if (mixedBeerOnUsing.getBeer().equals(Beers.BEER_MUG_NIGHT_HOWL_KVASS)) {
            NightHowlStatusEffect.playRandomHowlSound(world, user);
        }
        //Apply food level
        if (user instanceof Player && !((Player) user).isCreative()) {
            ((Player) user).getFoodData().eat(mixedBeerOnUsing.getHunger(), 0f);
        }
        //Apply health
        if (user instanceof Player) {
            if (!((Player) user).isCreative()) {
                if (mixedBeerOnUsing.getHealth() < 0) {
                    user.hurt(new AlcoholDamage(), Math.abs(mixedBeerOnUsing.getHealth()));
                } else {
                    user.heal(mixedBeerOnUsing.getHealth());
                }
            }
        } else {
            user.setHealth(user.getHealth() + mixedBeerOnUsing.getHealth());
        }
        //Apply status effects
        for (org.apache.commons.lang3.tuple.Pair<MobEffect, Integer> statusEffectPair : mixedBeerOnUsing.getStatusEffectList()) {
            user.addEffect(new MobEffectInstance(statusEffectPair.getKey(), statusEffectPair.getValue()));
        }
        //Apply flavor actions
        SpiceAndFlavorManager.applyFlavorAction(mixedBeerOnUsing, world, user);
    }

    private static List<Pair<MobEffect, Integer>> getBeerStatusEffectList(Item beerItem, Level world) {
        List<org.apache.commons.lang3.tuple.Pair<MobEffect, Integer>> resultStatusEffectList = new ArrayList<>();
        List<com.mojang.datafixers.util.Pair<MobEffectInstance, Float>> statusEffectList = Objects.requireNonNull(beerItem.getFoodProperties().getEffects());
        if (statusEffectList != null) {
            if (!statusEffectList.isEmpty()) {
                for (com.mojang.datafixers.util.Pair<MobEffectInstance, Float> statusEffect : statusEffectList)
                    resultStatusEffectList.add(Pair.of(statusEffect.getFirst().getEffect(), statusEffect.getFirst().getDuration()));
            }
        }
        if (beerItem.equals(Beers.BEER_MUG_NIGHT_HOWL_KVASS.getBeerItem())) {
            Pair<MobEffect, Integer> nightHowlStatusEffectPair = NightHowlStatusEffect.getStatusEffectPair(world);
            resultStatusEffectList.add(nightHowlStatusEffectPair);
        }
        return resultStatusEffectList;
    }

    /**
     * Get the number of the target action occurrences before current action.
     *
     * @param index        Current action's index in actionList
     * @param targetAction Which action to find
     * @param actionList   Current mixed beer's actionList
     * @return Number of the target action occurrences before current action
     */
    public static int getActionedTimes(int index, Flavors targetAction, List<Flavors> actionList) {
        if (index == 0)
            return 0;
        int actionTime = 0;
        for (int i = 0; i < index; i++) {
            if (actionList.get(i).equals(targetAction)) {
                actionTime++;
            }
        }
        return actionTime;
    }

    /**
     * Whether the target action exists before the current action.
     *
     * @param index        Current action's index in actionList
     * @param targetAction Which action to find
     * @param actionList   Current mixed beer's actionList
     * @return
     */
    public static boolean hasActionedBefore(int index, Flavors targetAction, List<Flavors> actionList) {
        return getActionedTimes(index, targetAction, actionList) != 0;
    }

    /**
     * Whether the target action exists after current action.
     *
     * @param index        Current action's index in actionList
     * @param targetAction Which action to find
     * @param actionList   Current mixed beer's actionList
     * @return
     */
    public static boolean hasActionAfter(int index, Flavors targetAction, List<Flavors> actionList) {
        if (actionList.size() - 1 == index) {
            return false;
        } else {
            for (int i = index + 1; i < actionList.size(); i++) {
                if (actionList.get(i).equals(targetAction)) {
                    return true;
                }
            }
        }
        return false;
    }
}
