package me.steven.indrev.gui.chopper

import io.github.cottonmc.cotton.gui.CottonCraftingController
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.blockentities.farms.ChopperBlockEntity
import me.steven.indrev.gui.widgets.EnergyWidget
import me.steven.indrev.gui.widgets.StringWidget
import me.steven.indrev.gui.widgets.TemperatureWidget
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.utils.add
import me.steven.indrev.utils.identifier
import net.minecraft.client.resource.language.I18n
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.TranslatableText

class ChopperController(syncId: Int, playerInventory: PlayerInventory, blockContext: BlockContext)
    : CottonCraftingController(null, syncId, playerInventory, getBlockInventory(blockContext), getBlockPropertyDelegate(blockContext)) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(150, 120)

        root.add(StringWidget(I18n.translate("block.indrev.chopper"), titleColor), 4, 0)
        root.add(createPlayerInventoryPanel(), 0.0, 5.4)

        blockContext.run { world, blockPos ->
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity !is ChopperBlockEntity) return@run
            val button = object : WButton(TranslatableText("block.indrev.chopper.toggle.btn")) {
                override fun addInformation(information: MutableList<String>?) {
                    information?.add(I18n.translate("block.indrev.chopper.toggle.${blockEntity.renderWorkingArea}"))
                }
            }
            button.setOnClick {
                blockEntity.renderWorkingArea = !blockEntity.renderWorkingArea
            }
            root.add(button, 7.95, 4.0)
        }

        root.add(EnergyWidget(propertyDelegate), 0, 0, 16, 64)

        val batterySlot = WItemSlot.of(blockInventory, 0)
        root.add(batterySlot, 0.0, 3.7)

        blockContext.run { world, blockPos ->
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity is UpgradeProvider) {
                for ((i, slot) in blockEntity.getUpgradeSlots().withIndex()) {
                    val s = WItemSlot.of(blockInventory, slot)
                    root.add(s, 8, i)
                }
            }
            if (blockEntity is MachineBlockEntity && blockEntity.temperatureController != null) {
                val controller = blockEntity.temperatureController!!
                root.add(TemperatureWidget(propertyDelegate, controller), 1, 0, 16, 64)
                val coolerSlot = WItemSlot.of(blockInventory, 1)
                root.add(coolerSlot, 1.0, 3.7)
            }
        }

        var x = 4
        var y = 0
        for ((index, slot) in (blockInventory as DefaultSidedInventory).outputSlots.withIndex()) {
            if (index.rem(3) == 0) {
                x = 4
                y++
            }
            x++
            root.add(WItemSlot.of(blockInventory, slot), x - .2, y.toDouble())
        }

        x = 1
        y = 0
        for ((index, slot) in (blockInventory as DefaultSidedInventory).inputSlots.withIndex()) {
            if (index.rem(2) == 0) {
                x = 1
                y++
            }
            x++
            root.add(WItemSlot.of(blockInventory, slot), x + .4, y + .5)
        }

        root.validate(this)
    }

    class Screen(controller: ChopperController, playerEntity: PlayerEntity)
        : CottonInventoryScreen<ChopperController>(controller, playerEntity)

    companion object {
        val SCREEN_ID = identifier("chopper_controller")
    }
}