package me.steven.indrev.utils

enum class Tier(val io: Double) {
    MK1(16.0),
    MK2(64.0),
    MK3(512.0),
    MK4(4096.0),
    CREATIVE(Double.MAX_VALUE);

    companion object {
        val VALUES = arrayOf(MK1, MK2, MK3, MK4)
    }
}