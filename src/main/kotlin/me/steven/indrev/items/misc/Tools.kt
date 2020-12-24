package me.steven.indrev.items.misc

import net.minecraft.item.*

//fuck Mojang with protected shit

class IRBasicSword(material: ToolMaterial, attackDamage: Int, attackSpeed: Float, settings: Settings) : SwordItem(material, attackDamage, attackSpeed, settings)
class IRBasicShovel(material: ToolMaterial, attackDamage: Float, attackSpeed: Float, settings: Settings) : ShovelItem(material, attackDamage, attackSpeed, settings)
class IRBasicPickaxe(material: ToolMaterial, attackDamage: Int, attackSpeed: Float, settings: Settings) : PickaxeItem(material, attackDamage, attackSpeed, settings)
class IRBasicHoe(material: ToolMaterial, attackDamage: Int, attackSpeed: Float, settings: Settings) : HoeItem(material, attackDamage, attackSpeed, settings)
class IRBasicAxe(material: ToolMaterial, attackDamage: Float, attackSpeed: Float, settings: Settings) : AxeItem(material, attackDamage, attackSpeed, settings)