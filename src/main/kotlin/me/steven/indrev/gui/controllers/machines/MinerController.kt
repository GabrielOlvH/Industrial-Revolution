package me.steven.indrev.gui.controllers.machines

import dev.technici4n.fasttransferlib.api.Simulation
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItem
import io.github.cottonmc.cotton.gui.widget.WSprite
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
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
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

        val scanSlot = WTooltipedItemSlot.of(blockInventory, 14, TranslatableText("gui.indrev.scan_output_slot_type"))
        root.add(scanSlot, 7.0, 4.3)

        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? MinerBlockEntity ?: return@run
            val activeDrills = blockEntity.getActiveDrills()
            val bg = WStaticTooltip()
            root.add(bg, 1.5, 1.0)
            bg.setSize(70, 60)
            root.add(WText(TranslatableText("block.indrev.drill.active"), HorizontalAlignment.CENTER, 0x8080), 3.45, 1.0)
            when {
                blockEntity.extract(blockEntity.requiredPower, Simulation.SIMULATE) != blockEntity.requiredPower -> {
                    val sprite = object : WSprite(identifier("textures/gui/not_enough_power.png")) {
                        override fun addTooltip(tooltip: TooltipBuilder?) {
                            tooltip?.add(
                                TranslatableText("block.indrev.drill.not_enough_power").formatted(Formatting.DARK_RED),
                                TranslatableText("block.indrev.drill.power_required", blockEntity.requiredPower)
                                    .formatted(Formatting.DARK_RED)
                            )
                        }
                    }
                    root.add(sprite, 3.1, 1.6)
                    sprite.setSize(16, 16)
                }
                activeDrills.isEmpty() -> {
                    val noDrillsText = TranslatableText("block.indrev.drill.no_drills")
                    root.add(WText(noDrillsText, HorizontalAlignment.CENTER, 0x404040), 3.45, 1.85)
                }
                else -> {
                    activeDrills.forEachIndexed { index, drill ->
                        val panel = getDrillInfo(drill)
                        root.add(panel, 1.5 + if (index > 3) index - 4 else index, 1.5 + if (index > 3) 0.75 else 0.0)
                    }
                }
            }
            root.add(WText({
                val totalMultiplier = activeDrills.sumByDouble { it.getSpeedMultiplier() }
                TranslatableText("block.indrev.drill.faster", totalMultiplier)
            }, HorizontalAlignment.CENTER, 0x8080), 3.45, 3.2)
        }
        root.add(WText({
            TranslatableText("block.indrev.miner.mined", "${propertyDelegate[3]}%")
        }, HorizontalAlignment.CENTER, 0x8080), 3.45, 3.9)

        root.validate(this)
    }

    private fun getDrillInfo(blockEntity: DrillBlockEntity): WGridPanel {
        val itemStack = blockEntity.inventory[0]
        val panel = WGridPanel()
        panel.add(object : WItem(itemStack)  {
            override fun addTooltip(tooltip: TooltipBuilder?) {
                tooltip?.add(itemStack.name)
                val seconds = blockEntity.getSpeedMultiplier()
                tooltip?.add(TranslatableText("block.indrev.drill.faster", seconds).formatted(Formatting.DARK_GRAY))
                if (blockEntity.position > 0)
                    tooltip?.add(TranslatableText("block.indrev.drill.activating").formatted(Formatting.DARK_GRAY))
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