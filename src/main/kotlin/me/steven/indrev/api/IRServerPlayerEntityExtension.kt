package me.steven.indrev.api

interface IRServerPlayerEntityExtension : IRPlayerEntityExtension {
    fun shouldSync(): Boolean
    fun sync()
}