package me.steven.indrev.networks.item

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.ServoNetworkState
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.function.LongFunction

class ItemNetworkState(world: ServerWorld) : ServoNetworkState<ItemNetwork>(Network.Type.ITEM, world) {

    val filters = Long2ObjectOpenHashMap<Object2ObjectOpenHashMap<Direction, ItemFilterData>>()
    private val recentlyRemovedFilters = Long2ObjectOpenHashMap<Object2ObjectOpenHashMap<Direction, ItemFilterData>>()

    override fun onRemoved(pos: BlockPos) {
        super.onRemoved(pos)
        if (filters.containsKey(pos.asLong()))
            recentlyRemovedFilters[pos.asLong()] = filters.remove(pos.asLong())
    }

    override fun onSet(blockPos: BlockPos, network: ItemNetwork) {
        super.onSet(blockPos, network)
        if (recentlyRemovedFilters.containsKey(blockPos.asLong())) {
            filters[blockPos.asLong()] = recentlyRemovedFilters.remove(blockPos.asLong())
        }
    }

    override fun clearCachedData(importCache: Boolean) {
        super.clearCachedData(importCache)
        if (importCache) {
            recentlyRemovedFilters.forEach { e -> filters[e.key] = e.value }
        }
        recentlyRemovedFilters.clear()
    }

    fun getFilterData(pos: BlockPos, direction: Direction, createIfAbsent: Boolean = false): ItemFilterData {
        return if (createIfAbsent)
            filters.computeIfAbsent(pos.asLong(), LongFunction { Object2ObjectOpenHashMap() }).computeIfAbsent(direction) { ItemFilterData() }
        else
            filters.get(pos.asLong())?.get(direction) ?: ItemFilterData.ACCEPTING_FILTER_DATA
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        val filtersTag = NbtList()
        filters.forEach { (pos, modes) ->
            val sidesTag = NbtList()
            modes.forEach { (dir, filterData) ->
                if (filterData != ItemFilterData.ACCEPTING_FILTER_DATA) {
                    val t = NbtCompound()
                    t.put(dir.ordinal.toString(), filterData.writeNbt(NbtCompound()))
                    sidesTag.add(t)
                }
            }
            val posTag = NbtCompound()
            posTag.putLong("pos", pos)
            posTag.put("sides", sidesTag)
            filtersTag.add(posTag)

        }
        tag.put("filters", filtersTag)
        return super.writeNbt(tag)
    }

    companion object {
        fun readNbt(tag: NbtCompound, supplier: () -> ItemNetworkState): ItemNetworkState {
            val state = supplier()
            val modesTag = tag.getList("filters", 10)
            modesTag.forEach { posTag ->
                posTag as NbtCompound
                val pos = posTag.getLong("pos")
                val map = Object2ObjectOpenHashMap<Direction, ItemFilterData>()
                val sidesTag = posTag.getList("sides", 10)
                sidesTag.forEach { t ->
                    t as NbtCompound
                    t.keys.forEach { id ->
                        val data = ItemFilterData().readNbt(t.getCompound(id))
                        val dir = Direction.values()[id.toInt()]
                        map[dir] = data
                    }
                }

                state.filters[pos] = map
            }

            ServoNetworkState.readNbt(tag) { state }

            return state

        }
    }


}