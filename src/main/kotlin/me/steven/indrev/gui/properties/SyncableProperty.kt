package me.steven.indrev.gui.properties

import me.steven.indrev.components.DefaultSyncableObject
import me.steven.indrev.components.SyncableObject
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf
import kotlin.reflect.KProperty

abstract class SyncableProperty<T>(val id: Int, val defaultValue: T, val setter: (T) -> T = { it }) : DefaultSyncableObject() {

    open var value = defaultValue

    fun set(value: Any?) {
        this.value = value as T
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val before = this.value
        this.value = setter(value)
        markDirty { before != value }
    }
}

object NullSyncableProperty : SyncableProperty<Int>(-1, -1, { it }) {
    override fun toPacket(buf: PacketByteBuf) {
    }

    override fun fromPacket(buf: PacketByteBuf) {
    }
}

open class IntSyncableProperty(id: Int, defaultValue: Int, setter: (Int) -> Int = { it }) : SyncableProperty<Int>(id, defaultValue, setter) {

    @Environment(EnvType.CLIENT)
    override fun fromPacket(buf: PacketByteBuf) {
        value = buf.readInt()
    }

    override fun toPacket(buf: PacketByteBuf) {
        buf.writeInt(value)
    }
}

open class LongSyncableProperty(id: Int, defaultValue: Long, setter: (Long) -> Long = { it }) : SyncableProperty<Long>(id, defaultValue, setter) {

    @Environment(EnvType.CLIENT)
    override fun fromPacket(buf: PacketByteBuf) {
        value = buf.readLong()
    }

    override fun toPacket(buf: PacketByteBuf) {
        buf.writeLong(value)
    }
}

open class DoubleSyncableProperty(id: Int, defaultValue: Double, setter: (Double) -> Double = { it }) : SyncableProperty<Double>(id, defaultValue, setter) {

    @Environment(EnvType.CLIENT)
    override fun fromPacket(buf: PacketByteBuf) {
        value = buf.readDouble()
    }

    override fun toPacket(buf: PacketByteBuf) {
        buf.writeDouble(value)
    }
}

open class BooleanSyncableProperty(id: Int, defaultValue: Boolean, setter: (Boolean) -> Boolean = { it }) : SyncableProperty<Boolean>(id, defaultValue, setter) {

    @Environment(EnvType.CLIENT)
    override fun fromPacket(buf: PacketByteBuf) {
        value = buf.readBoolean()
    }

    override fun toPacket(buf: PacketByteBuf) {
        buf.writeBoolean(value)
    }
}

open class EnumSyncableProperty<T : Enum<T>>(id: Int, defaultValue: T, val values: Array<T>, setter: (T) -> T = { it }) : SyncableProperty<T>(id, defaultValue, setter) {

    @Environment(EnvType.CLIENT)
    override fun fromPacket(buf: PacketByteBuf) {
        value = values[buf.readInt()]
    }

    override fun toPacket(buf: PacketByteBuf) {
        buf.writeInt(value.ordinal)
    }
}