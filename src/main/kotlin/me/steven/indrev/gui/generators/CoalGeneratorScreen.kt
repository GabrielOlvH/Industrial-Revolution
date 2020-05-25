package me.steven.indrev.gui.generators

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier

class CoalGeneratorScreen(container: CoalGeneratorController, player: PlayerEntity) : CottonInventoryScreen<CoalGeneratorController>(container, player) {
    companion object {
        val SCREEN_ID: Identifier = identifier("coal_generator_screen")
    }
}