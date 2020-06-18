package me.steven.indrev.utils

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.gui.widgets.EnergyWidget
import me.steven.indrev.gui.widgets.StringWidget
import me.steven.indrev.gui.widgets.TemperatureWidget
import net.minecraft.client.resource.language.I18n
import net.minecraft.inventory.Inventory
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.StringRenderable
import net.minecraft.text.TranslatableText

fun WGridPanel.add(w: WWidget, x: Double, y: Double, width: Double, height: Double) {
    this.add(w, x.toInt(), y.toInt(), width.toInt(), height.toInt())
    w.setLocation((x * 18).toInt(), (y * 18).toInt())
}

fun WGridPanel.add(w: WWidget, x: Double, y: Double) {
    this.add(w, x.toInt(), y.toInt())
    w.setLocation((x * 18).toInt(), (y * 18).toInt())
}

fun SyncedGuiDescription.configure(
    titleId: String,
    screenHandlerContext: ScreenHandlerContext,
    blockInventory: Inventory,
    propertyDelegate: PropertyDelegate
) {
    (rootPanel as WGridPanel).also {
        it.setSize(150, 120)
        it.add(createPlayerInventoryPanel(), 0, 5)
        it.add(StringWidget(I18n.translate(titleId), titleColor), 4, 0)

        it.add(EnergyWidget(propertyDelegate), 0, 0, 16, 64)

        val batterySlot = WItemSlot.of(blockInventory, 0)
        it.add(batterySlot, 0.0, 3.7)

        screenHandlerContext.run { world, blockPos ->
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity is UpgradeProvider) {
                for ((i, slot) in blockEntity.getUpgradeSlots().withIndex()) {
                    val s = WItemSlot.of(blockInventory, slot)
                    it.add(s, 8, i)
                }
            }
            if (blockEntity is MachineBlockEntity && blockEntity.temperatureController != null) {
                val controller = blockEntity.temperatureController!!
                it.add(TemperatureWidget(propertyDelegate, controller), 1, 0, 16, 64)
                val coolerSlot = WItemSlot.of(blockInventory, 1)
                it.add(coolerSlot, 1.0, 3.7)
            }
            if (blockEntity is AOEMachineBlockEntity) {
                val button = object : WButton(TranslatableText("block.indrev.aoe.toggle.btn")) {
                    override fun addTooltip(information: MutableList<StringRenderable>?) {
                        information?.add(TranslatableText("block.indrev.aoe.toggle.${blockEntity.renderWorkingArea}"))
                    }
                }
                button.setOnClick {
                    blockEntity.renderWorkingArea = !blockEntity.renderWorkingArea
                }
                it.add(button, 8.0, 4.0)
            }
        }
    }
}