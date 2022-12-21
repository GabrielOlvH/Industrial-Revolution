package me.steven.indrev.screens.widgets

import com.mojang.blaze3d.systems.RenderSystem
import me.steven.indrev.components.MachineItemInventory
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.MachineSlot
import me.steven.indrev.utils.argb
import me.steven.indrev.utils.identifier
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

class WidgetSlot(val index: Int, val inv: MachineItemInventory, val color: Int = -1, val big: Boolean = false) : WidgetSprite(MACHINE_SLOT_TEXTURE, 22, 22) {

    var slotTexture: Identifier? = if(big) MACHINE_BIG_SLOT_TEXTURE else MACHINE_SLOT_TEXTURE
    var overlay: Identifier? = null
    var filter: ((ItemStack) -> Boolean)? = null

    override fun draw(matrices: MatrixStack, x: Int, y: Int) {
        if (slotTexture != null) {
            RenderSystem.setShaderTexture(0, slotTexture)

            val (alpha, red, green, blue) = argb(color)
            RenderSystem.setShaderColor(red / 256f, green / 256f, blue / 256f, alpha / 256f)
            if (!big) {
                DrawableHelper.drawTexture(matrices, x - (width - 18) / 2, y - (height - 18) / 2, 0f, 0f, width, height, width, height)
            } else {
                DrawableHelper.drawTexture(matrices, x - 6, y - 6, 0f, 0f, 30, 30, 30, 30)
            }
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)

        if (overlay != null && inv[index].isEmpty()) {
            RenderSystem.setShaderTexture(0, overlay)
            DrawableHelper.drawTexture(matrices, x + 1, y + 1, 0f, 0f, 16, 16, 16, 16)
        }
    }

    override fun validate(handler: MachineScreenHandler) {
        handler.addSlot0(MachineSlot(inv, index, x + 1, y + 1, filter))
    }

    companion object {
        val MACHINE_SLOT_TEXTURE = identifier("textures/gui/widgets/widget_machine_slot.png")
        val MACHINE_BIG_SLOT_TEXTURE = identifier("textures/gui/widgets/widget_machine_big_slot.png")

        val ITEM_SLOT_TEXTURE = identifier("textures/gui/widgets/widget_item_slot.png")
    }
}