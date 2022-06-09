package lekavar.lma.drinkbeer;

import lekavar.lma.drinkbeer.client.DrinkBeerClient;
import lekavar.lma.drinkbeer.networking.NetWorking;
import lekavar.lma.drinkbeer.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("drinkbeer")
public class DrinkBeer {
    // We don't need this logger now since there is no need at all.
    // Directly reference a log4j logger.
    // private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "drinkbeer";

    public DrinkBeer() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        MobEffectRegistry.STATUS_EFFECTS.register(bus);
        ItemRegistry.ITEMS.register(bus);
        BlockRegistry.BLOCKS.register(bus);
        BlockEntityRegistry.BLOKC_ENTITIES.register(bus);
        SoundEventRegistry.SOUNDS.register(bus);
        ContainerTypeRegistry.CONTAINERS.register(bus);
        RecipeRegistry.RECIPE_TYPES.register(bus);
        RecipeRegistry.RECIPE_SERIALIZERS.register(bus);
        ParticleRegistry.PARTICLES.register(bus);

        bus.addListener(DrinkBeerClient::onInitializeClient);
        bus.addListener(NetWorking::init);
        // We just don't need these part now
        // Register the setup method for modloading
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        // Register ourselves for server and other game events we are interested in
        //MinecraftForge.EVENT_BUS.register(this);
    }

    // We just don't need these part now
    /*private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ScreenManager.register(ContainerTypeRegistry.beerBarrelContainer.get(), BeerBarrelContainerScreen::new));
    }


    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().options);
    }


    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }*/
}
