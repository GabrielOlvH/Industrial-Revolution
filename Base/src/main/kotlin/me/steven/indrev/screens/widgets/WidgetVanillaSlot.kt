package me.steven.indrev.screens.widgets

import com.mojang.blaze3d.systems.RenderSystem
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.utils.argb
import me.steven.indrev.utils.identifier
import net.minecraft.client.gui.DrawContext
import net.minecraft.inventory.Inventory
import net.minecraft.screen.slot.Slot

class WidgetVanillaSlot(val index: Int, val inv: Inventory, val color: Int = -1) : Widget() {

    override var width: Int = 18
    override var height: Int = 18

    override fun draw(ctx: DrawContext, x: Int, y: Int) {
        val (alpha, red, green, blue) = argb(color)
        ctx.setShaderColor(red / 256f, green / 256f, blue / 256f, alpha / 256f)
        ctx.drawTexture(SLOT_TEXTURE, x, y, 0f, 0f, width, height, width, height)
        ctx.setShaderColor(1f, 1f, 1f, 1f)
    }

    override fun validate(handler: MachineScreenHandler) {
        handler.addSlot0(Slot(inv, index, x + 1, y + 1))
    }

    companion object {
        private val SLOT_TEXTURE = identifier("textures/gui/widget_machine_slot.png")
    }
}