package core

import core.API.Passenger

fun List<Passenger>.convert(): List<MyPassenger> = this.map { MyPassenger(it) }