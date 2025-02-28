package one.voidstar.kdie

class DiceRollInstructionStack private constructor(initialSize: Int) {
    val instructions: MutableList<DiceRollInstruction> = ArrayList(if (initialSize == 0) 1 else initialSize)

    companion object {
        fun create(size: Int): DiceRollInstructionStack {
            return DiceRollInstructionStack(size)
        }
    }

    fun push(instruction: DiceRollInstruction) {
        instructions.add(instruction)
    }

    fun peek(): DiceRollInstruction? {
        return if (instructions.isEmpty()) null else instructions.last()
    }

    fun pop(): DiceRollInstruction? {
        return if (instructions.isEmpty()) null else instructions.removeAt(instructions.size - 1)
    }

    fun instructionAt(index: Int): DiceRollInstruction? {
        return if (index < 0 || index >= instructions.size) null else instructions[index]
    }

    fun getDiceCollections(): List<DiceCollection> {
        val diceCollections = mutableListOf<DiceCollection>()

        for (i in instructions.indices) {
            val instruction = instructionAt(i)
            val potentialDc = instruction?.getDiceCollection()

            if (potentialDc != null) {
                diceCollections.add(potentialDc)
            }
        }

        return diceCollections
    }

    fun evaluate(): DiceRollInstruction? {
        var dri: DiceRollInstruction? = null

        if (instructions.size == 1) {
            return pop()
        }

        val workingStack = mutableListOf<DiceRollInstruction>()

        while (peek() != null) {
            dri = pop() ?: break
            val opType = dri.operationType

            // Bad value
            if (opType == OperationType.UNKNOWN) {
                return null
            }

            // Operands
            if (opType.ordinal <= OperationType.NUMBER.ordinal) {
                workingStack.add(dri)
                continue
            }

            // Operations
            dri = dri.doOperation(workingStack) ?: continue
            workingStack.add(dri)
        }

        return if (workingStack.isNotEmpty()) workingStack.removeAt(workingStack.size - 1) else null
    }
}

// Helper function to convert from instruction to result
fun resultFromInstruction(dri: DiceRollInstruction): DiceRollInstructionResult? {
    return when (dri.operationType) {
        OperationType.NUMBER -> {
            val d = dri.getNumber()
            DiceRollInstructionResult.withDouble(d)
        }
        OperationType.DICE_COLLECTION -> {
            val dc = dri.getDiceCollection()
            if (dc != null) DiceRollInstructionResult.withDiceCollection(dc) else null
        }
        else -> null
    }
}

// Extension function for printing elements
fun List<DiceRollInstructionResult>.printElements() {
    for (element in this) {
        element.print()
    }
}

// Extension function for printing instructions
fun List<DiceRollInstruction>.printInstructions() {
    for (element in this) {
        println(element)
    }
}