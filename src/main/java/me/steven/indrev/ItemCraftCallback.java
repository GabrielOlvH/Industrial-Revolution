package me.steven.indrev;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;

public interface ItemCraftCallback {
    Event<ItemCraftCallback> EVENT = EventFactory.createArrayBacked(ItemCraftCallback.class, (listeners) -> (stack, craftingInventory, playerEntity) -> {
        for (ItemCraftCallback callback : listeners) {
            callback.onCraft(stack, craftingInventory, playerEntity);
        }
    });

    void onCraft(ItemStack stack, CraftingInventory craftingInventory, PlayerEntity playerEntity);
}