package me.steven.indrev.compat.modmenu

import io.github.prospector.modmenu.api.ConfigScreenFactory
import io.github.prospector.modmenu.api.ModMenuApi
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import me.steven.indrev.config.IRConfig
import net.minecraft.client.gui.screen.Screen

object ModMenuCompat : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> =
        ConfigScreenFactory { screen -> AutoConfig.getConfigScreen(IRConfig::class.java, screen).get() }
}