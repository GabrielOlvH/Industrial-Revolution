package me.steven.indrev.gui.nuclearreactor

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity

class NuclearReactorScreen(controller: NuclearReactorController, playerEntity: PlayerEntity) : CottonInventoryScreen<NuclearReactorController>(controller, playerEntity) {
    companion object {
        val SCREEN_ID = identifier("nuclear_reactor")
    }
}