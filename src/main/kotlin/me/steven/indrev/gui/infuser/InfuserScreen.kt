package me.steven.indrev.gui.infuser

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity

class InfuserScreen(controller: InfuserController, playerEntity: PlayerEntity) : CottonInventoryScreen<InfuserController>(controller, playerEntity) {
    companion object {
        val SCREEN_ID = identifier("infuser")
    }
}