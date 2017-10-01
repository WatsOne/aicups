package core

import core.API.Elevator
import core.API.Passenger
//import mu.KLogging
import kotlin.collections.HashMap

class Strategy : BaseStrategy() {
//    companion object: KLogging()
    private val walking = mutableListOf<IntArray>()
    private var startFloorMap: HashMap<Int, Pair<Int, Int>>
    private var tick = 0
    private val prevState = HashMap<Int?, PassengerState>()
    private var prevVisible = listOf<MyPassenger>()

    init {
        (1..10).forEach { walking.add(IntArray(8000, {0})) }

        startFloorMap = hashMapOf(2 to Pair(8, 9),
                    1 to Pair(8, 9),
                    4 to Pair(6, 7),
                    3 to Pair(6, 7),
                    6 to Pair(4, 5),
                    5 to Pair(4, 5),
                    8 to Pair(2, 3),
                    7 to Pair(2, 3))
    }

    private fun List<Elevator>.convert(): List<MyElevator> = this.map { MyElevator(it) }
    private fun List<MyPassenger>.onTheFloor(floor: Int): Boolean = this.any{ it.state == PassengerState.WAITING_FOR_ELEVATOR && it.floor == floor }
    private fun List<MyPassenger>.getFromFloor(floor: Int): List<MyPassenger> = this.filter{ it.state == PassengerState.WAITING_FOR_ELEVATOR && it.floor == floor }
    private fun List<MyPassenger>.runningToElevator(e: MyElevator): List<MyPassenger> = this.filter{ it.state != PassengerState.USING_ELEVATOR && it.elevator == e.id }
    private fun List<Passenger>.convert(isMy: Boolean): List<MyPassenger> = this.map { MyPassenger(it, isMy) }

    override fun onTick(myPassengers: List<Passenger>, myElevators: List<Elevator>, enemyPassengers: List<Passenger>, enemyElevators: List<Elevator>) {
        tick++
        processTick(myPassengers.convert(true), myElevators.convert(), enemyPassengers.convert(false), enemyElevators.convert())
    }

    private fun processTick(passengers: List<MyPassenger>, elevators: List<MyElevator>, enemyPassengers: List<MyPassenger>, enemyElevators: List<MyElevator>) {
        val allPassengers = passengers.plus(enemyPassengers)

        allPassengers.filter { it.state == PassengerState.EXITING && prevState[it.id] != PassengerState.EXITING }.filter { it.destFloor != 1 }.groupBy { it.destFloor }.forEach {
            (0..499).forEach { t ->
                walking[it.key][tick + 539 + t] += it.value.filter { it.isMy }.size + it.value.filter { !it.isMy }.size * 2
            }
        }

        allPassengers.filter { it.state == PassengerState.WAITING_FOR_ELEVATOR && !prevVisible.map { p -> p.id }.contains(it.id) }.groupBy { it.destFloor }.forEach {
            (0..499).forEach { t ->
                walking[it.key][tick + t] -= it.value.filter { it.isMy }.size + it.value.filter { !it.isMy }.size * 2
            }
        }

        val nowDisappear = prevVisible.filter { !allPassengers.map { a -> a.id }.contains(it.id) }
        nowDisappear.filter { prevState[it.id] == PassengerState.MOVING_TO_FLOOR }.filter { it.destFloor != 1 }.groupBy { it.destFloor }.forEach {
            (0..499).forEach { t ->
                walking[it.key][tick + 499 + t] += it.value.filter { it.isMy }.size + it.value.filter { !it.isMy }.size * 2
            }
        }

        elevators.filter { it.state == ElevatorState.FILLING }.forEach {

            if (!it.full && tick <= 1600 && it.floor == 1) {
                startFilling(it, allPassengers)
                return@forEach
            }

            if (!it.full && allPassengers.onTheFloor(it.floor)) {
                toWelcomeAll(allPassengers, it)
            } else {
                if (allPassengers.runningToElevator(it).isEmpty()) {

                    val currentScore = getScore(it.passengers, it.floor)
                    val potentialScore = getBestFloor(it, allPassengers, elevators)

                    if (it.full || currentScore.second > potentialScore.second) {
                        it.goToFloor(currentScore.first)
                    } else {
                        it.goToFloor(potentialScore.first)
                    }
                }
            }
        }

        allPassengers.forEach { prevState[it.id] = it.state }
        prevVisible = allPassengers
    }

