package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.gui.widgets.EnergyWidget
import me.steven.indrev.gui.widgets.StringWidget
import me.steven.indrev.gui.widgets.TemperatureWidget
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.utils.add
import me.steven.indrev.utils.identifier
import net.minecraft.client.resource.language.I18n
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class NuclearReactorController(
    syncId: Int,
    playerInventory: PlayerInventory,
    screenHandlerContext: ScreenHandlerContext
) :
    SyncedGuiDescription(
        syncId,
        playerInventory,
        getBlockInventory(screenHandlerContext),
        getBlockPropertyDelegate(screenHandlerContext)
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(150, 120)

        root.add(StringWidget(I18n.translate("block.indrev.nuclear_reactor"), titleColor), 4, 0)
        root.add(createPlayerInventoryPanel(), 0, 5)

        root.add(EnergyWidget(propertyDelegate), 0, 0, 16, 64)

        val batterySlot = WItemSlot.of(blockInventory, 0)
        root.add(batterySlot, 0.0, 3.7)

        screenHandlerContext.run { world, blockPos ->
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity is MachineBlockEntity && blockEntity.temperatureController != null) {
                val controller = blockEntity.temperatureController!!
                root.add(TemperatureWidget(propertyDelegate, controller), 1, 0, 16, 64)
                val coolerSlot = WItemSlot.of(blockInventory, 1)
                root.add(coolerSlot, 1.0, 3.7)
            }
        }

        var x = 4
        var y = 0
        for ((index, slot) in (blockInventory as DefaultSidedInventory).inputSlots.withIndex()) {
            if (index.rem(3) == 0) {
                x = 4
                y++
            }
            x++
            root.add(WItemSlot.of(blockInventory, slot), x, y)
        }

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("nuclear_reactor")
    }
}