package me.steven.indrev.items

import me.steven.indrev.utils.Material
import me.steven.indrev.utils.Upgrade
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.item
import net.minecraft.item.Item

val ALL_ITEMS = mutableListOf<Item>()

val OVERCLOCKER_ITEM = UpgradeItem(Upgrade.OVERCLOCKER)
val OVERCLOCKER_2X_ITEM = UpgradeItem(Upgrade.OVERCLOCKER_2X)
val OVERCLOCKER_4X_ITEM = UpgradeItem(Upgrade.OVERCLOCKER_4X)
val OVERCLOCKER_8X_ITEM = UpgradeItem(Upgrade.OVERCLOCKER_8X)
val AUTOMATED_ITEM_TRANSFER_ITEM = UpgradeItem(Upgrade.AUTOMATED_ITEM_TRANSFER)
val AUTOMATED_FLUID_TRANSFER_ITEM = UpgradeItem(Upgrade.AUTOMATED_FLUID_TRANSFER)
val FUEL_EFFICIENCY_ITEM = UpgradeItem(Upgrade.FUEL_EFFICIENCY)

val IRON = Material.create("iron", Material.Type.DUST, Material.Type.PLATE)
val GOLD = Material.create("gold", Material.Type.DUST, Material.Type.PLATE)
val COAL = Material.create("coal", Material.Type.DUST)
val DIAMOND = Material.create("diamond", Material.Type.DUST)
val COPPER = Material.create("copper", Material.Type.DUST, Material.Type.PLATE, Material.Type.NUGGET)
val NIKOLITE = Material.create("nikolite", Material.Type.ORE, Material.Type.DEEPSLATE_ORE, Material.Type.DUST, Material.Type.INGOT)
val ENRICHED_NIKOLITE = Material.create("enriched_nikolite", Material.Type.DUST, Material.Type.INGOT)
val TIN = Material.createOre("tin")
val LEAD = Material.createOre("lead")
val TUNGSTEN = Material.createOre("tungsten")
val SILVER = Material.createOre("silver")
val BRONZE = Material.createAlloy("bronze")
val ELECTRUM = Material.createAlloy("electrum")
val STEEL = Material.createAlloy("steel")

fun registerItems() {

    Material.register(NIKOLITE, ENRICHED_NIKOLITE, IRON, GOLD, COAL, DIAMOND, COPPER, TIN, LEAD, TUNGSTEN, SILVER, BRONZE, ELECTRUM, STEEL)

    identifier("overclocker").item(OVERCLOCKER_ITEM)
    identifier("overclocker_2x").item(OVERCLOCKER_2X_ITEM)
    identifier("overclocker_4x").item(OVERCLOCKER_4X_ITEM)
    identifier("overclocker_8x").item(OVERCLOCKER_8X_ITEM)

    identifier("automated_item_transfer_upgrade").item(AUTOMATED_ITEM_TRANSFER_ITEM)
    identifier("automated_fluid_transfer_upgrade").item(AUTOMATED_FLUID_TRANSFER_ITEM)
    identifier("fuel_efficiency_upgrade").item(FUEL_EFFICIENCY_ITEM)

    for (x in 1 ..9) {
        identifier("farm_station_${x}_range_card").item(RangeCardItem(x))
    }
}