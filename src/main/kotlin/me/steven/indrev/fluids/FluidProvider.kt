package me.steven.indrev.fluids

interface FluidProvider {
    fun getAmount(): Int
    fun setAmount(amount: Int)
    fun getMaximumStorage(): Int
    fun insert(amount: Int): Int = getAmount().plus(amount).coerceAtMost(getMaximumStorage()).apply { setAmount(this) }
    fun remove(amount: Int): Int = getAmount().minus(amount).coerceAtLeast(0).apply { setAmount(this) }
    fun canInsert(amount: Int): Boolean = getAmount() + amount < getMaximumStorage()
    fun canRemove(amount: Int): Boolean = getAmount() - amount < 0
}