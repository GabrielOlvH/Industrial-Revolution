package me.steven.indrev.gui.miner

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity

class MinerScreen(controller: MinerController, playerEntity: PlayerEntity) : CottonInventoryScreen<MinerController>(controller, playerEntity) {
    companion object {
        val SCREEN_ID = identifier("miner")
    }
}