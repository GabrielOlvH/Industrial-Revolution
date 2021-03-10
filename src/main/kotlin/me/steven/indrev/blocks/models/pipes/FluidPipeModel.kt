package me.steven.indrev.blocks.models.pipes

import me.steven.indrev.api.machines.Tier
import net.minecraft.client.util.SpriteIdentifier

class FluidPipeModel(tier: Tier) : BasePipeModel(tier, "fluid_pipe") {
    override val spriteIdCollection: MutableList<SpriteIdentifier> = mutableListOf(
        //this is an example, replace this with the textures from the item pipe
        /*blockSpriteId("block/cable_center"),
        blockSpriteId("block/cable_center_emissive_${tier.toString().toLowerCase()}"),
        blockSpriteId("block/cable_wrap"),
        blockSpriteId("block/cable_wire_emissive_${tier.toString().toLowerCase()}")*/
    )
}