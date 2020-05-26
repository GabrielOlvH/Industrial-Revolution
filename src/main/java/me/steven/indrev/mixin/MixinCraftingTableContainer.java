package me.steven.indrev.mixin;

import me.steven.indrev.items.rechargeable.Rechargeable;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(CraftingTableContainer.class)
public class MixinCraftingTableContainer {

    @Redirect(method = "updateResult",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeUnlocker;shouldCraftRecipe(Lnet/minecraft/world/World;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/recipe/Recipe;)Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/CraftingRecipe;craft(Lnet/minecraft/inventory/Inventory;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack itemStack(CraftingRecipe craftingRecipe, Inventory inv) {
        if (!(inv instanceof CraftingInventory)) return ItemStack.EMPTY;
        CraftingInventory craftingInventory = (CraftingInventory) inv;
        ItemStack result = craftingRecipe.craft(craftingInventory);
        if (result.getItem() instanceof Rechargeable) {
            int damage = 0;
            for (int i = 0; i < craftingInventory.getInvSize(); i++) {
                ItemStack stack = craftingInventory.getInvStack(i);
                if (stack.getItem() instanceof Rechargeable) damage += stack.getMaxDamage() - stack.getDamage();
            }
            result.setDamage(result.getMaxDamage() - Math.min(damage, result.getMaxDamage()));
        }
        return result;
    }
}
