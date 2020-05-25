package me.steven.indrev.gui.battery

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity

class BatteryScreen(controller: BatteryController, playerEntity: PlayerEntity) : CottonInventoryScreen<BatteryController>(controller, playerEntity) {
    companion object {
        val SCREEN_ID = identifier("battery_screen")
    }
}