    private fun getBestFloor(e: MyElevator, passengers: List<MyPassenger>, es: List<MyElevator>): Pair<Int, Double> {
        var maxFloor = e.floor
        var maxPpt = 0.0

        val movieList = es.filter { it != e && (it.state == ElevatorState.MOVING || it.state == ElevatorState.CLOSING) }.map { it.nextFloor }
        (1..9).filter { !movieList.contains(it) }.forEach {
            val tickToFloor = Math.abs(it - e.floor) * 50 + if (it == e.floor) 0 else 240
            val passengersWaiting = passengers.getFromFloor(it).filter { it.timeToAway!! > tickToFloor }.size
            val passengersArrive = walking[it][tick + tickToFloor] + passengersWaiting

            val maxPotentialFloor = if (it > 4) (9 - (9 - it)) else (9 - it)
            val ppt = (passengersArrive.toDouble() / 3 * maxPotentialFloor * 10) / (tickToFloor.toDouble() + maxPotentialFloor * 50 + 240)

            if (ppt > maxPpt) {
                maxPpt = ppt
                maxFloor = it
            }
        }

        val reduceParam = if (e.currentPassengers < 11) 1.0 else ((20 - e.currentPassengers) * 0.1)
        return Pair(maxFloor, maxPpt * reduceParam)
    }

    private fun toWelcomeAll(passengers: List<MyPassenger>, e: MyElevator) {
        var need = MyElevator.MAX - (passengers.runningToElevator(e).size + e.currentPassengers)

        passengers.getFromFloor(e.floor).filter { !it.isMy }.forEach {
            if (need == 0) return@forEach

            it.setElevator(e)
            need--
        }

        passengers.getFromFloor(e.floor).filter { it.isMy }.forEach {
            if (need == 0) return@forEach

            it.setElevator(e)
            need--
        }
    }

    private fun toWelcome(passengers: List<MyPassenger>, e: MyElevator): Int {
        var count = 0

        var need = MyElevator.MAX - (passengers.runningToElevator(e).size + e.currentPassengers)
        val totalPassengers = e.passengers.plus(passengers.runningToElevator(e)).toMutableList()

        while (need > 0) {
            val bestGroup = passengers.getFromFloor(e.floor).groupBy { it.destFloor }
                    .mapValues { getScore(totalPassengers.plus(it.value), e.floor) }.maxBy { it.value.second }

            if (bestGroup?.value?.second ?: 0.0 > getScore(totalPassengers, e.floor).second) {
                passengers.getFromFloor(e.floor).filter { it.destFloor == bestGroup?.key }.forEach {
                    it.setElevator(e)
                    totalPassengers.add(it)
                    need--
                    count++
                }
            } else {
                break
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

        if (scoredFloors.size == 1) {
            val floor = passengers[0].destFloor
            return Pair(floor, scoredFloors[floor]!!.first.toDouble() / scoredFloors[floor]!!.second)
        }

        var firstFloor = 1
        var maxScore = 0.0

        for ((key, value) in scoredFloors) {
            (1..9).filter { key != it }.forEach { floor ->
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

    private fun startFilling(e: MyElevator, passengers: List<MyPassenger>) {
        val floors = startFloorMap[e.id]!!

        passengers.getFromFloor(e.floor).filter { it.destFloor == floors.first || it.destFloor == floors.second }.forEach {
            if (passengers.runningToElevator(e).size + e.currentPassengers < MyElevator.MAX) {
                it.setElevator(e)
            }
        }
    }
}