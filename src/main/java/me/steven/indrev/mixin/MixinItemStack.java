package me.steven.indrev.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import me.steven.indrev.armor.Module;
import me.steven.indrev.items.armor.IRArmor;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    private static final UUID[] MODIFIERS = new UUID[]{UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};

    @Inject(method = "getAttributeModifiers", at = @At("TAIL"), cancellable = true)
    private void calcAttributeModifiers(EquipmentSlot equipmentSlot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof IRArmor) {
            ArmorItem item = (ArmorItem) stack.getItem();
            if (stack.getDamage() >= stack.getMaxDamage() - 1) {
                cir.setReturnValue(ImmutableMultimap.of());
            } else if (equipmentSlot == item.getSlotType()) {
                int level = Module.Companion.getLevel(stack, Module.PROTECTION);
                UUID uUID = MODIFIERS[equipmentSlot.getEntitySlotId()];
                ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> attr = ImmutableMultimap.builder();
                if (level > 0) {
                    attr.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                            new EntityAttributeModifier(uUID, "Armor toughness", item.method_26353() * level, EntityAttributeModifier.Operation.ADDITION));
                    attr.put(EntityAttributes.GENERIC_ARMOR,
                            new EntityAttributeModifier(uUID, "Armor modifier", item.getProtection() * level, EntityAttributeModifier.Operation.ADDITION));
                }
                cir.setReturnValue(attr.build());
            }
        }
    }

    @Inject(method = "setDamage", at = @At("HEAD"), cancellable = true)
    private void cancelBreak(int damage, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (damage == stack.getMaxDamage() && stack.getItem() instanceof IRArmor)
            ci.cancel();
    }
}
