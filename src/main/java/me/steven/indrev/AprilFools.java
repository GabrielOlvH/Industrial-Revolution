package me.steven.indrev;

import java.time.LocalDate;

public class AprilFools {
    public static final LocalDate DATE = LocalDate.parse("2021-03-27");

    public static boolean isToday() {
        return LocalDate.now().equals(DATE);
    }
}
