package one.voidstar.kdie

import Die

class DiceCollection(
    val size: Long,
    val sides: Long,
    var indexFoundAt: Long
) {
    private var dice: Array<Die> = Array(size.toInt()) { Die(sides) }
    private var _lastResults: MutableList<Long> = mutableListOf<Long>()
    var explosionLowerBound: Long = 0
    var doExplosionsStack: Boolean = true

    val lastResults: List<Long>
        get() {
            if (_lastResults == null) {
                rollSilent()
            }
            return _lastResults!!
        }

    fun dieAt(index: Int): Die {
        return dice[index]
    }

    fun setResults(results: List<Long>) {
        _lastResults = results.toMutableList()
    }

    fun rollSilent() {
        val newResults = mutableListOf<Long>()

        // Roll all dice
        for (i in 0 until size.toInt()) {
            val die = dieAt(i)
            die.roll()
            newResults.add(die.result)
        }

        _lastResults = newResults

        // Handle explosions if needed
        if (explosionLowerBound > 0) {
            doExplodes(0)
        }
    }

    private fun doExplodes(startIndex: Int) {
        val currentResults = _lastResults!!
        val count = currentResults.size
        var numExplosions = 0

        // Count explosions
        for (i in startIndex until count) {
            val dieResult = currentResults[i]
            if (dieResult >= explosionLowerBound) {
                numExplosions++
            }
        }

        if (numExplosions == 0) {
            return
        }

        // Roll extra dice for explosions
        val tempDie = Die(sides)
        for (i in 0 until numExplosions) {
            tempDie.roll()
            currentResults.add(tempDie.result)
        }

        // Handle stacking explosions if enabled
        if (doExplosionsStack) {
            doExplodes(count)
        }
    }

    fun roll(): List<Long> {
        rollSilent()
        return lastResults
    }

    fun total(): Long {
        return lastResults.sum()
    }

    fun countResultsAboveOrMatchingBound(bound: Long): Long {
        return lastResults.count { it >= bound }.toLong()
    }

    fun countResultsBelowOrMatchingBound(bound: Long): Long {
        return lastResults.count { it <= bound }.toLong()
    }

    override fun toString(): String {
        val resultValues = if (_lastResults != null) {
            _lastResults!!.joinToString(", ")
        } else {
            dice.map { it.result }.joinToString(", ")
        }

        return "DiceCollection($sides, $size){ $resultValues }"
    }

    companion object {
        fun create(size: Long, sides: Long): DiceCollection {
            return DiceCollection(size, sides, 0)
        }
    }
}