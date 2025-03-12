package one.voidstar.kdie

import Die

class DiceCollection(
    val size: Int,
    val sides: Int,
    var indexFoundAt: Int = -1
) {
    private var dice: Array<Die> = Array(size.toInt()) { Die(sides) }
    private var _lastResults: MutableList<Int> = mutableListOf<Int>()
    var explosionLowerBound: Int = 0
    var doExplosionsStack: Boolean = true

    val lastResults: List<Int>
        get() {
            if (_lastResults.isEmpty()) {
                rollSilent()
            }
            return _lastResults!!
        }

    fun dieAt(index: Int): Die {
        return dice[index]
    }

    fun setResults(results: List<Int>) {
        _lastResults = results.toMutableList()
    }

    fun rollSilent() {
        val newResults = ArrayList<Int>(size)

        // Roll all dice
        for (i in 0 until size) {
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

    fun doExplodes() {
        doExplodes(0)
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

    fun roll(): List<Int> {
        rollSilent()
        return lastResults
    }

    fun total(): Int {
        return lastResults.sum()
    }

    fun countResultsAboveOrMatchingBound(bound: Int): Int {
        return lastResults.count { it >= bound }.toInt()
    }

    fun countResultsBelowOrMatchingBound(bound: Int): Int {
        return lastResults.count { it <= bound }.toInt()
    }

    override fun toString(): String {
        val resultValues = if (!_lastResults.isEmpty()) {
            _lastResults.joinToString(", ")
        } else {
            dice.map { it.result }.joinToString(", ")
        }

        return "DiceCollection($sides, $size){ $resultValues }"
    }

    companion object {
        fun create(size: Int, sides: Int): DiceCollection {
            return DiceCollection(size, sides, 0)
        }
    }
}