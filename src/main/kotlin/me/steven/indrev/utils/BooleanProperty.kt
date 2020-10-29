package me.steven.indrev.utils

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import kotlin.reflect.KProperty

class BooleanProperty(private val id: Int, var value: Boolean) {
    operator fun getValue(ref: PropertyDelegateHolder, property: KProperty<*>) = value
    operator fun setValue(ref: PropertyDelegateHolder, property: KProperty<*>, value: Boolean) {
        this.value = value
        ref.propertyDelegate[id] = if (value) 1 else 0
    }
}