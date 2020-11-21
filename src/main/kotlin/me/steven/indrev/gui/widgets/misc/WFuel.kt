package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.widget.WBar
import me.steven.indrev.utils.identifier

class WFuel : WBar(UNLIT_TEXTURE_ID, LIT_TEXTURE_ID, 4, 5, Direction.UP) {
    init {
        this.setSize(14, 14)
    }

    companion object {
        val LIT_TEXTURE_ID =
            identifier("textures/gui/widget_fuel_burning.png")
        val UNLIT_TEXTURE_ID =
            identifier("textures/gui/widget_fuel_not_burning.png")
    }
}