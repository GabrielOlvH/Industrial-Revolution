package me.steven.indrev.api

interface IRServerPlayerEntityExtension : IRPlayerEntityExtension {
    fun indrev_shouldSync(): Boolean
    fun indrev_sync()
}