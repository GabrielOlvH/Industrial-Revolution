package me.steven.indrev.components

import me.steven.indrev.blockentities.BaseBlockEntity
import me.steven.indrev.gui.properties.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf

class GuiSyncableComponent  {

    val properties = mutableListOf<SyncableProperty<*>>()

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(index: Int) = properties[index].value as T

    fun add(index: Int, prop: SyncableProperty<*>) {
        val r = properties.size until index + 1
        for (x in r) {
            properties.add(NullSyncableProperty)
        }
        properties[index] = prop
    }
}

interface SyncableObject {

    var isDirty: Boolean

    fun markDirty(condition: () -> Boolean = { true })

    fun toPacket(buf: PacketByteBuf)

    @Environment(EnvType.CLIENT)
    fun fromPacket(buf: PacketByteBuf)
}

open class DefaultSyncableObject : SyncableObject {

    override var isDirty: Boolean = false

    override fun markDirty(condition: () -> Boolean) {
        if (isDirty || condition()) isDirty = true
    }

    override fun toPacket(buf: PacketByteBuf) {
    }

    override fun fromPacket(buf: PacketByteBuf) {
    }
}

fun <T : SyncableObject> wrapTrackedObject(index: Int, obj: T): SyncableProperty<T> {
    return object : SyncableProperty<T>(index, obj) {

        override var isDirty: Boolean
            get() = obj.isDirty
            set(value) { obj.isDirty = value }

        override fun toPacket(buf: PacketByteBuf) = obj.toPacket(buf)

        override fun fromPacket(buf: PacketByteBuf) = obj.fromPacket(buf)
    }
}

fun BaseBlockEntity.autosync(index: Int, value: Int, setter: (Int) -> Int = { it }): IntSyncableProperty {
    val component = this.guiSyncableComponent ?: error("$this does not provide gui_syncable component")
    return IntSyncableProperty(index, value, setter)
        .also { component.add(index, it) }
}

fun BaseBlockEntity.trackInt(index: Int, provider: () -> Int) {
    val component = this.guiSyncableComponent ?: error("$this does not provide gui_syncable component")
    component.add(index, object : IntSyncableProperty(index, provider()) {
        override var value: Int
            get() = if (world!!.isClient) clientValue else provider()
            set(value) {
                clientValue = value
            }

        var clientValue: Int = 0

        override var isDirty: Boolean
            get() = clientValue != value
            set(value) {
                clientValue = if (!value) this.value else -1
            }
    })
}

fun BaseBlockEntity.autosync(index: Int, value: Long, setter: (Long) -> Long = { it }) : LongSyncableProperty {
    val component = this.guiSyncableComponent ?: error("$this does not provide gui_syncable component")
    return LongSyncableProperty(index, value, setter)
        .also { component.add(index, it) }
}

fun BaseBlockEntity.trackLong(index: Int, provider: () -> Long) {
    val component = this.guiSyncableComponent ?: error("$this does not provide gui_syncable component")
    component.add(index, object : LongSyncableProperty(index, provider()) {
        override var value: Long
            get() = if (world!!.isClient) clientValue else provider()
            set(value) {
                clientValue = value
            }

        private var clientValue = 0L

        override var isDirty: Boolean
            get() = clientValue != value
            set(value) {
                clientValue = if (!value) this.value else -1
            }
    })
}

fun BaseBlockEntity.autosync(index: Int, value: Double, setter: (Double) -> Double = { it }) : DoubleSyncableProperty {
    val component = this.guiSyncableComponent ?: error("$this does not provide gui_syncable component")
    return DoubleSyncableProperty(index, value, setter)
        .also { component.add(index, it) }
}

fun BaseBlockEntity.trackDouble(index: Int, provider: () -> Double) {
    val component = this.guiSyncableComponent ?: error("$this does not provide gui_syncable component")
    component.add(index, object : DoubleSyncableProperty(index, provider()) {
        override var value: Double
            get() = if (world!!.isClient) clientValue else provider()
            set(value) {
                clientValue = value
            }

        private var clientValue = 0.0

        override var isDirty: Boolean
            get() = clientValue != value
            set(value) {
                clientValue = if (!value) this.value else -1.0
            }
    })
}

fun BaseBlockEntity.autosync(index: Int, value: Boolean, setter: (Boolean) -> Boolean = { it }) : BooleanSyncableProperty {
    val component = this.guiSyncableComponent ?: error("$this does not provide gui_syncable component")
    return BooleanSyncableProperty(index, value, setter)
        .also { component.add(index, it) }
}

fun BaseBlockEntity.trackBoolean(index: Int, provider: () -> Boolean) {
    val component = this.guiSyncableComponent ?: error("$this does not provide gui_syncable component")
    component.add(index, object : BooleanSyncableProperty(index, provider()) {
        override var value: Boolean
            get() = if (world!!.isClient) clientValue else provider()
            set(value) {
                clientValue = value
            }

        private var clientValue = false

        override var isDirty: Boolean
            get() = clientValue != value
            set(value) {
                clientValue = if (!value) this.value else !clientValue
            }
    })
}

fun <T : Enum<T>> BaseBlockEntity.autosync(index: Int, value: T, values: Array<T>, setter: (T) -> T = { it }) : EnumSyncableProperty<T> {
    val component = this.guiSyncableComponent ?: error("$this does not provide gui_syncable component")
    return EnumSyncableProperty(index, value, values, setter)
        .also { component.add(index, it) }
}

fun <T : Enum<T>> BaseBlockEntity.trackEnum(index: Int, values: Array<T>, provider: () -> T) {
    val component = this.guiSyncableComponent ?: error("$this does not provide gui_syncable component")
    component.add(index, object : EnumSyncableProperty<T>(index, provider(), values) {
        override var value: T
            get() = if (world!!.isClient) clientValue else provider()
            set(value) {
                clientValue = value
            }

        private var clientValue = defaultValue

        override var isDirty: Boolean
            get() = clientValue != value
            set(value) {
                clientValue = if (!value) this.value else values[0]
            }
    })
}


fun <T : SyncableObject> BaseBlockEntity.trackObject(index: Int, value: T) {
    val component = this.guiSyncableComponent ?: error("$this does not provide gui_syncable component")
    component.add(index, wrapTrackedObject(index, value))
}