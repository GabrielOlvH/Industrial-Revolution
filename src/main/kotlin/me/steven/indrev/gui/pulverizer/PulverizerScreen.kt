package me.steven.indrev.gui.pulverizer

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import me.steven.indrev.identifier
import net.minecraft.entity.player.PlayerEntity

class PulverizerScreen(controller: PulverizerController, playerEntity: PlayerEntity) : CottonInventoryScreen<PulverizerController>(controller, playerEntity) {
    companion object {
        val SCREEN_ID = identifier("pulverizer_screen")
    }
}