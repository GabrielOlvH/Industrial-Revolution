package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.blockentities.crafters.CompressorFactoryBlockEntity
import me.steven.indrev.gui.screenhandlers.COMPRESSOR_FACTORY_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.upProcessBar
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class CompressorFactoryScreenHandler (
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiScreenHandler(
        COMPRESSOR_FACTORY_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.compressor_factory", ctx, playerInventory, blockInventory, widgetPos = 0.15)
        withBlockEntity<CompressorFactoryBlockEntity> { blockEntity ->
            val slotsAmount = 5
            val offset = 2.2

            for ((index, slot) in blockEntity.inventoryComponent!!.inventory.inputSlots.withIndex()) {
                val inputSlot = WItemSlot.of(blockInventory, slot)
                root.add(inputSlot, offset + (index * 1.4), 0.6)
            }

            for (i in 0 until slotsAmount) {
                val processWidget = upProcessBar(blockEntity, CompressorFactoryBlockEntity.CRAFTING_COMPONENT_START_ID + i)
                root.add(processWidget, offset + (i * 1.4), 1.7)
            }

            for ((index, slot) in blockEntity.inventoryComponent!!.inventory.outputSlots.withIndex()) {
                val outputSlot = WItemSlot.of(blockInventory, slot)
                root.add(outputSlot, offset + (index * 1.4), 2.9)
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

    companion object {
        val SCREEN_ID = identifier("compressor_factory_screen")
    }
}