package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WSlider
import io.github.cottonmc.cotton.gui.widget.WSprite
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.gui.screenhandlers.FARMER_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import me.steven.indrev.utils.translatable

class FarmerScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiScreenHandler(
        FARMER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    private var value = -1
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.farmer", ctx, playerInventory, blockInventory, invPos = 4.45)

        val inputFrame = WSprite(identifier("textures/gui/input_frame.png"))
        root.add(inputFrame, 1.9, 0.7)
        inputFrame.setSize(40, 44)
        val outputFrame = WSprite(identifier("textures/gui/output_frame.png"))
        root.add(outputFrame, 5.1, 0.7)
        outputFrame.setSize(58, 62)

        val outputSlot = WTooltipedItemSlot.of(blockInventory, (blockInventory as IRInventory).outputSlots.first(), 3, 3, translatable("gui.indrev.output_slot_type"))
        outputSlot.isInsertingAllowed = false
        root.add(outputSlot, 5.2, 1.0)
        val inputSlot = WTooltipedItemSlot.of(blockInventory, (blockInventory as IRInventory).inputSlots.first(), 2, 2, translatable("gui.indrev.farmer_input_slot_type"))
        root.add(inputSlot, 2.0, 1.0)

        val slider = WSlider(1, 10, Axis.HORIZONTAL)
        root.add(slider, 1.6, 3.6)
        slider.setSize(50, 20)
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? AOEMachineBlockEntity<*> ?: return@run
            slider.value = blockEntity.range
        }
        slider.setValueChangeListener { newValue -> this.value = newValue }

        val text = WText({
            translatable("block.indrev.aoe.range", slider.value)
        }, HorizontalAlignment.LEFT)
        root.add(text, 1.8, 3.3)

        root.validate(this)
    }

    override fun close(player: PlayerEntity?) {
        super.close(player)
        AOEMachineBlockEntity.sendValueUpdatePacket(value, ctx)
    }

    companion object {
        val SCREEN_ID = identifier("farmer_screen")
    }
}