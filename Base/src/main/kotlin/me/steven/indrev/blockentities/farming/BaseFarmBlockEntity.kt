package me.steven.indrev.blockentities.farming

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.items.RangeCardItem
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import kotlin.math.pow

abstract class BaseFarmBlockEntity<T : MachineConfig>(type: BlockEntityType<*>, pos: BlockPos, state: BlockState)
    : MachineBlockEntity<T>(type, pos, state) {

    var renderWorkingArea = false

    fun getRange(): Int {
        return (inventory[0].resource.item as? RangeCardItem)?.range ?: 0
    }

    open fun getRenderColor() = 0xFF0000

    fun getActionCount() = (2.0.pow(tier.ordinal) + upgrades.getSpeedMultiplier() * 2).toInt()

    open fun getArea(): Box {
        val range = getRange()
        return Box(pos.offset(cachedState[MachineBlock.FACING].opposite, range + 1))
            .expand(range.toDouble(), 0.0, range.toDouble())
    }
}