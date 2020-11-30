package me.steven.indrev.api

import me.steven.indrev.tools.modular.ArmorModule

interface IRPlayerEntityExtension {

    var shieldDurability: Double

    fun getMaxShieldDurability(): Double

    fun getAppliedModules(): Map<ArmorModule, Int>
    fun applyModule(module: ArmorModule, level: Int)
    fun isApplied(module: ArmorModule): Boolean
    fun getAppliedLevel(module: ArmorModule): Int
}