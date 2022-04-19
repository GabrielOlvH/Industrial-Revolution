package me.steven.indrev.events.client

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.component3
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.nbt.NbtLong
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box

object IRWorldRenderer : WorldRenderEvents.BeforeBlockOutline {

    override fun beforeBlockOutline(context: WorldRenderContext, hitResult: HitResult?): Boolean {
        val player = MinecraftClient.getInstance().player ?: return true
        val stack = player.mainHandStack ?: return true
        if (!stack.isIn(IndustrialRevolution.SCREWDRIVER_TAG) || stack.nbt?.contains("SelectedHeliostats") != true) return true

        val positions = stack.nbt?.getList("SelectedHeliostats", 4)?.map { BlockPos.fromLong((it as NbtLong).longValue()) } ?: return true
        val vcp = context.consumers() as VertexConsumerProvider.Immediate
        val vc = vcp.getBuffer(RenderLayer.getLines()) ?: return true
        val (cX, cY, cZ) = MinecraftClient.getInstance().gameRenderer.camera.pos
        context.matrixStack().run {
            positions.forEach { (x, y, z) ->
                push()
                translate(x.toDouble() - cX, y.toDouble() - cY, z.toDouble() - cZ)
                WorldRenderer.drawBox(this, vc, Box(BlockPos.ORIGIN), 1f, 0.6f, 1f, 1f)
                pop()
            }
        }
        vcp.draw()
        return !positions.contains((hitResult as? BlockHitResult)?.blockPos)
    }
}