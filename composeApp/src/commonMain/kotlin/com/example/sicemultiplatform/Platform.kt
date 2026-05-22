package com.example.sicemultiplatform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform