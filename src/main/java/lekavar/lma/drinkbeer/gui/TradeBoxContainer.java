package lekavar.lma.drinkbeer.gui;

import lekavar.lma.drinkbeer.blockentities.TradeBoxBlockEntity;
import lekavar.lma.drinkbeer.managers.TradeBoxManager;
import lekavar.lma.drinkbeer.registries.BlockRegistry;
import lekavar.lma.drinkbeer.registries.ContainerTypeRegistry;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import lekavar.lma.drinkbeer.utils.tradebox.Good;
import lekavar.lma.drinkbeer.utils.tradebox.Locations;
import lekavar.lma.drinkbeer.utils.tradebox.Residents;
import lekavar.lma.drinkbeer.utils.tradebox.TradeMission;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class TradeBoxContainer extends AbstractContainerMenu {
    private final Container tradeboxInventory;
    private final List<Slot> tradeboxSlots;

    /**
     * goodSlots:
     * 0-3 goods to loacation
     * 4-7 goods from location
     */
    private final List<Slot> goodSlots;
    private final ContainerData syncData;

    private Runnable inventoryChangeListener;
    public Player player;

    private BlockPos pos;

    public TradeBoxContainer(int id, Inventory playerInventory, FriendlyByteBuf data) {
        this(id, playerInventory, data.readBlockPos());
    }

    public TradeBoxContainer(int id, Inventory playerInventory, BlockPos pos) {
        this(id, ((TradeBoxBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos)), ((TradeBoxBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos)).syncData, playerInventory, ((TradeBoxBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos)));
    }

    public TradeBoxContainer(int id, Container goodInventory, ContainerData syncData, Inventory playerInventory, TradeBoxBlockEntity tradeBoxBlockEntity) {
        super(ContainerTypeRegistry.tradeBoxContainer.get(), id);
        this.syncData = syncData;
        this.pos = tradeBoxBlockEntity.getBlockPos();
        //Tracking Data
        addDataSlots(syncData);

        this.inventoryChangeListener = () -> {
        };

        this.player = playerInventory.player;

        tradeboxSlots = new ArrayList<>();
        goodSlots = new ArrayList<>();

        this.tradeboxInventory = new SimpleContainer(4) {
            public void setChanged() {
                super.setChanged();
                TradeBoxContainer.this.slotsChanged(this);
                TradeBoxContainer.this.inventoryChangeListener.run();
            }
        };
        // check size
        if (goodInventory.getContainerSize() < 8) {
            throw new IllegalArgumentException("Container size " + goodInventory.getContainerSize() + " is smaller than expected " + 8);
        }

        AtomicInteger num = new AtomicInteger();
        //Init tradeboxSlots
        IntStream.range(0, 2).forEach(i ->
                IntStream.range(0, 2).forEach(j -> {
                    this.tradeboxSlots.add(this.addSlot(new InputSlot(tradeboxInventory, num.get(), 25 + i * 18, 26 + j * 18)));
                    num.getAndIncrement();
                }));
        //Init goodSlots
        num.set(0);
        IntStream.range(0, 2).forEach(j ->
                IntStream.range(0, 4).forEach(i -> {
                    this.goodSlots.add(this.addSlot(new OutputSlot(goodInventory, num.get(), 85 + i * 18, 26 + j * 18, syncData, this)));
                    num.getAndIncrement();
                }));

        // Player Inventory
        layoutPlayerInventorySlots(8, 84, new InvWrapper(playerInventory));

        //Generate trade mission if tradebox is in trading process but has illegal trade mission
        if (isTrading() && !hasLegalTradeMission()) {
            setTradeboxTrading();
        }

        //Tracking Data
        addDataSlots(syncData);
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
    public boolean stillValid(Player player) {
        return true;
    }

    public int getCoolingTime() {
        return syncData.get(0);
    }

    public int getProcess() {
        return syncData.get(3);
    }

    // @Dist(EnvType.CLIENT)
    public void setInventoryChangeListener(Runnable inventoryChangeListener) {
        this.inventoryChangeListener = inventoryChangeListener;
    }

    @Override
    public void slotsChanged(Container inventory) {
        checkTradeMission();
        this.broadcastChanges();
    }

    public void checkTradeMission() {
        if (!isTrading())
            return;

        //Test whether the inputted goods meet required goods
        Map<Item, Integer> inputGoodMap = goodSlotListToGoodMap(tradeboxSlots);
        Map<Item, Integer> neededGoodMap = goodSlotListToGoodMap(getToLocationGoodSlots());
        if (!TradeBoxManager.test(inputGoodMap, neededGoodMap)) {
            return;
        }

        //Set process cooling first so that does't trigger this method by itself
        setProcess(TradeBoxBlockEntity.PROCESS_COOLING);
        //Consume input goods
        consumeInputGood(neededGoodMap);
        //Drop extra input goods to player
        clearContainer(player, this.tradeboxInventory);

        //Set goods from location into tradeboxSlots
        int goodNum = 0;
        for (Slot slot : getFromLocationGoodSlots()) {
            if (slot.hasItem()) {
                this.tradeboxSlots.get(goodNum).set(slot.getItem());
                goodNum++;
            }
        }

        setTradeboxCooling();
    }

    public void consumeInputGood(Map<Item, Integer> targetGoodMap) {
        for (Map.Entry<Item, Integer> targetGood : targetGoodMap.entrySet()) {
            int currentNum = 0;
            int requiredNum = targetGood.getValue();
            for (Slot slot : tradeboxSlots) {
                if (targetGood.getKey().equals(slot.getItem().getItem())) {
                    ItemStack itemStack = slot.getItem();
                    if (itemStack.getCount() >= targetGood.getValue()) {
                        currentNum = requiredNum;
                        slot.remove(requiredNum);
                    } else {
                        currentNum += itemStack.getCount();
                        slot.remove(itemStack.getCount());
                    }
                }
                if (currentNum == requiredNum) {
                    break;
                }
            }
        }
    }

    public boolean isCooling() {
        return getProcess() == TradeBoxBlockEntity.PROCESS_COOLING;
    }

    public boolean isTrading() {
        return getProcess() == TradeBoxBlockEntity.PROCESS_TRADING;
    }

    public void setProcess(int process) {
        this.syncData.set(3, process);
    }

    public void setCoolingTime(int coolingTime) {
        this.syncData.set(0, coolingTime);
    }

    public void setLocationId(int locationId) {
        this.syncData.set(1, locationId);
    }

    public void setResidentId(int residentId) {
        this.syncData.set(2, residentId);
    }

    public int getLocationId() {
        return syncData.get(1);
    }

    public int getResidentId() {
        return syncData.get(2);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            // Return Item to Player;
            clearContainer(player, this.tradeboxInventory);
        }
        else {
            player.level().playSound((Player) null, player.blockPosition(), SoundEventRegistry.TRADEBOX_CLOSE.get(), SoundSource.BLOCKS, 0.6f, 1f);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < this.tradeboxSlots.size()) {
                if (!this.moveItemStackTo(originalStack, this.tradeboxSlots.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, this.tradeboxSlots.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    public void setTradeMission(TradeMission tradeMission) {
        setLocationId(tradeMission.getLocationId());
        setResidentId(tradeMission.getResidentId());
        if (!tradeMission.getGoodToLocationList().isEmpty()) {
            IntStream.range(0, tradeMission.getGoodToLocationList().size()).forEach(i -> {
                Good good = tradeMission.getGoodToLocationList().get(i);
                ItemStack goodItemStack = new ItemStack(good.getGoodItem(), good.getCount());
                goodSlots.get(i).set(goodItemStack);
            });
        }
        if (!tradeMission.getGoodFromLocationList().isEmpty()) {
            IntStream.range(0, tradeMission.getGoodFromLocationList().size()).forEach(i -> {
                Good good = tradeMission.getGoodFromLocationList().get(i);
                ItemStack goodItemStack = new ItemStack(good.getGoodItem(), good.getCount());
                goodSlots.get(4 + i).set(goodItemStack);
            });
        }
    }

    public void setTradeboxCooling() {
        setProcess(TradeBoxBlockEntity.PROCESS_COOLING);
        setCoolingTime(TradeBoxManager.COOLING_TIME_ON_REFRESH);
        setLocationId(Locations.EMPTY_LOCATION.getId());
        setResidentId(Residents.EMPTY_RESIDENT.getId());
        clearGoodInventory();
    }

    public void setTradeboxTrading() {
        TradeMission tradeMission = new TradeMission();
        try {
            Block block = player.level().getBlockState(pos).getBlock();
            if (block.asItem().equals(BlockRegistry.TRADE_BOX.get().asItem())) {
                tradeMission = TradeMission.genRandomTradeMission();
            }
            /*else if(block.asItem().equals(DrinkBeer.TRADE_BOX_NORTHON.asItem()){
                tradeMission = TradeMission.genSpecificTradeMission(Locations.NORTHON.getId());
            }*/
        } catch (Exception e) {
            tradeMission = TradeMission.genRandomTradeMission();
        }
        setTradeMission(tradeMission);
        setProcess(TradeBoxBlockEntity.PROCESS_TRADING);
    }

    public List<Slot> getToLocationGoodSlots() {
        return goodSlots.subList(0, 4);
    }

    public List<Slot> getFromLocationGoodSlots() {
        return goodSlots.subList(4, 8);
    }

    public boolean hasLegalTradeMission() {
        if (!(Math.max(getLocationId(), Locations.EMPTY_LOCATION.getId()) == Math.min(getLocationId(), Locations.size())))
            return false;
        if (!(Math.max(getResidentId(), Residents.EMPTY_RESIDENT.getId()) == Math.min(getResidentId(), Residents.size())))
            return false;
        if (getToLocationGoodSlots().stream().noneMatch(Slot::hasItem))
            return false;
        return getFromLocationGoodSlots().stream().anyMatch(Slot::hasItem);
    }

    private void clearGoodInventory() {
        goodSlots.forEach(slot -> slot.set(ItemStack.EMPTY));
    }

    private Map<Item, Integer> goodSlotListToGoodMap(List<Slot> goodSlots) {
        Map<Item, Integer> goodMap = new HashMap<>();
        IntStream.range(0, 4).forEach(i -> {
            if (!goodSlots.get(i).hasItem())
                return;

            ItemStack inputTradeboxSlotsItemStack = goodSlots.get(i).getItem();
            Item inputTradeboxSlotsItem = inputTradeboxSlotsItemStack.getItem();
            if (goodMap.containsKey(inputTradeboxSlotsItem)) {
                goodMap.replace(inputTradeboxSlotsItem, goodMap.get(inputTradeboxSlotsItem) + inputTradeboxSlotsItemStack.getCount());
            } else {
                goodMap.put(inputTradeboxSlotsItem, inputTradeboxSlotsItemStack.getCount());
            }
        });
        return goodMap;
    }

    static class InputSlot extends Slot {
        public InputSlot(Container p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
            super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        }

        // Placing item on output slot is prohibited.
        @Override
        public boolean mayPlace(ItemStack p_75214_1_) {
            return true;
        }
    }

    static class OutputSlot extends Slot {
        private final ContainerData syncData;
        private final TradeBoxContainer tradeBoxContainer;

        public OutputSlot(Container p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_, ContainerData syncData, TradeBoxContainer tradeBoxContainer) {
            super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
            this.syncData = syncData;
            this.tradeBoxContainer = tradeBoxContainer;
        }

        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        public boolean mayPickup(Player playerEntity) {
            return false;
        }

        public boolean isActive() {
            return !tradeBoxContainer.isCooling();
        }
    }
}
