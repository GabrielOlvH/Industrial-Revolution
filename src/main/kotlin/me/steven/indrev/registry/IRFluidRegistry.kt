package me.steven.indrev.registry

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.blocks.misc.AcidFluidBlock
import me.steven.indrev.datagen.utils.*
import me.steven.indrev.fluids.BaseFluid
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricMaterialBuilder
import net.minecraft.block.FluidBlock
import net.minecraft.block.MapColor
import net.minecraft.block.Material
import net.minecraft.item.BucketItem
import net.minecraft.item.Items

@Suppress("MemberVisibilityCanBePrivate")
object IRFluidRegistry {
    fun registerAll() {

        COOLANT_IDENTIFIER.block(COOLANT)
        identifier("${COOLANT_IDENTIFIER.path}_still").fluid(COOLANT_STILL)
        identifier("${COOLANT_IDENTIFIER.path}_flowing").fluid(COOLANT_FLOWING)
        identifier("${COOLANT_IDENTIFIER.path}_bucket").item(COOLANT_BUCKET)

        MOLTEN_NETHERITE_IDENTIFIER.block(MOLTEN_NETHERITE)
        identifier("${MOLTEN_NETHERITE_IDENTIFIER.path}_still").fluid(MOLTEN_NETHERITE_STILL)
        identifier("${MOLTEN_NETHERITE_IDENTIFIER.path}_flowing").fluid(MOLTEN_NETHERITE_FLOWING)
        identifier("${MOLTEN_NETHERITE_IDENTIFIER.path}_bucket").item(MOLTEN_NETHERITE_BUCKET)

        MOLTEN_IRON_IDENTIFIER.block(MOLTEN_IRON)
        identifier("${MOLTEN_IRON_IDENTIFIER.path}_still").fluid(MOLTEN_IRON_STILL)
        identifier("${MOLTEN_IRON_IDENTIFIER.path}_flowing").fluid(MOLTEN_IRON_FLOWING)
        identifier("${MOLTEN_IRON_IDENTIFIER.path}_bucket").item(MOLTEN_IRON_BUCKET)

        MOLTEN_GOLD_IDENTIFIER.block(MOLTEN_GOLD)
        identifier("${MOLTEN_GOLD_IDENTIFIER.path}_still").fluid(MOLTEN_GOLD_STILL)
        identifier("${MOLTEN_GOLD_IDENTIFIER.path}_flowing").fluid(MOLTEN_GOLD_FLOWING)
        identifier("${MOLTEN_GOLD_IDENTIFIER.path}_bucket").item(MOLTEN_GOLD_BUCKET)

        MOLTEN_COPPER_IDENTIFIER.block(MOLTEN_COPPER)
        identifier("${MOLTEN_COPPER_IDENTIFIER.path}_still").fluid(MOLTEN_COPPER_STILL)
        identifier("${MOLTEN_COPPER_IDENTIFIER.path}_flowing").fluid(MOLTEN_COPPER_FLOWING)
        identifier("${MOLTEN_COPPER_IDENTIFIER.path}_bucket").item(MOLTEN_COPPER_BUCKET)

        MOLTEN_TIN_IDENTIFIER.block(MOLTEN_TIN)
        identifier("${MOLTEN_TIN_IDENTIFIER.path}_still").fluid(MOLTEN_TIN_STILL)
        identifier("${MOLTEN_TIN_IDENTIFIER.path}_flowing").fluid(MOLTEN_TIN_FLOWING)
        identifier("${MOLTEN_TIN_IDENTIFIER.path}_bucket").item(MOLTEN_TIN_BUCKET)

        MOLTEN_LEAD_IDENTIFIER.block(MOLTEN_LEAD)
        identifier("${MOLTEN_LEAD_IDENTIFIER.path}_still").fluid(MOLTEN_LEAD_STILL)
        identifier("${MOLTEN_LEAD_IDENTIFIER.path}_flowing").fluid(MOLTEN_LEAD_FLOWING)
        identifier("${MOLTEN_LEAD_IDENTIFIER.path}_bucket").item(MOLTEN_LEAD_BUCKET)

        MOLTEN_SILVER_IDENTIFIER.block(MOLTEN_SILVER)
        identifier("${MOLTEN_SILVER_IDENTIFIER.path}_still").fluid(MOLTEN_SILVER_STILL)
        identifier("${MOLTEN_SILVER_IDENTIFIER.path}_flowing").fluid(MOLTEN_SILVER_FLOWING)
        identifier("${MOLTEN_SILVER_IDENTIFIER.path}_bucket").item(MOLTEN_SILVER_BUCKET)

        SULFURIC_ACID_IDENTIFIER.block(SULFURIC_ACID)
        identifier("${SULFURIC_ACID_IDENTIFIER.path}_still").fluid(SULFURIC_ACID_STILL)
        identifier("${SULFURIC_ACID_IDENTIFIER.path}_flowing").fluid(SULFURIC_ACID_FLOWING)
        identifier("${SULFURIC_ACID_IDENTIFIER.path}_bucket").item(SULFURIC_ACID_BUCKET)

        TOXIC_MUD_IDENTIFIER.block(TOXIC_MUD)
        identifier("${TOXIC_MUD_IDENTIFIER.path}_still").fluid(TOXIC_MUD_STILL)
        identifier("${TOXIC_MUD_IDENTIFIER.path}_flowing").fluid(TOXIC_MUD_FLOWING)
        identifier("${TOXIC_MUD_IDENTIFIER.path}_bucket").item(TOXIC_MUD_BUCKET)

        HYDROGEN_IDENTIFIER.block(HYDROGEN)
        identifier("${HYDROGEN_IDENTIFIER.path}_still").fluid(HYDROGEN_STILL)
        identifier("${HYDROGEN_IDENTIFIER.path}_flowing").fluid(HYDROGEN_FLOWING)
        identifier("${HYDROGEN_IDENTIFIER.path}_bucket").item(HYDROGEN_BUCKET)
        IRFluidFuelRegistry.register(HYDROGEN_STILL, 10, 900, 4, 100 * 81)

        OXYGEN_IDENTIFIER.block(OXYGEN)
        identifier("${OXYGEN_IDENTIFIER.path}_still").fluid(OXYGEN_STILL)
        identifier("${OXYGEN_IDENTIFIER.path}_flowing").fluid(OXYGEN_FLOWING)
        identifier("${OXYGEN_IDENTIFIER.path}_bucket").item(OXYGEN_BUCKET)

        METHANE_IDENTIFIER.block(METHANE)
        identifier("${METHANE_IDENTIFIER.path}_still").fluid(METHANE_STILL)
        identifier("${METHANE_IDENTIFIER.path}_flowing").fluid(METHANE_FLOWING)
        identifier("${METHANE_IDENTIFIER.path}_bucket").item(METHANE_BUCKET)
        IRFluidFuelRegistry.register(METHANE_STILL, 60, 900, 128, 250 * 81)


        STEAM_IDENTIFIER.block(STEAM)
        identifier("${STEAM_IDENTIFIER.path}_still").fluid(STEAM_STILL)
        identifier("${STEAM_IDENTIFIER.path}_flowing").fluid(STEAM_FLOWING)
        identifier("${STEAM_IDENTIFIER.path}_bucket").item(STEAM_BUCKET)

        MOLTEN_SALT_IDENTIFIER.block(MOLTEN_SALT)
        identifier("${MOLTEN_SALT_IDENTIFIER.path}_still").fluid(MOLTEN_SALT_STILL)
        identifier("${MOLTEN_SALT_IDENTIFIER.path}_flowing").fluid(MOLTEN_SALT_FLOWING)
        identifier("${MOLTEN_SALT_IDENTIFIER.path}_bucket").item(MOLTEN_SALT_BUCKET)
    }

