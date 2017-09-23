package core

import core.API.Elevator
import core.API.Passenger

class MyPassenger(val passenger: Passenger) {

    val id: Int?
        get() = passenger.id

    val elevator: Int?
        get() = passenger.elevator

    val fromFloor: Int?
        get() = passenger.fromFloor

    val destFloor: Int?
        get() = passenger.destFloor

    val state: PassengerState?
        get() = PassengerState.parse(passenger.state)

    val timeToAway: Int?
        get() = passenger.timeToAway

    val type: String
        get() = passenger.type

    val floor: Int?
        get() = passenger.floor

    val x: Double?
        get() = passenger.x

    val y: Double?
        get() = passenger.y

    val weight: Double?
        get() = passenger.weight

    fun setElevator(elevator: Elevator) {
        passenger.setElevator(elevator)
    }
}