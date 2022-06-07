package lekavar.lma.drinkbeer.gui;

import lekavar.lma.drinkbeer.blockentities.BartendingTableBlockEntity;
import lekavar.lma.drinkbeer.blockentities.BeerBarrelBlockEntity;
import lekavar.lma.drinkbeer.items.MixedBeerBlockItem;
import lekavar.lma.drinkbeer.items.SpiceBlockItem;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.registries.ContainerTypeRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import lekavar.lma.drinkbeer.utils.ModCreativeTab;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import lekavar.lma.drinkbeer.utils.mixedbeer.Spices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.List;

public class BartendingTableContainer  extends AbstractContainerMenu {
    private final Container bartendingSpace;
    private final ContainerData syncData;

    private static final int INPUT_SPICE_SLOTS = MixedBeerManager.MAX_SPICES_COUNT;


    public BartendingTableContainer(int id, Container bartendingSpace, ContainerData syncData, Inventory playerInventory, BartendingTableBlockEntity bartendingTableBlockEntity) {
        super(ContainerTypeRegistry.bartendingTableContainer.get(), id);
        this.bartendingSpace = bartendingSpace;
        this.syncData = syncData;

        // Layout Slot
        // Player Inventory
        layoutPlayerInventorySlots(8, 84, new InvWrapper(playerInventory));
        // Input Beer
        addSlot(new BeerInputSlot(bartendingSpace,0, 27, 34)).set(bartendingTableBlockEntity.getItem(0));
        // Input Spices
        addSlot(new SpiceInputSlot(bartendingSpace, 1, 67, 16, bartendingTableBlockEntity));
        addSlot(new SpiceInputSlot(bartendingSpace, 2, 67, 34, bartendingTableBlockEntity));
        addSlot(new SpiceInputSlot(bartendingSpace, 3, 67, 52, bartendingTableBlockEntity));

        // Output
        addSlot(new OutputSlot(bartendingSpace, 4, 128, 34, syncData, bartendingTableBlockEntity));

        //Tracking Data
        addDataSlots(syncData);
    }

    public BartendingTableContainer(int id, Inventory playerInventory, FriendlyByteBuf data) {
        this(id, playerInventory, data.readBlockPos());
    }

    public BartendingTableContainer(int id, Inventory playerInventory, BlockPos pos) {
        this(id, ((BartendingTableBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos)), ((BartendingTableBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos)).syncData, playerInventory, ((BartendingTableBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos)));
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow, IItemHandler playerInventory) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }


    @Override
    public ItemStack quickMoveStack(Player p_82846_1_, int p_82846_2_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_82846_2_);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Try quick-pickup output
            if (p_82846_2_ == 41) {
                if (!this.moveItemStackTo(itemstack1, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }

            // Try quick-move item in player inv.
            else if (p_82846_2_ < 36) {
                // Try to fill ingredient slot.
                if (!this.moveItemStackTo(itemstack1, 36, 40, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Try quick-move item to player inv.
            else if (!this.moveItemStackTo(itemstack1, 0, 36, false)) {
                return ItemStack.EMPTY;
            }

            // Detect whether the quick-move is successful or not
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            // Detect whether the quick-move is successful or not
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(p_82846_1_, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player p_75145_1_) {
        return this.bartendingSpace.stillValid(p_75145_1_);
    }

    @Override
    public void removed(Player player) {
        if (!player.level.isClientSide()) {
            // Return Item to Player;
            for (int i = 0; i <= MixedBeerManager.MAX_SPICES_COUNT; i++) {
                if (!bartendingSpace.getItem(i).isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, bartendingSpace.removeItem(i, bartendingSpace.getItem(i).getCount()));
                }
            }
        } else {
            // Play Closing Bartending Sound
            player.level.playSound(player, player.blockPosition(), SoundEventRegistry.BARTENDING_TABLE_CLOSE.get(), SoundSource.BLOCKS, 1f, 1f);
        }
        super.removed(player);
    }



    static class BeerInputSlot extends Slot {

        private BeerInputSlot(Container p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
            super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        }

        // Only BeerCups are allowed
        @Override
        public boolean mayPlace(ItemStack p_75214_1_) {
            return p_75214_1_.getItem().getCreativeTabs().contains(ModCreativeTab.BEER);
        }
    }

    static class SpiceInputSlot extends Slot {
        private BartendingTableBlockEntity bartendingTableBlockEntity;
        public SpiceInputSlot(Container p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_, BartendingTableBlockEntity bartendingTableBlockEntity) {
            super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
            this.bartendingTableBlockEntity = bartendingTableBlockEntity;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            //
            if (bartendingTableBlockEntity.getBeerInputSpiceList().size() + bartendingTableBlockEntity.getInputSpicesNum() >= INPUT_SPICE_SLOTS) {
                return false;
            }
            //Check if is spice
            return stack.getItem() instanceof SpiceBlockItem;
        }
    }


    static class OutputSlot extends Slot {
        private final ContainerData syncData;
        private final BartendingTableBlockEntity bartendingTableBlockEntity;

        public OutputSlot(Container p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_, ContainerData syncData, BartendingTableBlockEntity bartendingTableBlockEntity) {
            super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
            this.syncData = syncData;
            this.bartendingTableBlockEntity = bartendingTableBlockEntity;
        }

        @Override
        public void onTake(Player player, ItemStack p_190901_2_) {
            this.bartendingTableBlockEntity.clearInputs();
        }
        // Placing item on output slot is prohibited.
        @Override
        public boolean mayPlace(ItemStack p_75214_1_) {
            return false;
        }

    }
}
