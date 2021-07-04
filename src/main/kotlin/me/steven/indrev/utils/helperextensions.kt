package me.steven.indrev.utils

import me.steven.indrev.api.IREntityExtension
import me.steven.indrev.inventories.IRInventory
import net.minecraft.entity.Entity
import net.minecraft.entity.effect.StatusEffectType
import net.minecraft.item.FoodComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.WeightedList
import net.minecraft.util.math.*
import net.minecraft.util.thread.ThreadExecutor
import net.minecraft.world.World
import java.util.concurrent.CompletableFuture

fun World.isLoaded(pos: BlockPos): Boolean {
    return chunkManager.isChunkLoaded(pos.x shr 4, pos.z shr 4)
}

fun <E> WeightedList<E>.pickRandom(): E {
    return this.shuffle().entries.first().element
}

fun FoodComponent.hasNegativeEffects(): Boolean {
    return statusEffects.any { it.first.effectType.type == StatusEffectType.HARMFUL }
}

inline fun Entity.redirectDrops(inv: IRInventory, run: () -> Unit) {
    this as IREntityExtension
    this.machineInv = inv
    run()
    this.machineInv = null
}

fun BlockPos.toVec3d() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())

fun <V> ThreadExecutor<*>.submitAndGet(task: () -> V): V {
    return (if (!this.isOnThread)
        CompletableFuture.supplyAsync(task, this)
    else
        CompletableFuture.completedFuture(task())).get()
}

inline fun Box.any(f: (Int, Int, Int) -> Boolean): Boolean {
    for (x in minX.toInt()..maxX.toInt())
        for (y in minY.toInt()..maxY.toInt())
            for (z in minZ.toInt()..maxZ.toInt())
                if (f(x, y, z)) return true
    return false
}

inline fun Box.forEach(f: (Int, Int, Int) -> Unit) {
    for (x in minX.toInt() until maxX.toInt())
        for (y in minY.toInt() until maxY.toInt())
            for (z in minZ.toInt() until maxZ.toInt())
                f(x, y, z)
}

inline fun <T> Box.map(f: (Int, Int, Int) -> T): MutableList<T> {
    val list = ArrayList<T>((xLength * yLength * zLength).toInt())
    for (x in minX.toInt() until maxX.toInt())
        for (y in minY.toInt() until maxY.toInt())
            for (z in minZ.toInt() until maxZ.toInt())
                list.add(f(x, y, z))
    return list
}

operator fun Box.contains(pos: BlockPos): Boolean {
    return contains(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
}

inline fun Box.firstOrNull(f: (Int, Int, Int) -> Boolean): BlockPos? {
    for (x in minX.toInt()..maxX.toInt())
        for (y in minY.toInt()..maxY.toInt())
            for (z in minZ.toInt()..maxZ.toInt())
                if (f(x, y, z)) return BlockPos(x, y, z)
    return null
}

operator fun Vec3i.component1() = x
operator fun Vec3i.component2() = y
operator fun Vec3i.component3() = z

operator fun Vec3d.component1() = this.x
operator fun Vec3d.component2() = this.y
operator fun Vec3d.component3() = this.z

operator fun Vec3f.component1() = this.x
operator fun Vec3f.component2() = this.y
operator fun Vec3f.component3() = this.z

operator fun ItemStack.component1(): ItemStack = this
operator fun ItemStack.component2(): Item = item

fun <T> Collection<T>.asMutableList(): MutableList<T> {
    return this as? MutableList<T> ?: ArrayList(this)
}
