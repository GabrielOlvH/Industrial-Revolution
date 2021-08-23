package me.steven.indrev.mixin.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "knockDownwards", at = @At("HEAD"), cancellable = true)
    private void indrev_waterAffinityDownwards(CallbackInfo ci) {
        this.setVelocity(this.getVelocity().add(0.0D, -0.03999999910593033D * 2, 0.0D));
        ci.cancel();
    }

    @Inject(method = "swimUpward", at = @At("HEAD"), cancellable = true)
    private void indrev_waterAffinityUpwards(CallbackInfo ci) {
        this.setVelocity(this.getVelocity().add(0.0D, 0.03999999910593033D * 2, 0.0D));
        ci.cancel();
    }
}
