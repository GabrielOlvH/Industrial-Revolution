package me.steven.indrev.blockentities.farming

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.items.RangeCardItem
import me.steven.indrev.utils.tx
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
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

    protected fun insertLoot(state: BlockState, pos: BlockPos, stack: ItemStack, outSlots: IntArray) {
        val ctx = LootContext.Builder(world as ServerWorld)
            .parameter(LootContextParameters.BLOCK_STATE, state)
            .parameter(LootContextParameters.ORIGIN, Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()))
            .parameter(LootContextParameters.TOOL, stack)
            .build(LootContextTypes.BLOCK)
        tx { tx ->
            world!!.server!!.lootManager.getTable(state.block.lootTableId).generateLoot(ctx) { stack ->
                val inserted = inventory.insert(outSlots, ItemVariant.of(stack), stack.count.toLong(), tx)
                if (inserted < stack.count) {
                    stack.decrement(inserted.toInt())
                    ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack)
                }
            }
            tx.commit()
        }
    }
}