package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.solarpowerplant.SolarPowerPlantTowerBlockEntity
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.WFluid
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class SolarPowerPlantTowerScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiScreenHandler(
        IndustrialRevolution.SOLAR_POWER_PLANT_TOWER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)

        root.add(WLabel("Solar Power Plant Tower"), 0, 0)

        val wFluid = WSPPTFluid(ctx, 0)
        root.add(wFluid, 8, 0)
        wFluid.setLocation(8 * 18, 8)

        val inventoryPanel = createPlayerInventoryPanel()
        root.add(inventoryPanel, 0, 4)
        inventoryPanel.setLocation(0, 4 * 18 + 9)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    private class WSPPTFluid(private val ctx: ScreenHandlerContext, tank: Int) : WFluid(ctx, tank) {
        override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
            ScreenDrawing.texturedRect(x, y, width, height, ENERGY_EMPTY, -1)
            ctx.run { world, pos ->
                val blockEntity = world.getBlockEntity(pos)
                if (blockEntity is SolarPowerPlantTowerBlockEntity) {
                    val fluid = blockEntity.fluidComponent
                    val energy = fluid.tanks[tank].amount().asInexactDouble() * 1000
                    val maxEnergy = fluid.limit.asInexactDouble() * 1000
                    if (energy > 0) {
                        var percent = energy.toFloat() / maxEnergy.toFloat()
                        percent = (percent * height).toInt() / height.toFloat()
                        val barSize = (height * percent).toInt()
                        if (barSize > 0) {
                            val offset = 2.0
                            blockEntity.fluidComponent.tanks[tank].renderGuiRect(
                                x + offset,
                                y.toDouble() + height - barSize + offset,
                                x.toDouble() + width - offset,
                                y.toDouble() + height - offset
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        val SCREEN_ID = identifier("solar_power_plant_tower")
    }
}