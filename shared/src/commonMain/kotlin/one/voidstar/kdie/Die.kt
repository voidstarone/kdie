data class Die(
    val sides: Long,
    private var lastResult: Long = -1
) {
    val result: Long
        get() {
            if (lastResult == (-1).toLong()) {
                this.roll()
            }
            return lastResult
        }

    fun roll() {
        lastResult = (1..sides).random()
    }
}