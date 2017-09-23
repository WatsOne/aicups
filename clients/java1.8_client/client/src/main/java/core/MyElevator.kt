package core

import core.API.Elevator
import core.API.Passenger

class MyElevator(val elevator: Elevator) {
    companion object {
        const val MAX = 20
    }

    val id: Int?
        get() = elevator.id

    val y: Double?
        get() = elevator.y

    val passengers: List<MyPassenger>
        get() = elevator.passengers.convert()

    val currentPassengers: Int
        get() = elevator.passengers.size

    val full: Boolean
        get() = currentPassengers >= MAX

    val empty: Boolean
        get() = currentPassengers == 0

    val state: ElevatorState
        get() = ElevatorState.parse(elevator.state)!!

    val speed: Double?
        get() = elevator.speed

    val timeOnFloor: Int?
        get() = elevator.timeOnFloor

    val floor: Int
        get() = elevator.floor

    val type: String
        get() = elevator.type

    val nextFloor: Int?
        get() = elevator.nextFloor

    fun goToFloor(floor: Int?) {
        elevator.goToFloor(floor)
    }

    fun goToAvgFloor() {
        val avgFloor = elevator.passengers.map { it.destFloor }.average()
        goToFloor(avgFloor.toInt())
    }
}
