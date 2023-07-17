package me.steven.indrev.blockentities.farming

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.items.RangeCardItem
import me.steven.indrev.utils.Troubleshooter
import me.steven.indrev.utils.tx
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameterSet
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
    var troubleshooter: Troubleshooter = properties.sync(Troubleshooter(TROUBLESHOOTER_CODE_ID))

    protected fun inventoryUpdate() {
        markDirty()
        if (troubleshooter.contains(Troubleshooter.NO_SPACE) && inventory.parts.all { it.amount <= it.capacity })
            troubleshooter.solve(Troubleshooter.NO_SPACE)
    }
    fun getRange(): Int {
        val r = (inventory[0].resource.item as? RangeCardItem)?.range ?: 0
        troubleshooter.test(Troubleshooter.NO_RANGE, r != 0)
        return r
    }

    open fun getRenderColor() = 0xFF0000

    fun getActionCount() = (2.0.pow(tier.ordinal) + upgrades.getSpeedMultiplier() * 2).toInt()

    open fun getArea(): Box {
        val range = getRange()
        return Box(pos.offset(cachedState[MachineBlock.FACING].opposite, range + 1))
            .expand(range.toDouble(), 0.0, range.toDouble())
    }

    protected fun insertLoot(state: BlockState, pos: BlockPos, stack: ItemStack, outSlots: IntArray) {
        tx { tx ->
            world!!.server!!.lootManager.getLootTable(state.block.lootTableId).generateLoot(
                LootContextParameterSet.Builder(world as ServerWorld)
                    .add(LootContextParameters.BLOCK_STATE, state)
                    .add(LootContextParameters.ORIGIN, Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()))
                    .add(LootContextParameters.TOOL, stack)
                    .build(LootContextTypes.BLOCK)
            ) { stack ->
                val inserted = inventory.insert(outSlots, ItemVariant.of(stack), stack.count.toLong(), tx)
                if (inserted < stack.count) {
                    val overflowed = inventory.insertOverflow(outSlots, ItemVariant.of(stack), stack.count.toLong() - inserted, tx)
                    if (overflowed)
                        troubleshooter.offer(Troubleshooter.NO_SPACE)
                }
            }
            tx.commit()
        }
    }

    companion object {
        const val TROUBLESHOOTER_CODE_ID = 3
    }
}