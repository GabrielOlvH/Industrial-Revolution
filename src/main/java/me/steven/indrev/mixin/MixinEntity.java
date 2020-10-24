package me.steven.indrev.mixin;

import me.steven.indrev.items.armor.IRModularArmor;
import me.steven.indrev.tools.modular.ArmorModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow public abstract Iterable<ItemStack> getArmorItems();

    @Inject(method = "setAir", at = @At("INVOKE"), cancellable = true)
    private void oof(CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity) {
            ((LivingEntity) (Object) (this)).getArmorItems().forEach(itemStack -> {
                Item item = itemStack.getItem();
                if (item instanceof IRModularArmor && ArmorModule.BREATHING.isInstalled(itemStack)) {
                    ci.cancel();
                }
            });
        }
    }

    @Inject(method = "getJumpVelocityMultiplier", at = @At(value = "RETURN"), cancellable = true)
    private void v(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof LivingEntity) {
            ((LivingEntity) (Object) (this)).getArmorItems().forEach(itemStack -> {
                Item item = itemStack.getItem();
                if (item instanceof IRModularArmor) {
                    int level = ArmorModule.JUMP_BOOST.getLevel(itemStack);
                    if (level > 0) cir.setReturnValue((float) level);
                }
            });
        }
    }
}
