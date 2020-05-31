package me.steven.indrev.gui.generators

import io.github.cottonmc.cotton.gui.CottonCraftingController
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.blockentities.TemperatureController
import me.steven.indrev.gui.widgets.EnergyWidget
import me.steven.indrev.gui.widgets.FuelWidget
import me.steven.indrev.gui.widgets.StringWidget
import me.steven.indrev.gui.widgets.TemperatureWidget
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

        root.add(StringWidget(I18n.translate("block.indrev.coal_generator"), titleColor), 4, 0)

        root.add(createPlayerInventoryPanel(), 0, 4)

        root.add(EnergyWidget(propertyDelegate), 0, 0, 16, 64)
        val batterySlot = WItemSlot.of(blockInventory, 0)
        root.add(batterySlot, 0, 4)
        batterySlot.setLocation(0, (3.7 * 18).toInt())

        blockContext.run { world, blockPos ->
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity is TemperatureController) {
                root.add(TemperatureWidget(propertyDelegate, blockEntity), 1, 0, 16, 64)
                val coolerSlot = WItemSlot.of(blockInventory, 1)
                root.add(coolerSlot, 1, 4)
                coolerSlot.setLocation(1 * 18, (3.7 * 18).toInt())
            }
        }

        val itemSlot = WItemSlot.of(blockInventory, 2)
        root.add(itemSlot, 4, 2)

        root.add(FuelWidget(propertyDelegate), 4, 1)

        root.validate(this)
    }
}