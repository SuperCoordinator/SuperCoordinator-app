package failures.supportedTypes;

import failures.evaluations.condition_variable;
import failures.evaluations.failure_occurrence;
import failures.evaluations.failures_conditions;
import failures.evaluations.validation;
import models.sfe_x.SFEI_conveyor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class breakdown_repair extends failures_conditions {

    private enum SM {
        UNDEFINED,
        WORKING,
        DISABLED,
        REPAIRED
    }

    private SM state;
    private SM old_state;
    // For now, support only breakdown with repair on the conveyors
    private final SFEI_conveyor sfeiConveyor;
    private final int sfei_idx;
    private final condition_variable recoveryConditions;

    public breakdown_repair(String[] formulas, SFEI_conveyor sfeiConveyor, int sfei_idx, String[] recovery_formula) {
        super(formulas, type.BREAKDOWN_WITH_REPAIR);
        this.sfeiConveyor = sfeiConveyor;
        this.sfei_idx = sfei_idx;
        this.recoveryConditions = recovery_formula[0].contains("no") ? null : new condition_variable(recovery_formula[0], validation.method.TIME);

        this.state = SM.WORKING;
        this.old_state = state;

        if (getnCondition() == null && getaCondition() == null && getmCondition() == null) {
            this.state = SM.UNDEFINED;
            return;
        }
        System.out.println("BreakDown with Repair on -> " + sfeiConveyor.getName());
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
                if (recoveryConditions != null)
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

                }
            }
            case DISABLED -> {
                if (state != old_state) {
                    // IF SFEI == conveyor !!
                    actuatorsState.set(sfeiConveyor.getaConveyorMotor().getBit_offset(), 1);

                    failure_occurrence.activationVariable actVar = null;
                    if (wasActivated_by_N()) {
                        actVar = failure_occurrence.activationVariable.N;
                    } else if (wasActivated_by_A()) {
                        actVar = failure_occurrence.activationVariable.A;
                    } else if (wasActivated_by_M()) {
                        actVar = failure_occurrence.activationVariable.M;
                    }

                    if (actVar != null)
                        newOccurrence = new failure_occurrence(sfeiConveyor.getName(), type.BREAKDOWN_WITH_REPAIR, actVar, sfeiConveyor.getnPiecesMoved(), Instant.now());
                    else
                        throw new RuntimeException("(Breakdown w/ Repair) Activation Variable null but evalConditions was TRUE");

                    // Breakdown with Repair happened
                    System.out.println("********************");
                    System.out.println("   Failure " + sfeiConveyor.getFailuresHistory().size() + " on " + sfeiConveyor.getName() + " " + newOccurrence);
                    System.out.println("********************");
                }
            }
            case REPAIRED -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiConveyor.getaConveyorMotor().getBit_offset(), 0);

                    Instant t = Instant.now();
                    newOccurrence.setEnd_t(t);
                    sfeiConveyor.setDayOfLastMaintenance(t);
                    sfeiConveyor.addNewFailureOccurrence(newOccurrence);

                    // Breakdown with Repair Solved
                    System.out.println("********************");
                    System.out.println("   Failure " + (sfeiConveyor.getFailuresHistory().size() - 1) + " on " + sfeiConveyor.getName() + " solved at " + newOccurrence.getEnd_t());
                    System.out.println("********************");
                    newOccurrence = new failure_occurrence();

                }
            }
        }

//        if (old_state != state) {
//            System.out.println("*** BREAKDOWN w/ Repair on " + sfeiConveyor.getName() + " -> [" + state + "]");
//        }

        old_state = state;


    }

}
