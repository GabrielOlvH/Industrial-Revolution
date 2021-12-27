package me.steven.indrev.components

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.utils.identifier
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.Identifier
import net.minecraft.world.World

/**
 * Used for some internals, allows providing components without spamming interfaces
 * for example: a Solar Power Plant Smelter has a TemperatureComponent however it is not a child of MachineBlockEntity.
 */
class ComponentKey<T>(val id: Identifier) {

    @Suppress("UNCHECKED_CAST")
    fun get(provider: ComponentProvider): T? = provider.get(this) as T?

    @Suppress("UNCHECKED_CAST")
    fun get(provider: BlockEntity): T? = (provider as? ComponentProvider ?: error("$provider is not a ComponentProvider")).get(this) as T?

    companion object {
        val FLUID = ComponentKey<FluidComponent>(identifier("fluid"))
        val ITEM = ComponentKey<InventoryComponent>(identifier("item"))
        val TEMPERATURE = ComponentKey<TemperatureComponent>(identifier("temperature"))
        val MULTIBLOCK = ComponentKey<MultiBlockComponent>(identifier("multiblock"))
        val ENHANCER = ComponentKey<EnhancerComponent>(identifier("enhancer"))
        val WORLD_OBJECT = ComponentKey<World>(identifier("world"))
        val GUI_SYNCABLE = ComponentKey<GuiSyncableComponent>(identifier("gui_syncable"))
    }
}