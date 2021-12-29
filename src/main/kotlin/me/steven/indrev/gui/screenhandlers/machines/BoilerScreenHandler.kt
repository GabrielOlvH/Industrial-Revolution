package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WSprite
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.blockentities.solarpowerplant.BoilerBlockEntity
import me.steven.indrev.gui.screenhandlers.BOILER_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.fluidTank
import me.steven.indrev.gui.widgets.machines.temperatureBar
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.utils.add
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText

class BoilerScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        BOILER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)

        root.add(WText(TranslatableText("block.indrev.boiler"), HorizontalAlignment.CENTER, 0x404040), 4.7, 0.0)

        val leftoverSalt = WTooltipedItemSlot.of(blockInventory, 0, LiteralText("Salt leftover"))
        leftoverSalt.isInsertingAllowed = false
        root.add(leftoverSalt, 1.0, 2.0)

        withBlockEntity<BoilerBlockEntity> { be ->
            root.add(temperatureBar(be), 2.2, 0.7)

            val moltenSaltWidget = fluidTank(be, BoilerBlockEntity.MOLTEN_SALT_TANK)
            root.add(moltenSaltWidget, 0.1, 0.7)

            val saltSprite = WSprite(identifier("textures/gui/molten_salt_label.png"))
            root.add(saltSprite, 0.0, 3.0)
            saltSprite.setSize(32, 12)

            root.add(WSprite(identifier("textures/gui/widget_processing_split.png")), 1.1, 1.1)

            val waterWidget = fluidTank(be, BoilerBlockEntity.WATER_TANK)
            root.add(waterWidget, 5.0, 0.7)

            val waterSprite = WSprite(identifier("textures/gui/water_label.png"))
            root.add(waterSprite, 4.8, 3.0)
            waterSprite.setSize(32, 12)

            val steamWidget = fluidTank(be, BoilerBlockEntity.STEAM_TANK)
            root.add(steamWidget, 7.7, 0.7)

            val steamSprite = WSprite(identifier("textures/gui/steam_label.png"))
            root.add(steamSprite, 7.4, 3.0)
            steamSprite.setSize(32, 12)
        }

        root.add(WSprite(identifier("textures/gui/widget_processing_empty.png")), 6.3, 1.4)

        root.add(WSprite(identifier("textures/gui/widget_processing_empty.png")), 3.3, 1.4)

        root.add(createPlayerInventoryPanel(), 0.0, 3.8)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("boiler")
    }
}