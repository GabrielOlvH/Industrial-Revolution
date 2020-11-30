package me.steven.indrev.utils

enum class TransferMode(val rgb: Long, val input: Boolean, val output: Boolean) {
    INPUT(0x997e75ff, true, false),
    OUTPUT(0x99ffb175, false, true),
    INPUT_OUTPUT(0x99d875ff, true, true),
    NONE(-1, false, false);

    fun next(): TransferMode = when (this) {
        INPUT -> OUTPUT
        OUTPUT -> INPUT_OUTPUT
        INPUT_OUTPUT -> NONE
        NONE -> INPUT
    }


    fun next(available: Array<out TransferMode>): TransferMode {
        var current = this
        for (i in values().indices) {
            val possible = current.next()
            if (available.contains(possible)) {
                return possible
            }
            current = possible
        }
        return this
    }
}