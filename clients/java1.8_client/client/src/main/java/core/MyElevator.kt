package core

import core.API.Elevator
import core.API.Passenger

class MyElevator(val elevator: Elevator) {

    val id: Int?
        get() = elevator.id

    val y: Double?
        get() = elevator.y

    val passengers: List<Passenger>
        get() = elevator.passengers

    val state: ElevatorState?
        get() = ElevatorState.parse(elevator.state)

    val speed: Double?
        get() = elevator.speed

    val timeOnFloor: Int?
        get() = elevator.timeOnFloor

    val floor: Int?
        get() = elevator.floor

    val type: String
        get() = elevator.type

    val nextFloor: Int?
        get() = elevator.nextFloor

    fun goToFloor(floor: Int?) {
        elevator.goToFloor(floor)
    }
}
