package me.steven.indrev.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import me.steven.indrev.items.armor.IRModularArmor;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Inject(method = "getAttributeModifiers", at = @At("TAIL"), cancellable = true)
    private void checkForEnergy(EquipmentSlot equipmentSlot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof IRModularArmor && stack.getDamage() >= stack.getMaxDamage() - 1) {
            cir.setReturnValue(ImmutableMultimap.of());
        }
    }

    @Inject(method = "setDamage", at = @At("HEAD"), cancellable = true)
    private void cancelBreak(int damage, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (damage == stack.getMaxDamage() && stack.getItem() instanceof IRModularArmor)
            ci.cancel();
    }
}
