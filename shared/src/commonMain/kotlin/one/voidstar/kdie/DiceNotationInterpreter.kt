package one.voidstar.kdie

// Data classes instead of C structs
data class Range(
    var index: Int,
    var length: Int
) {
    fun isInRange(i: Int): Boolean {
        return i >= index && i <= (index + length)
    }
}

data class OperatorWithLocation(
    val operation: OperationType,
    val index: Int
)

data class RangeWithPriority(
    val range: Range,
    var priority: Int
)


class DiceNotationInterpreter {
    companion object {
        private const val DICE_NOTATION_SEPARATOR = "d"
        private const val DICE_NOTATION_PERCENTILE_INDICATOR = "%"

        private fun isDigit(c: Char): Boolean {
            return c in '0'..'9'
        }

        private fun extractNumDice(notation: String): Pair<Int, Int> {
            if (notation.startsWith("d")) {
                return Pair(1.toInt(), 0.toInt())
            }
            if (notation.startsWith("%")) {
                return Pair(100.toInt(), 0.toInt())
            }

            var numDigits: Int = 0
            while (numDigits < notation.length && isDigit(notation[numDigits])) {
                numDigits++
            }

            val numDice = notation.substring(0, numDigits).toIntOrNull() ?: 0
            return Pair(numDice, numDigits.toInt())
        }

        private fun isPercentile(notation: String, startIndex: Int): Pair<Boolean, Int> {
            val isPercentile = notation.getOrNull(startIndex.toInt()) == '%'
            return Pair(isPercentile, startIndex + (if (isPercentile) 1 else 0))
        }

        private fun extractNumSides(notation: String, startIndex: Int): Pair<Int, Int> {
            var numDigits = 0
            while (startIndex + numDigits < notation.length &&
                isDigit(notation[startIndex + numDigits])) {
                numDigits++
            }

            val numSides = if (numDigits > 0) {
                notation.substring(startIndex, startIndex + numDigits).toIntOrNull() ?: 0
            } else {
                0
            }

            return Pair(numSides, startIndex + numDigits)
        }

        private fun doesExplode(notation: String, startIndex: Int): Pair<Boolean, Int> {
            val doesExplode = notation.getOrNull(startIndex) == '!'
            return Pair(doesExplode, startIndex + (if (doesExplode) 1 else 0))
        }

        private fun extractExplodesAt(notation: String, startIndex: Int): Pair<Int, Int> {
            var numDigits = 0
            while (startIndex + numDigits < notation.length &&
                isDigit(notation[startIndex + numDigits])) {
                numDigits++
            }

            val explodesAt = if (numDigits > 0) {
                notation.substring(startIndex, startIndex + numDigits).toIntOrNull() ?: 0
            } else {
                0
            }

            return Pair(explodesAt, startIndex + numDigits)
        }

        fun diceCollectionFromCoreNotation(notation: String): DiceCollection? {
            // Handle simple cases like "2d6"
            val parts = notation.split(DICE_NOTATION_SEPARATOR)
            if (parts.size != 2) return null

            val count: Int = parts[0].toIntOrNull() ?: 1
            val sides: Int = parts[1].toIntOrNull() ?: 1

            return DiceCollection(count, sides)
        }

        fun diceCollectionFromPercentileNotation(notation: String): DiceCollection? {
            // Handle percentile dice like "2%"
            if (!notation.contains(DICE_NOTATION_PERCENTILE_INDICATOR)) {
                return null
            }

            val parts = notation.split(DICE_NOTATION_PERCENTILE_INDICATOR)
            val count = if (parts[0].isEmpty()) 1 else parts[0].toIntOrNull() ?: 1

            return DiceCollection(count.toInt(), 100)
        }

        fun createFromNotation(notation: String): DiceCollection? {
            var numDice: Int
            var numSides: Int
            var explodesAt: Int = 0

            // Extract number of dice
            val (extractedNumDice, nextIndex1) = extractNumDice(notation)
            numDice = extractedNumDice
            if (numDice == 0.toInt()) return null

            // Check if percentile
            val (isPercentile, nextIndex2) = isPercentile(notation, nextIndex1.toInt())
            if (isPercentile) {
                numSides = 100
            } else {
                // Extract number of sides
                val (extractedNumSides, nextIndex3) = extractNumSides(notation, nextIndex2)
                numSides = extractedNumSides.toInt()
                if (numSides == 0.toInt()) return null

                // Check if dice explode
                val (doesExplode, nextIndex4) = doesExplode(notation, nextIndex3)

                if (nextIndex4 >= notation.length) {
                    val dc = DiceCollection(numDice, numSides)
                    if (doesExplode) {
                        dc.explosionLowerBound = numSides
                    }
                    return dc
                }

                // Extract explosion threshold
                val (extractedExplodesAt, nextIndex5) = extractExplodesAt(notation, nextIndex4)
                explodesAt = if (extractedExplodesAt == 0) numSides else extractedExplodesAt

                if (nextIndex5 < notation.length) {
                    return null
                }
            }

            val dc = DiceCollection(numDice.toInt(), numSides)
            if (explodesAt > 0) {
                dc.explosionLowerBound = explodesAt.toInt()
            }
            return dc
        }
    }

