package me.steven.indrev.gui.controllers.pipes

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.LiteralText

class PipeFilterScreen(val controller: PipeFilterController, val player: PlayerEntity) : CottonInventoryScreen<PipeFilterController>(controller, player, LiteralText("Item Pipe Filter"))