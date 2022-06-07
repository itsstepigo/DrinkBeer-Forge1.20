package lekavar.lma.drinkbeer.blocks;

import lekavar.lma.drinkbeer.blockentities.BartendingTableBlockEntity;
import lekavar.lma.drinkbeer.blockentities.BeerBarrelBlockEntity;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;
import lekavar.lma.drinkbeer.utils.ModCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class BartendingTableBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPENED = BooleanProperty.create("opened");
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 1, 2);

    public final static VoxelShape SHAPE =  Block.box(0, 0.01, 0, 16, 16, 16);

    public BartendingTableBlock() {
        super(BlockBehaviour.Properties.of(Material.WOOD).strength(2.0f).noOcclusion());
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPENED, true)
                .setValue(TYPE, 1));
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(OPENED).add(TYPE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (itemStack.getItem().asItem().getCreativeTabs().contains(ModCreativeTab.BEER)) {
                world.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1f, 1f);
                BlockEntity blockentity = world.getBlockEntity(pos);
                if (blockentity instanceof BartendingTableBlockEntity) {
                    ((BartendingTableBlockEntity) blockentity).setBeer(itemStack);
                    itemStack.shrink(1);
                    NetworkHooks.openGui((ServerPlayer) player, (BartendingTableBlockEntity) blockentity, (FriendlyByteBuf packerBuffer) -> {
                        packerBuffer.writeBlockPos(blockentity.getBlockPos());
                    });
                }
                return InteractionResult.CONSUME;
            } else {
                boolean currentOpenedState = state.getValue(OPENED);
                world.playSound(null, pos, currentOpenedState ? SoundEventRegistry.BARTENDING_TABLE_CLOSE.get() : SoundEventRegistry.BARTENDING_TABLE_OPEN.get(), SoundSource.BLOCKS, 1f, 1f);
                world.setBlockAndUpdate(pos, state.setValue(OPENED, !currentOpenedState));
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntity) {
        if (level != null && level.isClientSide()) {
            return null;
        } else {
            return (theLevel, pos, state, tile) -> {
                if (tile instanceof BartendingTableBlockEntity bartendingTableBlockEntity) {
                    bartendingTableBlockEntity.tickServer();
                }
            };
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BartendingTableBlockEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }
}
