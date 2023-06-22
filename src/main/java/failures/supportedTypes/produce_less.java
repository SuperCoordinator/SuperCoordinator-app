package failures.supportedTypes;

import failures.evaluations.failure_occurrence;
import failures.evaluations.failures_conditions;
import models.sfe_x.SFEI_conveyor;
import models.base.part;
import utility.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;


public class produce_less extends failures_conditions {

    private enum SM {
        UNDEFINED,
        WORKING,
        REMOVING,
        WAITING,
        TURN_OFF

    }

    private SM state;
    private SM old_state;

    private final SFEI_conveyor sfeiConveyor;
    private final int sfei_idx;
    private boolean old_sRemover = false;

    public produce_less(String[] formulas, SFEI_conveyor sfeiConveyor, int sfei_idx) {
        super(formulas, type.PRODUCE_LESS);
        this.sfeiConveyor = sfeiConveyor;
        this.sfei_idx = sfei_idx;

        this.state = SM.WORKING;
        this.old_state = state;

        if (getnCondition() == null && getaCondition() == null && getmCondition() == null) {
            this.state = SM.UNDEFINED;
            return;
        }
        System.out.println("Produce Less on -> " + sfeiConveyor.getName());
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
        if (state == SM.WORKING || state == SM.TURN_OFF) {
            int[] lastFailureOccurrenceDetails = getLastFailureOccurrence(sfeiConveyor);

            nParts = sfeiConveyor.getnPartsMoved() - lastFailureOccurrenceDetails[0];

            // If it is 0, then not happened yet the 1st failure
            if (lastFailureOccurrenceDetails[1] == 0) {
                age = (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes();
            } else {
                age = lastFailureOccurrenceDetails[1];
            }
            if (lastFailureOccurrenceDetails[2] == 0) {
                maintenance = (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes();
            } else {
                maintenance = lastFailureOccurrenceDetails[2];
            }
        }

        // Evaluate transitions
        switch (state) {
            case WORKING -> {
                if (evalConditions(nParts, age, maintenance)) {
                    state = SM.REMOVING;
                }
            }
            case REMOVING -> state = SM.WAITING;
            case WAITING -> {
                boolean sensor = (int) sensorsState.get(sfeiConveyor.getsRemover().getOffset()) == 1;
                if (utils.getInstance().getLogicalOperator().FE_detector(sensor, old_sRemover)) {
                    state = SM.TURN_OFF;
                }
                old_sRemover = sensor;
            }
            case TURN_OFF -> {
                if (!evalConditions(nParts, age, maintenance)) {
                    state = SM.WORKING;
                }
            }
        }

        // Execute actions
        switch (state) {
            case WORKING, WAITING -> {
            }

            case REMOVING -> {
                if (state != old_state) {

                    actuatorsState.set(sfeiConveyor.getaRemover().getOffset(), 1);

                    failure_occurrence.activationVariable actVar = null;
                    if (wasActivated_by_N()) {
                        actVar = failure_occurrence.activationVariable.N;
                    } else if (wasActivated_by_A()) {
                        actVar = failure_occurrence.activationVariable.A;
                    } else if (wasActivated_by_M()) {
                        actVar = failure_occurrence.activationVariable.M;
                    }
                    if (actVar != null)
                        newOccurrence = new failure_occurrence(sfeiConveyor.getName(), type.PRODUCE_LESS, actVar, sfeiConveyor.getnPartsMoved(), Instant.now());
                    else
                        throw new RuntimeException("(Produce Less) Activation Variable null but evalConditions was TRUE");

                    // Produce Less happened
//                    System.out.println("********************");
//                    System.out.println("   Failure " + sfeiConveyor.getFailuresHistory().size() + " on " + sfeiConveyor.getName() + " " + newOccurrence);
//                    System.out.println("********************");
                }
            }
            case TURN_OFF -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiConveyor.getaRemover().getOffset(), 0);
                    part removedPart = Objects.requireNonNull(sfeiConveyor.getPartsATM().pollFirst());
                    removedPart.setState(part.status.REMOVED);
                    Instant t = Instant.now();
                    newOccurrence.setEnd_t(t);

                    sfeiConveyor.addNewFailureOccurrence(newOccurrence);
                    // Produce Less happened
//                    System.out.println("********************");
//                    System.out.println("   Failure " + (sfeiConveyor.getFailuresHistory().size() - 1) + " on " + sfeiConveyor.getName() + " solved at " + newOccurrence.getEnd_t());
//                    System.out.println("********************");
                    newOccurrence = new failure_occurrence();

                }
            }
        }

        old_state = state;
    }

}
