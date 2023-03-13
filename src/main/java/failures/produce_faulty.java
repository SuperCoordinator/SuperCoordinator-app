package failures;

import models.SFEI.SFEI_machine;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class produce_faulty extends failure {

    private enum SM {

        WORKING,
        WAITING_PART_POSITIONING,
        INJECT_FAILURE,
        INJECTED

    }

    private SM state;
    private SM old_state;
    private final SFEI_machine sfeiMachine;

    private boolean old_sMachine_door = false;
    private Instant closed_door_at;

    public produce_faulty(failure.type type, String formula, SFEI_machine sfeiMachine) {
        super(type, formula);
        this.sfeiMachine = sfeiMachine;

        this.state = SM.WORKING;
        this.old_state = state;

        System.out.println("Will PFaulty on -> " + sfeiMachine.getName());
    }
    public boolean isActive() {
        return state != SM.WORKING;
    }

    public void loop(List<Object> sensorsState, List<Object> actuatorsState) {

        // Evaluate transitions
        switch (state) {
            case WORKING -> {
                if (evalFormula(sfeiMachine.getnPiecesMoved(),
                        (int) Duration.between(sfeiMachine.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiMachine.getDayOfLastMaintenance(), Instant.now()).toMinutes())) {
                    state = SM.WAITING_PART_POSITIONING;
                }
            }
            case WAITING_PART_POSITIONING -> {
                boolean b_machine_door = (int) sensorsState.get(sfeiMachine.getsDoor().bit_offset()) == 1;
                if (sfeiMachine.getPartsATM().size() > 0) {
                    if (getUtility().getLogicalOperator().FE_detector(b_machine_door, old_sMachine_door)) {
                        state = SM.INJECT_FAILURE;
                    }
                }
                old_sMachine_door = b_machine_door;
            }
            case INJECT_FAILURE -> {
                if (Duration.between(closed_door_at, Instant.now()).toSeconds() >= 1) {
                    state = SM.INJECTED;
//                    state = SM.WORKING;
                }
            }
            case INJECTED -> {
                if (!evalFormula(sfeiMachine.getnPiecesMoved(),
                        (int) Duration.between(sfeiMachine.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiMachine.getDayOfLastMaintenance(), Instant.now()).toMinutes())) {
                    state = SM.WORKING;
                }
            }
        }

        // Execute actions
        switch (state) {
            case WORKING, WAITING_PART_POSITIONING -> {
                if (state != old_state) {
                    System.out.println("P_Faulty -> " + state);
                }
            }
            case INJECT_FAILURE -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiMachine.getaStop().bit_offset(), 1);
                    closed_door_at = Instant.now();
                    System.out.println("P_Faulty -> " + state);
                }

            }
            case INJECTED -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiMachine.getaStop().bit_offset(), 0);
                    System.out.println("P_Faulty -> " + state);
                }
            }
        }

        old_state = state;

//        return state != SM.WORKING;
    }

}
