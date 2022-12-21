package me.steven.indrev.components

import me.steven.indrev.items.UpgradeItem
import me.steven.indrev.utils.Upgrade
import net.minecraft.nbt.NbtCompound

open class MachineUpgrades(val upgrades: Array<Upgrade>, val onChange: () -> Unit) {
    val inventory = MachineItemInventory(size = 4, canInsert = inSlots(0, 1, 2, 3), canExtract = outSlots(0, 1, 2, 3), onChange = onChange)

    fun exists() = this != NullMachineUpgrades

    fun getSpeedMultiplier(): Double {
        var multiplier = 0.0
        inventory.forEach { slot ->
            val item = slot.resource.item
            if (item is UpgradeItem) {
                when (item.upgrade) {
                    Upgrade.OVERCLOCKER -> multiplier += 0.5
                    Upgrade.OVERCLOCKER_2X -> multiplier += 1.0
                    Upgrade.OVERCLOCKER_4X -> multiplier += 2.0
                    Upgrade.OVERCLOCKER_8X -> multiplier += 4.0
                    else -> {}
                }
            }
        }
        return multiplier
    }

    fun count(upgrade: Upgrade): Int {
        return inventory.count {
            val item = it.resource.item
            item is UpgradeItem && item.upgrade == upgrade
        }
    }

    fun contains(upgrade: Upgrade): Boolean {
        return inventory.any {
            val item = it.resource.item
            item is UpgradeItem && item.upgrade == upgrade
        }
    }

    fun writeNbt(): NbtCompound {
        val nbt = NbtCompound()
        nbt.put("inv", inventory.writeNbt())
        return nbt
    }

    fun readNbt(nbt: NbtCompound) {
        inventory.readNbt(nbt.getCompound("inv"))
        onChange()
    }
}

object NullMachineUpgrades : MachineUpgrades(emptyArray(), { })