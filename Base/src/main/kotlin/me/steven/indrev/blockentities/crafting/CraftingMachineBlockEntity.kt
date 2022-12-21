package me.steven.indrev.blockentities.crafting

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.MachineRecipeCrafter
import me.steven.indrev.components.MachineUpgrades
import me.steven.indrev.config.CraftingMachineConfig
import me.steven.indrev.utils.Upgrade
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.BlockPos

abstract class CraftingMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : MachineBlockEntity<CraftingMachineConfig>(type, pos, state) {

    override val upgrades: MachineUpgrades = MachineUpgrades(Upgrade.CRAFTING, ::updateUpgrades)

    override val maxOutput: Long = 0

    abstract val crafters: Array<MachineRecipeCrafter>

    override fun machineTick() {
        if (ticks == 1) updateCrafters()
        crafters.forEach { it.tick(this, world!!.recipeManager) }
    }

    protected fun updateCrafters() {
        if (!world!!.isClient) {
            crafters.forEach { crafter -> crafter.update(inventory, fluidInventory, world!!.recipeManager) }
            markForUpdate()
        }
    }

    open fun getProcessingSpeed(): Double {
        val tmpMultiplier =
            if (temperatureController.exists() && temperatureController.isFullEfficiency())
                config?.temperatureBoost ?: 0.0
            else 0.0
        val overclockMultiplier =
            if (upgrades.exists()) upgrades.getSpeedMultiplier() else 0.0
        return overclockMultiplier + tmpMultiplier + (config?.processingSpeed ?: 1.0)
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        val crafterList = NbtList()
        crafters.forEachIndexed { index, crafter ->
            val crafterNbt = NbtCompound()
            crafterNbt.putInt("index", index)
            crafter.writeNbt(nbt)
            crafterList.add(crafterNbt)
        }
        nbt.put("crafters", crafterList)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        val crafterList = nbt.getList("crafters", 10)
        crafterList.forEach { element ->
            val crafterNbt = element as NbtCompound
            val index = crafterNbt.getInt("index")
            crafters[index].readNbt(crafterNbt)
        }
    }
}