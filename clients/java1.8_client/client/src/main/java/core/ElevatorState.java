package core;

import core.API.Elevator;

public enum ElevatorState {
    /**
     * В этом состоянии лифт находится краткое время (1 тик в данный момент)
     * между закрытием дверей и отправкой на заказанный этаж
     */
    WAITING(0),

    /**
     * В этом состоянии лифт находится, когда он едет на этаж
     */
    MOVING(1),

    /**
     * В этом состоянии лифт находится, когда он открывает двери
     */
    OPENING(2),

    /**
     * В этом состоянии лифт находится, когда он открыл двери и готов к заходу пассажиров внутрь
     */
    FILLING(3),

    /**
     * В этом состоянии лифт находится, когда он закрывает двери
     */
    CLOSING(4);

    private int code;

    ElevatorState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ElevatorState parse(Integer code) {
        if (code == null) {
            return null;
        }

        for (ElevatorState state : values()) {
            if (state.code == code) {
                return state;
            }
        }

        return null;
    }
}
