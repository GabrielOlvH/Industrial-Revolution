package me.steven.indrev.events.common

import me.steven.indrev.mixin.RecipeManagerAccessor
import me.steven.indrev.recipes.MachineRecipeType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.resource.LifecycledResourceManager
import net.minecraft.server.MinecraftServer

object DatapackReloadEvent : ServerLifecycleEvents.EndDataPackReload {
    override fun endDataPackReload(
        server: MinecraftServer,
        resourceManager: LifecycledResourceManager?,
        success: Boolean
    ) {
        (server.recipeManager as RecipeManagerAccessor).recipes.keys.filterIsInstance<MachineRecipeType>()
            .forEach { it.provider.clearCache() }
    }

}