package me.steven.indrev.screens.widgets

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

open class WidgetSprite(private val id: Identifier, override var width: Int, override var height: Int): Widget() {
    override fun draw(matrices: MatrixStack, x: Int, y: Int) {
        RenderSystem.setShaderTexture(0, id)
        DrawableHelper.drawTexture(matrices, x, y, 0f, 0f, width, height, width, height)
    }

}