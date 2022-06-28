package lekavar.lma.drinkbeer.effects;

import lekavar.lma.drinkbeer.registries.MobEffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.entity.player.Player;

import java.awt.*;

public class DrunkFrostWalkerStatusEffect extends MobEffect {
    public DrunkFrostWalkerStatusEffect() {
        super(MobEffectCategory.BENEFICIAL, new Color(30, 144, 255, 255).getRGB());
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player) {
            FrostWalkerEnchantment.onEntityMoved(entity, entity.level, new BlockPos(entity.position()), 1);
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        DrunkStatusEffect.addStatusEffect(entity);
    }


    /*
    @Override
    public void applyEffectTick(LivingEntity entity, int p_76394_2_) {
        int remainingTime = entity.getEffect(MobEffectRegistry.DRUNK_FROST_WALKER.get()).getDuration();
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, remainingTime));
        FrostWalkerEnchantment.onEntityMoved(entity, entity.level, new BlockPos(entity.position()), 1);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
     */
}