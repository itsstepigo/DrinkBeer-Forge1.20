package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.blockentities.BartendingTableBlockEntity;
import lekavar.lma.drinkbeer.blockentities.BeerBarrelBlockEntity;
import lekavar.lma.drinkbeer.blockentities.MixedBeerBlockEntity;
import lekavar.lma.drinkbeer.blockentities.TradeBoxBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOKC_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, DrinkBeer.MOD_ID);
    public static final RegistryObject<BlockEntityType<BeerBarrelBlockEntity>> BEER_BARREL_TILEENTITY = BLOKC_ENTITIES.register("beer_barrel_blockentity", () -> BlockEntityType.Builder.of(BeerBarrelBlockEntity::new, BlockRegistry.BEER_BARREL.get()).build(null));
    public static final RegistryObject<BlockEntityType<BartendingTableBlockEntity>> BARTENDING_TABLE_TILEENTITY = BLOKC_ENTITIES.register("bartending_table_normal_blockentity", () -> BlockEntityType.Builder.of(BartendingTableBlockEntity::new, BlockRegistry.BARTENDING_TABLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TradeBoxBlockEntity>> TRADE_BOX_TILEENTITY = BLOKC_ENTITIES.register("trade_box_normal_blockentity", () -> BlockEntityType.Builder.of(TradeBoxBlockEntity::new, BlockRegistry.TRADE_BOX.get()).build(null));
    public static final RegistryObject<BlockEntityType<MixedBeerBlockEntity>> MIXED_BEER_TILEENTITY = BLOKC_ENTITIES.register("mixed_beer_blockentity", () -> BlockEntityType.Builder.of(MixedBeerBlockEntity::new, BlockRegistry.MIXED_BEER.get()).build(null));
}
