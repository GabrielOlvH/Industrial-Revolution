package me.steven.indrev.networks.energy

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.pipes.CableBlock
import me.steven.indrev.config.IRConfig
import me.steven.indrev.networks.Network
import me.steven.indrev.utils.energyOf
import me.steven.indrev.utils.insert
import me.steven.indrev.utils.isLoaded
import net.minecraft.block.Block
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import team.reborn.energy.api.EnergyStorage
import java.util.*

open class EnergyNetwork(
    world: ServerWorld,
    val cables: MutableSet<BlockPos> = ObjectOpenHashSet(),
    val machines: MutableMap<BlockPos, EnumSet<Direction>> = Object2ObjectOpenHashMap()
) : Network(Type.ENERGY, world, cables, machines) {

    var tier = Tier.MK1

    val maxCableTransfer: Long
        get() = when (tier) {
            Tier.MK1 -> IRConfig.cables.cableMk1
            Tier.MK2 -> IRConfig.cables.cableMk2
            Tier.MK3 -> IRConfig.cables.cableMk3
            else -> IRConfig.cables.cableMk4
        }.toLong()

    val insertables = ObjectOpenHashSet<BlockPos>()

    var energy = 0L
    val capacity: Long get() = pipes.size * maxCableTransfer

    override fun tick(world: ServerWorld) {
        val totalInput = insertables.sumOf { pos ->
            if (world.isLoaded(pos)) {
                machines[pos]?.sumOf { dir ->
                    val handler = energyOf(world, pos, dir) ?: return@sumOf 0L
                    if (handler.supportsInsertion()) handler.maxInput
                    else 0L
                } ?: 0
            } else 0L
        }
        if (totalInput <= 0) return
        var remainders = 0.0
        insertables.forEach outer@{ pos ->
            machines[pos]!!.forEach { direction ->
                if (!world.isLoaded(pos)) return@forEach
                val energyIo = energyOf(world, pos, direction)?: return@forEach
                if (!energyIo.supportsInsertion()) return@forEach

                val maxInput = energyIo.maxInput
                val toTransfer = ((maxInput / totalInput.toDouble()) * energy + remainders).toLong().coerceAtMost(energy)
                try  {
                    val transferred = energyIo.insert(toTransfer, true)

                    energy -= transferred

                    remainders += toTransfer - transferred
                } catch (e: Exception){
                    println("toTransfer: $toTransfer")
                    println("current: $energy")
                    println("target: $energyIo @ $pos {${world.getBlockEntity(pos)}}")
                    throw e
                }
            }
        }
    }

    override fun appendPipe(block: Block, blockPos: BlockPos) {
        val cable = block as? CableBlock ?: return
        this.tier = cable.tier
        super.appendPipe(block, blockPos)
    }

    companion object {

        private const val MAX_VALUE = (Long.MAX_VALUE - 1)

        private val EnergyStorage.maxInput: Long
            get() = insert(MAX_VALUE, false)
    }
}