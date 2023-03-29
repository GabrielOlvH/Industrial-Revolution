package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import me.steven.indrev.blockentities.solarpowerplant.SolarPowerPlantTowerBlockEntity
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.SOLAR_POWER_PLANT_TOWER_HANDLER
import me.steven.indrev.gui.widgets.machines.fluidTank
import me.steven.indrev.gui.widgets.machines.temperatureBar
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.literal
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class SolarPowerPlantTowerScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiScreenHandler(
        SOLAR_POWER_PLANT_TOWER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)

        root.add(WLabel(literal("Solar Power Plant Tower")), 0, 0)

        withBlockEntity<SolarPowerPlantTowerBlockEntity> { be ->
            val wFluid = fluidTank(be, SolarPowerPlantTowerBlockEntity.INPUT_TANK_ID)
            root.add(wFluid, 8, 0)
            wFluid.setLocation(8 * 18, 8)
            val wTemp = temperatureBar(be)
            root.add(wTemp, 0, 0)
            wTemp.setLocation(0, 8)

        }

        val inventoryPanel = createPlayerInventoryPanel()
        root.add(inventoryPanel, 0, 4)
        inventoryPanel.setLocation(0, 4 * 18 + 9)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("solar_power_plant_tower")
    }
}