package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WSprite
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.solarpowerplant.BoilerBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.screenhandlers.BOILER_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.WFluid
import me.steven.indrev.gui.widgets.machines.WTemperature
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.utils.add
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

class BoilerScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        BOILER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)

        root.add(WText(TranslatableText("block.indrev.boiler"), HorizontalAlignment.CENTER, 0x404040), 4.7, 0.0)

        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? BoilerBlockEntity ?: return@run
            root.add(WTemperature(blockEntity.temperatureComponent), 2.2, 0.7)
        }

        val leftoverSalt = WTooltipedItemSlot.of(blockInventory, 0, LiteralText("Salt leftover"))
        leftoverSalt.isInsertingAllowed = false
        root.add(leftoverSalt, 1.0, 2.0)

        val moltenSaltWidget = WFluid(ctx, propertyDelegate,0, BoilerBlockEntity.MOLTEN_SALT_TANK_SIZE, BoilerBlockEntity.MOLTEN_SALT_TANK_AMOUNT_ID, BoilerBlockEntity.MOLTEN_SALT_TANK_FLUID_ID)
        root.add(moltenSaltWidget, 0.1, 0.7)

        val saltSprite = WSprite(identifier("textures/gui/molten_salt_label.png"))
        root.add(saltSprite, 0.0, 3.0)
        saltSprite.setSize(32, 12)

        root.add(WSprite(identifier("textures/gui/widget_processing_split.png")), 1.1, 1.1)

        val waterWidget = WFluid(ctx, propertyDelegate, 1, BoilerBlockEntity.WATER_TANK_SIZE, BoilerBlockEntity.WATER_TANK_AMOUNT_ID, BoilerBlockEntity.WATER_TANK_FLUID_ID)
        root.add(waterWidget, 5.0, 0.7)

        val waterSprite = WSprite(identifier("textures/gui/water_label.png"))
        root.add(waterSprite, 4.8, 3.0)
        waterSprite.setSize(32, 12)

        val steamWidget = WFluid(ctx, propertyDelegate,2, BoilerBlockEntity.STEAM_TANK_SIZE, BoilerBlockEntity.STEAM_TANK_AMOUNT_ID, BoilerBlockEntity.STEAM_TANK_FLUID_ID)
        root.add(steamWidget, 7.7, 0.7)

        val steamSprite = WSprite(identifier("textures/gui/steam_label.png"))
        root.add(steamSprite, 7.4, 3.0)
        steamSprite.setSize(32, 12)

        root.add(WSprite(identifier("textures/gui/widget_processing_empty.png")), 6.3, 1.4)

        root.add(WSprite(identifier("textures/gui/widget_processing_empty.png")), 3.3, 1.4)

        root.add(createPlayerInventoryPanel(), 0.0, 3.8)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/basic_machines")

    override fun getPage(): Int = 5

    companion object {
        val SCREEN_ID = identifier("boiler")
    }
}