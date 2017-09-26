package core

import core.API.Passenger

class MyPassenger(val passenger: Passenger) {

    val id: Int?
        get() = passenger.id

    val elevator: Int?
        get() = passenger.elevator

    val fromFloor: Int
        get() = passenger.fromFloor!!

    val destFloor: Int
        get() = passenger.destFloor!!

    val state: PassengerState
        get() = PassengerState.parse(passenger.state)!!

    val timeToAway: Int?
        get() = passenger.timeToAway

    val type: String
        get() = passenger.type

    val floor: Int
        get() = passenger.floor!!

    val x: Double?
        get() = passenger.x

    val y: Double?
        get() = passenger.y

//    val weight: Double?
//        get() = passenger.weight

    fun isMy(imFirst: Boolean): Boolean {
        return imFirst && type == "FIRST_PLAYER" || !imFirst && type == "SECOND_PLAYER"
    }

    fun setElevator(elevator: MyElevator) {
        passenger.setElevator(elevator.elevator)
    }

    fun setElevatorOnStart(e: MyElevator) {
        if ((e.id == 1 || e.id == 8) && (destFloor == 2 || destFloor == 3)) {
            passenger.setElevator(e.elevator)
        }
        if ((e.id == 3 || e.id == 6) && (destFloor == 4 || destFloor == 5)) {
            passenger.setElevator(e.elevator)
        }
        if ((e.id == 5 || e.id == 4) && (destFloor == 6 || destFloor == 7)) {
            passenger.setElevator(e.elevator)
        }
        if ((e.id == 7 || e.id == 2) && (destFloor == 8 || destFloor == 9)) {
            passenger.setElevator(e.elevator)
        }
    }
}
