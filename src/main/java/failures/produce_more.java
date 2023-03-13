package failures;

import models.SFEI.SFEI_conveyor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class produce_more extends failure {

    private enum SM {
        WORKING,
        TURN_ON,
        WAITING,
        TURN_OFF

    }

    private SM state;
    private SM old_state;

    private final SFEI_conveyor sfeiConveyor;
    private boolean old_sEmitter = false;

    public produce_more(failure.type type, String formula, SFEI_conveyor sfeiConveyor) {
        super(type, formula);
        this.sfeiConveyor = sfeiConveyor;

        this.state = SM.WORKING;
        this.old_state = state;

        System.out.println("Will PMore on -> " + sfeiConveyor.getName());
    }

    public boolean isActive() {
        return state != SM.WORKING;
    }

    public void loop(List<Object> sensorsState, List<Object> actuatorsState) {

        // Evaluate transitions
        switch (state) {
            case WORKING -> {
                if (evalFormula(sfeiConveyor.getnPiecesMoved(),
                        (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes())) {
                    state = SM.TURN_ON;
                }
            }
            case TURN_ON -> {
                state = SM.WAITING;
            }
            case WAITING -> {
                boolean sensor = (int) sensorsState.get(sfeiConveyor.getsEmitter().bit_offset()) == 1;
                if (getUtility().getLogicalOperator().FE_detector(sensor, old_sEmitter)) {
                    state = SM.TURN_OFF;
                }
                old_sEmitter = sensor;
            }
            case TURN_OFF -> {
                if (!evalFormula(sfeiConveyor.getnPiecesMoved(),
                        (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes())) {
                    state = SM.WORKING;
                }
            }
        }

        // Execute actions
        switch (state) {
            case WORKING, WAITING -> {
                if (state != old_state) {
                    System.out.println("P_More -> " + state);
                }
            }
            case TURN_ON -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiConveyor.getaEmitter().bit_offset(), 1);
                    System.out.println("P_More -> " + state);
                }
            }
            case TURN_OFF -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiConveyor.getaEmitter().bit_offset(), 0);
                    System.out.println("P_More -> " + state);
                }
            }
        }

        old_state = state;
//        return state != SM.WORKING;
    }

}
