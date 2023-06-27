package me.steven.indrev.transportation.networks.types

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.steven.indrev.transportation.utils.PipeConnections
import me.steven.indrev.transportation.utils.nested
import me.steven.indrev.transportation.utils.transaction
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage
import java.util.function.LongFunction

class EnergyPipeNetwork(world: ServerWorld) : PipeNetwork<EnergyStorage>(world) {

    private val mutablePos = BlockPos.Mutable()

    override val minimumTransferable: Long = 1L
    override val maximumTransferable: Long = 8L

    private val insertablePositions = Long2ObjectOpenHashMap<PipeConnections>()
    private val extractablePositions = Long2ObjectOpenHashMap<PipeConnections>()

    private val insertables = Object2LongOpenHashMap<EnergyStorage>()
    private val extractables = Object2LongOpenHashMap<EnergyStorage>()

    override fun tick() {
        super.tick()

        findExtractablesAndInsertables()

        if (extractablePositions.isEmpty() || insertablePositions.isEmpty()) return

        var totalOut = 0.0
        var totalIn = 0.0

        transaction { tx ->
            extractablePositions.long2ObjectEntrySet().fastForEach { (longPos, connections) ->
                connections.forEach { dir ->
                    val storage = find(world, mutablePos.set(longPos), dir) ?: return@forEach
                    val extracted = storage.extract(maximumTransferable, tx)
                    if (extracted > 0)
                        extractables.put(storage, extracted)
                    totalOut += extracted
                }
            }

            insertablePositions.long2ObjectEntrySet().fastForEach { (longPos, connections) ->
                connections.forEach { dir ->
                    val storage = find(world, mutablePos.set(longPos), dir) ?: return@forEach
                    val inserted = storage.insert(maximumTransferable, tx)
                    if (inserted > 0)
                    insertables.put(storage, inserted)
                    totalIn += inserted
                }
            }
        }

        if (insertables.isEmpty() || extractables.isEmpty()) return

        val extractableIt = extractables.object2LongEntrySet().fastIterator()
        var extractable = extractableIt.next()
        var remOut = extractable.longValue

        val insertableIt = insertables.object2LongEntrySet().fastIterator()
        var insertable = insertableIt.next()
        var remIn = ((insertable.longValue / totalIn) * totalOut).toLong()

        transaction { tx ->
            do {
                if (remIn <= 0) {
                    if (!insertableIt.hasNext()) break
                    insertable = insertableIt.next()
                    remIn = ((insertable.longValue / totalIn) * totalOut).toLong()
                }
                if (remOut <= 0) {
                    if (!extractableIt.hasNext()) break
                    extractable = extractableIt.next()
                    remOut = extractable.longValue
                }

                val actuallyMoved = tx.nested { nestedTx ->
                    val inserted = insertable.key.insert(remIn, nestedTx)
                    if (inserted == 0L) {
                        remIn = 0
                    }
                    val extracted = extractable.key.extract(inserted.coerceAtMost(remOut), nestedTx)
                    extracted
                }

                tx.nested { nestedTx ->
                    val inserted = insertable.key.insert(actuallyMoved, nestedTx)
                    val extracted = extractable.key.extract(actuallyMoved, nestedTx)

                    if (inserted == extracted) {
                        remIn -= inserted
                        remOut -= extracted
                        nestedTx.commit()
                    }
                }
            } while (true)

            tx.commit()
        }

        insertables.clear()
        extractables.clear()
    }

    private fun findExtractablesAndInsertables() {
        if (insertablePositions.isEmpty()) {
            containers.forEach { longPos ->
                var directions = PipeConnections()
                nodes.get(longPos).connections.forEach { dir ->
                    if (find(world, mutablePos.set(longPos), dir)?.supportsInsertion() == true) directions = directions.with(dir)
                }
                if (directions.value != 0) insertablePositions[longPos] = directions
            }
        }

        if (extractablePositions.isEmpty()) {
            containers.forEach { longPos ->
                var directions = PipeConnections()
                nodes.get(longPos).connections.forEach { dir ->
                    if (find(world, mutablePos.set(longPos), dir)?.supportsExtraction() == true) directions = directions.with(dir)
                }
                if (directions.value != 0) extractablePositions[longPos] = directions
            }
        }
    }

    override fun find(world: ServerWorld, pos: BlockPos, direction: Direction): EnergyStorage? {
        return apiCache.computeIfAbsent(pos.asLong(), LongFunction {
            BlockApiCache.create(EnergyStorage.SIDED, world, pos)
        }).find(direction)
    }
}