package core

import core.API.Elevator
import core.API.Passenger
//import mu.KLogging
import kotlin.collections.HashMap

class Strategy : BaseStrategy() {
//    companion object: KLogging()
    private val walking = mutableListOf<IntArray>()
    private var startFloorMap: HashMap<Int, IntRange>
    private var tick = 0
    private val prevState = HashMap<Int?, PassengerState>()
    private var prevVisible = listOf<MyPassenger>()

    init {
        (1..10).forEach { walking.add(IntArray(8000, {0})) }

        startFloorMap = hashMapOf(2 to (8..9),
                    1 to (8..9),
                    4 to (7..9),
                    3 to (7..9),
                    6 to (5..7),
                    5 to (5..7),
                    8 to (1..4),
                    7 to (1..4))
    }

    private fun List<Elevator>.convert(): List<MyElevator> = this.map { MyElevator(it) }
    private fun List<MyPassenger>.onTheFloor(floor: Int): Boolean = this.any{ it.state == PassengerState.WAITING_FOR_ELEVATOR && it.floor == floor }
    private fun List<MyPassenger>.enemyOnTheFloor(floor: Int): Boolean = this.any{ it.state == PassengerState.WAITING_FOR_ELEVATOR && it.floor == floor && !it.isMy }
    private fun List<MyPassenger>.getFromFloor(floor: Int, e: MyElevator): List<MyPassenger> = this.filter{ it.state == PassengerState.WAITING_FOR_ELEVATOR && it.floor == floor && it.elevator != e.id }
    private fun List<MyPassenger>.runningToElevator(e: MyElevator): List<MyPassenger> = this.filter{ (it.state == core.PassengerState.WAITING_FOR_ELEVATOR || it.state == PassengerState.MOVING_TO_ELEVATOR) && it.elevator == e.id }
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

        allPassengers.filter { it.state == PassengerState.WAITING_FOR_ELEVATOR && !prevVisible.map { p -> p.id }.contains(it.id) }.groupBy { it.floor }.forEach {
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

            if (!it.full && tick <= 2000 && it.floor == 1) {
                startFilling(it, allPassengers)
                return@forEach
            }

            if (!it.full && allPassengers.onTheFloor(it.floor) && (tick <= 140 || it.timeOnFloor!! > if (allPassengers.enemyOnTheFloor(it.floor)) 140 else 100)) {
                toWelcome(allPassengers, it)
                toWelcomeAll(allPassengers, it, MyElevator.MAX - (allPassengers.runningToElevator(it).size + it.currentPassengers))
            } else {
                if ((allPassengers.runningToElevator(it).isEmpty() || it.full) && it.timeOnFloor!! > 140) {
                    val currentScore = getScore(it.passengers, it.floor)
                    val potentialScore = getBestFloor(it, allPassengers, elevators, enemyElevators)

                    if (tick > 6000 || it.full || currentScore.second > potentialScore.second) {
                        it.goToFloor(currentScore.first)
                    } else {
                        it.goToFloor(potentialScore.first)

//                    logger.trace { "$tick; id:${it.id}; cur:$currentScore; pot:$potentialScore" }
//                    it.passengers.groupBy { p -> p.destFloor }.forEach { g -> logger.trace { "dest: ${g.key}; count:${g.value.size}" } }
                    }
                }
            }
        }

