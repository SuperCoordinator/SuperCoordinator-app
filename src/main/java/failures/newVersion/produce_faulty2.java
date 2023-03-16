package failures.newVersion;

import models.SFEI.SFEI_machine;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class produce_faulty2 extends failures_conditions {

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

    public produce_faulty2(String[] formulas, SFEI_machine sfeiMachine) {
        super(formulas, type.PRODUCE_FAULTY);
        this.sfeiMachine = sfeiMachine;

        this.state = SM.WORKING;
        this.old_state = state;

        System.out.println("Will PFaulty on -> " + sfeiMachine.getName());
    }

    public boolean isActive() {
        return state != SM.WORKING;
    }

    private failure_occurrence newOccurrence = new failure_occurrence();

    public void loop(List<Object> sensorsState, List<Object> actuatorsState) {

        int nParts = 0, age = 0, maintenance = 0;
        if (state == SM.WORKING || state == SM.INJECTED) {
            int[] lastFailureOccurrenceDetails = getLastFailureOccurrence(sfeiMachine);

            nParts = sfeiMachine.getnPiecesMoved() - lastFailureOccurrenceDetails[0];

            // If it is 0, then not happened yet the 1st failure
            if (lastFailureOccurrenceDetails[1] == 0) {
                age = (int) Duration.between(sfeiMachine.getDayOfBirth(), Instant.now()).toMinutes();
            } else {
                age = lastFailureOccurrenceDetails[1];
            }
            if (lastFailureOccurrenceDetails[2] == 0) {
                maintenance = (int) Duration.between(sfeiMachine.getDayOfLastMaintenance(), Instant.now()).toMinutes();
            } else {
                maintenance = lastFailureOccurrenceDetails[2];
            }
        }

        // Evaluate transitions
        switch (state) {
            case WORKING -> {
                if (evalConditions(nParts, age, maintenance)) {
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
                if (!evalConditions(nParts, age, maintenance)) {
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

                    failure_occurrence.activationVariable actVar = null;
                    if (wasActivated_by_N()) {
                        actVar = failure_occurrence.activationVariable.N;
                    } else if (wasActivated_by_A()) {
                        actVar = failure_occurrence.activationVariable.A;
                    } else if (wasActivated_by_M()) {
                        actVar = failure_occurrence.activationVariable.M;
                    }
                    if (actVar != null)
                        newOccurrence = new failure_occurrence(type.PRODUCE_FAULTY, actVar, sfeiMachine.getnPiecesMoved(), Instant.now());
                    else
                        throw new RuntimeException("(breakdown) Activation Variable null but evalConditions was TRUE");


                    System.out.println("P_Faulty -> " + state);
                }

            }
            case INJECTED -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiMachine.getaStop().bit_offset(), 0);

                    Instant t = Instant.now();
                    newOccurrence.setEnd_t(t);

                    sfeiMachine.addNewFailureOccurrence(newOccurrence);

                    newOccurrence = new failure_occurrence();

                    System.out.println("P_Faulty -> " + state);
                }
            }
        }

        old_state = state;

//        return state != SM.WORKING;
    }

}
