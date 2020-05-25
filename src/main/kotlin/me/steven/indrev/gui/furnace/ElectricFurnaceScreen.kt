package me.steven.indrev.gui.furnace

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity

class ElectricFurnaceScreen(controller: ElectricFurnaceController, playerEntity: PlayerEntity) : CottonInventoryScreen<ElectricFurnaceController>(controller, playerEntity) {
    companion object {
        val SCREEN_ID = identifier("electric_furnace_screen")
    }
}