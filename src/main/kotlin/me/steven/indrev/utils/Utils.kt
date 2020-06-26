package me.steven.indrev.utils

import me.steven.indrev.IndustrialRevolution
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry


fun identifier(id: String) = Identifier(IndustrialRevolution.MOD_ID, id)

fun Identifier.block(block: Block): Identifier {
    Registry.register(Registry.BLOCK, this, block)
    return this
}

fun Identifier.fluid(fluid: Fluid): Identifier {
    Registry.register(Registry.FLUID, this, fluid)
    return this
}

fun Identifier.item(item: Item): Identifier {
    Registry.register(Registry.ITEM, this, item)
    return this
}

fun Identifier.blockEntityType(entityType: BlockEntityType<*>): Identifier {
    Registry.register(Registry.BLOCK_ENTITY_TYPE, this, entityType)
    return this
}

fun Identifier.tierBasedItem(vararg tiers: Tier = Tier.VALUES, itemSupplier: (Tier) -> Item) {
    tiers.forEach { tier ->
        val item = itemSupplier(tier)
        identifier("${this.path}_${tier.toString().toLowerCase()}").item(item)
    }
}

fun Box.isSide(vec3d: Vec3d) = vec3d.x == minX || vec3d.x == maxX - 1 || vec3d.y == minY || vec3d.y == maxY - 1 || vec3d.z == minZ || vec3d.z == maxZ - 1

fun itemSettings(): Item.Settings = Item.Settings().group(IndustrialRevolution.MOD_GROUP)

fun IntRange.toIntArray(): IntArray = this.map { it }.toIntArray()

fun BlockPos.toVec3d() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())

fun ChunkPos.asString() = "$x,$z"

fun getChunkPos(s: String): ChunkPos {
    val split = s.split(",")
    return ChunkPos(split[0].toInt(), split[1].toInt())
}