    val COOLANT_IDENTIFIER = identifier("coolant")
    val COOLANT_STILL: BaseFluid.Still =
        BaseFluid.Still(COOLANT_IDENTIFIER, { COOLANT }, { COOLANT_BUCKET }, 0x0C2340) { COOLANT_FLOWING }
    val COOLANT_FLOWING =
        BaseFluid.Flowing(COOLANT_IDENTIFIER, { COOLANT }, { COOLANT_BUCKET }, 0x0C2340) { COOLANT_STILL }
    val COOLANT_BUCKET = BucketItem(COOLANT_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val COOLANT = object : FluidBlock(COOLANT_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_NETHERITE_IDENTIFIER = identifier("molten_netherite")
    val MOLTEN_NETHERITE_STILL: BaseFluid.Still = BaseFluid.Still(MOLTEN_NETHERITE_IDENTIFIER, { MOLTEN_NETHERITE }, { MOLTEN_NETHERITE_BUCKET }, NETHERITE_SCRAP_BASE.toInt()) { MOLTEN_NETHERITE_FLOWING }
    val MOLTEN_NETHERITE_FLOWING = BaseFluid.Flowing(MOLTEN_NETHERITE_IDENTIFIER, { MOLTEN_NETHERITE }, { MOLTEN_NETHERITE_BUCKET }, NETHERITE_SCRAP_BASE.toInt()) { MOLTEN_NETHERITE_STILL }
    val MOLTEN_NETHERITE_BUCKET = BucketItem(MOLTEN_NETHERITE_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_NETHERITE = object : FluidBlock(MOLTEN_NETHERITE_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_IRON_IDENTIFIER = identifier("molten_iron")
    val MOLTEN_IRON_STILL: BaseFluid.Still = BaseFluid.Still(MOLTEN_IRON_IDENTIFIER, { MOLTEN_IRON }, { MOLTEN_IRON_BUCKET }, 0x7A0019) { MOLTEN_IRON_FLOWING }
    val MOLTEN_IRON_FLOWING = BaseFluid.Flowing(MOLTEN_IRON_IDENTIFIER, { MOLTEN_IRON }, { MOLTEN_IRON_BUCKET }, 0x7A0019) { MOLTEN_IRON_STILL }
    val MOLTEN_IRON_BUCKET = BucketItem(MOLTEN_IRON_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_IRON = object : FluidBlock(MOLTEN_IRON_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_GOLD_IDENTIFIER = identifier("molten_gold")
    val MOLTEN_GOLD_STILL: BaseFluid.Still = BaseFluid.Still(MOLTEN_GOLD_IDENTIFIER, { MOLTEN_GOLD }, { MOLTEN_GOLD_BUCKET }, 0xFFCC00) { MOLTEN_GOLD_FLOWING }
    val MOLTEN_GOLD_FLOWING = BaseFluid.Flowing(MOLTEN_GOLD_IDENTIFIER, { MOLTEN_GOLD }, { MOLTEN_GOLD_BUCKET }, 0xFFCC00) { MOLTEN_GOLD_STILL }
    val MOLTEN_GOLD_BUCKET = BucketItem(MOLTEN_GOLD_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_GOLD = object : FluidBlock(MOLTEN_GOLD_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_COPPER_IDENTIFIER = identifier("molten_copper")
    val MOLTEN_COPPER_STILL: BaseFluid.Still = BaseFluid.Still(MOLTEN_COPPER_IDENTIFIER, { MOLTEN_COPPER }, { MOLTEN_COPPER_BUCKET }, COPPER_BASE.toInt()) { MOLTEN_COPPER_FLOWING }
    val MOLTEN_COPPER_FLOWING = BaseFluid.Flowing(MOLTEN_COPPER_IDENTIFIER, { MOLTEN_COPPER }, { MOLTEN_COPPER_BUCKET }, COPPER_BASE.toInt()) { MOLTEN_COPPER_STILL }
    val MOLTEN_COPPER_BUCKET = BucketItem(MOLTEN_COPPER_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_COPPER = object : FluidBlock(MOLTEN_COPPER_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_TIN_IDENTIFIER = identifier("molten_tin")
    val MOLTEN_TIN_STILL: BaseFluid.Still =
        BaseFluid.Still(MOLTEN_TIN_IDENTIFIER, { MOLTEN_TIN }, { MOLTEN_TIN_BUCKET }, TIN_BASE.toInt()) { MOLTEN_TIN_FLOWING }
    val MOLTEN_TIN_FLOWING =
        BaseFluid.Flowing(MOLTEN_TIN_IDENTIFIER, { MOLTEN_TIN }, { MOLTEN_TIN_BUCKET }, TIN_BASE.toInt()) { MOLTEN_TIN_STILL }
    val MOLTEN_TIN_BUCKET = BucketItem(MOLTEN_TIN_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_TIN = object : FluidBlock(MOLTEN_TIN_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_LEAD_IDENTIFIER = identifier("molten_lead")
    val MOLTEN_LEAD_STILL: BaseFluid.Still =
        BaseFluid.Still(MOLTEN_LEAD_IDENTIFIER, { MOLTEN_LEAD }, { MOLTEN_LEAD_BUCKET }, LEAD_BASE.toInt()) { MOLTEN_LEAD_FLOWING }
    val MOLTEN_LEAD_FLOWING =
        BaseFluid.Flowing(MOLTEN_LEAD_IDENTIFIER, { MOLTEN_LEAD }, { MOLTEN_LEAD_BUCKET }, LEAD_BASE.toInt()) { MOLTEN_LEAD_STILL }
    val MOLTEN_LEAD_BUCKET = BucketItem(MOLTEN_LEAD_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_LEAD = object : FluidBlock(MOLTEN_LEAD_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_SILVER_IDENTIFIER = identifier("molten_silver")
    val MOLTEN_SILVER_STILL: BaseFluid.Still =
        BaseFluid.Still(MOLTEN_SILVER_IDENTIFIER, { MOLTEN_SILVER }, { MOLTEN_SILVER_BUCKET }, SILVER_BASE.toInt()) { MOLTEN_SILVER_FLOWING }
    val MOLTEN_SILVER_FLOWING =
        BaseFluid.Flowing(MOLTEN_SILVER_IDENTIFIER, { MOLTEN_SILVER }, { MOLTEN_SILVER_BUCKET }, SILVER_BASE.toInt()) { MOLTEN_SILVER_STILL }
    val MOLTEN_SILVER_BUCKET = BucketItem(MOLTEN_SILVER_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_SILVER = object : FluidBlock(MOLTEN_SILVER_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val ACID_MATERIAL: Material =
        FabricMaterialBuilder(MapColor.GREEN).allowsMovement().lightPassesThrough().notSolid().replaceable()
            .liquid().build()
    val MUD_MATERIAL: Material =
        FabricMaterialBuilder(MapColor.BROWN).allowsMovement().lightPassesThrough().notSolid().replaceable()
            .liquid().build()

    val SULFURIC_ACID_IDENTIFIER = identifier("sulfuric_acid")
    val SULFURIC_ACID_STILL: BaseFluid.Still = BaseFluid.Still(SULFURIC_ACID_IDENTIFIER, { SULFURIC_ACID }, { SULFURIC_ACID_BUCKET }, 0x003D1E) { SULFURIC_ACID_FLOWING }
    val SULFURIC_ACID_FLOWING = BaseFluid.Flowing(SULFURIC_ACID_IDENTIFIER, { SULFURIC_ACID }, { SULFURIC_ACID_BUCKET }, 0x003D1E) { SULFURIC_ACID_STILL }
    val SULFURIC_ACID_BUCKET = BucketItem(SULFURIC_ACID_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val SULFURIC_ACID = AcidFluidBlock(SULFURIC_ACID_STILL, FabricBlockSettings.of(ACID_MATERIAL).ticksRandomly())

    val TOXIC_MUD_IDENTIFIER = identifier("toxic_mud")
    val TOXIC_MUD_STILL: BaseFluid.Still =
        BaseFluid.Still(TOXIC_MUD_IDENTIFIER, { TOXIC_MUD }, { TOXIC_MUD_BUCKET }, 0x5c3b0e) { TOXIC_MUD_FLOWING }
    val TOXIC_MUD_FLOWING =
        BaseFluid.Flowing(TOXIC_MUD_IDENTIFIER, { TOXIC_MUD }, { TOXIC_MUD_BUCKET }, 0x5c3b0e) { TOXIC_MUD_STILL }
    val TOXIC_MUD_BUCKET = BucketItem(TOXIC_MUD_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val TOXIC_MUD = object : FluidBlock(TOXIC_MUD_STILL, FabricBlockSettings.of(MUD_MATERIAL)) {}

    val HYDROGEN_IDENTIFIER = identifier("hydrogen")
    val HYDROGEN_STILL: BaseFluid.Still =
        BaseFluid.Still(HYDROGEN_IDENTIFIER, { HYDROGEN }, { HYDROGEN_BUCKET }, -1) { HYDROGEN_FLOWING }
    val HYDROGEN_FLOWING =
        BaseFluid.Flowing(HYDROGEN_IDENTIFIER, { HYDROGEN }, { HYDROGEN_BUCKET }, -1) { HYDROGEN_STILL }
    val HYDROGEN_BUCKET = BucketItem(HYDROGEN_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val HYDROGEN = object : FluidBlock(HYDROGEN_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val OXYGEN_IDENTIFIER = identifier("oxygen")
    val OXYGEN_STILL: BaseFluid.Still =
        BaseFluid.Still(OXYGEN_IDENTIFIER, { OXYGEN }, { OXYGEN_BUCKET }, 0xfcfccf) { OXYGEN_FLOWING }
    val OXYGEN_FLOWING =
        BaseFluid.Flowing(OXYGEN_IDENTIFIER, { OXYGEN }, { OXYGEN_BUCKET }, 0xfcfccf) { OXYGEN_STILL }
    val OXYGEN_BUCKET = BucketItem(OXYGEN_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val OXYGEN = object : FluidBlock(OXYGEN_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val METHANE_IDENTIFIER = identifier("methane")
    val METHANE_STILL: BaseFluid.Still =
        BaseFluid.Still(METHANE_IDENTIFIER, { METHANE }, { METHANE_BUCKET }, 0xe8ffbf) { METHANE_FLOWING }
    val METHANE_FLOWING =
        BaseFluid.Flowing(METHANE_IDENTIFIER, { METHANE }, { METHANE_BUCKET }, 0xe8ffbf) { METHANE_STILL }
    val METHANE_BUCKET = BucketItem(METHANE_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val METHANE = object : FluidBlock(METHANE_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val STEAM_IDENTIFIER = identifier("steam")
    val STEAM_STILL: BaseFluid.Still = BaseFluid.Still(STEAM_IDENTIFIER, { STEAM }, { STEAM_BUCKET }, -1) { STEAM_FLOWING }
    val STEAM_FLOWING = BaseFluid.Flowing(STEAM_IDENTIFIER, { STEAM }, { STEAM_BUCKET }, -1) { STEAM_STILL }
    val STEAM_BUCKET = BucketItem(STEAM_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val STEAM = object : FluidBlock(STEAM_STILL, FabricBlockSettings.of(Material.WATER)) {}

    val MOLTEN_SALT_IDENTIFIER = identifier("molten_salt")
    val MOLTEN_SALT_STILL: BaseFluid.Still =
        BaseFluid.Still(MOLTEN_SALT_IDENTIFIER, { MOLTEN_SALT }, { MOLTEN_SALT_BUCKET }, -1) { MOLTEN_SALT_FLOWING }
    val MOLTEN_SALT_FLOWING =
        BaseFluid.Flowing(MOLTEN_SALT_IDENTIFIER, { MOLTEN_SALT }, { MOLTEN_SALT_BUCKET }, -1) { MOLTEN_SALT_STILL }
    val MOLTEN_SALT_BUCKET = BucketItem(MOLTEN_SALT_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_SALT = object : FluidBlock(MOLTEN_SALT_STILL, FabricBlockSettings.of(Material.LAVA)) {}
}