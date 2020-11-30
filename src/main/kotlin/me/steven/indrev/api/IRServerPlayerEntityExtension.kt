package me.steven.indrev.api

interface IRServerPlayerEntityExtension : IRPlayerEntityExtension {
    fun regenerateShield()
    fun applyDamageToShield(damage: Double): Double

    fun shouldSync(): Boolean
    fun sync()
}