package me.steven.indrev.transportation.networks

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

enum class ConnectionType(val id: Int) {
    NONE(-1),
    ROUND_ROBIN(0),
    NEAREST_FIRST(1),
    FARTHEST_FIRST(2);


    companion object {
        val BY_ID = Int2ObjectOpenHashMap<ConnectionType>()

        init {
            for (value in values()) {
                BY_ID[value.id] = value
            }
        }
    }
}