package me.steven.indrev.blockentities

interface TemperatureController {
    fun getCurrentTemperature(): Double
    fun setCurrentTemperature(temperature: Double)
    fun getOptimalRange(): IntRange
    fun getBaseHeatingEfficiency(): Double
    fun getLimitTemperature(): Double
}