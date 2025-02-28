package one.voidstar.kdie

class DiceRollInstruction constructor() {
    var value: Any? = null
    var expectedResultType: ResultType = ResultType.DOUBLE
    var numArgs: Int = 0
    var operationType: OperationType = OperationType.UNKNOWN

    companion object {
        private val operations = mutableMapOf<OperationType, (List<DiceRollInstruction>) -> DiceRollInstruction>()
        private var operationsInitialized = false

        private fun setupOperations() {
            if (operationsInitialized) return

            operations[OperationType.ADD] = { args ->
                val arg1 = args[0]
                val arg2 = args[1]
                val num1 = arg1.getNumber()
                val num2 = arg2.getNumber()
                createNumber(num1 + num2)
            }

            operations[OperationType.SUBTRACT] = { args ->
                val arg1 = args[0]
                val arg2 = args[1]
                val num1 = arg1.getNumber()
                val num2 = arg2.getNumber()
                createNumber(num2 - num1)
            }

            operations[OperationType.MULTIPLY] = { args ->
                val arg1 = args[0]
                val arg2 = args[1]
                val num1 = arg1.getNumber()
                val num2 = arg2.getNumber()
                createNumber(num1 * num2)
            }

            operations[OperationType.DIVIDE] = { args ->
                val arg1 = args[0]
                val arg2 = args[1]
                val num1 = arg1.getNumber()
                val num2 = arg2.getNumber()
                createNumber(num2 / num1)
            }

            operations[OperationType.MAX] = { args ->
                val arg1 = args[0]
                if (arg1.operationType != OperationType.DICE_COLLECTION) {
                    throw IllegalArgumentException("Max operation requires a dice collection")
                }

                val dc = arg1.getDiceCollection()
                val results = dc.lastResults
                val maximum = results.maxOrNull() ?: Double.MIN_VALUE
                createNumber(maximum.toDouble())
            }

            operations[OperationType.SUM] = { args ->
                val arg1 = args[0]
                if (arg1.operationType != OperationType.DICE_COLLECTION) {
                    throw IllegalArgumentException("Sum operation requires a dice collection")
                }

                val dc = arg1.getDiceCollection()
                createNumber(dc.total().toDouble())
            }

            operations[OperationType.MEAN] = { args ->
                val arg1 = args[0]
                if (arg1.operationType != OperationType.DICE_COLLECTION) {
                    throw IllegalArgumentException("Mean operation requires a dice collection")
                }

                val dc = arg1.getDiceCollection()
                val results = dc.lastResults
                val sum = results.sum()
                createNumber(sum.toDouble() / results.size)
            }

            operationsInitialized = true
        }

        fun create(): DiceRollInstruction {
            return DiceRollInstruction()
        }

        fun createNumber(num: Double): DiceRollInstruction {
            val dri = create()
            dri.operationType = OperationType.NUMBER
            dri.value = num
            dri.expectedResultType = ResultType.DOUBLE
            return dri
        }

        fun clone(original: DiceRollInstruction): DiceRollInstruction {
            val clone = create()
            clone.value = original.value
            clone.expectedResultType = original.expectedResultType
            clone.numArgs = original.numArgs
            clone.operationType = original.operationType
            return clone
        }

        fun fromString(stringRepresentation: String): DiceRollInstruction? {
            val dri = create()
            dri.expectedResultType = ResultType.DOUBLE

            val opType = findOperationTypeForString(stringRepresentation)
            dri.setOperationType(opType)

            when (opType) {
                OperationType.NUMBER -> {
                    val doubleValue = stringRepresentation.toDoubleOrNull() ?: -1.0
                    dri.value = doubleValue
                    dri.expectedResultType = ResultType.DOUBLE
                }
                OperationType.DICE_COLLECTION -> {
                    val dc = DiceNotationInterpreter.createFromNotation(stringRepresentation)
                    if (dc == null) {
                        return null
                    }
                    dri.value = dc
                    dri.expectedResultType = ResultType.DICE_COLLECTION
                }
                else -> {}
            }

            return dri
        }

        private fun findOperationTypeForString(stringRep: String): OperationType {
            return when {
                stringRep == "+" -> OperationType.ADD
                stringRep == "-" -> OperationType.SUBTRACT
                stringRep == "*" -> OperationType.MULTIPLY
                stringRep == "/" -> OperationType.DIVIDE
                stringRep == "sum" -> OperationType.SUM
                stringRep == "mean" || stringRep == "avg" -> OperationType.MEAN
                stringRep == "max" -> OperationType.MAX
                stringIsDiceCollection(stringRep) -> OperationType.DICE_COLLECTION
                stringIsDouble(stringRep) -> OperationType.NUMBER
                else -> OperationType.UNKNOWN
            }
        }

        private fun stringIsDiceCollection(stringRep: String): Boolean {
            val regex = Regex("[0-9]+(d[0-9]+|%)!?")
            return regex.matches(stringRep)
        }

        private fun stringIsDouble(stringRep: String): Boolean {
            val regex = Regex("[0-9]+(\\.[0-9]*)?")
            return regex.matches(stringRep)
        }
    }

    fun setOperationType(opType: OperationType) {
        numArgs = when {
            opType == OperationType.UNKNOWN -> 0
            opType == OperationType.SUM || opType == OperationType.MEAN || opType == OperationType.MAX -> 1
            opType == OperationType.ADD || opType == OperationType.SUBTRACT ||
                    opType == OperationType.MULTIPLY || opType == OperationType.DIVIDE -> 2
            else -> -1
        }

        operationType = opType
    }

    fun getNumber(): Double {
        return when (operationType) {
            OperationType.NUMBER -> value as Double
            OperationType.DICE_COLLECTION -> (value as DiceCollection).total().toDouble()
            else -> 0.0
        }
    }

    fun getDiceCollection(): DiceCollection {
        return if (operationType == OperationType.DICE_COLLECTION) {
            value as DiceCollection
        } else {
            throw IllegalStateException("Not a dice collection")
        }
    }

    fun doOperation(args: List<DiceRollInstruction>): DiceRollInstruction {
        setupOperations()
        val operation = operations[operationType] ?: throw IllegalStateException("Operation not supported")
        return operation(args)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is DiceRollInstruction) return false

        return value == other.value &&
                expectedResultType == other.expectedResultType &&
                numArgs == other.numArgs &&
                operationType == other.operationType
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + expectedResultType.hashCode()
        result = 31 * result + numArgs
        result = 31 * result + operationType.hashCode()
        return result
    }

    override fun toString(): String {
        return when (operationType) {
            OperationType.ADD -> "+"
            OperationType.SUBTRACT -> "-"
            OperationType.MULTIPLY -> "*"
            OperationType.DIVIDE -> "/"
            OperationType.SUM -> "sum"
            OperationType.MEAN -> "mean"
            OperationType.MAX -> "max"
            OperationType.NUMBER -> getNumber().toString()
            OperationType.DICE_COLLECTION -> {
                val dc = getDiceCollection()
                "${dc.size}d${dc.sides}"
            }
            else -> "Unknown Operation"
        }
    }
}