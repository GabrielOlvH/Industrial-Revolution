package me.steven.indrev.gui.pulverizer

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.minecraft.entity.player.PlayerEntity

class PulverizerScreen(controller: PulverizerController, playerEntity: PlayerEntity) : CottonInventoryScreen<PulverizerController>(controller, playerEntity)