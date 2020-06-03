package me.steven.indrev.gui.solargenerator

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity

class SolarGeneratorScreen(controller: SolarGeneratorController, playerEntity: PlayerEntity) : CottonInventoryScreen<SolarGeneratorController>(controller, playerEntity) {
    companion object {
        val SCREEN_ID = identifier("solar_generator")
    }
}