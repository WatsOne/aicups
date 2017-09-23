package core

import core.API.Passenger

fun List<Int>.avgFloor(): Int = this.groupingBy { it }.eachCount().maxBy { it.value }?.key!!