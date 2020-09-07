package me.steven.indrev.mixin;

import me.steven.indrev.items.energy.IREnergyItem;
import me.steven.indrev.items.energy.IRGamerAxeItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.reborn.energy.Energy;
import team.reborn.energy.EnergyHandler;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {
    @Inject(method = "getBlockBreakingSpeed", at = @At("HEAD"), cancellable = true)
    private void indrev_checkEnergyTool(BlockState block, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        PlayerInventory inventory = player.inventory;
        ItemStack itemStack = inventory.main.get(inventory.selectedSlot);
        Item item = itemStack.getItem();
        if (Energy.valid(itemStack) && item instanceof IREnergyItem) {
            EnergyHandler handler = Energy.of(itemStack);
            if (item instanceof IRGamerAxeItem) {
                CompoundTag tag = itemStack.getOrCreateTag();
                if (tag.contains("Active") && !tag.getBoolean("Active")) {
                    cir.setReturnValue(0.2F);
                    return;
                }
            }
            if (handler.getEnergy() < 1) cir.setReturnValue(0.2F);
        }
    }
}
