package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CreativeModeTabsRegistry {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DrinkBeer.MOD_ID);
    public static final List<Supplier<? extends ItemLike>> BEER_TAB_ITEMS = new ArrayList<>();
    public static final List<Supplier<? extends ItemLike>> GENERAL_TAB_ITEMS = new ArrayList<>();
    public static final List<Supplier<? extends ItemLike>> SPICES_TAB_ITEMS = new ArrayList<>();
    public static final RegistryObject<CreativeModeTab> BEER = TABS.register("beer",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.drinkbeer.beer"))
                    //just wow
                    .icon(ItemRegistry.BEER_MUG.get()::getDefaultInstance)
                    .noScrollBar()
                    .withTabsBefore(CreativeModeTabsRegistry.GENERAL.getId())
                    .displayItems((dispalyParams, output) -> {
                        BEER_TAB_ITEMS.forEach(itemLike -> output.accept(itemLike.get()));
                    })
                    .build()
            );
    public static final RegistryObject<CreativeModeTab> GENERAL = TABS.register("general",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.drinkbeer.general"))
                    .icon(ItemRegistry.BEER_BARREL.get()::getDefaultInstance)
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .displayItems((displayParams, output) ->{
                        GENERAL_TAB_ITEMS.forEach(itemLike -> output.accept(itemLike.get()));
                    })
                    .build()
            );

    public static final RegistryObject<CreativeModeTab> SPICES = TABS.register("spices",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.drinkbeer.spices"))
                    .icon(ItemRegistry.SPICE_AMETHYST_NIGELLA_SEEDS.get()::getDefaultInstance)
                    .withTabsBefore(CreativeModeTabsRegistry.BEER.getId())
                    .displayItems((displayParams, output) ->{
                        SPICES_TAB_ITEMS.forEach(itemLike -> output.accept(itemLike.get()));
                    })
                    .build()
    );
    public static <T extends Item> RegistryObject<T> addToTabBEER(RegistryObject<T> itemLike){
        BEER_TAB_ITEMS.add(itemLike);
        return itemLike;
    }
    public static <T extends Item> RegistryObject<T> addToTabGENERAL(RegistryObject<T> itemLike){
        GENERAL_TAB_ITEMS.add(itemLike);
        return itemLike;
    }
    public static <T extends Item> RegistryObject<T> addToTabSPICES(RegistryObject<T> itemLike) {
        SPICES_TAB_ITEMS.add(itemLike);
        return itemLike;
    }
}
