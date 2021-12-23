package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, DrinkBeer.MOD_ID);

     public static final RegistryObject<ParticleType<SimpleParticleType>> MIXED_BEER_DEFAULT = PARTICLES.register("mixed_beer_default", () -> new SimpleParticleType(true));
    public static final RegistryObject<ParticleType<SimpleParticleType>> CALL_BELL_TINKLE_PAW = PARTICLES.register("call_bell_tinkle_paw", () -> new SimpleParticleType(true));

}
