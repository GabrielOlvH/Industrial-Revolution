package me.steven.indrev.blocks.models.pipes

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.utils.blockSpriteId
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import java.util.*
import java.util.function.Supplier

class FluidPipeModel(tier: Tier) : BasePipeModel(tier, "fluid_pipe") {
    override val spriteIdCollection: MutableList<SpriteIdentifier> = mutableListOf(
        blockSpriteId("block/fluid_pipe_center_${tier.toString().toLowerCase()}"),
        blockSpriteId("block/fluid_pipe_side_${tier.toString().toLowerCase()}")
    )

    override fun emitItemQuads(stack: ItemStack?, p1: Supplier<Random>, context: RenderContext) {
        super.emitItemQuads(stack, p1, context)

        context.fallbackConsumer().accept(modelArray[1])
        context.fallbackConsumer().accept(modelArray[3])
    }
}