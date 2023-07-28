package me.steven.indrev.blocks

import me.steven.indrev.api.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.SolidFuelGeneratorBlockEntity
import me.steven.indrev.blockentities.crafting.*
import me.steven.indrev.blockentities.farming.*
import me.steven.indrev.blockentities.storage.LazuliFluxContainer
import me.steven.indrev.utils.*
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import java.util.function.Supplier

private val ALL_TIERS = arrayOf(Tier.MK1, Tier.MK2, Tier.MK3, Tier.MK4, Tier.CREATIVE)
private val SINGLE = arrayOf(Tier.MK1)

val MACHINES = mutableMapOf<Identifier, Machine>()

val SOLID_FUEL_GENERATOR = createMachine(identifier("solid_fuel_generator"), SINGLE, ::SolidFuelGeneratorBlockEntity)
val PULVERIZER = createMachine(identifier("pulverizer"), ALL_TIERS, ::PulverizerBlockEntity)
val ELECTRIC_FURNACE = createMachine(identifier("electric_furnace"), ALL_TIERS, ::ElectricFurnaceBlockEntity, {
    val model = MachineBakedModel("electric_furnace", true)
    model.onSpriteId = blockSpriteId("block/electric_furnace_emissive_on")
    model
})
val CHEMICAL_INFUSER = createMachine(identifier("chemical_infuser"), ALL_TIERS, ::ChemicalInfuserBlockEntity)
val ALLOY_SMELTER = createMachine(identifier("alloy_smelter"), ALL_TIERS, ::AlloySmelterBlockEntity, {
    val model = MachineBakedModel("alloy_smelter", true)
    model.onSpriteId = blockSpriteId("block/alloy_smelter_emissive_on")
    model
})
val COMPRESSOR = createMachine(identifier("compressor"), ALL_TIERS, ::CompressorBlockEntity)
val PLANTING_STATION = createMachine(identifier("planting_station"), ALL_TIERS, ::PlantingStationBlockEntity) { BaseFarmBlockEntityRenderer(it) }
val FERTILIZING_STATION = createMachine(identifier("fertilizing_station"), ALL_TIERS, ::FertilizingStationBlockEntity) { BaseFarmBlockEntityRenderer(it) }
val CHOPPING_STATION = createMachine(identifier("chopping_station"), ALL_TIERS, ::ChoppingStationBlockEntity) { BaseFarmBlockEntityRenderer(it) }
val HARVESTING_STATION = createMachine(identifier("harvesting_station"), ALL_TIERS, ::HarvestingStationBlockEntity) { BaseFarmBlockEntityRenderer(it) }
val LAZULI_FLUX_CONTAINER = createMachine(identifier("lazuli_flux_container"), ALL_TIERS, ::LazuliFluxContainer, {
    val model = object : MachineBakedModel("lazuli_flux_container", false) {
        override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<Random>?, ctx: RenderContext) {
            ctx.meshConsumer().accept(idleMesh)
            val item = stack.item as? MachineBlockItem ?: return

           // ctx.meshConsumer().accept(mesh)
        }
    }
    model
})

fun createMachine(id: Identifier, tiers: Array<Tier>, blockEntityProvider: (BlockPos, BlockState) -> MachineBlockEntity<*>, unbakedModelProvider: () -> UnbakedModel, blockEntityRenderer: ( BlockEntityRendererFactory.Context) -> MachineBlockEntityRenderer = { MachineBlockEntityRenderer(it) }): Machine {
    val block = MachineBlock(blockSettings(), blockEntityProvider)
    val blockItems = tiers.map {
        val blockItem = MachineBlockItem(block, it)
        Identifier(id.namespace, "${id.path}_${it.asString}").item(blockItem)
        blockItem
    }.toTypedArray()
    val blockEntityType = FabricBlockEntityTypeBuilder.create(blockEntityProvider, block).build()!!
    id.block(block).blockEntityType(blockEntityType)
    val machine = Machine(tiers, block, blockItems, blockEntityType, unbakedModelProvider, blockEntityRenderer)
    MACHINES[id] = machine
    return machine
}

fun createMachine(id: Identifier, tiers: Array<Tier>, blockEntityProvider: (BlockPos, BlockState) -> MachineBlockEntity<*>, hasOnModel: Boolean = true, blockEntityRenderer: ( BlockEntityRendererFactory.Context) -> MachineBlockEntityRenderer = { MachineBlockEntityRenderer(it) }): Machine {
    return createMachine(id, tiers, blockEntityProvider, {
        MachineBakedModel(
            id.path,
            hasOnModel
        )
    }, blockEntityRenderer)
}

class Machine(val tiers: Array<Tier>, val block: MachineBlock, val blockItems: Array<MachineBlockItem>, val type: BlockEntityType<MachineBlockEntity<*>>, val unbakedModelProvider: () -> UnbakedModel, val blockEntityRenderer: (BlockEntityRendererFactory.Context) -> MachineBlockEntityRenderer)
