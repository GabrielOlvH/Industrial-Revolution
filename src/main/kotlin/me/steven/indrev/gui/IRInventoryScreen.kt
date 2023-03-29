package me.steven.indrev.gui

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import io.github.cottonmc.cotton.gui.impl.client.MouseInputHandler
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import net.minecraft.entity.player.PlayerEntity
import java.util.function.Function

class IRInventoryScreen<T : SyncedGuiDescription>(controller: T, playerEntity: PlayerEntity) : CottonInventoryScreen<T>(controller, playerEntity) {


}