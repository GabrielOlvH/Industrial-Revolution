package me.steven.indrev.screens.widgets

import com.mojang.blaze3d.systems.RenderSystem
import me.steven.indrev.components.MachineItemInventory
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.MachineSlot
import me.steven.indrev.utils.argb
import me.steven.indrev.utils.identifier
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

class WidgetSlot(val index: Int, val inv: MachineItemInventory, val color: Int = -1, val big: Boolean = false) : WidgetSprite(MACHINE_SLOT_TEXTURE, 22, 22) {

    var slotTexture: Identifier? = if(big) MACHINE_BIG_SLOT_TEXTURE else MACHINE_SLOT_TEXTURE
    var overlay: Identifier? = null
    var filter: ((ItemStack) -> Boolean)? = null

    override fun draw(ctx: DrawContext, x: Int, y: Int) {
        ctx.matrices.push()
        ctx.matrices.translate(0.0, 0.0, 20.0)
        if (slotTexture != null) {
            val (alpha, red, green, blue) = argb(color)
            if (!big) {

                ctx.setShaderColor(red / 256f, green / 256f, blue / 256f, alpha / 256f)
                ctx.drawTexture(slotTexture, x - (width - 18) / 2, y - (height - 18) / 2, 0f, 0f, width, height, width, height)
            } else {
                ctx.setShaderColor(red / 256f, green / 256f, blue / 256f, alpha / 256f)
                ctx.drawTexture(slotTexture, x - 6, y - 6, 0f, 0f, 30, 30, 30, 30)
            }
        }

        ctx.setShaderColor(1f, 1f, 1f, 1f)

        if (overlay != null && inv[index].isEmpty()) {
            ctx.drawTexture(overlay, x + 1, y + 1, 0f, 0f, 16, 16, 16, 16)
        }
        ctx.matrices.pop()
    }

    override fun drawHighlight(ctx: DrawContext, x: Int, y: Int, width: Int, height: Int) {
        if (!big)
            super.drawHighlight(ctx, x- (width - 18) / 2, y- (height - 18) / 2, width, height)
        else
            super.drawHighlight(ctx, x - 6, y - 6, 30, 30)
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