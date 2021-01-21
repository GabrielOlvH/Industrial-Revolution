package me.steven.indrev.mixin.common;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SyncedGuiDescription.class)
public interface AccessorSyncedGuiDescription {
    @Invoker("insertItem")
    boolean indrev_callInsertItem(ItemStack toInsert, Inventory inventory, boolean walkBackwards, PlayerEntity player);
    @Invoker("swapHotbar")
    boolean indrev_callSwapHotbar(ItemStack toInsert, int slotNumber, Inventory inventory, PlayerEntity player);
}
