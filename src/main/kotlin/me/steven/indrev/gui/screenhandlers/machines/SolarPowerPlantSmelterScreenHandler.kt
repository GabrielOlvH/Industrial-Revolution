package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.*
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.solarpowerplant.SolarPowerPlantSmelterBlockEntity
import me.steven.indrev.components.ComponentKey
import me.steven.indrev.components.ensureIsProvider
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.SOLAR_POWER_PLANT_SMELTER_HANDLER
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.util.math.MathHelper

class SolarPowerPlantSmelterScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiScreenHandler(
        SOLAR_POWER_PLANT_SMELTER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)

        root.add(WLabel("Solar Power Plant Smelter"), 0, 0)

        ctx.run { world, pos ->

            repeat(4) { slot ->
                val wSlot = WItemSlot.of(blockInventory, slot)

                root.add(wSlot, 0, 0)
                wSlot.setLocation((slot % 2) * 28 + 7, (slot / 2) * 20 + 18)
                val w = WSlotTemperature { component!!.get<Double>(slot).toFloat() }
                root.add(w, 0, 0)
                w.setLocation((slot % 2) * 28, (slot / 2) * 20 + 18)
            }
        }

        val inventoryPanel = createPlayerInventoryPanel()
        root.add(inventoryPanel, 0, 4)
        inventoryPanel.setLocation(0, 4 * 18 + 9)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    private class WSlotTemperature(val progress: () -> Float) : WWidget() {
        override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {

            ScreenDrawing.drawBeveledPanel(matrices, x, y, 7, 18)

            var percent = progress() / 800f
            if (percent <= 0) return
            percent = ((percent * 17).toInt() / 17f)
            if (percent <= 0) return
            if (percent < 0) percent = 0f
            if (percent > 1) percent = 1f
            val barSize = (percent * 17).toInt()
            var top = y + getHeight()
            top -= barSize
            ScreenDrawing.texturedRect(matrices,
                x + 1, top -1, 5, barSize, SLOT_TEMP_ICON, 0f,
                MathHelper.lerp(percent, 1f, 0f), 1f, 1f, -1
            )
        }
    }

    companion object {
        val SCREEN_ID = identifier("solar_power_plant_smelter")
        val SLOT_TEMP_ICON = identifier("textures/gui/temp_slot.png")
    }
}