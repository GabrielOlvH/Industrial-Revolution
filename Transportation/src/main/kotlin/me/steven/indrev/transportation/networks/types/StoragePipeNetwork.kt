package me.steven.indrev.transportation.networks.types

import me.steven.indrev.transportation.blocks.PipeBlockEntity
import me.steven.indrev.transportation.networks.ConnectionType
import me.steven.indrev.transportation.networks.Path
import me.steven.indrev.transportation.utils.nested
import me.steven.indrev.transportation.utils.transaction
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class StoragePipeNetwork<T>(world: ServerWorld) : PipeNetwork<Storage<T>>(world) {

    private val mutablePos = BlockPos.Mutable()

    open fun onMove(resource: T, path: Path) {
    }

    override fun tick() {
        super.tick()
        if (ticks % 20 != 0) return

        tickingPositions.forEach { pos ->
            val blockEntity = world.getBlockEntity(pos) as? PipeBlockEntity ?: return@forEach
            blockEntity.forEachDirection { direction, type ->
                val offset = pos.offset(direction)
                val paths = getPathsFrom(offset)
                if (paths.isEmpty()) return@forEachDirection
                if (isValidStorage(world, offset, direction.opposite)) {
                    when (type) {
                        ConnectionType.ROUND_ROBIN -> tickRoundRobin(offset, direction.opposite, paths)

                        ConnectionType.NEAREST_FIRST -> tickNearestFirst(offset, direction.opposite, paths)

                        ConnectionType.FARTHEST_FIRST -> tickFarthestFirst(offset, direction.opposite, paths)

                        else -> {}
                    }
                }
            }
        }
        tickingPositions.clear()
    }

    private fun doMove(tx: Transaction, view: StorageView<T>, resource: T, amount: Long, path: Path): Long {
        val dest = path.nodes[0]
        val destNode = nodes[dest]
        val destDir = destNode.connections.getDirections()[0]
        mutablePos.set(dest)
        val destStorage = find(world, mutablePos, destDir) ?: return 0
        return move(tx, view, destStorage, resource, path, amount)
    }

    private fun tickRoundRobin(pos: BlockPos, direction: Direction, paths: List<Path>) {
        transaction { tx ->
            val storage = find(world, pos, direction) ?: return
            var remaining = maximumTransferable
            val it = storage.iterator()
            while (it.hasNext() && remaining > 0) {
                val view = it.next()
                if (view.isResourceBlank || view.amount == 0L) continue
                val resource = view.resource
                val amount = (view.amount.coerceAtMost(remaining) / paths.size).coerceAtLeast(minimumTransferable)
                paths.forEach { path ->
                    if (remaining > 0)
                        remaining -= doMove(tx, view, resource, amount, path)
                  /*  val dest = path.nodes[0]
                    val destNode = nodes[dest]
                    val destDir = destNode.connections.getDirections()[0]
                    mutablePos.set(dest)
                    val destStorage = find(world, mutablePos, destDir) ?: return@forEach
                    remaining -= move(tx, view, destStorage, resource, path, amount)*/
                }
            }
            tx.commit()
        }
    }

    private fun tickNearestFirst(pos: BlockPos, direction: Direction, paths: List<Path>) {
        transaction { tx ->
            val storage = find(world, pos, direction) ?: return
            var remaining = maximumTransferable
            val it = storage.iterator()
            while (it.hasNext() && remaining > 0) {
                val view = it.next()
                if (view.isResourceBlank || view.amount == 0L) continue
                val resource = view.resource
                val amount = (view.amount.coerceAtMost(remaining)).coerceAtLeast(minimumTransferable)

                paths.sortedBy { it.dist }.forEach { path ->
                    if (remaining > 0)
                        remaining -= doMove(tx, view, resource, amount, path)
                }
            }
            tx.commit()
        }
    }

    private fun tickFarthestFirst(pos: BlockPos, direction: Direction, paths: List<Path>) {
        transaction { tx ->
            val storage = find(world, pos, direction) ?: return
            var remaining = maximumTransferable
            val it = storage.iterator()
            while (it.hasNext() && remaining > 0) {
                val view = it.next()
                if (view.isResourceBlank || view.amount == 0L) continue
                val resource = view.resource
                val amount = (view.amount.coerceAtMost(remaining)).coerceAtLeast(minimumTransferable)

                paths.sortedByDescending { it.dist }.forEach { path ->
                    if (remaining > 0)
                        remaining -= doMove(tx, view, resource, amount, path)
                }
            }
            tx.commit()
        }
    }

    private fun move(tx: Transaction, view: StorageView<T>, destStorage: Storage<T>, resource: T, path: Path, amount: Long): Long {
        val transferredAmount = tx.nested { nestedTx ->
            val extracted = view.extract(resource, amount, nestedTx)
            val inserted = destStorage.insert(resource, extracted, nestedTx)
            extracted.coerceAtMost(inserted)
        }
        if (transferredAmount > 0) {
            tx.nested { nestedTx ->
                val extracted = view.extract(resource, transferredAmount, nestedTx)
                val inserted = destStorage.insert(resource, transferredAmount, nestedTx)
                if (inserted == extracted) {
                    onMove(resource, path)
                    nestedTx.commit()
                    return inserted
                }
            }
        }

        return 0L
    }

}