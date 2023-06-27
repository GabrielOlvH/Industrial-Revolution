package me.steven.indrev.transportation.networks

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

object ClientPipeNetworkData {

    const val PATH_RENDER_TIME = 40

    val renderData = Long2IntOpenHashMap()
    val renderSnapshot = Long2IntOpenHashMap()
    val pathsToRender = Object2IntOpenHashMap<Path>()
    val scheduleUpdates = LongOpenHashSet()

    init {
        renderData.defaultReturnValue(-1)
        renderSnapshot.defaultReturnValue(-1)
    }

    fun removeAll(toRemove: LongOpenHashSet) {
        toRemove.forEach { pos ->
            if (renderData.remove(pos) != renderSnapshot[pos]) {
                scheduleUpdates.add(pos)
            }
        }
    }

    fun addAll(toAdd: Long2IntOpenHashMap) {
        toAdd.forEach { (pos, connections) ->
            renderData.put(pos, connections)
            if (connections != renderSnapshot[pos]) {
                scheduleUpdates.add(pos)
            }
        }
    }
}