package me.steven.indrev.events.client

import draylar.magna.api.MagnaTool
import me.steven.indrev.tools.modular.DrillModule
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.util.hit.BlockHitResult


object MatterProjectorPreviewRenderer : WorldRenderEvents.BeforeEntities {
    override fun beforeEntities(context: WorldRenderContext) {
        val player = MinecraftClient.getInstance().player ?: return
        val target = MinecraftClient.getInstance().crosshairTarget
        val stack = player.mainHandStack
        val item = stack.item
        if (player.isSneaking && DrillModule.MATTER_PROJECTOR.isInstalled(stack) && item is MagnaTool && target is BlockHitResult) {
            val blockState = context.world().getBlockState(target.blockPos)
            item.blockFinder.findPositions(context.world(), player, item.getRadius(stack)).forEach { pos ->

                val offset = pos.offset(target.side)
                if (context.world().getBlockState(offset).material.isReplaceable) {
                    val cameraPos = MinecraftClient.getInstance().gameRenderer.camera.pos
                    val x = offset.x - cameraPos.x
                    val y = offset.y - cameraPos.y
                    val z = offset.z - cameraPos.z
                    context.matrixStack().push()
                    context.matrixStack().translate(x+0.2, y, z+0.2)
                    context.matrixStack().scale(0.6f, 0.6f, 0.6f)
                    MinecraftClient.getInstance().blockRenderManager.renderBlockAsEntity(blockState, context.matrixStack(), context.consumers(), 0xFF, OverlayTexture.DEFAULT_UV)
                    context.matrixStack().pop()
                }
            }
        }
    }
}