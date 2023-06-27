package me.steven.indrev.screens.widgets

import me.steven.indrev.screens.machine.MachineScreenHandler
import net.minecraft.client.item.TooltipData
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

abstract class Widget {
    abstract var width: Int
    abstract var height: Int

    var x: Int = 0
    var y: Int = 0

    var tooltipBuilder: ((MutableList<Text>) -> Unit)? = null
    var tooltipData: () -> TooltipData? = { null }

    open fun draw(matrices: MatrixStack, x: Int, y: Int) {

    }

    open fun drawMouseover(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {

    }

    fun setTooltip(builder: (MutableList<Text>) -> Unit): Widget {
        this.tooltipBuilder = builder
        return this
    }

    fun setTooltipDataProvider(provider: () -> TooltipData?): Widget {
        this.tooltipData = provider
        return this
    }

    open fun onClick(mouseX: Double, mouseY: Double, button: Int) {

    }

    open fun validate(handler: MachineScreenHandler) {
    }
}