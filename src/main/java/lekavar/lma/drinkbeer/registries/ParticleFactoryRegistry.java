package lekavar.lma.drinkbeer.registries;


import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = DrinkBeer.MOD_ID, value = Dist.CLIENT , bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleFactoryRegistry {

    @SubscribeEvent()
    public static void registerParticleFactory(RegisterParticleProvidersEvent event)
    {
            Minecraft.getInstance().particleEngine.register(ParticleRegistry.MIXED_BEER_DEFAULT.get(), FlameParticle.Provider::new);
            Minecraft.getInstance().particleEngine.register(ParticleRegistry.CALL_BELL_TINKLE_PAW.get(), HeartParticle.AngryVillagerProvider::new);
    }
}
