package me.steven.indrev.networks.fluid

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import me.steven.indrev.networks.Node
import me.steven.indrev.utils.groupedFluidInv
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.World
import java.util.*
import kotlin.random.Random

data class FluidEndpointData(var type: Type, var mode: Mode) {

    fun toTag(tag: CompoundTag): CompoundTag {
        tag.putInt("t", type.ordinal)
        tag.putInt("m", mode.ordinal)
        return tag
    }

    companion object {
        fun fromTag(tag: CompoundTag): FluidEndpointData {
            val type = Type.VALUES[tag.getInt("t")]
            val mode = Mode.VALUES[tag.getInt("m")]
            return FluidEndpointData(type, mode)
        }
    }

    enum class Type {
        RETRIEVER,
        OUTPUT,
        INPUT;

        companion object {
            val VALUES = values()
        }
    }

    enum class Mode(val comparator: (World, Type, FluidFilter) -> Comparator<Node>) {
        ROUND_ROBIN({ world, type, fluid ->
            if (type == Type.RETRIEVER)
                Comparator.comparing<Node, FluidAmount> { node ->
                    groupedFluidInv(world, node.target, node.direction).getAmount_F(fluid)
                }.reversed()
            else
                Comparator.comparing {
                    groupedFluidInv(world, it.target, it.direction).getAmount_F(fluid)
                }
        }),
        FURTHEST_FIRST({ _, _, _ ->
            Comparator { first, second ->
                when {
                    first.dist > second.dist -> -1
                    first.dist < second.dist -> 1
                    else -> 0
                }
            }
        }),
        NEAREST_FIRST({ _, _, _ -> Comparator.comparing { it } }),
        RANDOM({ _, _, _ -> Comparator.comparing { R.nextInt(3) - 1 } });

        fun next(): Mode {
            return when (this) {
                ROUND_ROBIN -> FURTHEST_FIRST
                FURTHEST_FIRST -> NEAREST_FIRST
                NEAREST_FIRST -> RANDOM
                RANDOM -> ROUND_ROBIN
            }
        }

        companion object {
            val R = Random(System.currentTimeMillis())
            val VALUES = values()
        }
    }
}