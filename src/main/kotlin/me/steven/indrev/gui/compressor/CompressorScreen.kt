package me.steven.indrev.gui.compressor

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import me.steven.indrev.identifier
import net.minecraft.entity.player.PlayerEntity

class CompressorScreen(controller: CompressorController, playerEntity: PlayerEntity) : CottonInventoryScreen<CompressorController>(controller, playerEntity) {
    companion object {
        val SCREEN_ID = identifier("compressor_screen")
    }
}