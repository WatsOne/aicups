package core;


import core.API.Elevator;
import core.API.Passenger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Strategy extends BaseStrategy {
    public void onTick(List<Passenger> myPassengers, List<Elevator> myElevators, List<Passenger> enemyPassengers, List<Elevator> enemyElevators) {

        List<MyPassenger> newMyPassengers = myPassengers.stream().map(MyPassenger::new).collect(Collectors.toList());
        List<MyElevator> newMyElevators = myElevators.stream().map(MyElevator::new).collect(Collectors.toList());
        List<MyPassenger> newEnemyPassengers = enemyPassengers.stream().map(MyPassenger::new).collect(Collectors.toList());
        List<MyElevator> newEnemyElevators = enemyElevators.stream().map(MyElevator::new).collect(Collectors.toList());

        processTick(newMyPassengers, newMyElevators, newEnemyPassengers, newEnemyElevators);
    }

    private void processTick(List<MyPassenger> myPassengers, List<MyElevator> myElevators, List<MyPassenger> enemyPassengers, List<MyElevator> enemyElevators) {
        for (MyElevator e : myElevators) {
            for (MyPassenger p : myPassengers) {
                if (p.getState().getCode() < PassengerState.USING_ELEVATOR.getCode()) {
//                    if (e.getState() != ElevatorState.MOVING) {
//                        e.goToFloor(p.getFromFloor());
//                    }
                    if (Objects.equals(e.getFloor(), p.getFromFloor())) {
                        p.setElevator(e.getElevator());
                    }
                }
            }
            if (e.getPassengers().size() == 20 && e.getState() != ElevatorState.MOVING) {
                e.goToFloor(getMinFloor(e.getPassengers()));
            }
        }
    }

    private static int getMinFloor(List<Passenger> passengers) {
        return passengers.stream().map(Passenger::getDestFloor).min(Integer::compare).get();
    }
}