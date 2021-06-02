package me.steven.indrev.networks

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.item.filter.ItemFilter
import me.steven.indrev.utils.groupedFluidInv
import me.steven.indrev.utils.groupedItemInv
import net.minecraft.nbt.NbtCompound
import net.minecraft.world.World
import kotlin.random.Random

open class EndpointData(var type: Type, var mode: Mode?) {

    open fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putInt("t", type.ordinal)
        if (mode != null)
            tag.putInt("m", mode!!.ordinal)
        return tag
    }

    open fun readNbt(tag: NbtCompound): EndpointData {
        val type = Type.VALUES[tag.getInt("t")]
        val mode = if (tag.contains("m")) Mode.VALUES[tag.getInt("m")] else null
        return EndpointData(type, mode)
    }

    enum class Type {
        RETRIEVER,
        OUTPUT,
        INPUT;

        companion object {
            val VALUES = values()
        }
    }

    enum class Mode {
        ROUND_ROBIN {
            override fun getFluidComparator(world: World, type: Type, filter: FluidFilter): Comparator<Node> {
                return if (type == Type.RETRIEVER)
                    Comparator.comparing<Node, FluidAmount> { node ->
                        groupedFluidInv(world, node.target, node.direction).getAmount_F(filter)
                    }.reversed()
                else
                    Comparator.comparing {
                        groupedFluidInv(world, it.target, it.direction).getAmount_F(filter)
                    }
            }

            override fun getItemComparator(world: World, type: Type, filter: ItemFilter): Comparator<Node> {
                return if (type == Type.RETRIEVER)
                    Comparator.comparing<Node, Int> { node ->
                        groupedItemInv(world, node.target, node.direction).getAmount(filter)
                    }.reversed()
                else
                    Comparator.comparing {
                        groupedItemInv(world, it.target, it.direction).getAmount(filter)
                    }
            }
        },
        FURTHEST_FIRST {
            override fun getFluidComparator(world: World, type: Type, filter: FluidFilter): Comparator<Node> {
                return Comparator { first, second ->
                    when {
                        first.dist > second.dist -> -1
                        first.dist < second.dist -> 1
                        else -> 0
                    }
                }
            }

            override fun getItemComparator(world: World, type: Type, filter: ItemFilter): Comparator<Node> {
                return Comparator { first, second ->
                    when {
                        first.dist > second.dist -> -1
                        first.dist < second.dist -> 1
                        else -> 0
                    }
                }
            }
        },
        NEAREST_FIRST {
            override fun getFluidComparator(world: World, type: Type, filter: FluidFilter): Comparator<Node> {
                return Comparator.comparing { it }
            }

            override fun getItemComparator(world: World, type: Type, filter: ItemFilter): Comparator<Node> {
                return Comparator.comparing { it }
            }
        },
        RANDOM {
            override fun getFluidComparator(world: World, type: Type, filter: FluidFilter): Comparator<Node> {
                return Comparator.comparing { R.nextInt(3) - 1 }
            }

            override fun getItemComparator(world: World, type: Type, filter: ItemFilter): Comparator<Node> {
                return Comparator.comparing { R.nextInt(3) - 1 }
            }
        };

        abstract fun getFluidComparator(world: World, type: Type, filter: FluidFilter): Comparator<Node>

        abstract fun getItemComparator(world: World, type: Type, filter: ItemFilter): Comparator<Node>

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