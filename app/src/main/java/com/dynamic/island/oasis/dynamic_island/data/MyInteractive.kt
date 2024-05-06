package com.dynamic.island.oasis.dynamic_island.data

data class MyInteractive(
    val skipNext:Boolean = false,
    val skipPrev:Boolean = false,
    val playPause:Boolean = false,
    val seek:Boolean = false
){
    fun hasControls() = skipNext || skipPrev || playPause
}