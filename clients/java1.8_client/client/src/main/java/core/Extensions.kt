package core

import core.API.Passenger

fun List<Passenger>.convert(): List<MyPassenger> = this.map { MyPassenger(it) }
fun List<Int>.avgFloor(): Int = this.groupingBy { it }.eachCount().maxBy { it.value }?.key!!