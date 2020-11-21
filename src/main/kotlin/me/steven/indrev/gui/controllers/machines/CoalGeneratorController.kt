package me.steven.indrev.gui.controllers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.widgets.misc.WFuel
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class CoalGeneratorController(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiController(
        IndustrialRevolution.COAL_GENERATOR_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.coal_generator", ctx, playerInventory, blockInventory)

        val itemSlot = WItemSlot.of(blockInventory, 2)
        root.add(itemSlot, 4, 2)

        val wFuel = WFuel()
        root.add(wFuel, 4.1, 1.1)
        wFuel.setSize(14, 14)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/generators")

    override fun getPage(): Int = 1

    companion object {
        val SCREEN_ID: Identifier = identifier("coal_generator_screen")
    }
}