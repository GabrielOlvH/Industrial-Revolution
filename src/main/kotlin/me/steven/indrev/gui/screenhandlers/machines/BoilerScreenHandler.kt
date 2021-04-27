package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.solarpowerplant.BoilerBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.WFluid
import me.steven.indrev.gui.widgets.machines.WTemperature
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.utils.add
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

class BoilerScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        IndustrialRevolution.BOILER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)

        root.add(WText(TranslatableText("block.indrev.boiler"), HorizontalAlignment.CENTER, 0x404040), 5, 0)

        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? BoilerBlockEntity ?: return@run
            root.add(WTemperature(blockEntity.temperatureComponent), 0, 2)
        }

        val leftoverSalt = WItemSlot.of(blockInventory, 0)
        leftoverSalt.isInsertingAllowed = false
        root.add(leftoverSalt, 3.7, 2.2)

        val moltenSaltWidget = WFluid(ctx, 0)
        root.add(moltenSaltWidget, 2.5, 0.7)

        val waterWidget = WFluid(ctx, 1)
        root.add(waterWidget, 4.0, 0.7)

        val steamWidget = WFluid(ctx, 2)
        root.add(steamWidget, 7.7, 0.7)

        root.add(createPlayerInventoryPanel(), 0, 5)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/basic_machines")

    override fun getPage(): Int = 5

    companion object {
        val SCREEN_ID = identifier("boiler")
    }
}