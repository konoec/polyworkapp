package com.konoec.polyworkapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform