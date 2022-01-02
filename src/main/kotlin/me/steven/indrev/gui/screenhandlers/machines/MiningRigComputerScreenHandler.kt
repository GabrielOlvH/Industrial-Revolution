package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.drill.DrillBlockEntity
import me.steven.indrev.blockentities.farms.MiningRigBlockEntity
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.MINING_RIG_HANDLER
import me.steven.indrev.gui.widgets.misc.WStaticTooltip
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

class MiningRigComputerScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        MINING_RIG_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.mining_rig", ctx, playerInventory, blockInventory, invPos = 5.0, widgetPos = 0.7)

        val outputSlots = WTooltipedItemSlot.of(blockInventory, 1, 3, 3, TranslatableText("gui.indrev.output_slot_type"))
        outputSlots.isInsertingAllowed = false
        root.add(outputSlots, 6.0, 0.85)

        val scanSlot = WTooltipedItemSlot.of(blockInventory, 14, TranslatableText("gui.indrev.scan_output_slot_type"))
        root.add(scanSlot, 7.0, 4.3)
        root.add(object : WWidget() {
            override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
                if (!component!!.get<Boolean>(MiningRigBlockEntity.LOCATION_ID))
                    ScreenDrawing.coloredRect(matrices, x, y, width, height, 0x88ff6666.toInt())
            }
                                    }, 7.0, 4.3)

        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? MiningRigBlockEntity ?: return@run
            val activeDrills = blockEntity.getActiveDrills()
            val bg = WStaticTooltip()
            root.add(bg, 1.5, 0.9)
            bg.setSize(70, 60)
            root.add(WText(TranslatableText("block.indrev.drill.active"), HorizontalAlignment.CENTER, 0x8080), 3.45, 1.0)

            val requiredPower = component!!.get<Long>(MiningRigBlockEntity.ENERGY_REQUIRED_ID).toDouble()
            when {
                component!!.get<Double>(MachineBlockEntity.ENERGY_ID) < requiredPower -> {
                    val sprite = object : WSprite(identifier("textures/gui/not_enough_power.png")) {
                        override fun addTooltip(tooltip: TooltipBuilder?) {
                            tooltip?.add(
                                TranslatableText("block.indrev.drill.not_enough_power").formatted(Formatting.DARK_RED),
                                TranslatableText("block.indrev.drill.power_required", requiredPower)
                                    .formatted(Formatting.DARK_RED)
                            )
                        }
                    }
                    root.add(sprite, 3.1, 1.5)
                    sprite.setSize(16, 16)
                }
                activeDrills.isEmpty() -> {
                    val noDrillsText = TranslatableText("block.indrev.drill.no_drills")
                    root.add(WText(noDrillsText, HorizontalAlignment.CENTER, 0x404040), 3.45, 1.75)
                }
                else -> {
                    activeDrills.forEachIndexed { index, drill ->
                        val panel = getDrillInfo(drill)
                        root.add(panel, 1.6 + if (index > 3) index - 4 else index, 1.4 + if (index > 3) 0.75 else 0.0)
                    }
                }
            }
            root.add(WText({
                val totalMultiplier = activeDrills.sumOf { it.getSpeedMultiplier() }
                TranslatableText("block.indrev.drill.faster", totalMultiplier)
            }, HorizontalAlignment.CENTER, 0x8080), 3.45, 3.1)
        }
        root.add(WText({
            TranslatableText("block.indrev.mining_rig.mined", "${component!!.get<Int>(MiningRigBlockEntity.EXPLORED_PERCENTAGE_ID)}%")
        }, HorizontalAlignment.CENTER, 0x8080), 3.45, 3.8)

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

    companion object {
        val SCREEN_ID = identifier("mining_rig")
    }
}