        allPassengers.forEach { prevState[it.id] = it.state }
        prevVisible = allPassengers
    }

    private fun getTicksToFloorMap(e: MyElevator, targetFloor: Int): Map<Int, Int> {
        return (1..9).associateBy({it},
                    {
                        if (it == targetFloor) 0
                        else {
                            if (it < targetFloor) {
                                50 * (targetFloor - it)
                            } else {
                                ((it - targetFloor) / e.speed!!).toInt()
                            }
                        }
                    }
        )
    }

    private fun getBestFloor(e: MyElevator, passengers: List<MyPassenger>, myEs: List<MyElevator>, enEs: List<MyElevator>): Pair<Int, Double> {
        var maxFloor = e.floor
        var maxPpt = 0.0

        val ticksToFloorMap = getTicksToFloorMap(e, e.floor)
        val myElevatorsNext = myEs.filter { it != e && (it.state == ElevatorState.MOVING || it.state == ElevatorState.CLOSING) }.map { it.nextFloor }

        val enemyMoving = enEs.filter { it.state == ElevatorState.MOVING }

        (1..9).filter { !myElevatorsNext.contains(it) }.forEach {
            val enemyTickToTarget = enemyMoving.filter { e -> e.nextFloor == it }.
                    map {e -> (e.nextFloor!! - e.y!!) / e.speed!! }.maxBy { it }?.toInt() ?: 7200

            val tickForDoors = if (it == e.floor) 0 else 100
            val tickToFloor = ticksToFloorMap[it]!! + tickForDoors

            var ppt = 0.0
            if (tickToFloor < enemyTickToTarget) {
                val passengersWaiting = passengers.getFromFloor(it, e).filter { it.timeToAway!! > tickToFloor }.size
                val passengersArrive = walking[it][tick + tickToFloor + tickForDoors] + passengersWaiting

                val maxPotentialFloor = if (it > 4) (9 - (9 - it)) else (9 - it)
                ppt = (passengersArrive.toDouble() / 3 * maxPotentialFloor * 10) / (tickToFloor.toDouble() + maxPotentialFloor * 60 + 200)
            }

            if (ppt > maxPpt) {
                maxPpt = ppt
                maxFloor = it
            }
        }

        val reduceParam = if (e.currentPassengers < 11) 1.0 else ((20 - e.currentPassengers) * 0.1)
        return Pair(maxFloor, maxPpt * reduceParam)
    }

    private fun toWelcomeAll(passengers: List<MyPassenger>, e: MyElevator, needParam: Int) {
        var need = needParam
        passengers.getFromFloor(e.floor, e).filter { !it.isMy }.forEach {
            if (need <= 0) return@forEach

            it.setElevator(e)
            need--
        }

        passengers.getFromFloor(e.floor, e).filter { it.isMy }.forEach {
            if (need <= 0) return@forEach

            it.setElevator(e)
            need--
        }
    }

    private fun toWelcome(passengers: List<MyPassenger>, e: MyElevator): Int {
        var count = 0

        var need = MyElevator.MAX - (passengers.runningToElevator(e).size + e.currentPassengers)
        val totalPassengers = e.passengers.plus(passengers.runningToElevator(e)).toMutableList()

        while (need > 0) {
            val bestGroup = passengers.getFromFloor(e.floor, e).groupBy { it.destFloor }
                    .mapValues { getScore(totalPassengers.plus(it.value), e.floor) }.maxBy { it.value.second }

            if (bestGroup?.value?.second ?: 0.0 > getScore(totalPassengers, e.floor).second) {
                passengers.getFromFloor(e.floor, e).filter { it.destFloor == bestGroup?.key }.forEach {
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
        var speed = 0.02
        passengers.forEach { p ->
            speed /= p.weight!!
        }

        speed = if (passengers.size > 10) (speed / 1.1) else speed

        val tickToFloors = (1..9).associateBy({it},
                {
                    if (it == currentFloor) 0
                    else {
                        if (it < currentFloor) {
                            50 * (currentFloor - it)
                        } else {
                            ((it - currentFloor) / speed).toInt()
                        }
                    }
                })

        val scoredFloors = passengers.groupingBy { it.destFloor }
                .aggregate { _: Int, sum: Int?, p: MyPassenger, first: Boolean ->
                    Math.abs(p.fromFloor - p.destFloor) * p.score + if (first) 0 else sum!!
                }
                .mapValues { Pair(it.value, tickToFloors[it.key]!! + 240) }

        if (scoredFloors.size == 1) {
            val floor = passengers[0].destFloor
            return Pair(floor, scoredFloors[floor]!!.first.toDouble() / scoredFloors[floor]!!.second)
        }

        var firstFloor = 1
        var maxScore = 0.0

        for ((key, value) in scoredFloors) {
            (1..9).filter { key != it }.forEach { floor ->

                val restPassengers = passengers.minus(passengers.filter { p -> p.destFloor == key })
                val points = restPassengers.filter { it.destFloor == floor }.sumBy { it.score * Math.abs(it.fromFloor - floor) }

                val ticks = if (floor < key) (key - floor) * 50
                else {
                    var speedNew = 0.02
                    restPassengers.forEach { p ->
                        speedNew /= p.weight!!
                    }

                    speedNew = if (restPassengers.size > 10) (speedNew / 1.1) else speedNew

                    ((floor - key) / speedNew).toInt()
                }

                val pps = (points.toDouble() + value.first) / (ticks.toDouble() + 240 + value.second)

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

        passengers.getFromFloor(e.floor, e).filter { it.destFloor in floors }.forEach {
            if (passengers.runningToElevator(e).size + e.currentPassengers < MyElevator.MAX) {
                it.setElevator(e)
            }
        }
    }
}