    fun createOperatorWithLocation(text: String, index: Int): OperatorWithLocation {
        val operation = when (text) {
            "+" -> OperationType.ADD
            "-" -> OperationType.SUBTRACT
            "*" -> OperationType.MULTIPLY
            "/" -> OperationType.DIVIDE
            else -> throw IllegalArgumentException("Unknown operator: $text")
        }
        return OperatorWithLocation(operation, index)
    }

    fun splitString(str: String, delimiter: String): List<String> {
        return str.split(delimiter)
    }

    fun isOperator(c: Char): Boolean {
        return c == '+' || c == '-' || c == '*' || c == '/'
    }

    fun isDigit(c: Char): Boolean {
        return c.isDigit()
    }

    fun couldBeNumber(str: String): Boolean {
        if (!isDigit(str[0])) {
            return false
        }

        var countDecimals = 0
        for (i in 1 until str.length) {
            val c = str[i]
            if (c == '.') {
                countDecimals++
                continue
            }
            if (countDecimals > 1) {
                return false
            }
            if (!isDigit(c)) {
                return false
            }
        }
        return true
    }

    fun couldBeDiceCollection(str: String): Boolean {
        if (!isDigit(str[0])) {
            return false
        }

        var countDs = 0
        for (i in 1 until str.length) {
            val c = str[i]
            if (c == 'd' || c == 'D') {
                countDs++
                if (i + 1 == str.length) {
                    return false
                }
                if (isDigit(str[i + 1])) {
                    continue
                }
            }
            if (countDs > 1) {
                return false
            }
            if (!isDigit(c)) {
                return false
            }
        }
        return countDs == 1
    }

    fun couldBeOperand(str: String): Boolean {
        return isDigit(str[0])
    }

    fun indexOfInnermostOpeningParen(str: String, startIndex: Int, length: Int): Int {
        var parenIndex = 0
        var maxDepth = 0
        var depth = 0

        for (i in startIndex until length) {
            if (str[i] == '(') {
                depth++
                if (depth > maxDepth) {
                    parenIndex = i
                    maxDepth = depth
                }
            }
            if (str[i] == ')') {
                depth--
            }
        }

        return if (maxDepth == 0) -1 else parenIndex
    }

    fun indexOfInnermostOpeningParenIgnoringRanges(
        str: String,
        startIndex: Int,
        length: Int,
        rangesToIgnore: DynamicArray<Range>
    ): Int {
        var parenIndex = 0
        var maxDepth = 0
        var depth = 0

        for (i in startIndex until length) {
            var shouldIgnore = false
            for (ri in 0 until rangesToIgnore.count()) {
                val r = rangesToIgnore.elementAtIndex(ri)
                if (r.isInRange(i)) {
                    shouldIgnore = true
                    break
                }
            }

            if (!shouldIgnore) {
                if (str[i] == '(') {
                    depth++
                    if (depth > maxDepth) {
                        parenIndex = i
                        maxDepth = depth
                    }
                }
                if (str[i] == ')') {
                    depth--
                }
            }
        }

        return parenIndex
    }

    fun indexOfNextClosingParam(str: String, startIndex: Int): Int {
        for (i in startIndex until str.length) {
            if (str[i] == ')') {
                return i
            }
        }
        return 0
    }

    fun countPairsOfParensInRange(str: String, startIndex: Int, length: Int): Int {
        var depth = 0
        var count = 0

        for (i in startIndex until length) {
            if (str[i] == '(') {
                depth++
            }
            if (str[i] == ')') {
                if (depth == 1) {
                    count++
                }
                depth--
            }
        }

        return count
    }

    fun charIsOperator(c: Char): Boolean {
        return c == '+' || c == '-' || c == '*' || c == '/'
    }

    fun charIsInlineWhitespace(c: Char): Boolean {
        return c == ' ' || c == '\t'
    }

    fun precedenceForOperator(c: Char): Int {
        return when (c) {
            ')', '(' -> 127
            '^' -> 6
            '*', '/' -> 5
            '+', '-' -> 4
            else -> 0
        }
    }

    fun indexOfNextOperatorInRange(str: String, startIndex: Int, length: Int): Int {
        for (i in startIndex until length) {
            if (charIsOperator(str[i])) {
                return i
            }
        }
        return -1
    }

