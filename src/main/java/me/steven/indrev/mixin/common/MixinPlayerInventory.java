package me.steven.indrev.mixin.common;

import me.steven.indrev.items.armor.JetpackHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class MixinPlayerInventory {
    @Shadow public abstract ItemStack getArmorStack(int slot);

    @Shadow @Final public PlayerEntity player;

    @Inject(method = "updateItems", at = @At("TAIL"))
    private void indrev_tickJetpack(CallbackInfo ci) {
        ItemStack armorStack = getArmorStack(EquipmentSlot.CHEST.getEntitySlotId());
        if (armorStack.getItem() instanceof JetpackHandler handler && handler.isUsable(armorStack)) {
            handler.tickJetpack(armorStack, player);
        }
    }
}
