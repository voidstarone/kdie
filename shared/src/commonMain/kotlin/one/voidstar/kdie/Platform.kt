package one.voidstar.kdie

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform