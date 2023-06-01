package failures.supportedTypes;

import failures.evaluations.failure_occurrence;
import failures.evaluations.failures_conditions;
import models.sfe_x.SFEI_machine;
import utility.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class produce_faulty extends failures_conditions {

    private enum SM {
        UNDEFINED,
        WORKING,
        WAITING_PART_POSITIONING,
        INJECT_FAILURE,
        INJECTED

    }

    private SM state;
    private SM old_state;
    private final SFEI_machine sfeiMachine;
    private final int sfei_idx;

    private boolean old_sMachine_door = false;
    private Instant closed_door_at;

    public produce_faulty(String[] formulas, SFEI_machine sfeiMachine,int sfei_idx) {
        super(formulas, type.PRODUCE_FAULTY);
        this.sfeiMachine = sfeiMachine;
        this.sfei_idx = sfei_idx;

        this.state = SM.WORKING;
        this.old_state = state;

        if (getnCondition() == null && getaCondition() == null && getmCondition() == null) {
            this.state = SM.UNDEFINED;
            return;
        }
        System.out.println("Produce Faulty on -> " + sfeiMachine.getName());
    }

    public boolean isActive() {
        if (state.equals(SM.UNDEFINED))
            return false;
        return state != SM.WORKING;
    }

    public int getSfei_idx() {
        return sfei_idx;
    }

    public boolean isUndefined() {
        return state.equals(SM.UNDEFINED);
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
                    // in this case the machine should have parts in buffer
//                    if (sfeiMachine.getPartsATM().size() > 0)
//                        state = SM.WAITING_PART_POSITIONING;
                }
            }
            case WAITING_PART_POSITIONING -> {
                boolean b_machine_door = (int) sensorsState.get(sfeiMachine.getsDoor().getBit_offset()) == 1;
                if (sfeiMachine.getPartsATM().size() > 0) {
                    if (utils.getInstance().getLogicalOperator().FE_detector(b_machine_door, old_sMachine_door)) {
                        state = SM.INJECT_FAILURE;
                    }
                }
                old_sMachine_door = b_machine_door;
            }
            case INJECT_FAILURE -> {
                if (Duration.between(closed_door_at, Instant.now()).toSeconds() >= 1) {
                    state = SM.INJECTED;
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
                }
            }
            case INJECT_FAILURE -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiMachine.getaStop().getBit_offset(), 1);
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
                        newOccurrence = new failure_occurrence(sfeiMachine.getName(), type.PRODUCE_FAULTY, actVar, sfeiMachine.getnPiecesMoved(), Instant.now());
                    else
                        throw new RuntimeException("(Produce Faulty) Activation Variable null but evalConditions was TRUE");

                    // Produce Faulty happened
                    System.out.println("********************");
                    System.out.println("   Failure " + sfeiMachine.getFailuresHistory().size() + " on " + sfeiMachine.getName() + " " + newOccurrence);
                    System.out.println("********************");
                }

            }
            case INJECTED -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiMachine.getaStop().getBit_offset(), 0);

                    Instant t = Instant.now();
                    newOccurrence.setEnd_t(t);

                    sfeiMachine.addNewFailureOccurrence(newOccurrence);
                    // Produced Faulty Solved
                    System.out.println("********************");
                    System.out.println("   Failure " + (sfeiMachine.getFailuresHistory().size() - 1) + " on " + sfeiMachine.getName() + " solved at " + newOccurrence.getEnd_t());
                    System.out.println("********************");
                    newOccurrence = new failure_occurrence();
                }
            }
        }
//        if (old_state != state) {
//            System.out.println("*** Produce Faulty on " + sfeiMachine.getName() + " -> [" + state + "]");
//        }
        old_state = state;

//        return state != SM.WORKING;
    }

}
