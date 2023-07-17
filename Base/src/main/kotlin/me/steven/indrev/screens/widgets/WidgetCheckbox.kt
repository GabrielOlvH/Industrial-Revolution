package me.steven.indrev.screens.widgets

import com.mojang.blaze3d.systems.RenderSystem
import me.steven.indrev.utils.identifier
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents

class WidgetCheckbox : Widget() {
    override var height: Int = 10
    override var width: Int = 10

    var checked = false
    var onChange: ((Boolean) -> Unit)? = null

    override fun draw(ctx: DrawContext, x: Int, y: Int) {
        ctx.drawTexture(CHECKBOX, x + 2, y + 2, 0f, 0f, 6, 6, 6, 6)

        if (checked) {
            RenderSystem.setShaderTexture(0, CHECKMARK)
            ctx.drawTexture(CHECKMARK, x + 2, y + 1, 0f, 0f, 9, 6, 9, 6)
        }
    }

    override fun drawMouseover(ctx: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ctx.fill(x+ 2, y+2, x + width-2, y+ height-2, 0x99555555.toInt())
    }

    override fun onClick(mouseX: Double, mouseY: Double, button: Int) {
        checked = !checked
        onChange?.invoke(checked)
        MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F))
    }

    companion object {
        val CHECKBOX = identifier("textures/gui/widgets/widget_checkbox.png")
        val CHECKMARK = identifier("textures/gui/widgets/widget_checkbox_selected.png")
    }
}