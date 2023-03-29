package me.steven.indrev;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@Environment(EnvType.CLIENT)
public final class AprilFools {
    private static final File CHECK =
        new File(FabricLoader.getInstance().getGameDir().toString(), ".indrev_" + LocalDate.now().getYear());

    public static void init() {
        if (isToday()) {
            ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, list) -> {
                String itemNamespace = Registry.ITEM.getId(itemStack.getItem()).getNamespace();
                if (itemNamespace.equals(IndustrialRevolution.MOD_ID) && list.size() > 1) {
                    list.add(Text.literal("")); // break line
                    list.add(Text.literal("every good modpack uses forge...").formatted(Formatting.ITALIC));
                }
            });

            try { CHECK.createNewFile(); }
            catch (IOException ex) { ex.printStackTrace(); }
        }
    }

    public static boolean isToday() {
        return false;
        /*LocalDate now = LocalDate.now();
        return now.getDayOfMonth() == 1 && now.getMonthValue() == 4 && !CHECK.exists();*/
    }
}
