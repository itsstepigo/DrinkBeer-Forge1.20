package lekavar.lma.drinkbeer.blockentities;

import lekavar.lma.drinkbeer.gui.BartendingTableContainer;
import lekavar.lma.drinkbeer.items.BeerMugItem;
import lekavar.lma.drinkbeer.items.MixedBeerBlockItem;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BartendingTableBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int beerId = Beers.EMPTY_BEER_ID;
    private int isMixedBeer = 0;
    private List<Integer> spiceList = new ArrayList<>();
    private boolean[] usedSlots = new boolean[MixedBeerManager.MAX_SPICES_COUNT];

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

    public List<Integer> getBeerInputSpiceList(){
        List<Integer> inputSpiceList = new ArrayList<>();
         ItemStack stack =  items.get(0);
        if ( stack.getItem() instanceof MixedBeerBlockItem) {
            return MixedBeerManager.getSpiceList(stack);
        } else {
            return inputSpiceList;
        }
    }
    public int getInputSpicesNum(){
        for(int i = 1; i < MixedBeerManager.MAX_SPICES_COUNT; i++)
        {
            if(items.get(i).isEmpty())
            {
                return i - 2;
            }
        }
        return MixedBeerManager.MAX_SPICES_COUNT;
    }

    private void resetUsedSlots(){
        for(int i = 0; i < usedSlots.length; i++)
        {
            usedSlots[i] = false;
        }
    }

    public void tickServer() {
        ItemStack input = items.get(0);
        if (input == ItemStack.EMPTY || input.getItem().equals(Items.AIR)) {
            items.set(0,ItemStack.EMPTY);
            setChanged();
            return;
        }

        if(! (input.getItem() instanceof BeerMugItem))
        {
            return;
        }
        //Mark all spices in spiceSlot are not used, so they are currently not marked to be consumed
        resetUsedSlots();

        int beerId = MixedBeerManager.getBeerId(input);
        ItemStack resultStack;
        Item beer = input.getItem();
        //If is basic liquor and there's no spices, the result is basic liquor itself
        if (! ( beer instanceof MixedBeerBlockItem) && getInputSpicesNum() == 0) {
            resultStack = new ItemStack(Beers.byId(beerId).getBeerItem(), 1);
            items.set(MixedBeerManager.MAX_SPICES_COUNT + 1, resultStack);
            setChanged();
        } else { //Otherwise the result must be mixed beer
            List<Integer> oriSpiceList = new ArrayList<>();
            oriSpiceList = (beer instanceof MixedBeerBlockItem) ? getBeerInputSpiceList() : oriSpiceList;
            for (int i = 0; i < MixedBeerManager.MAX_SPICES_COUNT; i++) {
                ItemStack stack = items.get(i + 1);
                //If maximum amount of spice is already in beer, there's no need to check spiceSlot
                if (oriSpiceList.size() < MixedBeerManager.MAX_SPICES_COUNT) {
                    //Check if spiceSlot is not empty
                    if (stack != ItemStack.EMPTY) {
                        //If there are enough spices, insert new spice in the middle or at the start
                        if (i < oriSpiceList.size()) {
                            oriSpiceList.add(i, Spices.byItem(stack.getItem()).getId());
                        }
                        else {//Otherwise insert new spice at the end
                            oriSpiceList.add(Spices.byItem(stack.getItem()).getId());
                        }
                        //Mark spice in spiceSlot#i will be consumed to mix beer
                        usedSlots[i] = true;
                    }
                }
            }
            //Generate result item stack by beerId and spiceList
            resultStack = MixedBeerManager.genMixedBeerItemStack(beerId, oriSpiceList);
            items.set(MixedBeerManager.MAX_SPICES_COUNT + 1, resultStack);
            setChanged();
        }

    }


    public void clearInputs()
    {
        items.get(0).shrink(1);
        for(int i = 0; i < MixedBeerManager.MAX_SPICES_COUNT; i++)
        {
            if(usedSlots[i]) {
                items.get(i + 1).shrink(1);
            }
        }
        setChanged();
    }


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

    @Override
    public CompoundTag getUpdateTag() {

        CompoundTag tag = super.getUpdateTag();
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putShort("beerId", (short) this.beerId);
        tag.putShort("isMixedBeer", (short) this.isMixedBeer);
        tag.putIntArray("spiceList", spiceList);
        return tag;
    }
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        ContainerHelper.loadAllItems(tag, this.items);
        this.beerId = tag.getShort("beerId");
        this.isMixedBeer = tag.getShort("isMixedBeer");
        for (int s: tag.getIntArray("spiceList")) {
            this.spiceList.add(s);
        }
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
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putShort("beerId", (short) this.beerId);
        tag.putShort("isMixedBeer", (short) this.isMixedBeer);
        tag.putIntArray("spiceList", spiceList);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);

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
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int p_18941_) {
        return  p_18941_ >= 0 && p_18941_ < this.items.size() ? this.items.get(p_18941_) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int p_18942_, int p_18943_) {
        return ContainerHelper.removeItem(this.items, p_18942_, p_18943_);
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_18951_) {
        return ContainerHelper.takeItem(this.items, p_18951_);
    }

    @Override
    public void setItem(int p_18944_, ItemStack p_18945_) {
        if (p_18944_ >= 0 && p_18944_ < this.items.size()) {
            this.items.set(p_18944_, p_18945_);
        }
    }

    @Override
    public boolean stillValid(Player p_18946_) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(p_18946_.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) > 64.0D);
        }
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }
}