    fun copyRangeToString(target: String, startIndex: Int, length: Int): String {
        if (length == 0) return ""
        return target.substring(startIndex, startIndex + length)
    }

    fun postfixifyExpression(postfixRanges: DynamicArray<Range>, expression: String) {
        val length = expression.length
        val operatorStack = DynamicArray<Range>()

        var i = 0
        while (i < length) {
            val c = expression[i]

            when {
                c == '(' -> {
                    operatorStack.push(Range(i, 1))
                }
                c == ')' -> {
                    // Pop until opening parenthesis
                    while (!operatorStack.isEmpty()) {
                        val r = operatorStack.pop() ?: break
                        if (expression[r.index] == '(') {
                            break
                        }
                        postfixRanges.push(r)
                    }
                }
                isOperator(c) -> {
                    val precedence = precedenceForOperator(c)

                    while (!operatorStack.isEmpty()) {
                        val r = operatorStack.peek() ?: break
                        val operator = expression[r.index]

                        if (operator == '(' || precedenceForOperator(operator) < precedence) {
                            break
                        }

                        postfixRanges.push(operatorStack.pop()!!)
                    }

                    operatorStack.push(Range(i, 1))
                }
                isDigit(c) || c == '.' || c.isLetter() -> {
                    // Find the end of the operand
                    var indexAfterOperand = -1
                    for (oI in i until length) {
                        val tmpC = expression[oI]
                        if (tmpC == ')' || charIsInlineWhitespace(tmpC) ||
                            charIsOperator(tmpC) || tmpC == '\u0000') {
                            indexAfterOperand = oI
                            break
                        }
                    }

                    if (indexAfterOperand == -1) {
                        println("Error processing operand!")
                        continue
                    }

                    val operandLength = indexAfterOperand - i
                    val r = Range(i, operandLength)
                    postfixRanges.push(r)
                    i = indexAfterOperand - 1
                }
                charIsInlineWhitespace(c) -> {
                    // Skip whitespace
                }
                c == '\u0000' -> {
                    break
                }
                else -> {
                    // Handle error
                    break
                }
            }

            i++
        }

        // Pop remaining operators
        while (!operatorStack.isEmpty()) {
            val r = operatorStack.pop() ?: break
            postfixRanges.push(r)
        }

        postfixRanges.reverseInPlace()
    }

    private fun numArgsForOpType(operationType: OperationType): Int {
        return when (operationType) {
            OperationType.ADD,
            OperationType.SUBTRACT,
            OperationType.MULTIPLY,
            OperationType.DIVIDE -> 2
            else -> 0
        }
    }

    fun diceRollInstructionFromString(operandStr: String): DiceRollInstruction? {
        // Implementation depends on what this function does in the original code
        // This is a simplified version based on usage in the code
        val operationType = when {
            operandStr == "+" -> OperationType.ADD
            operandStr == "-" -> OperationType.SUBTRACT
            operandStr == "*" -> OperationType.MULTIPLY
            operandStr == "/" -> OperationType.DIVIDE
            couldBeDiceCollection(operandStr) -> OperationType.DICE_COLLECTION
            couldBeNumber(operandStr) -> OperationType.NUMBER
            else -> throw IllegalArgumentException("Unknown operation: $operandStr")
        }

        val diceCollection = if (operationType == OperationType.DICE_COLLECTION) {
            diceCollectionFromCoreNotation(operandStr)
        } else {
            null
        }

        val numericValue = if (operationType == OperationType.NUMBER) {
            operandStr.toDoubleOrNull()
        } else {
            null
        }

        if (diceCollection == null && numericValue == null) {
            return null
        }

        val resultType: ResultType = if (diceCollection == null) ResultType.DOUBLE else ResultType.DICE_COLLECTION
        val numArgs = numArgsForOpType(operationType)
        return DiceRollInstruction(diceCollection ?: numericValue!!, resultType, numArgs, operationType)
    }

    fun diceRollInstructionStackFromExpression(expression: String): DiceRollInstructionStack {
        val instructionStack = DiceRollInstructionStack(8)
        val postfixRanges = DynamicArray<Range>()

        // Add parentheses around the expression
        val workingExpression = "($expression)"

        postfixifyExpression(postfixRanges, workingExpression)

        for (i in 0 until postfixRanges.count()) {
            val range = postfixRanges.elementAtIndex(i)
            val rangeStart = range.index
            val operandStr = copyRangeToString(workingExpression, rangeStart, range.length)

            val instruction = diceRollInstructionFromString(operandStr)

            if (instruction != null) {
                if (instruction.opType == OperationType.DICE_COLLECTION) {
                    val dc = instruction.getDiceCollection()
                    dc?.indexFoundAt = rangeStart
                }
                instructionStack.push(instruction)
            }
        }

        return instructionStack
    }
}