package me.steven.indrev.components

import me.steven.indrev.blockentities.MachineBlockEntity
import kotlin.reflect.KProperty

class Property<T : Number>(private val id: Int, var value: T, val test: (T) -> T = { it }) {
    operator fun getValue(ref: MachineBlockEntity, property: KProperty<*>) = test(value)
    operator fun setValue(ref: MachineBlockEntity, property: KProperty<*>, value: T) {
        this.value = test(value)
        ref.sync()
        ref.propertyDelegate[id] = this.value.toInt()
    }
}