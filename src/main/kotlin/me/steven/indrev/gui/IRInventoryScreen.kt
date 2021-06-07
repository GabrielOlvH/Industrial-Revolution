package me.steven.indrev.gui

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import io.github.cottonmc.cotton.gui.impl.client.MouseInputHandler
import net.minecraft.entity.player.PlayerEntity

class IRInventoryScreen<T : SyncedGuiDescription>(controller: T, playerEntity: PlayerEntity) : CottonInventoryScreen<T>(controller, playerEntity) {
    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        val result = super.mouseClicked(mouseX, mouseY, mouseButton)
        val containerX = mouseX.toInt() - x
        val containerY = mouseY.toInt() - y
        return if (containerX < width && containerY < height) {
            MouseInputHandler.onMouseDown(description, this, containerX, containerY, mouseButton)
            true
        } else {
            result
        }
    }
}