package me.steven.indrev.mixin.aprilfools;

import me.steven.indrev.IndustrialRevolution;
//import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

//@Mixin(TranslatableText.class)
public class MixinTranslatableText {
/*    @Shadow @Final private String key;

    @ModifyVariable(name = "string", at = @At("STORE"), method = "updateTranslations")
    private String owo(String original) {
        if (this.key.contains(IndustrialRevolution.MOD_ID)) {
            if (!original.contains(".")) {
                String[] words = original.split(" ");
                for (int i = 0; i < words.length; i++) {
                    if (words[i].endsWith("ger") || words[i].endsWith("der"))
                        words[i] = words[i].substring(0, words[i].length() - 2) + "ah";
                    else if (words[i].endsWith("er") || words[i].endsWith("re"))
                        words[i] = words[i].substring(0, words[i].length() - 2) + 'y';
                    else if (words[i].endsWith("xe"))
                        words[i] = words[i].substring(0, words[i].length() - 2) + "xie";
                    else if (words[i].endsWith("et"))
                        words[i] = words[i].substring(0, words[i].length() - 2) + "ie";

                    words[i] = words[i].replaceFirst("est", "ess"); // Chest -> Chess
                    words[i] = words[i].replaceFirst("O", "Ow").replaceFirst("o", "ow");
                }

                return String.join(" ", words);
            }
        }

        return original;
    }*/
}
