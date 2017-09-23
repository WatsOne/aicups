package core

import core.API.Elevator
import core.API.Passenger
import mu.KLogging

class Strategy : BaseStrategy() {
    companion object: KLogging()
    var tick = 0

    private fun List<Elevator>.convert(): List<MyElevator> = this.map { MyElevator(it) }
    private fun List<MyPassenger>.onTheFloor(floor: Int): Boolean = this.any{ it.state == PassengerState.WAITING_FOR_ELEVATOR && it.floor == floor }
    private fun List<MyPassenger>.fromFloor(floor: Int): List<MyPassenger> = this.filter{ it.floor == floor }
    private fun List<MyPassenger>.runningToElevator(e: MyElevator): Int = this.filter{ it.state == PassengerState.MOVING_TO_ELEVATOR && it.elevator == e.id }.size

    override fun onTick(myPassengers: List<Passenger>, myElevators: List<Elevator>, enemyPassengers: List<Passenger>, enemyElevators: List<Elevator>) {
        tick++
        processTick(myPassengers.convert(), myElevators.convert(), enemyPassengers.convert(), enemyElevators.convert())
    }

    private fun processTick(passengers: List<MyPassenger>, elevators: List<MyElevator>, enemyPassengers: List<MyPassenger>, enemyElevators: List<MyElevator>) {
        elevators.forEach {
            if (it.state == ElevatorState.FILLING && !it.full && (passengers.onTheFloor(it.floor) || enemyPassengers.onTheFloor(it.floor))) {
                toWelcome(passengers, it)
                toWelcome(enemyPassengers, it)
            } else if (passengers.runningToElevator(it) == 0 && enemyPassengers.runningToElevator(it) == 0) {
                if (it.empty) {
                    val enemyOnFloor = enemyPassengers.filter { it.state == PassengerState.WAITING_FOR_ELEVATOR }.map { it.floor }
                    val totalOnFloor = passengers.filter { it.state == PassengerState.WAITING_FOR_ELEVATOR }.map { it.floor }.plus(enemyOnFloor)

                    it.goToFloor(totalOnFloor.avgFloor())
                } else {
                    it.goToAvgFloor()
                }
            }
        }
    }

    private fun toWelcome(passengers: List<MyPassenger>, e: MyElevator) {
        passengers.fromFloor(e.floor).forEach {
            if (it.state == PassengerState.WAITING_FOR_ELEVATOR) {
                if (passengers.runningToElevator(e) + e.currentPassengers < MyElevator.MAX) {
                    it.setElevator(e.elevator)
                }
            }
        }
    }
}