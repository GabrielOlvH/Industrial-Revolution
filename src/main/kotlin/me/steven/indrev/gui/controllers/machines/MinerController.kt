package me.steven.indrev.gui.controllers.machines

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItem
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.drill.DrillBlockEntity
import me.steven.indrev.blockentities.farms.MinerBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.widgets.misc.WStaticTooltip
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

class MinerController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiController(
        IndustrialRevolution.MINER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.miner", ctx, playerInventory, blockInventory)

        val outputSlots = WTooltipedItemSlot.of(blockInventory, 1, 3, 3, TranslatableText("gui.indrev.output_slot_type"))
        outputSlots.isInsertingAllowed = false
        root.add(outputSlots, 6.0, 0.85)

        //root.add(WText({
            //TranslatableText("block.indrev.miner.mined", "${propertyDelegate[3]}%")
        //}, HorizontalAlignment.CENTER), 3.0, 3.2)

        val scanSlot = WTooltipedItemSlot.of(blockInventory, 14, TranslatableText("gui.indrev.scan_output_slot_type"))
        root.add(scanSlot, 7.0, 4.3)

        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? MinerBlockEntity ?: return@run
            val activeDrills = blockEntity.getActiveDrills()
            if (activeDrills.isEmpty()) {
                val wText = io.github.cottonmc.cotton.gui.widget.WText(LiteralText("No active drills detected..."))
                root.add(wText, 1, 1)
                wText.setSize(85, 18)
                return@run
            }
            val bg = WStaticTooltip()
            root.add(bg, 1.5, 1.0)
            bg.setSize(70, 55)
            root.add(WText(LiteralText("Active drills"), HorizontalAlignment.CENTER, 0x4040), 3.45, 1.0)
            activeDrills.forEachIndexed { index, drill ->
                val panel = getDrillInfo(drill)
                root.add(panel, 1.5 + index, 2.0)
            }
            val totalMultiplier = IndustrialRevolution.CONFIG.machines.miner.processSpeed / (IndustrialRevolution.CONFIG.machines.miner.processSpeed / activeDrills.sumByDouble { DrillBlockEntity.getSpeedMultiplier(it.inventory[0].item) })
            root.add(WText(LiteralText("${totalMultiplier}x faster"), HorizontalAlignment.CENTER, 0x4040), 3.45, 3.5)
        }

        root.validate(this)
    }

    private fun getDrillInfo(blockEntity: DrillBlockEntity): WGridPanel {
        val itemStack = blockEntity.inventory[0]
        val panel = WGridPanel()
        panel.add(object : WItem(itemStack)  {
            override fun addTooltip(tooltip: TooltipBuilder?) {
                tooltip?.add(itemStack.name)
                val seconds = IndustrialRevolution.CONFIG.machines.miner.processSpeed / (IndustrialRevolution.CONFIG.machines.miner.processSpeed / DrillBlockEntity.getSpeedMultiplier(itemStack.item))
                tooltip?.add(LiteralText("${seconds}x faster"))
            }
        }, 0, 0)

        return panel
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/miner")

    override fun getPage(): Int = 0

    companion object {
        val SCREEN_ID = identifier("miner")
    }
}