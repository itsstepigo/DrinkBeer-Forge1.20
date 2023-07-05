package lekavar.lma.drinkbeer.blockentities;

import lekavar.lma.drinkbeer.gui.BartendingTableContainer;
import lekavar.lma.drinkbeer.items.BeerBlockItem;
import lekavar.lma.drinkbeer.items.BeerMugItem;
import lekavar.lma.drinkbeer.items.MixedBeerBlockItem;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import lekavar.lma.drinkbeer.utils.ItemStackHelper;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BartendingTableBlockEntity extends BaseContainerBlockEntity {
    public static final int OUTPUT_SLOT_INDEX = MixedBeerManager.MAX_SPICES_COUNT + 1;

    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int beerId = Beers.EMPTY_BEER_ID;
    private int isMixedBeer = 0;
    private List<Integer> spiceList = new ArrayList<>();
    private boolean[] usedSlots = new boolean[MixedBeerManager.MAX_SPICES_COUNT];

    public final ContainerData syncData = new ContainerData() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return beerId;
                case 1:
                    return isMixedBeer;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    beerId = value;
                    break;
                case 1:
                    isMixedBeer = value;
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
        ItemStack stack = getBeerInput();
        if (stack.getItem() instanceof MixedBeerBlockItem) {
            return MixedBeerManager.getSpiceList(stack);
        } else {
            return inputSpiceList;
        }
    }
    public int getInputSpicesNum(){
        for(int i = 0; i < MixedBeerManager.MAX_SPICES_COUNT; i++)
        {
            if(items.get(1 + i).isEmpty())
            {
                return i;
            }
        }
        return MixedBeerManager.MAX_SPICES_COUNT;
    }

    private void resetUsedSlots(){
        Arrays.fill(usedSlots, false);
    }

    public void tickServer() {
        ItemStack input = items.get(0);
        if (ItemStackHelper.isAirOrEmpty(input)) {
            items.set(OUTPUT_SLOT_INDEX, ItemStack.EMPTY);
            setChanged();
            return;
        }

        ItemStack resultStack;
        if(! (input.getItem() instanceof BeerBlockItem))
        {
            resultStack = ItemStack.EMPTY;
        }
        else {
            //Mark all spices in spiceSlot are not used, so they are currently not marked to be consumed
            resetUsedSlots();

            int beerId = getBeerInputBeerId();
            Item beer = input.getItem();
            boolean isInputMixedBeer = beer instanceof MixedBeerBlockItem;
            //If is basic liquor and there's no spices, the result is basic liquor itself
            if (!isInputMixedBeer && getInputSpicesNum() == 0) {
                resultStack = new ItemStack(Beers.byId(beerId).getBeerItem(), 1);
            } else { //Otherwise the result must be mixed beer
                List<Integer> oriSpiceList = new ArrayList<>();
                oriSpiceList = isInputMixedBeer ? getBeerInputSpiceList() : oriSpiceList;
                for (int i = 0; i < MixedBeerManager.MAX_SPICES_COUNT; i++) {
                    ItemStack stack = items.get(i + 1);
                    //If maximum amount of spice is already in beer, there's no need to check spiceSlot
                    if (oriSpiceList.size() < MixedBeerManager.MAX_SPICES_COUNT) {
                        //Check if spiceSlot is not empty
                        if (!ItemStackHelper.isAirOrEmpty(stack)) {
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
            }
        }
        items.set(OUTPUT_SLOT_INDEX, resultStack);
        setChanged();
    }

    private ItemStack getBeerInput()
    {
        return items.get(0);
    }

    private boolean isMixedBeer()
    {
        return getBeerInput().getItem() instanceof MixedBeerBlockItem;
    }

    private int getBeerInputBeerId() {
        if (isMixedBeer()) {
            return MixedBeerManager.getBeerId(getBeerInput());
        } else
            return Beers.byItem(getBeerInput().getItem()).getId();
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
        return Component.translatable("block.drinkbeer.bartending_table_normal");
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.drinkbeer.bartending_table_normal");
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

            ItemStack oriBeerItemStack = isMixedBeer == 1 ? MixedBeerManager.genMixedBeerItemStack(beerId, spiceList) : new ItemStack(Beers.byId(beerId).getBeerItem(), 1);
            setItem(0, oriBeerItemStack);

            return true;
        } catch (Exception e) {
            System.out.println("Something wrong when reading beer properties in BartendingTableEntity");
            return false;
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

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
