package lekavar.lma.drinkbeer.registries;

//TODO: Clean unused imports
import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, DrinkBeer.MOD_ID);

     public static final RegistryObject<ParticleType<SimpleParticleType>> MIXED_BEER_DEFAULT = PARTICLES.register("mixed_beer_default", () -> new SimpleParticleType(true));
    public static final RegistryObject<ParticleType<SimpleParticleType>> CALL_BELL_TINKLE_PAW = PARTICLES.register("call_bell_tinkle_paw", () -> new SimpleParticleType(true));

}
