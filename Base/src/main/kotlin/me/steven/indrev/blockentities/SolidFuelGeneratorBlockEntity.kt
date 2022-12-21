package me.steven.indrev.blockentities

import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.blocks.SOLID_FUEL_GENERATOR
import me.steven.indrev.components.MachineItemInventory
import me.steven.indrev.components.MachineUpgrades
import me.steven.indrev.components.inSlots
import me.steven.indrev.config.GeneratorConfig
import me.steven.indrev.config.machinesConfig
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetBar
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.Upgrade
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.particle.ParticleTypes
import net.minecraft.screen.ScreenHandler
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class SolidFuelGeneratorBlockEntity(pos: BlockPos, state: BlockState) : MachineBlockEntity<GeneratorConfig>(SOLID_FUEL_GENERATOR.type, pos, state) {

    override val config: GeneratorConfig = machinesConfig.solidFuelGenerator

    override val inventory: MachineItemInventory = MachineItemInventory(1, inSlots(0, test = IS_FUEL), onChange = ::markForUpdate)
    override val upgrades: MachineUpgrades = MachineUpgrades(VALID_UPGRADES, ::updateUpgrades)

    var burnTime by properties.sync(BURN_TIME_ID, 0)
    var totalBurnTime by properties.sync(TOTAL_BURN_TIME_ID, 0)

    override val maxInput: Long = 0

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        if (idle) return
        val x = pos.x.toDouble() + 0.5
        val y = pos.y.toDouble() + 8/16.0
        val z = pos.z.toDouble() + 0.5
        if (random.nextDouble() < 0.1) {
            world.playSound(x, y, z, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0f, 1.0f, false)
        }

        val direction = state[MachineBlock.FACING]
        val axis = direction.axis
        val xOff = if (axis == Direction.Axis.X) direction.offsetX.toDouble() * 0.52 else random.nextDouble() * 0.5 - 0.3
        val yOff = random.nextDouble() * 4.0 / 16.0
        val zOff = if (axis == Direction.Axis.Z) direction.offsetZ.toDouble() * 0.52 else random.nextDouble() * 0.5 - 0.3
        world.addParticle(ParticleTypes.SMOKE, x + xOff, y + yOff, z + zOff, 0.0, 0.0, 0.0)
        world.addParticle(ParticleTypes.FLAME, x + xOff, y + yOff, z + zOff, 0.0, 0.0, 0.0)
    }

    override fun machineTick() {
        when {
            burnTime > 0 -> {
                if (capacity >= energy + ENERGY_GENERATION_RATIO) {
                    burnTime--
                    insertEnergy(ENERGY_GENERATION_RATIO)
                }
            }
            IS_FUEL(inventory[0].resource) -> {
                val d = 0.25 * (upgrades.count(Upgrade.FUEL_EFFICIENCY)+1)
                totalBurnTime = (FuelRegistry.INSTANCE.get(inventory[0].variant.item) * d).toInt()
                burnTime = totalBurnTime
                inventory[0].decrement()
            }
            else -> {
                totalBurnTime = 0
                burnTime = 0
            }
        }
        idle = burnTime <= 0
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
        handler.addDefaultBackground()
        handler.addEnergyBar(this)
        handler.addUpgradeSlots(upgrades)

        handler.add(WidgetSlot(0, inventory, 0xFFFFAAAA.toInt()), grid(4), grid(2))
        handler.add(WidgetBar.burning({ burnTime }, { totalBurnTime }), grid(4) - 4, grid(1) + 4)

        handler.addPlayerInventorySlots()
        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Solid Fuel Generator")

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.putInt("burnTime", burnTime)
        nbt.putInt("totalBurnTime", totalBurnTime)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        burnTime = nbt.getInt("burnTime")
        totalBurnTime = nbt.getInt("totalBurnTime")
    }

    companion object {
        const val BURN_TIME_ID = 2
        const val TOTAL_BURN_TIME_ID = 3
        const val ENERGY_GENERATION_RATIO = 4L
        val IS_FUEL: (ItemVariant) -> Boolean = { variant -> FuelRegistry.INSTANCE.get(variant.item) != null }
        val VALID_UPGRADES = arrayOf(Upgrade.AUTOMATED_ITEM_TRANSFER, Upgrade.FUEL_EFFICIENCY)
    }
}