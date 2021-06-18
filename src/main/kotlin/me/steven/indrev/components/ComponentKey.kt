package me.steven.indrev.components

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.utils.identifier
import net.minecraft.util.Identifier

class ComponentKey<T>(val id: Identifier) {

    @Suppress("UNCHECKED_CAST")
    fun get(provider: ComponentProvider): T? = provider.get(this) as T?

    companion object {
        val FLUID = ComponentKey<FluidComponent>(identifier("fluid"))
        val ITEM = ComponentKey<InventoryComponent>(identifier("item"))
        val TEMPERATURE = ComponentKey<TemperatureComponent>(identifier("temperature"))
        val MULTIBLOCK = ComponentKey<MultiBlockComponent>(identifier("multiblock"))
        val PROPERTY_HOLDER = ComponentKey<PropertyDelegateHolder>(identifier("property_holder"))
    }
}