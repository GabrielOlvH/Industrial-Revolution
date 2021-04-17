package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WDynamicLabel
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.generators.SteamTurbineBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class SteamTurbineScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        IndustrialRevolution.STEAM_TURBINE_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.steam_turbine_mk4", ctx, playerInventory, blockInventory)


        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? SteamTurbineBlockEntity ?: return@run

            root.add(WDynamicLabel { "Speed: " + String.format("%.2f", blockEntity.speed) }, 1, 1)
            root.add(WDynamicLabel { "MaxSpeed: " + String.format("%.2f", blockEntity.maxSpeed) }, 1, 2)
            root.add(WDynamicLabel { "Generating: " + String.format("%.2f", blockEntity.generating) }, 1, 3)
        }


        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/basic_machines")

    override fun getPage(): Int = 4

    companion object {
        val SCREEN_ID = identifier("steam_turbine")
    }
}