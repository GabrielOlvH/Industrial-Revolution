package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WDynamicLabel
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import me.steven.indrev.blockentities.generators.SteamTurbineBlockEntity
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.STEAM_TURBINE_HANDLER
import me.steven.indrev.gui.widgets.misc.WKnob
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class SteamTurbineScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        STEAM_TURBINE_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.steam_turbine_mk4", ctx, playerInventory, blockInventory)

        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? SteamTurbineBlockEntity ?: return@run

            root.add(WDynamicLabel { "  Efficiency: " + String.format("%.2f", blockEntity.efficiency) }, 1, 1)
            root.add(WDynamicLabel {
               // val a = blockEntity.consuming.asInt(1000).coerceAtLeast(1)
               // val inexact = blockEntity.consuming.asInt(1000)
               // val prefix = if (inexact > a) ">" else if (inexact < a) "<" else ""
                //"  Consuming: $prefix$a mB"
                                   ""
            }, 1, 2)
            root.add(WDynamicLabel { "  Generating: ${propertyDelegate[SteamTurbineBlockEntity.GENERATING] / 100.0}" }, 1, 3)

            //root.add(WFluid(ctx, 0), -1, 0)

            val wKnob = WKnob((propertyDelegate[SteamTurbineBlockEntity.EFFICIENCY] * 3f) + 30f, pos)
            root.add(wKnob, 7, 4)

            wKnob.setSize(32, 32)
            wKnob.setLocation(7 * 18, 2 * 18 + 9)
        }
        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("steam_turbine")
    }
}