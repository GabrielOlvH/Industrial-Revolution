package me.steven.indrev.blocks.misc

import net.minecraft.block.OreBlock
import net.minecraft.util.math.intprovider.UniformIntProvider

class NikoliteOreBlock(settings: Settings) : OreBlock(settings, UniformIntProvider.create(1, 6))