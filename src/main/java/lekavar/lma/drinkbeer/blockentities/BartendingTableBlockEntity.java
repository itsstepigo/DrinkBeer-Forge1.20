package lekavar.lma.drinkbeer.blockentities;

import lekavar.lma.drinkbeer.gui.BartendingTableContainer;
import lekavar.lma.drinkbeer.gui.BeerBarrelContainer;
import lekavar.lma.drinkbeer.items.BeerMugItem;
import lekavar.lma.drinkbeer.items.MixedBeerBlockItem;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BartendingTableBlockEntity extends BaseContainerBlockEntity {
    private int beerId = Beers.EMPTY_BEER_ID;
    private int isMixedBeer = 0;
    private List<Integer> spiceList = new ArrayList<>();


    public final ContainerData syncData = new ContainerData() {
        @Override
        public int get(int p_221476_1_) {
            switch (p_221476_1_) {
                case 0:
                    return beerId;
                case 1:
                    return isMixedBeer;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int p_221477_1_, int p_221477_2_) {
            switch (p_221477_1_) {
                case 0:
                    beerId = p_221477_2_;
                    break;
                case 1:
                    isMixedBeer = p_221477_2_;
                    break;
            }
        }
        @Override
        public int getCount() {
            return 1;
        }
    };

    public BartendingTableBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.BARTENDING_TABLE_TILEENTITY.get(),pos,state);
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("block.drinkbeer.bartending_table");
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("block.drinkbeer.bartending_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new BartendingTableContainer(id, this, syncData, inventory, this);
    }

    @Override
    protected AbstractContainerMenu createMenu(int p_58627_, Inventory p_58628_) {
        return null;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    public boolean setBeer(ItemStack beerItemStack) {
        try {
            Item beerItem = beerItemStack.getItem();
            if (beerItem instanceof BeerMugItem) {
                isMixedBeer = 0;
                //No spiceList for a basic liquor
                //Get beerId
                this.beerId = (Beers.byItem(beerItem).getId());
            } else if (beerItem instanceof MixedBeerBlockItem) {
                isMixedBeer = 1;
                //Read beerId
                this.beerId = (MixedBeerManager.getBeerId(beerItemStack));
                //Read spiceList
                this.spiceList = MixedBeerManager.getSpiceList(beerItemStack);
            }
            return true;
        } catch (Exception e) {
            System.out.println("Something wrong when reading beer properties in BartendingTableEntity");
            return false;
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putShort("beerId", (short) this.beerId);
        tag.putShort("isMixedBeer", (short) this.isMixedBeer);
        tag.putIntArray("spiceList", spiceList);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);

        this.spiceList.clear();
        for (int Spice: tag.getIntArray("spiceList")
             ) {
            this.spiceList.add(Spice);

        }
        this.beerId = tag.getShort("beerId");
        this.isMixedBeer = tag.getShort("isMixedBeer");
    }

    @Override
    public int getContainerSize() {
        return MixedBeerManager.MAX_SPICES_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return spiceList.isEmpty();
    }

    @Override
    public ItemStack getItem(int p_18941_) {
        return null;
    }

    @Override
    public ItemStack removeItem(int p_18942_, int p_18943_) {
        return null;
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_18951_) {
        return null;
    }

    @Override
    public void setItem(int p_18944_, ItemStack p_18945_) {

    }

    @Override
    public boolean stillValid(Player p_18946_) {
        return true;
    }

    @Override
    public void clearContent() {
            spiceList.clear();
    }
}
