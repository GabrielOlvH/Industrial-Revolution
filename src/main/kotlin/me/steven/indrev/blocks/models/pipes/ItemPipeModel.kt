package me.steven.indrev.blocks.models.pipes

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.utils.blockSpriteId
import net.minecraft.client.util.SpriteIdentifier

class ItemPipeModel(tier: Tier) : BasePipeModel(tier, "item_pipe") {
    override val spriteIdCollection: MutableList<SpriteIdentifier> = mutableListOf(
        blockSpriteId("block/item_pipe_center_${tier.toString().toLowerCase()}"),
        blockSpriteId("block/item_pipe_side_${tier.toString().toLowerCase()}")
    )
}