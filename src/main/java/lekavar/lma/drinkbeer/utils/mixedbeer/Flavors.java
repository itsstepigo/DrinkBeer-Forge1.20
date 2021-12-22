package lekavar.lma.drinkbeer.utils.mixedbeer;

import lekavar.lma.drinkbeer.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

public enum  Flavors {
    SPICY(1, "spicy", null, ParticleTypes.LAVA),
    FIERY(2, "fiery", SPICY, ParticleTypes.LAVA),
    SOOOOO_SPICY(3, "soooo_spicy", null, null),
    AROMITIC(4, "aromatic", null, (SimpleParticleType)ParticleRegistry.MIXED_BEER_DEFAULT.get()),
    AROMITIC1(5, "aromatic1", AROMITIC, (SimpleParticleType)ParticleRegistry.MIXED_BEER_DEFAULT.get()),
    REFRESHING(6, "refreshing", null,(SimpleParticleType)ParticleRegistry.MIXED_BEER_DEFAULT.get()),
    REFRESHING1(7, "refreshing1", REFRESHING, (SimpleParticleType)ParticleRegistry.MIXED_BEER_DEFAULT.get()),
    STORMY(8, "stormy", null, (SimpleParticleType)ParticleRegistry.MIXED_BEER_DEFAULT.get()),
    THE_FALL_OF_THE_GIANT(9, "the_fall_of_the_giant", null, null),
    NUTTY(10, "nutty", null, (SimpleParticleType)ParticleRegistry.MIXED_BEER_DEFAULT.get()),
    SWEET(11, "sweet", null, (SimpleParticleType)ParticleRegistry.MIXED_BEER_DEFAULT.get()),
    LUSCIOUS(12, "luscious", SWEET, (SimpleParticleType)ParticleRegistry.MIXED_BEER_DEFAULT.get()),
    CLOYING(13,"cloying",null,null),
    NUTTY1(14, "nutty1", NUTTY, (SimpleParticleType)ParticleRegistry.MIXED_BEER_DEFAULT.get()),
    MELLOW(15,"mellow",null,(SimpleParticleType)ParticleRegistry.MIXED_BEER_DEFAULT.get()),
    DRYING(16,"drying",null,(SimpleParticleType)ParticleRegistry.MIXED_BEER_DEFAULT.get());

    private final int id;
    private final String name;
    private final Flavors fatherFlavor;
    private final SimpleParticleType particle;

    public static final int EMPTY_FLAVOR_ID = 0;
    public static final Flavors DEFAULT_FLAVOR = Flavors.SPICY;
    public static final SimpleParticleType DEFAULT_PARTICLE = (SimpleParticleType)ParticleRegistry.MIXED_BEER_DEFAULT.get();

    Flavors(int id, String name, Flavors fatherFlavor, SimpleParticleType particle) {
        this.id = id;
        this.name = name;
        this.fatherFlavor = fatherFlavor;
        this.particle = particle;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Flavors getFatherFlavor() {
        return fatherFlavor;
    }

    public SimpleParticleType getParticle() {
        return particle;
    }

    public static Flavors byId(int id) {
        Flavors[] flavors = values();
        for (Flavors flavor : flavors) {
            if (flavor.id == id) {
                return flavor;
            }
        }
        return DEFAULT_FLAVOR;
    }
}
