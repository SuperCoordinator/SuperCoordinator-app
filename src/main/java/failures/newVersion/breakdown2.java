package failures.newVersion;

import failures.failure;
import models.SFEI.SFEI_conveyor;
import org.apache.commons.math3.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class breakdown2 extends failures_conditions {

    private enum SM {
        WORKING,
        DISABLED,
        RESUMING
    }

    private SM state;
    private SM old_state;

    private boolean resume;

    // For now, support only breakdown with repair on the conveyors
    private final SFEI_conveyor sfeiConveyor;

    public breakdown2(String[] formulas, SFEI_conveyor sfeiConveyor) {
        super(formulas, type.BREAKDOWN);
        this.sfeiConveyor = sfeiConveyor;

        this.resume = false;
        this.state = SM.WORKING;
        this.old_state = state;

        System.out.println("Will BD on -> " + sfeiConveyor.getName());
    }

    public void setResume(boolean resume) {
        this.resume = resume;
    }

    public boolean isActive() {
        return state != SM.WORKING;
    }

    private failure_occurrence newOccurrence = new failure_occurrence();

    public void loop(List<Object> sensorsState, List<Object> actuatorsState) {
        int nParts = 0, age = 0, maintenance = 0;
        if (state != SM.DISABLED) {
            nParts = sfeiConveyor.getnPiecesMoved()
                    - getLastFailureOccurrenceByVariable(failure_occurrence.activationVariable.N);

            age = (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes()
                    - getLastFailureOccurrenceByVariable(failure_occurrence.activationVariable.A);

            maintenance = (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes()
                    - getLastFailureOccurrenceByVariable(failure_occurrence.activationVariable.M);
        }

        // Evaluate transitions
        switch (state) {
            case WORKING -> {
/*                if (evalFormula(sfeiConveyor.getnPiecesMoved(),
                        (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes()))
                    state = SM.DISABLED;*/

                // Os valores tem de ser sempre calculados conforme os mais recentes


//                System.out.println("nParts: " + nParts + " age: " + age + " maintenance: " + maitenance);

                if (evalConditions(nParts, age, maintenance)) {
                    state = SM.DISABLED;
                }
            }

            case DISABLED -> {
                if (resume) {
                    state = SM.RESUMING;
                    resume = false;
                }
            }
            case RESUMING -> {
                // WAIT until the condition is not verified again
/*                if (!evalFormula(sfeiConveyor.getnPiecesMoved(),
                        (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes())) {
                    state = SM.WORKING;*/

                if (!evalConditions(nParts, age, maintenance)) {
                    state = SM.WORKING;
                }

            }
        }

        // Execute actions
        switch (state) {
            case WORKING -> {
                if (state != old_state) {
//                    sfeiConveyor.setDayOfLastMaintenance(Instant.now());
                    System.out.println("BD -> " + state);
                }
            }
            case DISABLED -> {
                if (state != old_state) {
                    // IF SFEI == conveyor !!
                    actuatorsState.set(sfeiConveyor.getaConveyorMotor().bit_offset(), 1);

                    failure_occurrence.activationVariable actVar = null;
                    if (wasActivated_by_N()) {
                        actVar = failure_occurrence.activationVariable.N;
                    } else if (wasActivated_by_A()) {
                        actVar = failure_occurrence.activationVariable.A;
                    } else if (wasActivated_by_M()) {
                        actVar = failure_occurrence.activationVariable.M;
                    }
                    if (actVar != null)
                        newOccurrence = new failure_occurrence(type.BREAKDOWN, actVar, sfeiConveyor.getnPiecesMoved(), Instant.now());
                    else
                        throw new RuntimeException("(breakdown) Activation Variable null but evalConditions was TRUE");

                    //sfeiConveyor.addBreakdown(new Pair<>(sfeiConveyor.getnPiecesMoved(), Instant.now()));
                    System.out.println("BD -> " + state);
                }
            }
            case RESUMING -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiConveyor.getaConveyorMotor().bit_offset(), 0);

                    Instant t = Instant.now();
                    newOccurrence.setEnd_t(t);
                    sfeiConveyor.setDayOfLastMaintenance(t);
                    sfeiConveyor.addNewFailureOccurrence(newOccurrence);

                    newOccurrence = new failure_occurrence();

                    System.out.println("BD -> " + state);
                }
            }

        }

        old_state = state;

//        return state != SM.WORKING;
    }

    private int getLastFailureOccurrenceByVariable(failure_occurrence.activationVariable actVar) {
        if (sfeiConveyor.getFailuresHistory().size() == 0)
            return 0;

        int ret_value = 0;

        switch (actVar) {
            case N -> {
                for (Map.Entry<Integer, failure_occurrence> fOcurr : sfeiConveyor.getFailuresHistory().entrySet()) {
                    if (fOcurr.getValue().getnPartsMoved_at_time() > ret_value)
                        ret_value = fOcurr.getValue().getnPartsMoved_at_time();
                }
            }
            case A -> {
                ret_value = Integer.MAX_VALUE;
                for (Map.Entry<Integer, failure_occurrence> fOcurr : sfeiConveyor.getFailuresHistory().entrySet()) {
                    if (Duration.between(fOcurr.getValue().getStart_t(), Instant.now()).toMinutes() < ret_value)
                        ret_value = (int) Duration.between(fOcurr.getValue().getStart_t(), Instant.now()).toMinutes();
                }
            }
            case M -> {
                ret_value = Integer.MAX_VALUE;
                for (Map.Entry<Integer, failure_occurrence> fOcurr : sfeiConveyor.getFailuresHistory().entrySet()) {
                    if (fOcurr.getValue().getEnd_t() != null)
                        if (Duration.between(fOcurr.getValue().getEnd_t(), Instant.now()).toMinutes() < ret_value)
                            ret_value = (int) Duration.between(fOcurr.getValue().getStart_t(), Instant.now()).toMinutes();
                }
            }
        }

        return ret_value;
    }

}
