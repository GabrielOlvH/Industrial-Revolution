package me.steven.indrev;

import net.fabricmc.loader.FabricLoader;

import java.io.File;
import java.time.LocalDate;

public class AprilFools {
    public static final File CHECK =
        new File(FabricLoader.INSTANCE.getGameDir() + "/.indrev_" + LocalDate.now().getYear());

    public static boolean isToday() {
        LocalDate now = LocalDate.now();
        return now.getDayOfMonth() == 1 && now.getMonthValue() == 4 && !CHECK.exists();
    }
}
