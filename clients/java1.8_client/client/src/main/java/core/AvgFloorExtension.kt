package core

fun List<Int>.avgFloor(): Int = this.groupingBy { it }.eachCount().maxBy { it.value }?.key!!