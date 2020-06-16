package me.steven.indrev.utils

import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

object EmptyEnergyStorage : EnergyStorage {
    override fun setStored(p0: Double) {}

    override fun getMaxStoredPower(): Double = 0.0

    override fun getTier(): EnergyTier? = null

    override fun getMaxInput(side: EnergySide?): Double = 0.0

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getStored(p0: EnergySide?): Double = 0.0
}