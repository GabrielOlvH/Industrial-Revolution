package me.steven.indrev.utils

enum class Tier(val maxInput: Double, val maxOutput: Double) {
    LOW(8.0, 8.0),
    MEDIUM(64.0, 64.0),
    HIGH(128.0, 128.0),
    ULTIMATE(256.0, 256.0)
}