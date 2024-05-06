package com.dynamic.island.oasis.dynamic_island.data

sealed class DiState() {

    data class Main(val animShake: Boolean = false) : DiState()
    data class Music(
        val expanded: Boolean,
        val packageName: String
    ) : DiState()

     class IncomingCall() : DiState()

    data class ActiveCall(
        val expanded: Boolean
    ) : DiState()

    data class Notification(
        val expanded: Boolean,
        val packageName: String
    ) : DiState()

    data class Timer( val packageName: String, val expanded: Boolean, val progress:Float) : DiState()


    data class Alert(val animation: Int, val text: String) : DiState()
     class QuickAction() : DiState()


}