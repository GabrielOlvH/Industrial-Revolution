package me.steven.indrev.api.machines

enum class Tier(val io: Double, val id: String) {
    MK1(64.0, "mk1"),
    MK2(128.0, "mk2"),
    MK3(512.0, "mk3"),
    MK4(4096.0, "mk4"),
    CREATIVE(4096.0, "creative");

    companion object {
        val VALUES = arrayOf(MK1, MK2, MK3, MK4)

        val ALL_VALUES = values()
    }
}