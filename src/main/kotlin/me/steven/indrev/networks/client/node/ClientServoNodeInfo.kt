package me.steven.indrev.networks.client.node

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.steven.indrev.networks.EndpointData
import net.minecraft.util.math.Direction

open class ClientServoNodeInfo(override val pos: Long, val servos: Object2ObjectOpenHashMap<Direction, EndpointData.Type>) :
    ClientNodeInfo {

    val size: Int get() = servos.size

    inline fun forEach(f: (Direction, EndpointData.Type) -> Unit) = servos.forEach { (dir, type) -> f(dir, type) }

    override fun equals(other: Any?): Boolean {
        return when {
            other !is ClientServoNodeInfo -> false
            other.pos != pos -> false
            other.servos.size != servos.size -> false
            other.servos != servos -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = pos.hashCode()
        result = 31 * result + servos.hashCode()
        return result
    }
}