package one.voidstar.kdie

fun String.format(vararg args: Any): String {
    val result = StringBuilder()
    var argIndex = 0
    var i = 0
    val format = this
    while (i < format.length) {
        val currentChar = format[i]
        if (currentChar == '%' && i + 1 < format.length) {
            val nextChar = format[i + 1]
            when (nextChar) {
                's' -> {
                    if (argIndex < args.size) {
                        result.append(args[argIndex].toString())
                        argIndex++
                    }
                    i++
                }
                'd' -> {
                    if (argIndex < args.size) {
                        when (val arg = args[argIndex]) {
                            is Int -> result.append(arg.toString())
                            is Double -> result.append(arg.toInt().toString()) // Convert Double to Int
                            else -> throw IllegalArgumentException("Expected Int or Double for %d, but got ${arg::class.simpleName}")
                        }
                        argIndex++
                    }
                    i++
                }
                'f' -> {
                    if (argIndex < args.size) {
                        when (val arg = args[argIndex]) {
                            is Double -> result.append(arg.toString())
                            is Int -> result.append(arg.toDouble().toString()) // Convert Int to Double
                            else -> throw IllegalArgumentException("Expected Double or Int for %f, but got ${arg::class.simpleName}")
                        }
                        argIndex++
                    }
                    i++
                }
                '%' -> {
                    result.append('%')
                    i++
                }
                else -> {
                    result.append(currentChar)
                }
            }
        } else {
            result.append(currentChar)
        }
        i++
    }
    return result.toString()
}