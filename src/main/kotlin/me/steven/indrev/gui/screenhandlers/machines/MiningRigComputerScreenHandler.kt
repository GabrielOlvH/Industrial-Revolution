package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.api.OreDataCards
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.miningrig.DrillBlockEntity
import me.steven.indrev.blockentities.miningrig.MiningRigBlockEntity
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.MINING_RIG_HANDLER
import me.steven.indrev.gui.widgets.misc.WCircleProgressBar
import me.steven.indrev.gui.widgets.misc.WStaticTooltip
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import me.steven.indrev.utils.literal
import me.steven.indrev.utils.translatable
import net.minecraft.text.Text
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
        configure("block.indrev.mining_rig", ctx, playerInventory, blockInventory, invPos = 6.0, widgetPos = 1.5)


        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? MiningRigBlockEntity ?: return@run
            val activeDrills = blockEntity.getActiveDrills()
            val bg = WStaticTooltip()
            root.add(bg, 1.0, 0.9)
            bg.setSize(142, 85)

            root.add(WText(literal("Insert"), HorizontalAlignment.CENTER, 0x8080), 7.5, 1.9)
            root.add(WText(literal("data card"), HorizontalAlignment.CENTER, 0x8080), 7.5, 2.6)
            val cardSlot = WTooltipedItemSlot.of(blockInventory, 0, translatable("gui.indrev.scan_output_slo1t_type"))
            root.add(cardSlot, 7.0, 3.3)

            root.add(WText(translatable("block.indrev.drill.active"), HorizontalAlignment.CENTER, 0x8080), 3.35, 1.0)

            val requiredPower = component!!.get<Long>(MiningRigBlockEntity.ENERGY_REQUIRED_ID).toDouble()
            when {
                component!!.get<Double>(MachineBlockEntity.ENERGY_ID) < requiredPower -> {
                    val sprite = object : WSprite(identifier("textures/gui/not_enough_power.png")) {
                        override fun addTooltip(tooltip: TooltipBuilder?) {
                            tooltip?.add(
                                translatable("block.indrev.drill.not_enough_power").formatted(Formatting.DARK_RED),
                                translatable("block.indrev.drill.power_required", requiredPower)
                                    .formatted(Formatting.DARK_RED)
                            )
                        }
                    }
                    root.add(sprite, 3.0, 1.5)
                    sprite.setSize(16, 16)
                }
                activeDrills.isEmpty() -> {
                    val noDrillsText = translatable("block.indrev.drill.no_drills")
                    root.add(WText(noDrillsText, HorizontalAlignment.CENTER, 0x404040), 3.35, 1.75)
                }
                else -> {
                    activeDrills.forEachIndexed { index, drill ->
                        val panel = getDrillInfo(drill)
                        root.add(panel, 1.4 + if (index > 3) index - 4 else index, 1.9 + if (index > 3) 1.1 else 0.0)
                    }
                }
            }
        }
        root.add(WText({
            val data = OreDataCards.readNbt(blockInventory.getStack(0)) ?: return@WText literal("")
            val remaining = data.maxCycles - data.used
            literal("$remaining")
        }, HorizontalAlignment.CENTER, 0x8080), 3.35, 4.4)
        root.add(WText(translatable("block.indrev.mining_rig.mined"), HorizontalAlignment.CENTER, 0x8080), 3.35, 5.0)

        root.validate(this)
    }

    private fun getDrillInfo(blockEntity: DrillBlockEntity): WGridPanel {
        val itemStack = blockEntity.inventory[0]
        val panel = WGridPanel()

        panel.add(WItem(itemStack), 0, 0)

        ctx.run { world, pos ->
            val miningRig = world.getBlockEntity(pos) as? MiningRigBlockEntity ?: return@run
            val tmpPos = miningRig.pos.mutableCopy()
            tmpPos.y = blockEntity.pos.y
            val offset = blockEntity.pos.subtract(tmpPos)

            val index = MiningRigBlockEntity.VALID_DRILL_POSITIONS.indexOf(offset)
            val progress = object : WCircleProgressBar({ component!!.get<Double>(MiningRigBlockEntity.START_DRILL_ID + index).toInt() }, { component!![MiningRigBlockEntity.MAX_SPEED_ID] }, { _, _ -> 0xFF007E7E.toInt()}) {
                override fun addTooltip(tooltip: TooltipBuilder?) {
                    tooltip?.add(itemStack.name)
                    val seconds = blockEntity.getSpeedMultiplier()
                    tooltip?.add(translatable("block.indrev.drill.faster", seconds).formatted(Formatting.DARK_GRAY))
                    if (blockEntity.position > 0)
                        tooltip?.add(translatable("block.indrev.drill.activating").formatted(Formatting.DARK_GRAY))
                }
            }
            panel.add(progress, 0, 0)
        }

        return panel
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("mining_rig")
    }
}