package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WBar
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.blockentities.crafters.SolidInfuserFactoryBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.SOLID_INFUSER_FACTORY_HANDLER
import me.steven.indrev.utils.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class SolidInfuserFactoryScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiScreenHandler(
        SOLID_INFUSER_FACTORY_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.solid_infuser_factory", ctx, playerInventory, blockInventory, invPos = 4.85, widgetPos = 0.5)
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? SolidInfuserFactoryBlockEntity ?: return@run
            val offset = 2.2

            for (index in blockEntity.inventoryComponent!!.inventory.inputSlots.indices step 2) {
                val slot = blockEntity.inventoryComponent!!.inventory.inputSlots[index]
                val inputSlot = WItemSlot.of(blockInventory, slot, 1, 2)
                root.add(inputSlot, offset + ((index * 1.4) / 2), 0.6)
            }

            for (i in 0 until 5) {
                val processWidget = createProcessBar(WBar.Direction.DOWN, PROCESS_VERTICAL_EMPTY, PROCESS_VERTICAL_FULL, 4 + (i * 2), 5 + (i * 2))
                root.add(processWidget, offset + (i * 1.4), 2.7)
            }

            for ((index, slot) in blockEntity.inventoryComponent!!.inventory.outputSlots.withIndex()) {
                val outputSlot = WItemSlot.of(blockInventory, slot)
                root.add(outputSlot, offset + (index * 1.4), 3.8)
                outputSlot.addChangeListener { _, _, _, _ ->
                    val player = playerInventory.player
                    if (!player.world.isClient) {
                        blockEntity.dropExperience(player)
                    }
                }
                outputSlot.isInsertingAllowed = false
            }

        }
        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/basic_machines")

    override fun getPage(): Int = 1

    companion object {
        val SCREEN_ID = identifier("infuser_factory_screen")
    }
}