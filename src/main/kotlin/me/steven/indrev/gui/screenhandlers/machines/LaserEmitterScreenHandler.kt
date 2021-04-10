package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.WEnergy
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText

class LaserEmitterScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiScreenHandler(
        IndustrialRevolution.LASER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)

        val label = WLabel(TranslatableText("block.indrev.laser_mk4"))
        root.add(label, 0, 0)
        label.setLocation(2, 0)

        val energy = WEnergy()
        root.add(energy, 1, 1)
        energy.setLocation(9, 18)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun addPainters() {
        if (rootPanel != null && !fullscreen) {
            rootPanel.backgroundPainter = BackgroundPainter.VANILLA
        }
    }

    companion object {
        val SCREEN_ID = identifier("laser")
    }
}