package one.voidstar.kdie

import kotlin.math.pow

// Enum classes to replace C enums
enum class RangeType {
    RANGE,
    RANGE_WITH_PRIORITY
}


// Replace C's DynArray with a more Kotlinesque approach
class DynamicArray<T>(initialCapacity: Int = 16) {
    private var elements = ArrayList<T>(initialCapacity)

    fun push(element: T) {
        elements.add(element)
    }

    fun pop(): T? {
        if (elements.isEmpty()) return null
        return elements.removeAt(elements.size - 1)
    }

    fun peek(): T? {
        if (elements.isEmpty()) return null
        return elements[elements.size - 1]
    }

    fun count(): Int {
        return elements.size
    }

    fun isEmpty(): Boolean {
        return elements.isEmpty()
    }

    fun elementAtIndex(index: Int): T {
        return elements[index]
    }

    fun reverseInPlace() {
        elements.reverse()
    }
}
