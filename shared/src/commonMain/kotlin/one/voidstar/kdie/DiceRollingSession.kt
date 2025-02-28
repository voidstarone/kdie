package one.voidstar.kdie

import kotlin.math.log10
import kotlin.math.floor

class DiceRollingSession private constructor() {
    var diceCollections: MutableList<DiceCollection>? = null
    var botchUpperBound: Long = 0
    var successLowerBound: Long = Long.MAX_VALUE

    companion object {
        fun create(): DiceRollingSession {
            return DiceRollingSession()
        }

        // Helper function to count number of digits in a number
        private fun numDigits(n: Long): Int {
            return if (n == 0L) 1 else floor(log10(n.toDouble()) + 1).toInt()
        }

        // Helper function to count decimals in a double
        private fun countDecimals(value: Double): Int {
            val stringValue = value.toString()
            val decimalIndex = stringValue.indexOf('.')
            return if (decimalIndex < 0) 0 else stringValue.length - decimalIndex - 1
        }

        // Helper function to calculate expected output length for dice collections
        private fun expectedOutputLengthForDiceCollections(dcs: List<DiceCollection>): Int {
            var length = 64
            for (dc in dcs) {
                val maxDigits = numDigits(dc.sides)
                val numDice = dc.size
                length += (maxDigits * numDice + numDice).toInt()
            }
            return length
        }
    }

    fun resolveNotation(expression: String): String {
        val instructions = diceRollInstructionStackFromExpression(expression)

        diceCollections = instructions.getDiceCollections().toMutableList()
        diceCollections?.sortBy { it.indexFoundAt }

        val expectedLength = diceCollections?.let { expectedOutputLengthForDiceCollections(it) } ?: 64
        val resultBuilder = StringBuilder(expectedLength)

        diceCollections?.forEach { dc ->
            val resultsString = "${dc.size}d${dc.sides}: ${dc.createResultsString()}"
            resultBuilder.append(resultsString)
            resultBuilder.append("\n")

            if (successLowerBound < Long.MAX_VALUE) {
                val botches = dc.countResultsBelowOrMatchingBound(botchUpperBound)
                val successes = dc.countResultsAboveOrMatchingBound(successLowerBound)
                resultBuilder.append("successes: ${successes - botches}\n")
            }
        }

        val finalResult = instructions.evaluate()
        val finalResultNum = finalResult?.getNumber() ?: 0.0

        if (countDecimals(finalResultNum) == 0) {
            resultBuilder.append(finalResultNum.toInt().toString())
        } else {
            resultBuilder.append("%.2f".format(finalResultNum))
        }

        return resultBuilder.toString()
    }
}

// This function would need to be implemented or imported from elsewhere
fun diceRollInstructionStackFromExpression(expression: String): DiceRollInstructionStack {
    // Implementation would depend on your parser
    return DiceRollInstructionStack.create(16) // Placeholder
}

// Extension function for DiceCollection to create results string
fun DiceCollection.createResultsString(): String {
    return lastResults.joinToString(", ")
}