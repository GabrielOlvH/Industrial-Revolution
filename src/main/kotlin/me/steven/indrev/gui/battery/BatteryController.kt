package me.steven.indrev.gui.battery

import io.github.cottonmc.cotton.gui.CottonCraftingController
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.gui.widgets.EnergyWidget
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerInventory

class BatteryController(syncId: Int, playerInventory: PlayerInventory, blockContext: BlockContext)
    : CottonCraftingController(null, syncId, playerInventory, getBlockInventory(blockContext), getBlockPropertyDelegate(blockContext)) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(150, 120)

        root.add(EnergyWidget(propertyDelegate), 0, 0, 16, 64)

        root.add(WItemSlot.of(blockInventory, 0), 5, 2)

        root.add(createPlayerInventoryPanel(), 0, 4)

        root.validate(this)
    }
}