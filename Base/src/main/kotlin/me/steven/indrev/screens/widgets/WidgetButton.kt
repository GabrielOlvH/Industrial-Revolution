package me.steven.indrev.screens.widgets

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier

open class WidgetButton(val icon: Identifier) : Widget() {

    var click: (mouseX: Double, mouseY: Double, button: Int) -> Unit = { _, _, _ -> }
    override var width: Int = 18
    override var height: Int = 18

    var enabled = true
    var disabledIcon: Identifier? = null

    override fun draw(matrices: MatrixStack, x: Int, y: Int) {
        RenderSystem.setShaderTexture(0, if (!enabled && disabledIcon != null) disabledIcon else icon)
        RenderSystem.enableBlend()
        DrawableHelper.drawTexture(matrices, x, y, 0f, 0f, width, height, width, height)
        if (!enabled && disabledIcon == null) {
            DrawableHelper.fill(matrices, x, y, x + width, y + height, 0x99999999.toInt())
        }
    }

    override fun onClick(mouseX: Double, mouseY: Double, button: Int) {
        if (enabled) {
            click(mouseX, mouseY, button)
            MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F))

        }
    }
}