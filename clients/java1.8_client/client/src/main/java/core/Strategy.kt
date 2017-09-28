package core

import core.API.Elevator
import core.API.Passenger
import java.util.*
import kotlin.collections.HashMap

class Strategy : BaseStrategy() {
//    companion object: KLogging()
    var startFloors = Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9).toMutableList()
    var startFloorMap = HashMap<Int, Pair<Int, Int>>()
    var tick = 0

    private fun List<Elevator>.convert(): List<MyElevator> = this.map { MyElevator(it) }
    private fun List<MyPassenger>.onTheFloor(floor: Int): Boolean = this.any{ it.state == PassengerState.WAITING_FOR_ELEVATOR && it.floor == floor }
    private fun List<MyPassenger>.getFromFloor(floor: Int): List<MyPassenger> = this.filter{ it.state == PassengerState.WAITING_FOR_ELEVATOR && it.floor == floor }
    private fun List<MyPassenger>.runningToElevator(e: MyElevator): Int = this.filter{ it.state != PassengerState.USING_ELEVATOR && it.elevator == e.id }.size
    private fun List<Passenger>.convert(isMy: Boolean): List<MyPassenger> = this.map { MyPassenger(it, isMy) }
    private fun List<Int>.avgFloor(): Int = this.groupingBy { it }.eachCount().maxBy { it.value }?.key!!

    override fun onTick(myPassengers: List<Passenger>, myElevators: List<Elevator>, enemyPassengers: List<Passenger>, enemyElevators: List<Elevator>) {
        tick++

        processTick(myPassengers.convert(true), myElevators.convert(), enemyPassengers.convert(false), enemyElevators.convert())
    }

    private fun processTick(passengers: List<MyPassenger>, elevators: List<MyElevator>, enemyPassengers: List<MyPassenger>, enemyElevators: List<MyElevator>) {
        val allPassengers = passengers.plus(enemyPassengers)

        elevators.filter { it.state == ElevatorState.FILLING }.forEach {

            if (!it.full && tick <= 1600 && it.floor == 1) {
                startFilling(it, allPassengers)
                return@forEach
            }

            var welcomeCount = 0

            if (!it.full && (passengers.onTheFloor(it.floor) || enemyPassengers.onTheFloor(it.floor))) {
                welcomeCount += toWelcome(allPassengers, it)
            }

            if (welcomeCount == 0 && passengers.runningToElevator(it) == 0 && enemyPassengers.runningToElevator(it) == 0) {
                if (it.empty) {
                    val enemyOnFloor = enemyPassengers.filter { it.state == PassengerState.WAITING_FOR_ELEVATOR }.map { it.floor }
                    val totalOnFloor = passengers.filter { it.state == PassengerState.WAITING_FOR_ELEVATOR }.map { it.floor }.plus(enemyOnFloor)

                    if (totalOnFloor.isNotEmpty()) {
                        it.goToFloor(totalOnFloor.avgFloor())
                    }
                } else {
                    it.goToFloor(getScore(it.passengers, it.floor).first)
                }
            }
        }
    }

    private fun toWelcome(passengers: List<MyPassenger>, e: MyElevator): Int {
        var count = 0

        passengers.getFromFloor(e.floor).forEach {
            if (passengers.runningToElevator(e) + e.currentPassengers < MyElevator.MAX) {
                val totalPassengers = e.passengers.plus(passengers.filter { it.state != PassengerState.USING_ELEVATOR && it.elevator == e.id })
                val currentScore = getScore(totalPassengers, e.floor)
                val newScore = getScore(totalPassengers.plus(it), e.floor)
                if (newScore.second > currentScore.second) {
                    it.setElevator(e)
                    count++
                }
            }
        }

        return count
    }

    private fun getScore(passengers: List<MyPassenger>, currentFloor: Int): Pair<Int, Double> {
        val scoredFloors = passengers.groupingBy { it.destFloor }
                .aggregate { _: Int, sum: Int?, p: MyPassenger, first: Boolean ->
                    Math.abs(p.fromFloor - p.destFloor) * p.score + if (first) 0 else sum!!
                }
                .mapValues { Pair(it.value, (Math.abs(currentFloor - it.key) * 50 + 240)) }

        var firstFloor = 1
        var maxScore = 0.0

        for ((key, value) in scoredFloors) {
            (1..9)
                    .filter { key != it }
                    .forEach { floor ->
                        val points = passengers.filter { it.destFloor == floor }.sumBy { it.score * Math.abs(it.fromFloor - floor) }
                        val ticks = (Math.abs(key - floor) * 50 + 240)

                        val pps = (points.toDouble() + value.first) / (ticks.toDouble() + value.second)

                        if (pps > maxScore) {
                            maxScore = pps
                            firstFloor = key
                        }
                    }
        }

        return Pair(firstFloor, maxScore)
    }

    private fun getRandomFloors(eId: Int): Pair<Int, Int> {
        if (startFloors.size == 2) {
            startFloorMap[eId] = Pair(startFloors[0], startFloors[1])
            return startFloorMap[eId]!!
        }

        var randomFloorPos = Random().nextInt(startFloors.size - 1)
        val floor1 = startFloors[randomFloorPos]
        startFloors.remove(floor1)

        randomFloorPos = Random().nextInt(startFloors.size - 1)
        val floor2 = startFloors[randomFloorPos]
        startFloors.remove(floor2)

        startFloorMap[eId] = Pair(floor1, floor2)
        return startFloorMap[eId]!!
    }

    private fun startFilling(e: MyElevator, passengers: List<MyPassenger>) {
        val floors = startFloorMap[e.id] ?: getRandomFloors(e.id!!)

        passengers.getFromFloor(e.floor).filter { it.destFloor == floors.first || it.destFloor == floors.second }.forEach {
            if (passengers.runningToElevator(e) + e.currentPassengers < MyElevator.MAX) {
                it.setElevator(e)
            }
        }
    }
}