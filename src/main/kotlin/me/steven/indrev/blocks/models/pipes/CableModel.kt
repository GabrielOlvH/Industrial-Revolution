package me.steven.indrev.blocks.models.pipes

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.utils.blockSpriteId

class CableModel(tier: Tier) : BasePipeModel(tier, "cable") {
    override val spriteIdCollection = mutableListOf(
        blockSpriteId("block/cable_center"),
        blockSpriteId("block/cable_center_emissive_${tier.toString().toLowerCase()}"),
        blockSpriteId("block/cable_wrap"),
        blockSpriteId("block/cable_wire_emissive_${tier.toString().toLowerCase()}")
    )
}