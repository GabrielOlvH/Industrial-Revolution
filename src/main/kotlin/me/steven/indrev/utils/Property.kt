package me.steven.indrev.utils

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import kotlin.reflect.KProperty

class Property<T : Number>(private val id: Int, var value: T, val test: (T) -> T = { it }) {
    operator fun getValue(ref: PropertyDelegateHolder, property: KProperty<*>) = test(value)
    operator fun setValue(ref: PropertyDelegateHolder, property: KProperty<*>, value: T) {
        this.value = test(value)
        ref.propertyDelegate[id] = this.value.toInt()
    }
}