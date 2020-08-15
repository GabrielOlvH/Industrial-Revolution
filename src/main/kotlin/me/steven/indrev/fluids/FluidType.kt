package me.steven.indrev.fluids

import me.steven.indrev.utils.identifier
import net.minecraft.util.Identifier

enum class FluidType(val stillId: Identifier, val flowId: Identifier) {
    LAVA(identifier("block/molten_netherite_still"), identifier("block/molten_netherite_flow")),
    WATER(Identifier("block/water_still"), Identifier("block/water_flow"))
}