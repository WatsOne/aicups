package core

import core.API.Elevator
import core.API.Passenger

class Strategy : BaseStrategy() {
    override fun onTick(myPassengers: List<Passenger>, myElevators: List<Elevator>, enemyPassengers: List<Passenger>, enemyElevators: List<Elevator>) {

        fun convert(passengers: List<Passenger>): List<MyPassenger> = passengers.map { MyPassenger(it) }
        fun convert(elevators: List<Elevator>): List<MyElevator> = elevators.map { MyElevator(it) }

        processTick(convert(myPassengers), convert(myElevators), convert(enemyPassengers), convert(enemyElevators))
    }

    private fun processTick(passengers: List<MyPassenger>, elevators: List<MyElevator>, enemyPassengers: List<MyPassenger>, enemyElevators: List<MyElevator>) {

    }
}