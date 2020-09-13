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
    val cables: MutableSet<BlockPos> = mutableSetOf(),
    val machines: MutableMap<BlockPos, MutableSet<Direction>> = mutableMapOf()
) {
    var tier =  Tier.MK1

    fun tick(world: ServerWorld) {
        if (machines.isEmpty()) return
        val receiversHandlers = mutableSetOf<EnergyHandler>()
        val senderHandlers = mutableSetOf<EnergyHandler>()
        val cachedChunks = mutableMapOf<ChunkPos, Chunk>()
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

        fun updateBlock(world: ServerWorld, pos: BlockPos, isRemoved: Boolean, tier: Tier) {
            world.profiler.push("indrev_networkUpdate")
            val state = EnergyNetworkState.getNetworkState(world)
            if (state.networksByPos.containsKey(pos))
                state.networksByPos[pos]?.remove()
            if (isRemoved) {
                Direction.values().forEach {
                    val offset = pos.offset(it)
                    updateBlock(world, offset, false, tier)
                }
            } else {
                val network = EnergyNetwork(world)
                state.networks.add(network)
                val scanned = mutableSetOf<BlockPos>()
                Direction.values().forEach { dir ->
                    //val offset = pos.offset(dir)
                    //search(scanned, state, network, world.getChunk(offset), world, offset, dir)
                    search(scanned, state, network, world.getChunk(pos), world, pos, pos, dir)
                }
                if (network.machines.isEmpty() || network.cables.isEmpty())
                    network.remove()
            }
            state.markDirty()
            world.profiler.pop()
        }

        private fun search(scanned: MutableSet<BlockPos>, state: EnergyNetworkState, network: EnergyNetwork, chunk: Chunk, world: ServerWorld, blockPos: BlockPos, source: BlockPos, direction: Direction) {
            if (network.machines.containsKey(blockPos)) {
                network.machines.computeIfAbsent(blockPos) { mutableSetOf() }.add(direction.opposite)
                return
            }
            if (blockPos != source && !scanned.add(blockPos)) return
            val blockEntity = chunk.getBlockEntity(blockPos) ?: return
            if (Energy.valid(blockEntity) || blockEntity is CableBlockEntity) {
                if (state.networksByPos.containsKey(blockPos)) {
                    val oldNetwork = state.networksByPos[blockPos]
                    if (state.networks.contains(oldNetwork) && oldNetwork != network) {
                        oldNetwork?.remove()
                    }
                }
                if (blockEntity is CableBlockEntity) {
                    val blockState = chunk.getBlockState(blockPos)
                    if (blockState[CableBlock.getProperty(direction.opposite)]) {
                        Direction.values().forEach { dir ->
                            val nPos = blockPos.offset(dir)
                            if (nPos.x shr 4 == chunk.pos.x && nPos.z shr 4 == chunk.pos.z)
                                search(scanned, state, network, chunk, world, nPos, source, dir)
                            else
                                search(scanned, state, network, world.getChunk(nPos), world, nPos, source, dir)
                        }
                        network.tier = blockEntity.tier
                        network.cables.add(blockPos)
                        state.networksByPos[blockPos] = network
                    }
                } else {
                    network.machines.computeIfAbsent(blockPos) { mutableSetOf() }.add(direction.opposite)
                }
            }
        }

        fun fromTag(world: ServerWorld, tag: CompoundTag): EnergyNetwork {
            val cablesList = tag.getList("cables", 4)
            val machinesList = tag.getList("machines", 10)
            val network = EnergyNetwork(world)
            cablesList.forEach { cableTag ->
                cableTag as LongTag
                network.cables.add(BlockPos.fromLong(cableTag.long))
            }
            machinesList.forEach { machineTag ->
                machineTag as CompoundTag
                val posLong = machineTag.getLong("pos")
                val pos = BlockPos.fromLong(posLong)
                val dirList = machineTag.getList("dir", 8)
                val directions = mutableSetOf<Direction>()
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