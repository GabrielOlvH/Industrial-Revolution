package me.steven.indrev.mixin;

import me.steven.indrev.FabricRecipeRemainder;
import net.minecraft.container.CraftingResultSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CraftingResultSlot.class)
public abstract class MixinCraftingResultSlot {
    @Shadow
    @Final
    private CraftingInventory craftingInv;

    @Shadow
    @Final
    private PlayerEntity player;

    @ModifyVariable(method = "onTakeItem", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/recipe/RecipeManager;getRemainingStacks(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Lnet/minecraft/util/DefaultedList;"))
    private DefaultedList<ItemStack> defaultedList(DefaultedList<ItemStack> list) {
        for (int i = 0; i < craftingInv.getInvSize(); i++) {
            ItemStack invStack = craftingInv.getInvStack(i);

            if (invStack.getItem() instanceof FabricRecipeRemainder) {
                ItemStack remainder = ((FabricRecipeRemainder) invStack.getItem()).getRemainder(invStack.copy(), craftingInv, player);
                list.set(i, remainder);
            }
        }

        return list;
    }
}