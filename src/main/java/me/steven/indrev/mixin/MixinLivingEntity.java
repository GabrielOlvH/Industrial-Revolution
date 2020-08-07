package me.steven.indrev.mixin;

import me.steven.indrev.utils.FakePlayerEntity;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Shadow
    protected abstract void damageArmor(DamageSource source, float amount);

    @Inject(method = "drop", at = @At("INVOKE"), cancellable = true)
    public void indrev_cancelDrop(DamageSource source, CallbackInfo ci) {
        if (source.getAttacker() instanceof FakePlayerEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "applyArmorToDamage", at = @At("HEAD"), cancellable = true)
    public void indrev_applyArmorToDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof ServerPlayerEntity && !source.bypassesArmor()) {
            float remaining = DamageUtil.getDamageLeft(amount, entity.getArmor(), (float) entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
            if (remaining != amount)
                damageArmor(source, amount);
            ci.setReturnValue(remaining);
        }
    }
}
