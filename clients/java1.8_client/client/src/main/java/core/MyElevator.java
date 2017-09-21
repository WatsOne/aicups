package core;

import core.API.Elevator;
import core.API.Passenger;

import java.util.List;

public class MyElevator {
    private Elevator elevator;

    public MyElevator(Elevator elevator) {
        this.elevator = elevator;
    }

    public Integer getId() {
        return elevator.getId();
    }

    public Double getY() {
        return elevator.getY();
    }

    public List<Passenger> getPassengers() {
        return elevator.getPassengers();
    }

    public ElevatorState getState() {
        return ElevatorState.parse(elevator.getState());
    }

    public Double getSpeed() {
        return elevator.getSpeed();
    }

    public Integer getTimeOnFloor() {
        return elevator.getTimeOnFloor();
    }

    public Integer getFloor() {
        return elevator.getFloor();
    }

    public String getType() {
        return elevator.getType();
    }

    public Integer getNextFloor() {
        return elevator.getNextFloor();
    }

    public Elevator getElevator() {
        return elevator;
    }

    public void goToFloor(Integer floor) {
        elevator.goToFloor(floor);
    }
}
