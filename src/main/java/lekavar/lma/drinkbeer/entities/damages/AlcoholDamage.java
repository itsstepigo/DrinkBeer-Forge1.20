package lekavar.lma.drinkbeer.entities.damages;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class AlcoholDamage extends DamageSource {
    public AlcoholDamage() {
        super((Holder<DamageType>) new ResourceLocation("drinkbeer.alcohol"));
    }

    // TODO Check if needed
    /* Not sure about this override, the parent method should do just fine...
    @Override
    public Component getLocalizedDeathMessage(LivingEntity entity) {
        String str = "death.attack." + this.getMsgId();
        return new TranslatableComponent(str, entity.getDisplayName());
    }
     */
}
