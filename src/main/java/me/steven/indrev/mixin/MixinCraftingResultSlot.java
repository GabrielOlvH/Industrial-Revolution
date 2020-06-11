package me.steven.indrev.mixin;

import me.steven.indrev.FabricRecipeRemainder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CraftingResultSlot.class)
public abstract class MixinCraftingResultSlot {
    @Shadow
    @Final
    private CraftingInventory input;

    @Shadow
    @Final
    private PlayerEntity player;

    @ModifyVariable(method = "onTakeItem", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/recipe/RecipeManager;getRemainingStacks(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Lnet/minecraft/util/collection/DefaultedList;"))
    private DefaultedList<ItemStack> defaultedList(DefaultedList<ItemStack> list) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack invStack = input.getStack(i);

            if (invStack.getItem() instanceof FabricRecipeRemainder) {
                ItemStack remainder = ((FabricRecipeRemainder) invStack.getItem()).getRemainder(invStack.copy(), input, player);
                list.set(i, remainder);
            }
        }

        return list;
    }
}