package me.steven.indrev.utils

import me.steven.indrev.mixin.AccessorWeightedList
import me.steven.indrev.mixin.AccessorWeightedListEntry
import net.minecraft.util.collection.WeightedList

val <T> WeightedList<T>.entries: MutableList<WeightedList.Entry<T>>
    get() = (this as AccessorWeightedList<T>).entries

val <T> WeightedList.Entry<T>.weight: Int
    get() = (this as AccessorWeightedListEntry).weight