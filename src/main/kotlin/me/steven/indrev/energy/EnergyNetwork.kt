package me.steven.indrev.energy

import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blocks.machine.CableBlock
import me.steven.indrev.mixin.AccessorEnergyHandler
import me.steven.indrev.utils.Tier
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.StringTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Direction
import net.minecraft.world.chunk.Chunk
import team.reborn.energy.Energy
import team.reborn.energy.EnergyHandler

@Suppress("CAST_NEVER_SUCCEEDS")
class EnergyNetwork(
    val world: ServerWorld,
    val cables: MutableSet<BlockPos> = hashSetOf(),
    val machines: MutableMap<BlockPos, MutableSet<Direction>> = hashMapOf()
) {
    var lastSenderSize = 0
    var lastReceiverSize = 0
    var tier = Tier.MK1

    fun tick(world: ServerWorld) {
        if (machines.isEmpty()) return
        val receiversHandlers = ArrayList<EnergyHandler>(lastReceiverSize)
        val senderHandlers = ArrayList<EnergyHandler>(lastSenderSize)
        val cachedChunks = hashMapOf<ChunkPos, Chunk>()
        machines.forEach { (pos, directions) ->
            if (!world.isChunkLoaded(pos)) return@forEach
            val chunk = cachedChunks.computeIfAbsent(ChunkPos(pos)) { world.getChunk(pos) }
            val blockEntity = chunk.getBlockEntity(pos) ?: return@forEach
            if (Energy.valid(blockEntity)) {
                directions.forEach { dir ->
                    val handler = Energy.of(blockEntity).side(dir)
                    if (handler.maxInput > 0)
                        receiversHandlers.add(handler)
                    if (handler.maxOutput > 0)
                        senderHandlers.add(handler)
                }
            }
        }

        lastReceiverSize = receiversHandlers.size
        lastSenderSize = senderHandlers.size

        if (senderHandlers.isEmpty() || receiversHandlers.isEmpty()) return

        val totalInput = receiversHandlers.sumByDouble { handler ->
            (handler.maxStored - handler.energy).coerceAtMost(handler.maxInput)
        }
        val totalEnergy = senderHandlers.sumByDouble { handler -> handler.energy.coerceAtMost(handler.maxOutput) }
        val senderIt = senderHandlers.iterator()
        val receiverIt = receiversHandlers.iterator()

        var sender = senderIt.next()
        var receiver = receiverIt.next()
        var sentThisTick = 0.0
        var receivedThisTick = 0.0

        while (true) {
            if ((sender as AccessorEnergyHandler).holder == (receiver as AccessorEnergyHandler).holder) {
                if (senderIt.hasNext())
                    sender = senderIt.next()
                else if (receiverIt.hasNext())
                    receiver = receiverIt.next()
                else break
                continue
            }
            val amount = ((receiver.maxInput / totalInput) * totalEnergy).coerceAtMost(tier.io)
            val moved = sender.into(receiver).move(amount)
            sentThisTick += moved
            receivedThisTick += moved
            if (sentThisTick >= sender.maxOutput) {
                if (!senderIt.hasNext()) break
                sentThisTick = 0.0
                sender = senderIt.next()
            }
            if (receivedThisTick >= receiver.maxInput || receivedThisTick >= amount) {
                if (!receiverIt.hasNext()) break
                sentThisTick = 0.0
                receiver = receiverIt.next()
            }
        }
    }

    fun remove() {
        val state = EnergyNetworkState.getNetworkState(world)
        state.networks.remove(this)
        cables.forEach { state.networksByPos.remove(it) }
    }

    fun appendCable(state: EnergyNetworkState, blockEntity: CableBlockEntity, blockPos: BlockPos) {
        tier = blockEntity.tier
        cables.add(blockPos)
        state.networksByPos[blockPos] = this
    }

    fun appendMachine(blockPos: BlockPos, direction: Direction) {
        machines.computeIfAbsent(blockPos) { hashSetOf() }.add(direction)
    }

    fun toTag(tag: CompoundTag) {
        val cablesList = ListTag()
        cables.forEach { pos ->
            cablesList.add(LongTag.of(pos.asLong()))
        }
        val machinesList = ListTag()
        machines.forEach { (pos, directions) ->
            val machineTag = CompoundTag()
            machineTag.putLong("pos", pos.asLong())
            val dirList = ListTag()
            directions.forEach { dir ->
                dirList.add(StringTag.of(dir.toString()))
            }
            machineTag.put("dir", dirList)
            machinesList.add(machineTag)
        }
        tag.put("cables", cablesList)
        tag.put("machines", machinesList)
        tag.putInt("tier", tier.ordinal)
    }

    companion object {
        fun handleBreak(world: ServerWorld, pos: BlockPos) {
            val state = EnergyNetworkState.getNetworkState(world)
            if (state.networksByPos.containsKey(pos))
                state.networksByPos[pos]?.remove()
            Direction.values().forEach {
                val offset = pos.offset(it)
                handleUpdate(world, offset)
            }
        }

        fun handleUpdate(world: ServerWorld, pos: BlockPos) {
            val state = EnergyNetworkState.getNetworkState(world)
            if (state.networksByPos.containsKey(pos))
                state.networksByPos[pos]?.remove()
            val network = EnergyNetwork(world)
            state.networks.add(network)
            val scanned = hashSetOf<BlockPos>()
            Direction.values().forEach { dir ->
                buildNetwork(scanned, state, network, world.getChunk(pos), world, pos, pos, dir)
            }
            if (network.machines.isEmpty() || network.cables.isEmpty())
                network.remove()
            state.markDirty()
        }

        private fun buildNetwork(scanned: MutableSet<BlockPos>, state: EnergyNetworkState, network: EnergyNetwork, chunk: Chunk, world: ServerWorld, blockPos: BlockPos, source: BlockPos, direction: Direction) {
            if (network.machines.containsKey(blockPos)) {
                network.appendMachine(blockPos, direction.opposite)
                return
            }
            if (blockPos != source && !scanned.add(blockPos)) return
            val blockEntity = chunk.getBlockEntity(blockPos) ?: return
            if (blockEntity is CableBlockEntity) {
                if (state.networksByPos.containsKey(blockPos)) {
                    val oldNetwork = state.networksByPos[blockPos]
                    if (state.networks.contains(oldNetwork) && oldNetwork != network) {
                        oldNetwork?.remove()
                    }
                }
                val blockState = chunk.getBlockState(blockPos)
                Direction.values().forEach { dir ->
                    if (blockState[CableBlock.getProperty(dir)]) {
                        val nPos = blockPos.offset(dir)
                        if (nPos.x shr 4 == chunk.pos.x && nPos.z shr 4 == chunk.pos.z)
                            buildNetwork(scanned, state, network, chunk, world, nPos, source, dir)
                        else
                            buildNetwork(scanned, state, network, world.getChunk(nPos), world, nPos, source, dir)
                    }
                }
                if (blockState[CableBlock.getProperty(direction)])
                    network.appendCable(state, blockEntity, blockPos.toImmutable())
            } else if (Energy.valid(blockEntity)) {
                network.appendMachine(blockPos, direction.opposite)
            }
        }

        fun fromTag(world: ServerWorld, tag: CompoundTag): EnergyNetwork {
            val cablesList = tag.getList("cables", 4)
            val machinesList = tag.getList("machines", 10)
            val network = EnergyNetwork(world)
            cablesList.forEach { cableTag ->
                cableTag as LongTag
                network.cables.add(BlockPos.fromLong(cableTag.long).toImmutable())
            }
            machinesList.forEach { machineTag ->
                machineTag as CompoundTag
                val posLong = machineTag.getLong("pos")
                val pos = BlockPos.fromLong(posLong)
                val dirList = machineTag.getList("dir", 8)
                val directions = hashSetOf<Direction>()
                dirList.forEach { dirTag ->
                    dirTag as StringTag
                    val dir = Direction.valueOf(dirTag.asString().toUpperCase())
                    directions.add(dir)
                }
                network.machines[pos] = directions
            }
            val tier = Tier.values()[tag.getInt("tier")]
            network.tier = tier
            return network
        }
    }
}