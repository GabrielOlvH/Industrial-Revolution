package me.steven.indrev.mixin.client;

import me.steven.indrev.items.energy.IREnergyItem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This fixes an issue where energy based items crafted in versions prior to 1.8.0 would render a buggy durability bar.
 * Will be removed in 1.9.0.
 */
@Mixin(value = ItemRenderer.class, priority = 1001)
public class MixinItemRenderer {
    @Inject(
            method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isDamaged()Z")),
            at = @At("HEAD"),
            cancellable = true
    )
    private void a(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        if (stack.getItem() instanceof IREnergyItem) {
            ci.cancel();
        }
    }
}
