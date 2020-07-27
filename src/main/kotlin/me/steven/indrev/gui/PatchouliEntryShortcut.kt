package me.steven.indrev.gui

import net.minecraft.util.Identifier

interface PatchouliEntryShortcut {
    fun getEntry(): Identifier
    fun getPage(): Int
}