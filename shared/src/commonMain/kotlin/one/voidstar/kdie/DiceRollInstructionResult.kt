package one.voidstar.kdie

class DiceRollInstructionResult private constructor() {
    var resultValue: Any? = null
    var type: ResultType = ResultType.VOID

    companion object {
        fun create(): DiceRollInstructionResult {
            return DiceRollInstructionResult()
        }

        fun init(): DiceRollInstructionResult {
            return DiceRollInstructionResult()
        }

        fun withDouble(value: Double): DiceRollInstructionResult {
            val result = create()
            result.type = ResultType.DOUBLE
            result.resultValue = value
            return result
        }

        fun withDiceCollection(dc: DiceCollection): DiceRollInstructionResult {
            val result = create()
            result.type = ResultType.DICE_COLLECTION
            result.resultValue = dc
            return result
        }
    }

    fun valueAsDouble(): Double {
        return resultValue as Double
    }

    fun getNumber(): Double {
        if (this.type == ResultType.DOUBLE) {
            return valueAsDouble()
        }
        if (this.type == ResultType.DICE_COLLECTION) {
            return (resultValue as DiceCollection).total().toDouble()
        }
        return -1.0
    }

    fun getDiceCollection(): DiceCollection? {
        if (this.type != ResultType.DICE_COLLECTION) {
            return null
        }
        return resultValue as DiceCollection
    }

    fun print() {
        when (type) {
            ResultType.DOUBLE -> {
                val num = getNumber()
                print("$num,")
            }
            ResultType.DICE_COLLECTION -> {
                val dc = getDiceCollection()
                print("${dc?.size}d${dc?.sides},")
            }
            else -> {
                print("Bad Op!,")
            }
        }
    }
}