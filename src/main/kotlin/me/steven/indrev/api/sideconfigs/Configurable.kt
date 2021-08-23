package me.steven.indrev.api.sideconfigs

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WToggleButton
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.netty.buffer.Unpooled
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.blocks.machine.FacingMachineBlock
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.gui.widgets.machines.WMachineSideDisplay
import me.steven.indrev.packets.common.ConfigureIOPackets
import me.steven.indrev.utils.add
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.function.Consumer

interface Configurable {
    fun isConfigurable(type: ConfigurationType): Boolean
    fun isFixed(type: ConfigurationType): Boolean
    fun getValidConfigurations(type: ConfigurationType): Array<TransferMode>
    fun getCurrentConfiguration(type: ConfigurationType): SideConfiguration
    fun applyDefault(state: BlockState, type: ConfigurationType, configuration: MutableMap<Direction, TransferMode>)

}