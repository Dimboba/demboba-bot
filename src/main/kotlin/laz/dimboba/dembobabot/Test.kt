package laz.dimboba.dembobabot

import laz.dimboba.dembobabot.overwatch.OverbuffReader

fun main() {
    val stats = OverbuffReader().getSeparatePlayerStats("fluviatilis#2795")
    println(stats)
}