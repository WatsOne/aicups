package core;

import core.API.Elevator;
import core.API.Passenger;

public class MyPassenger {
    private Passenger passenger;

    public MyPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public Integer getId() {
        return passenger.getId();
    }

    public Integer getElevator() {
        return passenger.getElevator();
    }

    public Integer getFromFloor() {
        return passenger.getFromFloor();
    }

    public Integer getDestFloor() {
        return passenger.getDestFloor();
    }

    public PassengerState getState() {
        return PassengerState.parse(passenger.getState());
    }

    public Integer getTimeToAway() {
        return passenger.getTimeToAway();
    }

    public String getType() {
        return passenger.getType();
    }

    public Integer getFloor() {
        return passenger.getFloor();
    }

    public Double getX() {
        return passenger.getX();
    }

    public Double getY() {
        return passenger.getY();
    }

    public Double getWeight() {
        return passenger.getWeight();
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setElevator(Elevator elevator) {
        passenger.setElevator(elevator);
    }
}
