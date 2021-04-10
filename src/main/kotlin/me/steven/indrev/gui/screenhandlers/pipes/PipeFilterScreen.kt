package me.steven.indrev.gui.screenhandlers.pipes

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.LiteralText

class PipeFilterScreen(val screenHandler: PipeFilterScreenHandler, val player: PlayerEntity) : CottonInventoryScreen<PipeFilterScreenHandler>(screenHandler, player, LiteralText("Item Pipe Filter"))