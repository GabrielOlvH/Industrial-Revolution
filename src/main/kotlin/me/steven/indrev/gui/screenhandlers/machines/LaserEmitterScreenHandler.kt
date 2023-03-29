package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.data.Insets
import me.steven.indrev.blockentities.laser.LaserBlockEntity
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.LASER_HANDLER
import me.steven.indrev.gui.widgets.machines.energyBar
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import me.steven.indrev.utils.translatable

class LaserEmitterScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiScreenHandler(
        LASER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.insets = Insets.ROOT_PANEL

        val label = WLabel(translatable("block.indrev.laser_emitter_mk4"))
        root.add(label, 0, 0)
        label.setSize(75, 0)

        withBlockEntity<LaserBlockEntity> { be ->
            val energy = energyBar(be)
            root.add(energy, 1, 1)
            energy.setLocation(9 + 16 + root.insets.left, 16 + root.insets.top)
        }

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