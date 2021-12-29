package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.blockentities.crafters.ElectricFurnaceBlockEntity
import me.steven.indrev.gui.screenhandlers.ELECTRIC_FURNACE_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.WCustomBar
import me.steven.indrev.gui.widgets.machines.processBar
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class ElectricFurnaceScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiScreenHandler(
        ELECTRIC_FURNACE_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.electric_furnace", ctx, playerInventory, blockInventory)

        val inputSlot = WItemSlot.of(blockInventory, 2)
        root.add(inputSlot, 3.3, 1.8)

        val processWidget = query<ElectricFurnaceBlockEntity, WCustomBar> { be -> processBar(be, ElectricFurnaceBlockEntity.CRAFTING_COMPONENT_ID) }
        root.add(processWidget, 4.45, 1.8)

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
        root.add(outputSlot, 5.94, 1.8)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("electric_furnace_screen")
    }
}