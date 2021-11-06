package me.steven.indrev.gui

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import io.github.cottonmc.cotton.gui.impl.client.MouseInputHandler
import net.minecraft.entity.player.PlayerEntity
import org.anti_ad.mc.ipn.api.IPNIgnore

@IPNIgnore
class IRInventoryScreen<T : SyncedGuiDescription>(controller: T, playerEntity: PlayerEntity) : CottonInventoryScreen<T>(controller, playerEntity) {
}