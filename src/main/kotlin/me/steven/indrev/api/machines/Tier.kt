package me.steven.indrev.api.machines

enum class Tier(val io: Double) {
    MK1(64.0),
    MK2(128.0),
    MK3(512.0),
    MK4(4096.0),
    CREATIVE(4096.0);

    companion object {
        val VALUES = arrayOf(MK1, MK2, MK3, MK4)
    }
}