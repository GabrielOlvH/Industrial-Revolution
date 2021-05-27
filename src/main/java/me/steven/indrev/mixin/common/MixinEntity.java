package me.steven.indrev.mixin.common;

import me.steven.indrev.api.IREntityExtension;
import me.steven.indrev.api.IRPlayerEntityExtension;
import me.steven.indrev.inventories.IRInventory;
import me.steven.indrev.tools.modular.ArmorModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity implements IREntityExtension {
    @Shadow public World world;

    @Shadow public abstract double getX();

    @Shadow public abstract double getY();

    @Shadow public abstract double getZ();

    private IRInventory machineInv = null;

    @Inject(method = "setAir", at = @At("INVOKE"), cancellable = true)
    private void indrev_breathingModule(CallbackInfo ci) {
        if (this instanceof IRPlayerEntityExtension && ((IRPlayerEntityExtension) this).isApplied(ArmorModule.BREATHING)) {
            ci.cancel();
        }
    }

    @Inject(method = "getJumpVelocityMultiplier", at = @At(value = "RETURN"), cancellable = true)
    private void indrev_jumpBoostModule(CallbackInfoReturnable<Float> cir) {
        if (this instanceof IRPlayerEntityExtension && ((IRPlayerEntityExtension) this).isApplied(ArmorModule.JUMP_BOOST)) {
            cir.setReturnValue((float) ((IRPlayerEntityExtension) this).getAppliedLevel(ArmorModule.JUMP_BOOST));
        }
    }

    @Inject(method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void indrev_onDropItem(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        if (!world.isClient && machineInv != null) {
            if (!machineInv.output(stack)) {
                ItemEntity itemEntity = new ItemEntity(world, getX(), getY() + yOffset, getZ(), stack);
                itemEntity.setToDefaultPickupDelay();
                this.world.spawnEntity(itemEntity);
            }
            cir.cancel();
        }
    }

    @Nullable
    @Override
    public IRInventory getMachineInv() {
        return machineInv;
    }

    @Override
    public void setMachineInv(IRInventory machineInv) {
        this.machineInv = machineInv;
    }
}
