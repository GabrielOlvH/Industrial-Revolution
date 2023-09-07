package me.steven.indrev.utils

import com.mojang.blaze3d.systems.RenderSystem
import me.steven.indrev.MOD_ID
import me.steven.indrev.items.ALL_ITEMS
import me.steven.indrev.recipes.MachineRecipeSerializer
import me.steven.indrev.recipes.MachineRecipeType
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.tag.BlockTags
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import org.joml.Matrix4f
import kotlin.math.ceil

const val INPUT_COLOR = 0xFFAAAAFF.toInt()
const val OUTPUT_COLOR = 0xFFFFD4A8.toInt()

internal fun identifier(path: String) = Identifier(MOD_ID, path)

fun blockSpriteId(id: String) = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier(id))

fun item(): Item = Item(itemSettings())

fun itemSettings(): FabricItemSettings = FabricItemSettings()

fun blockSettings(): FabricBlockSettings = FabricBlockSettings.create()

fun Identifier.register(block: Block, item: Item, blockEntityType: BlockEntityType<*>): Identifier {
    return block(block).item(item).blockEntityType(blockEntityType)
}

fun Identifier.block(block: Block): Identifier {
    Registry.register(Registries.BLOCK, this, block)
    return this
}

fun Identifier.fluid(fluid: Fluid): Identifier {
    Registry.register(Registries.FLUID, this, fluid)
    return this
}

fun Identifier.item(item: Item): Identifier {
    Registry.register(Registries.ITEM, this, item)
    ALL_ITEMS.add(item)
    return this
}

fun Identifier.blockEntityType(entityType: BlockEntityType<*>): Identifier {
    Registry.register(Registries.BLOCK_ENTITY_TYPE, this, entityType)
    return this
}

fun Identifier.screenHandlerType(type: ScreenHandlerType<*>): Identifier {
    Registry.register(Registries.SCREEN_HANDLER, this, type)
    return this
}

fun Identifier.createRecipeType(type: MachineRecipeType): MachineRecipeType {
    Registry.register(Registries.RECIPE_TYPE, this, type)
    Registry.register(Registries.RECIPE_SERIALIZER, this, MachineRecipeSerializer())
    return type
}

operator fun Int.component1() = this shr 24 and 0x000000FF

operator fun Int.component2() = this shr 16 and 0x000000FF

operator fun Int.component3() = this shr 8 and 0x000000FF

operator fun Int.component4() = this and 0x000000FF

fun renderInGui(matrices: MatrixStack, resource: FluidVariant, amt: Long, max: Long, x: Int, y: Int, width: Int, height: Int) {
    val client = MinecraftClient.getInstance()
    RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
    RenderSystem.setShader(GameRenderer::getPositionColorTexProgram)
    val fluidRender = FluidRenderHandlerRegistry.INSTANCE.get(resource.fluid) ?: return
    val fluidColor = fluidRender.getFluidColor(client.world, client.player!!.blockPos, resource.fluid.defaultState)
    val sprite = fluidRender.getFluidSprites(client.world, BlockPos.ORIGIN, resource.fluid.defaultState)[0]
    val r = (fluidColor shr 16 and 255) / 255f
    val g = (fluidColor shr 8 and 255) / 255f
    val b = (fluidColor and 255) / 255f

    val tess = Tessellator.getInstance()
    tess.buffer.run {
        val matrix = matrices.peek().positionMatrix

        var percentage = (amt / max.toFloat()) * height.toFloat()

        repeat(ceil(percentage / 16).toInt()) { i ->
            val p = if (percentage > 16f) 16f else percentage
            begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)
            vertex(matrix, x.toFloat(), y - (i * 16f), 0f).color(r, g, b, 1f).texture(sprite.maxU, sprite.minV).next()
            vertex(matrix, x + width.toFloat(), y - (i * 16f), 0f).color(r, g, b, 1f).texture(sprite.minU, sprite.minV).next()
            val atlasHeight = sprite.contents.height / (sprite.maxV - sprite.minV)
            vertex(matrix, x + width.toFloat(), y - p - (i * 16f), 0f).color(r, g, b, 1f).texture(sprite.minU, (sprite.maxV - ((sprite.contents.height - p) / atlasHeight))).next()
            vertex(matrix, x.toFloat(), y - p - (i * 16f), 0f).color(r, g, b, 1f).texture(sprite.maxU, (sprite.maxV - ((sprite.contents.height - p) / atlasHeight))).next()
            tess.draw()
            percentage -= p
        }
    }

}

fun drawTexturedQuad(
    matrix: Matrix4f,
    x0: Int,
    x1: Int,
    y0: Int,
    y1: Int,
    z: Int,
    u0: Float,
    u1: Float,
    v0: Float,
    v1: Float
) {
    RenderSystem.setShader { GameRenderer.getPositionTexProgram() }
    val bufferBuilder = Tessellator.getInstance().buffer
    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
    bufferBuilder.vertex(matrix, x0.toFloat(), y1.toFloat(), z.toFloat()).texture(u0, v1).next()
    bufferBuilder.vertex(matrix, x1.toFloat(), y1.toFloat(), z.toFloat()).texture(u1, v1).next()
    bufferBuilder.vertex(matrix, x1.toFloat(), y0.toFloat(), z.toFloat()).texture(u1, v0).next()
    bufferBuilder.vertex(matrix, x0.toFloat(), y0.toFloat(), z.toFloat()).texture(u0, v0).next()
    BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
}


inline fun Box.forEach(block: (BlockPos) -> Unit) {
    val pos = BlockPos.Mutable()
    for (x in minX.toInt() until maxX.toInt()) {
        for (y in minY.toInt() until maxY.toInt()) {
            for (z in minZ.toInt() until maxZ.toInt()) {
                pos.set(x, y, z)
                block(pos)
            }
        }
    }
}

fun Box.toList(): List<BlockPos> {
    val list = ArrayList<BlockPos>((xLength * yLength * zLength).toInt())
    for (y in maxY.toInt() downTo minY.toInt()) {
        for (x in minX.toInt() until maxX.toInt()) {
            for (z in minZ.toInt() until maxZ.toInt()) {
                list.add(BlockPos(x, y, z))
            }
        }
    }
    return list
}

fun tx(block: (Transaction) -> Unit) {
    block(Transaction.openOuter())
}