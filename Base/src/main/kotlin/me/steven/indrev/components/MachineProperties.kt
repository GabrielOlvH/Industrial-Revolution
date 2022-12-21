package me.steven.indrev.components

import net.minecraft.network.PacketByteBuf
import kotlin.reflect.KProperty

class MachineProperties {

    val properties = mutableListOf<SyncableObject>()

    fun <T : SyncableObject> sync(property: T): T {
        while (properties.size <= property.syncId) properties.add(NullProperty)
        properties[property.syncId] = property
        return property
    }

    fun sync(syncId: Int, initialValue: Int) = sync(object : SyncableProperty<Int> {
        override val syncId: Int = syncId
        override var isDirty: Boolean = false
        override var value: Int = initialValue

        override fun toPacket(buf: PacketByteBuf) {
            buf.writeInt(value)
        }

        override fun fromPacket(buf: PacketByteBuf) {
            value = buf.readInt()
        }

        override fun setValue(ref: Any?, property: KProperty<*>, value: Int) {
            if (this.value != value) isDirty = true
            this.value = value
        }

        override fun getValue(ref: Any?, property: KProperty<*>): Int {
            return value
        }
    })

    fun sync(syncId: Int, initialValue: Long) = sync(object : SyncableProperty<Long> {
        override val syncId: Int = syncId
        override var isDirty: Boolean = false
        override var value: Long = initialValue

        override fun toPacket(buf: PacketByteBuf) {
            buf.writeLong(value)
        }

        override fun fromPacket(buf: PacketByteBuf) {
            value = buf.readLong()
        }

        override fun setValue(ref: Any?, property: KProperty<*>, value: Long) {
            if (this.value != value) isDirty = true
            this.value = value
        }

        override fun getValue(ref: Any?, property: KProperty<*>): Long {
            return value
        }
    })
}

interface SyncableProperty<T> : SyncableObject {

    var value: T

    operator fun getValue(ref: Any?, property: KProperty<*>): T
    operator fun setValue(ref: Any?, property: KProperty<*>, value: T)
}

interface SyncableObject {

    val syncId: Int
    var isDirty: Boolean

    fun toPacket(buf: PacketByteBuf)
    fun fromPacket(buf: PacketByteBuf)
}

private object NullProperty : SyncableProperty<Any?> {

    override val syncId: Int = -1
    override var isDirty: Boolean
        get() = false
        set(_) {}
    override var value: Any? = null

    override fun toPacket(buf: PacketByteBuf) {

    }

    override fun fromPacket(buf: PacketByteBuf) {
    }

    override fun getValue(ref: Any?, property: KProperty<*>): Any? {
        return null
    }

    override fun setValue(ref: Any?, property: KProperty<*>, value: Any?) {
    }

}

