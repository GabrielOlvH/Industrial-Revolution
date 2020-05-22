package me.steven.indrev.items

enum class Upgrade(val modifier: Double, val maxCount: Int) {
    SPEED(1.2, 4),
    ENERGY(1.2, 4),
    BUFFER(2.0, 4);

    companion object {
        val ALL = arrayOf(SPEED, ENERGY, BUFFER)
    }
}