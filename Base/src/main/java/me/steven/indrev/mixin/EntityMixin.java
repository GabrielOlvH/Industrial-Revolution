package me.steven.indrev.mixin;

import me.steven.indrev.extensions.EntityExtension;
import me.steven.indrev.utils.ItemStackCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements EntityExtension {
    @Unique
    private ItemStackCallback callback;

    @Inject(method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("INVOKE"), cancellable = true)
    private void indrev_redirectDrops(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        if (callback != null && !stack.isEmpty()) {
            callback.invoke(stack);
            if (stack.isEmpty()) cir.setReturnValue(null);
        }
    }

    @Nullable
    @Override
    public ItemStackCallback getIndrev$inventoryRedirect() {
        return callback;
    }

    @Override
    public void setIndrev$inventoryRedirect(@Nullable ItemStackCallback callback) {
        this.callback = callback;
    }
}
