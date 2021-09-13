package me.steven.indrev.registry

import com.mojang.blaze3d.systems.RenderSystem
import draylar.magna.api.BlockBreaker
import draylar.magna.api.MagnaTool
import me.steven.indrev.tools.modular.DrillModule
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayers
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos


object MatterProjectorPreviewRenderer : WorldRenderEvents.BeforeEntities {
    override fun beforeEntities(context: WorldRenderContext) {
        val player = MinecraftClient.getInstance().player ?: return
        val target = MinecraftClient.getInstance().crosshairTarget
        val stack = player.mainHandStack
        val item = stack.item
        if (player.isSneaking && DrillModule.MATTER_PROJECTOR.isInstalled(stack) && item is MagnaTool && target is BlockHitResult) {
            item.blockFinder.findPositions(context.world(), player, item.getRadius(stack)).forEach { pos ->
                val blockState = context.world().getBlockState(pos)
                val offset = pos.offset(target.side)
                if (context.world().getBlockState(offset).material.isReplaceable) {
                    val cameraPos = MinecraftClient.getInstance().gameRenderer.camera.pos
                    val x = offset.x - cameraPos.x
                    val y = offset.y - cameraPos.y
                    val z = offset.z - cameraPos.z
                    context.matrixStack().push()
                    context.matrixStack().translate(x, y, z)
                    context.matrixStack().scale(0.6f, 0.6f, 0.6f)
                    MinecraftClient.getInstance().blockRenderManager.renderBlockAsEntity(blockState, context.matrixStack(), context.consumers(), 0xFF, OverlayTexture.DEFAULT_UV)
                    context.matrixStack().pop()
                }
            }
        }
    }
}