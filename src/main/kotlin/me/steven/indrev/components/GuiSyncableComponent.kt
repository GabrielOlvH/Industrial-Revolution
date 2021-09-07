package me.steven.indrev.components

import me.steven.indrev.gui.properties.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf

class GuiSyncableComponent()  {

    val properties = mutableListOf<SyncableProperty<*>>()

    operator fun <T> get(index: Int) = properties[index].value as T

    fun add(index: Int, prop: SyncableProperty<*>) {
        if (properties.size > index)
            println("Overriding property $prop @ $this")
        val r = properties.size until index + 1
        for (x in r) {
            properties.add(NullSyncableProperty)
        }
        if (r.count() > 1) println("Non linear id registering @ $this {$prop}")
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

fun ComponentProvider.autosync(index: Int, value: Int, setter: (Int) -> Int = { it }): IntSyncableProperty {
    val component = ComponentKey.GUI_SYNCABLE.get(this) ?: error("$this does not provide gui_syncable component")
    return IntSyncableProperty(index, value, setter)
        .also { component.add(index, it) }
}

fun ComponentProvider.trackInt(index: Int, provider: () -> Int) {
    val component = ComponentKey.GUI_SYNCABLE.get(this) ?: error("$this does not provide gui_syncable component")
    component.add(index, object : IntSyncableProperty(index, provider()) {
        override var value: Int
            get() = provider()
            set(value) {}

        private var previousValue = 0

        override var isDirty: Boolean
            get() = previousValue != value
            set(value) {
                if (!value) {
                    previousValue = this.value
                }
            }
    })
}

fun ComponentProvider.autosync(index: Int, value: Long, setter: (Long) -> Long = { it }) : LongSyncableProperty {
    val component = ComponentKey.GUI_SYNCABLE.get(this) ?: error("$this does not provide gui_syncable component")
    return LongSyncableProperty(index, value, setter)
        .also { component.add(index, it) }
}

fun ComponentProvider.trackLong(index: Int, provider: () -> Long) {
    val component = ComponentKey.GUI_SYNCABLE.get(this) ?: error("$this does not provide gui_syncable component")
    component.add(index, object : LongSyncableProperty(index, provider()) {
        override var value: Long
            get() = provider()
            set(value) {}

        private var previousValue = 0L

        override var isDirty: Boolean
            get() = previousValue != value
            set(value) {
                if (!value) {
                    previousValue = this.value
                }
            }
    })
}

fun ComponentProvider.autosync(index: Int, value: Double, setter: (Double) -> Double = { it }) : DoubleSyncableProperty {
    val component = ComponentKey.GUI_SYNCABLE.get(this) ?: error("$this does not provide gui_syncable component")
    return DoubleSyncableProperty(index, value, setter)
        .also { component.add(index, it) }
}

fun ComponentProvider.trackDouble(index: Int, provider: () -> Double) {
    val component = ComponentKey.GUI_SYNCABLE.get(this) ?: error("$this does not provide gui_syncable component")
    component.add(index, object : DoubleSyncableProperty(index, provider()) {
        override var value: Double
            get() = provider()
            set(value) {}

        private var previousValue = 0.0

        override var isDirty: Boolean
            get() = previousValue != value
            set(value) {
                if (!value) {
                    previousValue = this.value
                }
            }
    })
}

fun ComponentProvider.autosync(index: Int, value: Boolean, setter: (Boolean) -> Boolean = { it }) : BooleanSyncableProperty {
    val component = ComponentKey.GUI_SYNCABLE.get(this) ?: error("$this does not provide gui_syncable component")
    return BooleanSyncableProperty(index, value, setter)
        .also { component.add(index, it) }
}

fun ComponentProvider.trackBoolean(index: Int, provider: () -> Boolean) {
    val component = ComponentKey.GUI_SYNCABLE.get(this) ?: error("$this does not provide gui_syncable component")
    component.add(index, object : BooleanSyncableProperty(index, provider()) {
        override var value: Boolean
            get() = provider()
            set(value) {}

        private var previousValue = false

        override var isDirty: Boolean
            get() = previousValue != value
            set(value) {
                if (!value) {
                    previousValue = this.value
                }
            }
    })
}

fun <T : Enum<T>> ComponentProvider.autosync(index: Int, value: T, values: Array<T>, setter: (T) -> T = { it }) : EnumSyncableProperty<T> {
    val component = ComponentKey.GUI_SYNCABLE.get(this) ?: error("$this does not provide gui_syncable component")
    return EnumSyncableProperty(index, value, values, setter)
        .also { component.add(index, it) }
}

fun<T : Enum<T>> ComponentProvider.trackEnum(index: Int, values: Array<T>, provider: () -> T) {
    val component = ComponentKey.GUI_SYNCABLE.get(this) ?: error("$this does not provide gui_syncable component")
    component.add(index, object : EnumSyncableProperty<T>(index, provider(), values) {
        override var value: T
            get() = provider()
            set(value) {}

        private var previousValue = defaultValue

        override var isDirty: Boolean
            get() = previousValue != value
            set(value) {
                if (!value) {
                    previousValue = this.value
                }
            }
    })
}


fun <T : SyncableObject> ComponentProvider.trackObject(index: Int, value: T) {
    val component = ComponentKey.GUI_SYNCABLE.get(this) ?: error("$this does not provide gui_syncable component")
    component.add(index, wrapTrackedObject(index, value))
}