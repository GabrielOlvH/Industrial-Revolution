package me.steven.indrev.gui.controllers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.createProcessBar
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class ElectricFurnaceController(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiController(
        IndustrialRevolution.ELECTRIC_FURNACE_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.electric_furnace", ctx, playerInventory, blockInventory)

        val inputSlot = WItemSlot.of(blockInventory, 2)
        root.add(inputSlot, 3.5, 2.0)

        val processWidget = createProcessBar()
        root.add(processWidget, 4.65, 2.0)

        val outputSlot = WItemSlot.outputOf(blockInventory, 3)
        outputSlot.addChangeListener { _, _, _, _ ->
            val player = playerInventory.player
            if (!player.world.isClient) {
                ctx.run { world, pos ->
                    val blockEntity = world.getBlockEntity(pos) as? CraftingMachineBlockEntity<*> ?: return@run
                    blockEntity.dropExperience(player)
                }
            }
        }
        outputSlot.isInsertingAllowed = false
        root.add(outputSlot, 6.14, 2.0)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/basic_machines")

    override fun getPage(): Int = 1

    companion object {
        val SCREEN_ID = identifier("electric_furnace_screen")
    }
}