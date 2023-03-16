package failures.newVersion;

import models.SFEI.SFEI_conveyor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class breakdown_repair2 extends failures_conditions {

    private enum SM {
        WORKING,
        DISABLED,
        REPAIRED
    }

    private SM state;
    private SM old_state;
    // For now, support only breakdown with repair on the conveyors
    private final SFEI_conveyor sfeiConveyor;
    private final condition_variable recoveryConditions;

    public breakdown_repair2(String[] formulas, SFEI_conveyor sfeiConveyor, String[] recovery_formula) {
        super(formulas, type.BREAKDOWN_WITH_REPAIR);
        this.sfeiConveyor = sfeiConveyor;
        this.recoveryConditions = new condition_variable(recovery_formula[0], validation.method.TIME);

        this.state = SM.WORKING;
        this.old_state = state;

        System.out.println("Will BDwR on -> " + sfeiConveyor.getName());
    }

    public boolean isActive() {
        return state != SM.WORKING;
    }

    private failure_occurrence newOccurrence = new failure_occurrence();

    public void loop(List<Object> sensorsState, List<Object> actuatorsState) {

        int nParts = 0, age = 0, maintenance = 0;
        if (state != SM.DISABLED) {
            int[] lastFailureOccurrenceDetails = getLastFailureOccurrence(sfeiConveyor);

            nParts = sfeiConveyor.getnPiecesMoved() - lastFailureOccurrenceDetails[0];

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
                    state = SM.DISABLED;
                }
            }
            case DISABLED -> {
/*                if (evalGivenFormula(recovery_f,
                        sfeiConveyor.getnPiecesMoved(),
                        (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes())) {
                    state = SM.REPAIRED;
                }*/

                if (recoveryConditions.evalFormula(
                        (int) Duration.between(newOccurrence.getStart_t(), Instant.now()).toMinutes())) {
                    state = SM.REPAIRED;
                }
            }
            case REPAIRED -> {
                // WAIT until the condition is not verified again
                // The time of repair is calculated based on the instant of breakdown of the SFEI
                if (!evalConditions(nParts, age, maintenance)) {
                    state = SM.WORKING;

                }
            }
        }

        // Execute actions
        switch (state) {
            case WORKING -> {
                if (state != old_state) {
//                        sfeiConveyor.setDayOfLastMaintenance(Instant.now());
                    System.out.println("BDwR -> " + state);
                }
            }
            case DISABLED -> {
                if (state != old_state) {
                    // IF SFEI == conveyor !!
                    actuatorsState.set(sfeiConveyor.getaConveyorMotor().bit_offset(), 1);
//                        sfeiConveyor.addBreakdown(new Pair<>(sfeiConveyor.getnPiecesMoved(), Instant.now()));
                    failure_occurrence.activationVariable actVar = null;
                    if (wasActivated_by_N()) {
                        actVar = failure_occurrence.activationVariable.N;
                    } else if (wasActivated_by_A()) {
                        actVar = failure_occurrence.activationVariable.A;
                    } else if (wasActivated_by_M()) {
                        actVar = failure_occurrence.activationVariable.M;
                    }
                    if (actVar != null)
                        newOccurrence = new failure_occurrence(type.BREAKDOWN_WITH_REPAIR, actVar, sfeiConveyor.getnPiecesMoved(), Instant.now());
                    else
                        throw new RuntimeException("(breakdown) Activation Variable null but evalConditions was TRUE");

                    System.out.println("BDwR -> " + state);
                }

            }
            case REPAIRED -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiConveyor.getaConveyorMotor().bit_offset(), 0);

                    Instant t = Instant.now();
                    newOccurrence.setEnd_t(t);
                    sfeiConveyor.setDayOfLastMaintenance(t);
                    sfeiConveyor.addNewFailureOccurrence(newOccurrence);

                    newOccurrence = new failure_occurrence();

                    System.out.println("BDwR -> " + state);
                }
            }
        }

        old_state = state;

//        return state != SM.WORKING;

    }

}
