package me.steven.indrev.mixin.aprilfools;

import me.steven.indrev.IndustrialRevolution;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TranslatableText.class)
public class MixinTranslatableText {
    @Shadow @Final private String key;

    @ModifyVariable(
        method = "visitSelf(Lnet/minecraft/text/StringVisitable$StyledVisitor;Lnet/minecraft/text/Style;)Ljava/util/Optional;",
        name = "stringVisitable",
        at = @At(value = "STORE")
    )
    private StringVisitable owo(StringVisitable original) {
        if (this.key.contains(IndustrialRevolution.MOD_ID)) {
            String str = original.getString();
            if (!str.contains("."))
                return StringVisitable.plain(
                    str.replace("O", "Ow")
                       .replace("o", "ow")
                );
        }

        return original;
    }
}
