package com.tneff.remotecomposeexample

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform