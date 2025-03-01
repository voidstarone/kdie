package one.voidstar.kdie

import kotlin.math.*

fun String.format(vararg args: Any): String {
    val result = StringBuilder()
    var argIndex = 0
    var i = 0
    val format = this

    while (i < format.length) {
        val currentChar = format[i]

        if (currentChar == '%' && i + 1 < format.length) {
            // Save starting position to handle the entire format specifier
            val specifierStart = i
            i++ // Move past '%'

            // Parse width and precision if present
            var width = 0
            var precision = -1
            var widthSpecified = false

            // Parse width (digits after %)
            while (i < format.length && format[i].isDigit()) {
                width = width * 10 + (format[i] - '0')
                widthSpecified = true
                i++
            }

            // Parse precision (after .)
            if (i < format.length && format[i] == '.') {
                i++ // Skip '.'
                precision = 0
                while (i < format.length && format[i].isDigit()) {
                    precision = precision * 10 + (format[i] - '0')
                    i++
                }
            }

            // Now parse the format specifier type
            if (i < format.length) {
                when (val specifier = format[i]) {
                    's' -> {
                        if (argIndex < args.size) {
                            val str = args[argIndex].toString()
                            // Apply width padding if specified
                            if (widthSpecified) {
                                result.append(str.padStart(width))
                            } else {
                                result.append(str)
                            }
                            argIndex++
                        } else {
                            throw IllegalArgumentException("Missing argument for format specifier %s")
                        }
                        i++ // Move past specifier
                    }
                    'd' -> {
                        if (argIndex < args.size) {
                            val numStr = when (val arg = args[argIndex]) {
                                is Int -> arg.toString()
                                is Long -> arg.toString()
                                is Short -> arg.toString()
                                is Byte -> arg.toString()
                                is Double -> arg.toInt().toString()
                                is Float -> arg.toInt().toString()
                                else -> throw IllegalArgumentException("Expected numeric type for %d, but got ${arg::class.simpleName}")
                            }

                            // Apply width padding if specified
                            if (widthSpecified) {
                                result.append(numStr.padStart(width))
                            } else {
                                result.append(numStr)
                            }
                            argIndex++
                        } else {
                            throw IllegalArgumentException("Missing argument for format specifier %d")
                        }
                        i++ // Move past specifier
                    }
                    'f' -> {
                        if (argIndex < args.size) {
                            val num = when (val arg = args[argIndex]) {
                                is Double -> arg
                                is Float -> arg.toDouble()
                                is Int -> arg.toDouble()
                                is Long -> arg.toDouble()
                                is Short -> arg.toDouble()
                                is Byte -> arg.toDouble()
                                else -> throw IllegalArgumentException("Expected numeric type for %f, but got ${arg::class.simpleName}")
                            }

                            // Pure Kotlin decimal formatting
                            val actualPrecision = if (precision >= 0) precision else 6

                            // Format with correct precision
                            val formattedNum = formatFloatWithPrecision(num, actualPrecision)

                            // Apply width padding if specified
                            if (widthSpecified) {
                                result.append(formattedNum.padStart(width))
                            } else {
                                result.append(formattedNum)
                            }
                            argIndex++
                        } else {
                            throw IllegalArgumentException("Missing argument for format specifier %f")
                        }
                        i++ // Move past specifier
                    }
                    '%' -> {
                        result.append('%')
                        i++ // Move past second '%'
                    }
                    else -> {
                        // Unsupported format specifier - add everything as-is
                        result.append(format.substring(specifierStart, i + 1))
                        i++ // Move past unsupported specifier
                    }
                }
            } else {
                // Handle case where string ends with %
                result.append('%')
            }
        } else {
            result.append(currentChar)
            i++
        }
    }

    return result.toString()
}

// Helper function to format floating point numbers with specific precision
private fun formatFloatWithPrecision(value: Double, precision: Int): String {
    // Handle special cases
    if (value.isNaN()) return "NaN"
    if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

    // For precision = 0, just round to nearest integer
    if (precision == 0) {
        return value.roundToInt().toString()
    }

    // Scale the value by 10^precision for rounding
    val factor = 10.0.pow(precision)
    val scaledValue = round(value * factor) / factor

    // Get the string representation
    val result = StringBuilder()

    // Add the integer part
    val intPart = scaledValue.toLong()
    result.append(intPart.toString())

    // Add decimal point and fractional part
    result.append(".")

    // Calculate the fractional part ensuring proper padding with zeros
    val fractionalPart = (abs(scaledValue) * factor).roundToLong() % factor.toLong()

    // Pad with leading zeros if needed
    val fractionalStr = fractionalPart.toString()
    val zerosToPad = precision - fractionalStr.length
    for (j in 0 until zerosToPad) {
        result.append('0')
    }
    result.append(fractionalStr.toString())

    return result.toString()
}