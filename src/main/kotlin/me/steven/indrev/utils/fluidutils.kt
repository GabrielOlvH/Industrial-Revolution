package me.steven.indrev.utils

import com.mojang.blaze3d.systems.RenderSystem
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.OrderedText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import java.util.*
import java.util.function.LongFunction
import kotlin.math.ceil


//these are just for reference, they're not used much

val bucket = 81000L

val bottle = bucket / 3 // 27000 droplets

val half_bucket = bucket / 2 // 40500 droplets

val block = bucket
val ingot = block / 9 // 9000 droplets
val nugget = ingot / 9 // 1000 droplets
val scrap = ingot / 4 // 250 droplets

val mb = bucket / 1000

fun FluidBlock.drainFluid(world: World, pos: BlockPos, state: BlockState): Fluid {
    return if (state.get(FluidBlock.LEVEL) as Int == 0) {
        world.setBlockState(pos, Blocks.AIR.defaultState, 11)
        fluid
    } else {
        Fluids.EMPTY
    }
}

//TODO fuck weakhashmaps
val fluidApiCache = WeakHashMap<World, Long2ObjectOpenHashMap<BlockApiCache<Storage<FluidVariant>, Direction>>>()

fun fluidStorageOf(world: ServerWorld, blockPos: BlockPos, direction: Direction): Storage<FluidVariant>? {
    return fluidApiCache
        .computeIfAbsent(world) { Long2ObjectOpenHashMap() }
        .computeIfAbsent(blockPos.asLong(), LongFunction { BlockApiCache.create(FluidStorage.SIDED, world, blockPos) })
        .find(direction)
}


fun fluidStorageOf(world: World, blockPos: BlockPos, direction: Direction): Storage<FluidVariant>? {
    if (world is ServerWorld)
        return fluidStorageOf(world, blockPos, direction)
    else return FluidStorage.SIDED.find(world, blockPos, direction)
}

fun fluidStorageOf(itemStack: ItemStack?): Storage<FluidVariant>? {
    return if (itemStack == null) null
    else FluidStorage.ITEM.find(itemStack, null)
}

typealias IRFluidAmount = ResourceAmount<FluidVariant>

fun IRFluidAmount.toPacket(buf: PacketByteBuf) {
    resource.toPacket(buf)
    buf.writeLong(amount)
}

/*fun IRFluidAmount.renderGuiRect(x0: Double, y0: Double, x1: Double, y1: Double) {
    FluidKeys.get(resource.fluid).withAmount(FluidAmount.BUCKET).renderGuiRect(x0, y0, x1, y1)
}*/

fun fromPacket(buf: PacketByteBuf): IRFluidAmount {
    val res = FluidVariant.fromPacket(buf)
    val amt = buf.readLong()
    return amt of res
}

infix fun Long.of(variant: FluidVariant) = IRFluidAmount(variant, this)

fun getTooltip(variant: FluidVariant, amount: Long, capacity: Long): List<Text> {
    val tooltips = mutableListOf<Text>()
    val id = Registry.BLOCK.getId(variant.fluid.defaultState.blockState.block)
    val color = FluidRenderHandlerRegistry.INSTANCE.get(variant.fluid)?.getFluidColor(null, null, variant.fluid.defaultState) ?: -1

    tooltips.add(translatable("block.${id.namespace}.${id.path}").setStyle(Style.EMPTY.withColor(color)))

    val asMb = amount / 81
    val accurate = amount / 81.0
    val prefix = when {
        accurate > asMb -> ">"
        accurate < asMb -> "<"
        else -> ""
    }
    if (capacity > 0)
        tooltips.add(translatable("$prefix$asMb / ${capacity / 81} mB"))
    else
        tooltips.add(translatable("$prefix$asMb mB"))

    if (Screen.hasShiftDown()) {
        if (capacity > 0)
            tooltips.add(translatable("$amount / $capacity droplets"))
        else
            tooltips.add(translatable("$amount droplets"))
    }
    
    return tooltips
}

fun renderInGui(matrices: MatrixStack, resource: FluidVariant, amt: Long, max: Long, x: Int, y: Int, width: Int, height: Int) {
    val client = MinecraftClient.getInstance()
    RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
    RenderSystem.setShader(GameRenderer::getPositionColorTexShader)
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
            val atlasHeight = sprite.height / (sprite.maxV - sprite.minV)
            vertex(matrix, x + width.toFloat(), y - p - (i * 16f), 0f).color(r, g, b, 1f).texture(sprite.minU, (sprite.maxV - ((sprite.height - p) / atlasHeight))).next()
            vertex(matrix, x.toFloat(), y - p - (i * 16f), 0f).color(r, g, b, 1f).texture(sprite.maxU, (sprite.maxV - ((sprite.height - p) / atlasHeight))).next()
            tess.draw()
            percentage -= p
        }
    }

}

const val FULL_LIGHT = 0x00F000F0

// Original code from https://github.com/AztechMC/Modern-Industrialization/blob/f38dc066be9e249ac604a1d188786b9d4abf5c54/src/main/java/aztech/modern_industrialization/util/RenderHelper.java#L126
fun drawFluidInTank(
    world: World,
    pos: BlockPos,
    ms: MatrixStack,
    vcp: VertexConsumerProvider,
    fluid: FluidVariant,
    fill: Float
) {
    val width = 1.8f/16f
    var fill = fill
    val vc = vcp.getBuffer(RenderLayer.getTranslucent())
    val sprite = FluidVariantRendering.getSprite(fluid)
    val color = FluidRenderHandlerRegistry.INSTANCE.get(fluid.fluid).getFluidColor(world, pos, fluid.fluid.defaultState)
    val r = (color shr 16 and 255) / 256f
    val g = (color shr 8 and 255) / 256f
    val b = (color and 255) / 256f

    // Make sure fill is within [TANK_W, 1 - TANK_W]
    fill = width + (1 - 2 * width) * Math.min(1f, Math.max(fill, 0f))
    // Top and bottom positions of the fluid inside the tank
    var topHeight = fill
    var bottomHeight: Float = width
    // Render gas from top to bottom
    if (FluidVariantAttributes.isLighterThanAir(fluid)) {
        topHeight = 1 - width
        bottomHeight = 1 - fill
    }
    val renderer = RendererAccess.INSTANCE.renderer!!
    for (direction in Direction.values()) {
        val emitter: QuadEmitter = renderer.meshBuilder().emitter
        if (direction.axis.isVertical) {
            emitter.square(
                direction,
                width,
                width,
                1 - width,
                1 - width,
                if (direction == Direction.UP) 1 - topHeight else bottomHeight
            )
        } else {
            emitter.square(direction, width, bottomHeight, 1 - width, topHeight, width)
        }
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
        emitter.spriteColor(0, -1, -1, -1, -1)
        vc.quad(ms.peek(), emitter.toBakedQuad(0, sprite, false), r, g, b, FULL_LIGHT, OverlayTexture.DEFAULT_UV)
    }
}
