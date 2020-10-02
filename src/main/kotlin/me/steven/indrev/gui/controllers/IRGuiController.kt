package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.NinePatch
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier

open class IRGuiController(
    type: ScreenHandlerType<*>?,
    syncId: Int,
    playerInventory: PlayerInventory,
    val ctx: ScreenHandlerContext
) : SyncedGuiDescription(type, syncId, playerInventory, getBlockInventory(ctx), getBlockPropertyDelegate(ctx)) {
    override fun addPainters() {
        super.addPainters()
        ctx.run { world, pos ->
            var offset = 8
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is UpgradeProvider) offset = -22
            if (blockEntity is AOEMachineBlockEntity<*>) offset = -24
                rootPanel.backgroundPainter = BackgroundPainter.createLightDarkVariants(
                    NinePatch(Identifier("libgui", "textures/widget/panel_light.png")).setPadding(8).setRightPadding(offset),
                    NinePatch(Identifier("libgui", "textures/widget/panel_dark.png")).setPadding(8).setRightPadding(offset)
                )
        }
    }

    companion object {
        val UPGRADE_SLOT_PANEL_PAINTER = BackgroundPainter.createLightDarkVariants(
            BackgroundPainter.createNinePatch(Identifier("libgui", "textures/widget/panel_light.png"), 4),
            BackgroundPainter.createNinePatch(Identifier("libgui", "textures/widget/panel_dark.png"), 4)
        )
    }
}