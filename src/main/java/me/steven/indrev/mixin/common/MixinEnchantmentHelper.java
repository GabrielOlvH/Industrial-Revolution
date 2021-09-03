package me.steven.indrev.mixin.common;

import me.steven.indrev.api.CustomEnchantmentProvider;
import me.steven.indrev.api.IRPlayerEntityExtension;
import me.steven.indrev.tools.modular.ArmorModule;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {
    @Inject(method = "getLevel", at = @At("HEAD"), cancellable = true)
    private static void indrev_customEnchantProvider(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof CustomEnchantmentProvider) {
            int level = ((CustomEnchantmentProvider) stack.getItem()).getLevel(enchantment, stack);
            if (level > -1)
                cir.setReturnValue(level);
        }
    }

    @Inject(method = "hasAquaAffinity", at = @At("HEAD"), cancellable = true)
    private static void indrev_waterAffinityChest(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        //specifically checks for water affinity on chestplate
        if (entity instanceof PlayerEntity player
                && ArmorModule.WATER_AFFINITY.getLevel(player.getInventory().getArmorStack(EquipmentSlot.CHEST.getEntitySlotId())) > 0
                && entity instanceof IRPlayerEntityExtension ext
                && ext.isApplied(ArmorModule.WATER_AFFINITY)
        )
            cir.setReturnValue(true);
    }

    @Inject(method = "getDepthStrider", at = @At("HEAD"), cancellable = true)
    private static void indrev_waterAffinityLegs(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        //specifically checks for water affinity on leggings
        if (entity instanceof PlayerEntity player
                && ArmorModule.WATER_AFFINITY.getLevel(player.getInventory().getArmorStack(EquipmentSlot.LEGS.getEntitySlotId())) > 0
                && entity instanceof IRPlayerEntityExtension ext
                && ext.isApplied(ArmorModule.WATER_AFFINITY)
        )
            cir.setReturnValue(3);
    }
}
