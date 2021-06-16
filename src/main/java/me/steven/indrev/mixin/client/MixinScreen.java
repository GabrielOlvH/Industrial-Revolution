package me.steven.indrev.mixin.client;

import me.steven.indrev.gui.tooltip.CustomTooltipData;
import me.steven.indrev.gui.tooltip.CustomTooltipDataProvider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Shadow
    public abstract List<Text> getTooltipFromItem(ItemStack stack);

    @Shadow protected abstract void renderTooltipFromComponents(MatrixStack matrices, List<TooltipComponent> components, int x, int y);

    @Inject(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At("INVOKE"), cancellable = true)
    private void indrev_tooltipComponent(MatrixStack matrices, ItemStack stack, int x, int y, CallbackInfo ci) {
        if (stack.getItem() instanceof CustomTooltipDataProvider provider) {
            // mimic vanilla behaviour, cursed mixin yes but blame mojang
            List<Text> lines = getTooltipFromItem(stack);
            Optional<TooltipData> data = stack.getTooltipData();
            List<TooltipComponent> list = lines.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
            data.ifPresent((datax) -> list.add(1, TooltipComponent.of(datax)));

            // add custom tooltip stuff
            for (CustomTooltipData customData : provider.getData(stack)) {
                list.add(1, customData.toComponent());
            }

            this.renderTooltipFromComponents(matrices, list, x, y);
            ci.cancel();
        }
    }
}
