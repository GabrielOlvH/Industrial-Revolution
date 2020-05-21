package me.steven.indrev.gui.generators

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import me.steven.indrev.gui.generators.CoalGeneratorController
import net.minecraft.entity.player.PlayerEntity

class CoalGeneratorScreen(container: CoalGeneratorController, player: PlayerEntity) : CottonInventoryScreen<CoalGeneratorController>(container, player)