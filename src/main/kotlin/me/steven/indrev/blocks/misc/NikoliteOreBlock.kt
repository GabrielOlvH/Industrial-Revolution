package me.steven.indrev.blocks.misc

import net.minecraft.block.OreBlock
import java.util.*

class NikoliteOreBlock(settings: Settings) : OreBlock(settings) {
    override fun getExperienceWhenMined(random: Random): Int {
        return 1 + random.nextInt(5)
    }
}