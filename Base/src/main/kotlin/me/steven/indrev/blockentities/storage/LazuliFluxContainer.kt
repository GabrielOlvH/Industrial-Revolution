package me.steven.indrev.blockentities.storage

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.LAZULI_FLUX_CONTAINER
import me.steven.indrev.components.MachineItemInventory
import me.steven.indrev.components.inSlots
import me.steven.indrev.components.outSlots
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.config.machinesConfig
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetEntity
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.screens.widgets.WidgetVanillaSlot
import me.steven.indrev.utils.INPUT_COLOR
import me.steven.indrev.utils.SidedConfiguration
import me.steven.indrev.utils.energyOf
import me.steven.indrev.utils.identifier
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BoneMealItem
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import team.reborn.energy.api.EnergyStorageUtil

class LazuliFluxContainer(pos: BlockPos, state: BlockState) : MachineBlockEntity<MachineConfig>(LAZULI_FLUX_CONTAINER.type, pos, state) {

    override val config: MachineConfig get() = machinesConfig.lazuliFluxContainer.get(tier)
    override val inventory: MachineItemInventory = MachineItemInventory(1, inSlots(0), outSlots(0), onChange = ::markForUpdate)

    val sideConfig: SidedConfiguration = SidedConfiguration()

    init {
        sideConfig.forceDefault = false
    }

    override fun machineTick() {
        val itemEnergy = energyOf(inventory[0]) ?: return
        EnergyStorageUtil.move(energyInventories[ITEM_ENERGY_INVENTORY_VIEW], itemEnergy, Long.MAX_VALUE, null)
    }

    override fun getMaxInput(dir: Direction?): Long {
        return if (dir == null || sideConfig.getMode(dir).allowInput) maxInput else 0
    }

    override fun getMaxOutput(dir: Direction?): Long {
        return if (dir == null || sideConfig.getMode(dir).allowOutput) maxOutput else 0
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        super.writeScreenOpeningData(player, buf)
        sideConfig.toPacket(buf)
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity?): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
        handler.addEnergyBar(this)

        handler.add(WidgetEntity({ player!! }, false), grid(1), 10)
        handler.add(WidgetVanillaSlot(36, inv, overlay = EMPTY_HELMET_SLOT_TEXTURE), grid(0), grid(0)+2)
        handler.add(WidgetVanillaSlot(37, inv, overlay = EMPTY_CHESTPLATE_SLOT_TEXTURE), grid(0), grid(1)+2)
        handler.add(WidgetVanillaSlot(38, inv, overlay = EMPTY_LEGGINGS_SLOT_TEXTURE), grid(0), grid(2)+2)
        handler.add(WidgetVanillaSlot(39, inv, overlay = EMPTY_BOOTS_SLOT_TEXTURE), grid(0), grid(3)+2)
        handler.add(WidgetVanillaSlot(40, inv, overlay = EMPTY_OFFHAND_ARMOR_SLOT), grid(4), grid(3)+2)

        handler.add(WidgetSlot(0, inventory, color = INPUT_COLOR, overlay = POWER_ICON), grid(6), grid(1)-9)

        handler.addPlayerInventorySlots()

        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Lazuli Flux Container")

    companion object {
        val EMPTY_HELMET_SLOT_TEXTURE = Identifier("textures/item/empty_armor_slot_helmet.png")
        val EMPTY_CHESTPLATE_SLOT_TEXTURE = Identifier("textures/item/empty_armor_slot_chestplate.png")
        val EMPTY_LEGGINGS_SLOT_TEXTURE = Identifier("textures/item/empty_armor_slot_leggings.png")
        val EMPTY_BOOTS_SLOT_TEXTURE = Identifier("textures/item/empty_armor_slot_boots.png")
        val EMPTY_OFFHAND_ARMOR_SLOT = Identifier("textures/item/empty_armor_slot_shield.png")
        val POWER_ICON = identifier("textures/gui/icons/power_icon.png")
    }

}