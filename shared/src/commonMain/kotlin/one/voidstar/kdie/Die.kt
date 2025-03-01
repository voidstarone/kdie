data class Die(
    val sides: Int,
    private var lastResult: Int = -1
) {
    val result: Int
        get() {
            if (lastResult == -1) {
                this.roll()
            }
            return lastResult
        }

    fun roll() {
        lastResult = (1..sides).random()
    }
}