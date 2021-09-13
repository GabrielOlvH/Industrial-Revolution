package me.steven.indrev.networks.energy

import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import dev.technici4n.fasttransferlib.api.energy.EnergyMovement
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.pipes.CableBlock
import me.steven.indrev.config.IRConfig
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.Node
import me.steven.indrev.utils.ReusableArrayDeque
import me.steven.indrev.utils.energyOf
import me.steven.indrev.utils.isLoaded
import net.minecraft.block.Block
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.*
import kotlin.math.absoluteValue

open class EnergyNetwork(
    world: ServerWorld,
    val cables: MutableSet<BlockPos> = ObjectOpenHashSet(),
    val machines: MutableMap<BlockPos, EnumSet<Direction>> = Object2ObjectOpenHashMap()
) : Network(Type.ENERGY, world, cables, machines) {

    var tier = Tier.MK1

    val maxCableTransfer: Double
        get() = when (tier) {
            Tier.MK1 -> IRConfig.cables.cableMk1
            Tier.MK2 -> IRConfig.cables.cableMk2
            Tier.MK3 -> IRConfig.cables.cableMk3
            else -> IRConfig.cables.cableMk4
        }

    val insertables = ObjectOpenHashSet<BlockPos>()

    var energy = 0.0
    val capacity: Double get() = pipes.size * maxCableTransfer

    override fun tick(world: ServerWorld) {
        val totalInput = insertables.sumOf { pos ->
            if (world.isLoaded(pos))
                energyOf(world, pos, machines[pos]!!.first())?.maxInput ?: 0.0
            else 0.0
        }
        if (totalInput <= 0) return
        var remainders = 0.0
        insertables.forEachIndexed { index, pos ->
            machines[pos]!!.forEach { direction ->
                if (!world.isLoaded(pos)) return@forEach
                val energyIo = energyOf(world, pos, direction)?: return@forEach
                var leftoverToInsert = remainders / (insertables.size - index)
                if (leftoverToInsert < 1e-9) // to small to split
                    leftoverToInsert = remainders

                remainders -= leftoverToInsert

                val toTransfer = (energyIo.maxInput / totalInput) * energy + leftoverToInsert
                val leftover = energyIo.insert(toTransfer, Simulation.ACT)

                energy -= toTransfer - leftover

                remainders += leftover

                if (remainders < 1e-9) remainders = 0.0
            }
        }
    }

    override fun appendPipe(block: Block, blockPos: BlockPos) {
        val cable = block as? CableBlock ?: return
        this.tier = cable.tier
        super.appendPipe(block, blockPos)
    }

    companion object {

        private const val MAX_VALUE = (Integer.MAX_VALUE - 1).toDouble()

        private val EnergyIo.maxInput: Double
            get() = MAX_VALUE - insert(MAX_VALUE, Simulation.SIMULATE)
    }
}