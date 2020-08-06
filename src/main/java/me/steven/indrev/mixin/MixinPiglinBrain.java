package me.steven.indrev.mixin;

import me.steven.indrev.armor.Module;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.reborn.energy.Energy;

@Mixin(PiglinBrain.class)
public class MixinPiglinBrain {
    @Inject(method = "wearsGoldArmor", at = @At("RETURN"), cancellable = true)
    private static void indrev_hasPiglinTrickerModule(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        for (ItemStack armorItem : entity.getArmorItems()) {
            if (armorItem.getItem() instanceof ArmorItem
                    && Module.Companion.isInstalled(armorItem, Module.PIGLIN_TRICKER)
                    && Energy.of(armorItem).use(10.0)) {
                cir.setReturnValue(true);
            }
        }
    }
}
