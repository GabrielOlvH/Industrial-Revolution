package me.steven.indrev.networks.energy

import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import dev.technici4n.fasttransferlib.api.energy.EnergyMovement
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap
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
    private val maxCableTransfer: Double
        get() = when (tier) {
            Tier.MK1 -> IRConfig.cables.cableMk1
            Tier.MK2 -> IRConfig.cables.cableMk2
            Tier.MK3 -> IRConfig.cables.cableMk3
            else -> IRConfig.cables.cableMk4
        }

    var deques = Object2ObjectOpenHashMap<BlockPos, ReusableArrayDeque<Node>>()

    override fun tick(world: ServerWorld) {
        if (machines.isEmpty()) return
        else if (queue.isEmpty()) {
            buildQueue()
        }
        if (queue.isNotEmpty()) {
            val remainingInputs = Long2DoubleOpenHashMap()
            machines.forEach { (pos, directions) ->
                var q = deques[pos]
                if (q == null) {
                    q = ReusableArrayDeque(queue[pos] ?: return@forEach)
                    deques[pos] = q
                }
                if (!world.isLoaded(pos)) return@forEach
                directions.forEach inner@{ dir ->
                    val energyIo = energyOf(world, pos, dir.opposite) ?: return@inner
                    var remaining = energyIo.maxOutput

                    while (q.isNotEmpty() && energyIo.supportsExtraction() && remaining > 1e-9) {
                        val (_, targetPos, _, targetDir) = q.removeFirst()
                        if (!world.isLoaded(targetPos)) continue
                        val target = energyOf(world, targetPos, targetDir.opposite) ?: continue
                        if (!target.supportsInsertion()) continue
                        val maxInput = remainingInputs.computeIfAbsent(targetPos.asLong()) { target.maxInput }
                        if (maxInput < 1e-9) continue

                        val amount = remaining.coerceAtMost(maxInput).coerceAtMost(maxCableTransfer)
                        val before = target.energy
                        val moved = EnergyMovement.move(energyIo, target, amount)
                        val after = target.energy
                        if (moved > 1e-9 && (after - before).absoluteValue > 1e-9) {
                            remaining -= moved
                            remainingInputs.addTo(targetPos.asLong(), -moved)
                        }
                    }

                    q.resetHead()
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

        private const val MAX_VALUE = (Integer.MAX_VALUE - 1).toDouble()

        private val EnergyIo.maxInput: Double
            get() = MAX_VALUE - insert(MAX_VALUE, Simulation.SIMULATE)
        private val EnergyIo.maxOutput: Double
            get() = extract(MAX_VALUE, Simulation.SIMULATE)
    }
}