package me.steven.indrev.api.sideconfigs

import me.steven.indrev.utils.TransferMode
import net.minecraft.block.BlockState
import net.minecraft.util.math.Direction

interface Configurable {
    fun isConfigurable(type: ConfigurationType): Boolean
    fun isFixed(type: ConfigurationType): Boolean
    fun getValidConfigurations(type: ConfigurationType): Array<TransferMode>
    fun getCurrentConfiguration(type: ConfigurationType): MutableMap<Direction, TransferMode>
    fun applyDefault(state: BlockState, type: ConfigurationType, configuration: MutableMap<Direction, TransferMode>)
}