package me.steven.indrev.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import me.steven.indrev.items.armor.IRModularArmor;
import me.steven.indrev.items.energy.IRGamerAxeItem;
import me.steven.indrev.tools.modular.ArmorModule;
import me.steven.indrev.tools.modular.GamerAxeModule;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.reborn.energy.Energy;

import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    private static final UUID[] MODIFIERS = new UUID[]{UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    @Inject(method = "getAttributeModifiers", at = @At("TAIL"), cancellable = true)
    private void indrev_calcAttributeModifiers(EquipmentSlot equipmentSlot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof IRModularArmor) {
            ArmorItem item = (ArmorItem) stack.getItem();
            if (Energy.of(stack).getEnergy() <= 0) {
                cir.setReturnValue(ImmutableMultimap.of());
            } else if (equipmentSlot == item.getSlotType()) {
                int level = ArmorModule.Companion.getLevel(stack, ArmorModule.PROTECTION);
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
        } else if (stack.getItem() instanceof IRGamerAxeItem) {
            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.contains("Active") || !tag.getBoolean("Active") || Energy.of(stack).getEnergy() <= 0)
                cir.setReturnValue(ImmutableMultimap.of());
            else if (equipmentSlot == EquipmentSlot.MAINHAND) {
                ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", ((IRGamerAxeItem) stack.getItem()).getAttackDamage() * ((GamerAxeModule.SHARPNESS.getLevel(stack) / 2f) + 1), EntityAttributeModifier.Operation.ADDITION));
                builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Tool modifier", -2f, EntityAttributeModifier.Operation.ADDITION));
                cir.setReturnValue(builder.build());
            }
        }
    }

    @Inject(method = "damage(ILjava/util/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void indrev_useArmorEnergy1(int amount, Random random, ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof IRModularArmor) {
            extractEnergy(stack, amount);
            cir.cancel();
        }
    }

    @Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void indrev_useArmorEnergy2(int amount, LivingEntity entity, Consumer<LivingEntity> breakCallback, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof IRModularArmor) {
            extractEnergy(stack, amount);
            ci.cancel();
        }
    }

    private static void extractEnergy(ItemStack stack, int amount) {
        if (Energy.valid(stack)) {
            Energy.of(stack).extract(amount);
        }
    }
}
