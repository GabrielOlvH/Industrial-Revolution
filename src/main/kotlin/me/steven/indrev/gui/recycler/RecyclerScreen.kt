package me.steven.indrev.gui.recycler

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity

class RecyclerScreen(controller: RecyclerController, playerEntity: PlayerEntity) : CottonInventoryScreen<RecyclerController>(controller, playerEntity) {
    companion object {
        val SCREEN_ID = identifier("recycler")
    }
}