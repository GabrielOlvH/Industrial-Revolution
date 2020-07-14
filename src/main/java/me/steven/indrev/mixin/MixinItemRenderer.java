package me.steven.indrev.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.steven.indrev.items.rechargeable.IRRechargeable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.reborn.energy.EnergyHolder;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {
    @Shadow
    protected abstract void renderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha);

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    private void renderEnergy(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        Item item = stack.getItem();
        if (item instanceof IRRechargeable && item instanceof EnergyHolder) {
            CompoundTag tag = stack.getOrCreateTag();
            float energy;
            if (tag.contains("energy")) energy = tag.getInt("energy");
            else energy = 0;
            float maxStoredPower = (float) ((EnergyHolder) item).getMaxStoredPower();
            energy = maxStoredPower - energy;
            RenderSystem.disableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            float h = Math.max(0.0F, (maxStoredPower - energy) / maxStoredPower);
            int i = Math.round(13.0F - energy * 13.0F / maxStoredPower);
            int j = MathHelper.hsvToRgb(h / 3.0F, 1.0F, 1.0F);
            this.renderGuiQuad(bufferBuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
            this.renderGuiQuad(bufferBuilder, x + 2, y + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }
    }
}
