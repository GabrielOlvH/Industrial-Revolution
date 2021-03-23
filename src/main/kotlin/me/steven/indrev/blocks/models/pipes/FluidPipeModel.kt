package me.steven.indrev.blocks.models.pipes

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.utils.blockSpriteId
import net.minecraft.client.util.SpriteIdentifier

class FluidPipeModel(tier: Tier) : BasePipeModel(tier, "fluid_pipe") {
    override val spriteIdCollection: MutableList<SpriteIdentifier> = mutableListOf(
        blockSpriteId("block/fluid_pipe_center_${tier.toString().toLowerCase()}"),
        blockSpriteId("block/fluid_pipe_side_${tier.toString().toLowerCase()}")
    )
}