package me.steven.indrev;

import net.fabricmc.loader.FabricLoader;

import java.io.File;
import java.time.LocalDate;

public class AprilFools {
    public static final LocalDate DATE = LocalDate.parse("2021-03-27");
    public static final File CHECK = new File(FabricLoader.INSTANCE.getGameDir().toString() + ".indrev_ANO");

    public static boolean isToday() {
        return LocalDate.now().equals(DATE) && !CHECK.exists();
    }
}
