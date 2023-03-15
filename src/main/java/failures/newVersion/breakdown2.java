package failures.newVersion;

import failures.failure;
import models.SFEI.SFEI_conveyor;
import org.apache.commons.math3.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

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
        super(formulas);
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


    public void loop(List<Object> sensorsState, List<Object> actuatorsState) {

        // Evaluate transitions
        switch (state) {
            case WORKING -> {
/*                if (evalFormula(sfeiConveyor.getnPiecesMoved(),
                        (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes()))
                    state = SM.DISABLED;*/
                // Os valores tem de ser sempre calculados conforme os mais recentes
                if (evalConditions(sfeiConveyor.getnPiecesMoved(),
                        (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes()))
                    state = SM.DISABLED;
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

                if (!evalConditions(sfeiConveyor.getnPiecesMoved(),
                        (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes()))
                    state = SM.WORKING;

            }
        }

        // Execute actions
        switch (state) {
            case WORKING -> {
                if (state != old_state) {
                    sfeiConveyor.setDayOfLastMaintenance(Instant.now());
                    System.out.println("BD -> " + state);
                }
            }
            case DISABLED -> {
                if (state != old_state) {
                    // IF SFEI == conveyor !!
                    actuatorsState.set(sfeiConveyor.getaConveyorMotor().bit_offset(), 1);
                    sfeiConveyor.addBreakdown(new Pair<>(sfeiConveyor.getnPiecesMoved(), Instant.now()));
                    System.out.println("BD -> " + state);
                }
            }
            case RESUMING -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiConveyor.getaConveyorMotor().bit_offset(), 0);
                    System.out.println("BD -> " + state);
                }
            }

        }

        old_state = state;

//        return state != SM.WORKING;
    }

}
