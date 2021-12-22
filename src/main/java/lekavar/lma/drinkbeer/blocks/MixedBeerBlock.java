package lekavar.lma.drinkbeer.blocks;

import lekavar.lma.drinkbeer.blockentities.BeerBarrelBlockEntity;
import lekavar.lma.drinkbeer.blockentities.MixedBeerBlockEntity;
import lekavar.lma.drinkbeer.managers.SpiceAndFlavorManager;
import lekavar.lma.drinkbeer.utils.beer.Beers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MixedBeerBlock extends BaseEntityBlock {
    public final static VoxelShape ONE_MUG_SHAPE = box(4, 0, 4, 12, 6, 12);

    public static final IntegerProperty BEER_ID = IntegerProperty.create("beer_id", 0, Beers.SIZE);
    public List<Integer> spiceList;

    public MixedBeerBlock() {
        super(Properties.of(Material.WOOD).strength(1.0f).noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(BEER_ID,Beers.DEFAULT_BEER_ID));
        this.spiceList = new ArrayList<>();
    }
    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return ONE_MUG_SHAPE;
    }



    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BEER_ID);
    }

    @Override
    public BlockState updateShape(BlockState p_196271_1_, Direction p_196271_2_, BlockState p_196271_3_, LevelAccessor p_196271_4_, BlockPos p_196271_5_, BlockPos p_196271_6_) {
        return p_196271_2_ == Direction.DOWN && !p_196271_1_.canSurvive(p_196271_4_, p_196271_5_) ? Blocks.AIR.defaultBlockState() : super.updateShape(p_196271_1_, p_196271_2_, p_196271_3_, p_196271_4_, p_196271_5_, p_196271_6_);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.DESTROY;
    }

    @Override
    public boolean canSurvive(BlockState p_196260_1_, LevelReader p_196260_2_, BlockPos p_196260_3_) {
        if (p_196260_2_.getBlockState(p_196260_3_.below()).getBlock() instanceof BeerMugBlock) return false;
        return Block.canSupportCenter(p_196260_2_, p_196260_3_.below(), Direction.UP);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.isEmpty()) {
            if (!world.isClientSide()) {
                world.removeBlock(pos, false);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        try {
            MixedBeerBlockEntity mixedBeerEntity = (MixedBeerBlockEntity) world.getBlockEntity(pos);
            ItemStack mixedBeerItemStack = mixedBeerEntity.getPickStack(state);
            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), mixedBeerItemStack );
        } catch (Exception e) {
            System.out.println("Somthing wrong with dropping mixed beer item stack!");
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MixedBeerBlockEntity(blockPos, blockState);
    }



    public void setBeerId(int beerId) {
        this.registerDefaultState(this.defaultBlockState().setValue(BEER_ID, beerId));
    }

    public void setSpiceList(List<Integer> spiceList) {
        this.spiceList.clear();
        this.spiceList.addAll(spiceList);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
        if(world.isClientSide()) {
            super.animateTick(state, world, pos, random);
            if (random.nextInt(5) == 0) {
                MixedBeerBlockEntity mixedBeerEntity = (MixedBeerBlockEntity) world.getBlockEntity(pos);
                SimpleParticleType particle = SpiceAndFlavorManager.getLastSpiceFlavorParticle(mixedBeerEntity.getSpiceList());
                if (random.nextInt(5) == 0) {
                    double x = (double) pos.getX() + 0.5D;
                    double y = (double) pos.getY() + 0.5D + random.nextDouble() / 4;
                    double z = (double) pos.getZ() + 0.5D;
                    world.addParticle(particle, x, y, z, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }



}
