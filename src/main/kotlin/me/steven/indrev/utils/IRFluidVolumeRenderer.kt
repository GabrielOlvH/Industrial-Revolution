package me.steven.indrev.utils

import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace
import alexiil.mc.lib.attributes.fluid.render.FluidVolumeRenderer
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object IRFluidVolumeRenderer : FluidVolumeRenderer() {
    override fun render(
        fluid: FluidVolume,
        faces: MutableList<FluidRenderFace>?,
        vcp: VertexConsumerProvider?,
        matrices: MatrixStack?
    ) {
    }

    fun render(
        world: World,
        pos: BlockPos,
        fluid: FluidVolume,
        faces: List<FluidRenderFace>?,
        vcp: VertexConsumerProvider?,
        matrices: MatrixStack?
    ) {

        val sprites = getSprites(fluid)
        val layer = getRenderLayer(fluid)
        val renderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid.rawFluid) ?: return
        val fluidColor = renderHandler.getFluidColor(world, pos, fluid.rawFluid?.defaultState)
        renderSimpleFluid(faces, vcp!!.getBuffer(layer), matrices, sprites[0], sprites[1], fluidColor)
    }

}