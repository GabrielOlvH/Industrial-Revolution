package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WSlider
import io.github.cottonmc.cotton.gui.widget.WSprite
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.gui.screenhandlers.CHOPPER_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.setIcon
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import me.steven.indrev.utils.translatable

class ChopperScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        CHOPPER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {

    var value = -1
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.chopper", ctx, playerInventory, blockInventory, invPos = 4.45)

        val inputFrame = WSprite(identifier("textures/gui/input_frame.png"))
        root.add(inputFrame, 1.9, 0.7)
        inputFrame.setSize(40, 44)
        val outputFrame = WSprite(identifier("textures/gui/output_frame.png"))
        root.add(outputFrame, 5.1, 0.7)
        outputFrame.setSize(58, 62)

        val outputSlot = WTooltipedItemSlot.of(blockInventory, 6, 3, 3, translatable("gui.indrev.output_slot_type"))
        outputSlot.isInsertingAllowed = false
        root.add(outputSlot, 5.2, 1.0)
        
        val axeSlot = WTooltipedItemSlot.of(blockInventory, 2, translatable("gui.indrev.chopper_input_axe"))
        axeSlot.setIcon(ctx, blockInventory, 2, AXE_ICON)
        root.add(axeSlot, 2.0, 1.0)

        val boneMealSlot = WTooltipedItemSlot.of(blockInventory, 3, translatable("gui.indrev.chopper_input_bone_meal"))
        boneMealSlot.setIcon(ctx, blockInventory, 3, BONE_MEAL_ICON)
        root.add(boneMealSlot, 3.0, 1.0)

        val saplingSlot = WTooltipedItemSlot.of(blockInventory, 4, translatable("gui.indrev.chopper_input_sapling"))
        saplingSlot.setIcon(ctx, blockInventory, 4, SAPLING_ICON)
        root.add(saplingSlot, 2.0, 2.0)

        val otherSaplingSlot = WTooltipedItemSlot.of(blockInventory, 5, translatable("gui.indrev.chopper_input_sapling"))
        otherSaplingSlot.setIcon(ctx, blockInventory, 5, SAPLING_ICON)
        root.add(otherSaplingSlot, 3.0, 2.0)

        val slider = WSlider(1, 9, Axis.HORIZONTAL)
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

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("chopper_controller")
        val AXE_ICON = identifier("textures/gui/axe_icon.png")
        val BONE_MEAL_ICON = identifier("textures/gui/bone_meal_icon.png")
        val SAPLING_ICON = identifier("textures/gui/sapling_icon.png")
    }
}