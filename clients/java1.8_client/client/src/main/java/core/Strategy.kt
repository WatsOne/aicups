package core

import core.API.Elevator
import core.API.Passenger
import mu.KLogging

class Strategy : BaseStrategy() {
    companion object: KLogging()
    var tick = 0

    private fun List<Elevator>.convert(): List<MyElevator> = this.map { MyElevator(it) }
    private fun List<MyPassenger>.onTheFloor(floor: Int): Boolean = this.any{ it.state == PassengerState.WAITING_FOR_ELEVATOR && it.floor == floor }
    private fun List<MyPassenger>.getFromFloor(floor: Int): List<MyPassenger> = this.filter{ it.state == PassengerState.WAITING_FOR_ELEVATOR && it.floor == floor }
    private fun List<MyPassenger>.runningToElevator(e: MyElevator): Int = this.filter{ it.state == PassengerState.MOVING_TO_ELEVATOR && it.elevator == e.id }.size
    private fun List<Passenger>.convert(isMy: Boolean): List<MyPassenger> = this.map { MyPassenger(it, isMy) }
    private fun List<Int>.avgFloor(): Int = this.groupingBy { it }.eachCount().maxBy { it.value }?.key!!

    override fun onTick(myPassengers: List<Passenger>, myElevators: List<Elevator>, enemyPassengers: List<Passenger>, enemyElevators: List<Elevator>) {
        tick++

        processTick(myPassengers.convert(true), myElevators.convert(), enemyPassengers.convert(false), enemyElevators.convert())
    }

    private fun processTick(passengers: List<MyPassenger>, elevators: List<MyElevator>, enemyPassengers: List<MyPassenger>, enemyElevators: List<MyElevator>) {
        elevators.filter { it.state == ElevatorState.FILLING }.forEach {
            if (it.state == ElevatorState.FILLING && !it.full && (passengers.onTheFloor(it.floor) || enemyPassengers.onTheFloor(it.floor))) {
                toWelcome(passengers, it)
                toWelcome(enemyPassengers, it)
            } else if (passengers.runningToElevator(it) == 0 && enemyPassengers.runningToElevator(it) == 0) {
                if (it.empty) {
                    val enemyOnFloor = enemyPassengers.filter { it.state == PassengerState.WAITING_FOR_ELEVATOR }.map { it.floor }
                    val totalOnFloor = passengers.filter { it.state == PassengerState.WAITING_FOR_ELEVATOR }.map { it.floor }.plus(enemyOnFloor)

                    it.goToFloor(totalOnFloor.avgFloor())
                } else {
                    it.goToFloor(getBestScoredFloor(it).first)
                }
            }
        }
    }

    private fun toWelcome(passengers: List<MyPassenger>, e: MyElevator) {
        passengers.getFromFloor(e.floor).forEach {
            if (passengers.runningToElevator(e) + e.currentPassengers < MyElevator.MAX) {
                it.setElevator(e)
            }
        }
    }

    private fun getBestScoredFloor(e: MyElevator): Pair<Int, Double> {
        return e.passengers.groupingBy { it.destFloor }
                .aggregate {_: Int, sum: Int?, p: MyPassenger, first: Boolean ->
                    Math.abs(p.fromFloor - p.destFloor) * (if (p.isMy) 10 else 20) + if (first) 0 else sum!!}
                .mapValues { it.value.toDouble() / (Math.abs(e.floor - it.key) * 50 + 200) }
                .maxBy { it.value }?.let { Pair(it.key, it.value) } ?: Pair(0, 0.0)
    }

    private fun stayPoint(e: MyElevator, my: List<MyPassenger>, enemy: List<MyPassenger>): Double {
        return 0.0
    }
}