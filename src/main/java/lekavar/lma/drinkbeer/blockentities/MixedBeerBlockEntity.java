/*
package lekavar.lma.drinkbeer.blockentities;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.managers.MixedBeerManager;
import lekavar.lma.drinkbeer.registries.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MixedBeerBlockEntity extends BlockEntity {
    private int beerId;
    private List<Integer> spiceList = new ArrayList<>();

    public MixedBeerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.MIXED_BEER_TILEENTITY.get(), pos, state);
    }

    public MixedBeerBlockEntity(BlockPos pos, BlockState state, int beerId, List<Integer> spiceList) {
        super(BlockEntityRegistry.MIXED_BEER_TILEENTITY.get(), pos, state);
        this.beerId = beerId;
        this.spiceList.clear();
        this.spiceList.addAll(spiceList);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putShort("beerId", (short) this.beerId);
        tag.putIntArray("spiceList", getSpiceList() );
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        this.beerId = tag.getShort("beerId");
        for (int spice: tag.getIntArray("spiceList")) {
            this.spiceList.add(spice);
        }
    }

    public ItemStack getPickStack(BlockState state) {
        //Generate mixed beer item stack for dropping
        ItemStack resultStack = MixedBeerManager.genMixedBeerItemStack(this.beerId, this.spiceList);
        return resultStack;
    }

    public List<Integer> getSpiceList() {
        return spiceList;
    }

    public int getBeerId() {
        return beerId;
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

}
*/