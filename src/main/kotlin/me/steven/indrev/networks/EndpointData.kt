package me.steven.indrev.networks

import me.steven.indrev.utils.ItemFilter
import me.steven.indrev.utils.fluidStorageOf
import me.steven.indrev.utils.itemStorageOf
import me.steven.indrev.utils.sumOf
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import java.util.*
import kotlin.random.Random

data class EndpointData(var type: Type, var mode: Mode?) {

    fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putInt("t", type.ordinal)
        if (mode != null)
            tag.putInt("m", mode!!.ordinal)
        return tag
    }

    fun readNbt(tag: NbtCompound): EndpointData {
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
            override fun getFluidSorter(world: ServerWorld, type: Type, filter: (FluidVariant) -> Boolean): (Array<Any?>) -> Unit {
                return { array ->
                    Transaction.openOuter().use { tx ->
                        Arrays.sort(array,
                            if (type == Type.RETRIEVER)
                                Comparator.comparing<Any?, Long> { node ->
                                    node as Node
                                    fluidStorageOf(world, node.target, node.direction)?.iterator()?.sumOf { v -> if (!filter(v.resource)) 0 else v.amount } ?: 0
                                }.reversed()
                            else
                                Comparator.comparing { node ->
                                    node as Node
                                    fluidStorageOf(world, node.target, node.direction)?.iterator()?.sumOf { v -> if (!filter(v.resource)) 0 else v.amount } ?: 0
                                })
                        tx.abort()
                    }
                }
            }

            override fun getItemSorter(world: ServerWorld, type: Type, filter: ItemFilter): (Array<Any?>) -> Unit {
                return { array ->
                    Transaction.openOuter().use { tx ->
                        Arrays.sort(array,
                            (if (type == Type.RETRIEVER)
                                Comparator.comparing<Any, Long> { node ->
                                    node as Node
                                    itemStorageOf(world, node.target, node.direction)?.iterator()?.sumOf { v -> if (!filter(v.resource)) 0 else v.amount } ?: 0
                                }.reversed()
                            else
                                Comparator.comparing { node ->
                                    node as Node
                                    itemStorageOf(world, node.target, node.direction)?.iterator()?.sumOf { v -> if (!filter(v.resource)) 0 else v.amount } ?: 0
                                })
                        )
                    }

                }
            }
        },
        FURTHEST_FIRST {
            override fun getFluidSorter(world: ServerWorld, type: Type, filter: (FluidVariant) -> Boolean): (Array<Any?>) -> Unit {
                return { array ->
                    Arrays.sort(array) { first, second ->
                        first as Node
                        second as Node
                        when {
                            first.dist > second.dist -> -1
                            first.dist < second.dist -> 1
                            else -> 0
                        }
                    }
                }
            }

            override fun getItemSorter(world: ServerWorld, type: Type, filter: ItemFilter): (Array<Any?>) -> Unit {
                return { array ->
                    Arrays.sort(array) { first, second ->
                        first as Node
                        second as Node
                        when {
                            first.dist > second.dist -> -1
                            first.dist < second.dist -> 1
                            else -> 0
                        }
                    }
                }
            }
        },
        NEAREST_FIRST {
            override fun getFluidSorter(world: ServerWorld, type: Type, filter: (FluidVariant) -> Boolean): (Array<Any?>) -> Unit {
                return { array -> Arrays.sort(array) }
            }

            override fun getItemSorter(world: ServerWorld, type: Type, filter: ItemFilter): (Array<Any?>) -> Unit {
                return { array -> Arrays.sort(array) }
            }
        },
        RANDOM {
            override fun getFluidSorter(world: ServerWorld, type: Type, filter: (FluidVariant) -> Boolean): (Array<Any?>) -> Unit {
                return { array -> array.shuffle() }
            }

            override fun getItemSorter(world: ServerWorld, type: Type, filter: ItemFilter): (Array<Any?>) -> Unit {
                return { array -> array.shuffle() }
            }
        };

        abstract fun getFluidSorter(world: ServerWorld, type: Type, filter: (FluidVariant) -> Boolean): (Array<Any?>) -> Unit

        abstract fun getItemSorter(world: ServerWorld, type: Type, filter: ItemFilter): (Array<Any?>) -> Unit

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