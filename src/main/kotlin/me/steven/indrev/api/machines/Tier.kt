package me.steven.indrev.api.machines

enum class Tier(val io: Long, val id: String) {
    MK1(64, "mk1"),
    MK2(128, "mk2"),
    MK3(512, "mk3"),
    MK4(4096, "mk4"),
    CREATIVE(4096, "creative");

    fun next(): Tier {
        return when (this) {
            MK1 -> MK2
            MK2 -> MK3
            MK3 -> MK4
            else -> error("no tier after $this")
        }
    }

    companion object {
        val VALUES = arrayOf(MK1, MK2, MK3, MK4)

        val ALL_VALUES = values()
    }
}