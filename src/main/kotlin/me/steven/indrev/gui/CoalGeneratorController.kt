package me.steven.indrev.gui

import io.github.cottonmc.cotton.gui.CottonCraftingController
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.gui.widgets.EnergyWidget
import me.steven.indrev.gui.widgets.FuelWidget
import me.steven.indrev.gui.widgets.StringWidget
import net.minecraft.client.resource.language.I18n
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.recipe.RecipeType

class CoalGeneratorController(syncId: Int, playerInventory: PlayerInventory, blockContext: BlockContext) :
    CottonCraftingController(RecipeType.SMELTING, syncId, playerInventory, getBlockInventory(blockContext), getBlockPropertyDelegate(blockContext)) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(150, 120)

        root.add(createPlayerInventoryPanel(), 0, 4)

        root.add(EnergyWidget(propertyDelegate), 0, 0, 16, 64)

        val itemSlot = WItemSlot.of(blockInventory, 0)
        root.add(itemSlot, 4, 2)

        root.add(FuelWidget(propertyDelegate), 4, 1)

        val string = I18n.translate("gui.widget.output", 0)
        root.add(StringWidget(string, this.titleColor), 4, 0)

        root.validate(this)
    }
}