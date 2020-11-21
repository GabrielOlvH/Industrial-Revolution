package me.steven.indrev.utils

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import net.minecraft.util.Identifier

val UPGRADE_SLOT_PANEL_PAINTER = BackgroundPainter.createLightDarkVariants(
    BackgroundPainter.createNinePatch(Identifier("libgui", "textures/widget/panel_light.png"), 4),
    BackgroundPainter.createNinePatch(Identifier("libgui", "textures/widget/panel_dark.png"), 4)
)