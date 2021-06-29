package me.steven.indrev.utils

import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import me.steven.indrev.api.ServerWorldExtension
import me.steven.indrev.mixin.common.*
import me.steven.indrev.networks.NetworkState
import me.steven.indrev.networks.energy.EnergyNetwork
import me.steven.indrev.networks.fluid.FluidNetworkState
import me.steven.indrev.networks.item.ItemNetworkState
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.minecraft.block.FluidBlock
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FlowableFluid
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.*
import net.minecraft.screen.Property
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.collection.WeightedList
import net.minecraft.util.math.Direction

@Suppress("UNCHECKED_CAST")
val <T> WeightedList<T>.entries: MutableList<WeightedList.Entry<T>>
    get() = (this as AccessorWeightedList<T>).entries

@Suppress("UNCHECKED_CAST")
fun <T : Recipe<Inventory>> RecipeManager.getAllOfType(type: RecipeType<T>): Map<Identifier, T>
        = (this as AccessorRecipeManager).indrev_getAllOfType(type) as Map<Identifier, T>

fun RecipeManager.getRecipes(): Map<RecipeType<*>, Map<Identifier, Recipe<*>>> = (this as AccessorRecipeManager).recipes

val ScreenHandler.properties: MutableList<Property>
    get() = (this as AccessorScreenHandler).properties

val ScreenHandler.trackedPropertyValues: IntList
    get() = (this as AccessorScreenHandler).trackedPropertyValues

val AbstractCookingRecipe.input: Ingredient
    get() = (this as AccessorAbstractCookingRecipe).input

fun AnimalEntity.eat(player: PlayerEntity, hand: Hand, stack: ItemStack) = (this as AccessorAnimalEntity).callEat(player, hand, stack)

val FluidBlock.fluid: FlowableFluid
    get() = (this as AccessorFluidBlock).fluid

val LivingEntity.isJumping: Boolean get() = (this as AccessorLivingEntity).isJumping

val ServerWorld.energyIoCache: Long2ObjectOpenHashMap<BlockApiCache<EnergyIo, Direction>>
    get() = (this as ServerWorldExtension).indrev_getEnergyCache()

val ServerWorld.energyNetworkState: NetworkState<EnergyNetwork>
    get() = (this as ServerWorldExtension).indrev_getEnergyNetworkState()

val ServerWorld.fluidNetworkState: FluidNetworkState
    get() = (this as ServerWorldExtension).indrev_getFluidNetworkState()

val ServerWorld.itemNetworkState: ItemNetworkState
    get() = (this as ServerWorldExtension).indrev_getItemNetworkState()