package me.steven.indrev.events.client

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.MACHINES
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.items.TierUpgradeItem
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.component3
import me.steven.indrev.utils.component4
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.WorldRenderer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box


object OutlineRenderer : WorldRenderEvents.BlockOutline {
    override fun onBlockOutline(
        ctx: WorldRenderContext,
        outlineCtx: WorldRenderContext.BlockOutlineContext
    ): Boolean {
        val heldStack = MinecraftClient.getInstance().player?.mainHandStack
        val item = heldStack?.item
        val block = outlineCtx.blockState().block
        val blockEntity = ctx.world().getBlockEntity(outlineCtx.blockPos())
        if (block is MachineBlock && item is TierUpgradeItem && blockEntity is MachineBlockEntity<*>) {
            val machine = MACHINES[block.id]
            if (machine != null && item.canUse(blockEntity, machine)) {
                val cam = MinecraftClient.getInstance().gameRenderer.camera.pos
                val matrices = ctx.matrixStack()
                matrices.push()
                matrices.translate(outlineCtx.blockPos().x.toDouble() - cam.x, outlineCtx.blockPos().y.toDouble() - cam.y, outlineCtx.blockPos().z.toDouble() - cam.z)
                val (_, r, g, b) = item.from.color
                WorldRenderer.drawBox(matrices, ctx.consumers()?.getBuffer(RenderLayer.getLines()), Box(BlockPos.ORIGIN).expand(0.025),  r/255f, g/255f, b/255f, 1f)
                matrices.pop()
                return false
            }
        }
        return true
    }
}