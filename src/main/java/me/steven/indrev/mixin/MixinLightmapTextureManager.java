package me.steven.indrev.mixin;

import me.steven.indrev.items.armor.IRModularArmor;
import me.steven.indrev.tools.modular.ArmorModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LightmapTextureManager.class)
public abstract class MixinLightmapTextureManager {

    @ModifyVariable(
            method = "update",
            at = @At(
                    value = "INVOKE_ASSIGN"
            ), ordinal = 2)
    private float indrev_nightVisionModule(float amount) {
        for (ItemStack stack : MinecraftClient.getInstance().player.getArmorItems()) {
            if (stack.getItem() instanceof IRModularArmor && ArmorModule.NIGHT_VISION.getLevel(stack) > 0) {
                return 1.0f;
            }
        }
        return amount;
    }
}
