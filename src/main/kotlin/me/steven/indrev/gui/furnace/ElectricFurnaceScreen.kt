package me.steven.indrev.gui.furnace

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.minecraft.entity.player.PlayerEntity

class ElectricFurnaceScreen(controller: ElectricFurnaceController, playerEntity: PlayerEntity) : CottonInventoryScreen<ElectricFurnaceController>(controller, playerEntity)