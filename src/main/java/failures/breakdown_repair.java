package failures;

import models.SFEI.SFEI_conveyor;
import org.apache.commons.math3.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class breakdown_repair extends failure {

    private enum SM {
        WORKING,
        DISABLED,
        REPAIRED
    }

    private SM state;
    private SM old_state;
    // For now, support only breakdown with repair on the conveyors
    private final SFEI_conveyor sfeiConveyor;
    private final String recovery_f;

    public breakdown_repair(failure.type type, String formula, SFEI_conveyor sfeiConveyor, String recovery_formula) {
        super(type, formula);
        this.sfeiConveyor = sfeiConveyor;
        this.recovery_f = recovery_formula;
        this.state = SM.WORKING;
        this.old_state = SM.WORKING;

        System.out.println("Will BDwR on -> " + sfeiConveyor.getName());
    }

    public boolean isActive() {
        return state != SM.WORKING;
    }

    private int old_nPiecesMoved = 0;

    public void loop(List<Object> sensorsState, List<Object> actuatorsState) {

        // Evaluate transitions
        switch (state) {
            case WORKING -> {
                if (isProbability()) {
                    if (sfeiConveyor.getnPiecesMoved() != old_nPiecesMoved) {
                        if (evalFormula(sfeiConveyor.getnPiecesMoved(),
                                (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                                (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes())) {
                            state = SM.DISABLED;
                        }
                    }
                    old_nPiecesMoved = sfeiConveyor.getnPiecesMoved();
                } else if (evalFormula(sfeiConveyor.getnPiecesMoved(),
                        (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes())) {
                    state = SM.DISABLED;
                }
            }
            case DISABLED -> {
                if (evalGivenFormula(recovery_f,
                        sfeiConveyor.getnPiecesMoved(),
                        (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes())) {
                    state = SM.REPAIRED;
                }
            }
            case REPAIRED -> {
                // WAIT until the condition is not verified again
                // The time of repair is calculated based on the instant of breakdown of the SFEI
                if (isProbability()) {
                    state = SM.WORKING;
                } else if (!evalFormula(sfeiConveyor.getnPiecesMoved(),
                        (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiConveyor.getLastBreakdown().getSecond(), Instant.now()).toMinutes())) {
                    state = SM.WORKING;
                }
            }
        }

        // Execute actions
        switch (state) {
            case WORKING -> {
                if (state != old_state) {
                    sfeiConveyor.setDayOfLastMaintenance(Instant.now());
                    System.out.println("BDwR -> " + state);
                }
            }
            case DISABLED -> {
                if (state != old_state) {
                    // IF SFEI == conveyor !!
                    actuatorsState.set(sfeiConveyor.getaConveyorMotor().bit_offset(), 1);
                    sfeiConveyor.addBreakdown(new Pair<>(sfeiConveyor.getnPiecesMoved(), Instant.now()));
                    System.out.println("BDwR -> " + state);
                }

            }
            case REPAIRED -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiConveyor.getaConveyorMotor().bit_offset(), 0);
                    System.out.println("BDwR -> " + state);
                }
            }
        }

        old_state = state;

//        return state != SM.WORKING;

    }

}
