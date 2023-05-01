package failures.oldVersion;

import models.SFEx_particular.SFEI_conveyor;
import models.base.part;
import models.partDescription;
import utility.utils;

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
                            state = SM.TURN_ON;
                        }
                    }
                    old_nPiecesMoved = sfeiConveyor.getnPiecesMoved();
                } else if (evalFormula(sfeiConveyor.getnPiecesMoved(),
                        (int) Duration.between(sfeiConveyor.getDayOfBirth(), Instant.now()).toMinutes(),
                        (int) Duration.between(sfeiConveyor.getDayOfLastMaintenance(), Instant.now()).toMinutes())) {
                    state = SM.TURN_ON;
                }
            }
            case TURN_ON -> {
                state = SM.WAITING;
            }
            case WAITING -> {
                boolean sensor = (int) sensorsState.get(sfeiConveyor.getsEmitter().getBit_offset()) == 1;
                if (utils.getInstance().getLogicalOperator().FE_detector(sensor, old_sEmitter)) {
                    int id = 0;
                    if (sfeiConveyor.getPartsATM().size() > 0) {
                        if (sfeiConveyor.getPartsATM().last().getId() >= sfeiConveyor.getnPiecesMoved()) {
                            id = sfeiConveyor.getPartsATM().last().getId() + 1;
                        }
                    } else
                        id = sfeiConveyor.getnPiecesMoved();

                    part p = new part(id, new partDescription(partDescription.material.BLUE, partDescription.form.RAW));

                    // This operation of concat is faster than + operation
                    String itemName = sfeiConveyor.getName();
                    itemName = itemName.concat("-");
                    itemName = itemName.concat(sfeiConveyor.getInSensor().getName());

                    p.addTimestamp(itemName);
                    sfeiConveyor.addNewPartATM(p);

                    state = SM.TURN_OFF;
                }
                old_sEmitter = sensor;
            }
            case TURN_OFF -> {
                if (isProbability()) {
                    state = SM.WORKING;
                } else if (!evalFormula(sfeiConveyor.getnPiecesMoved(),
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
                    actuatorsState.set(sfeiConveyor.getaEmitter().getBit_offset(), 1);
                    System.out.println("P_More -> " + state);
                }
            }
            case TURN_OFF -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiConveyor.getaEmitter().getBit_offset(), 0);
                    System.out.println("P_More -> " + state);
                }
            }
        }

        old_state = state;
//        return state != SM.WORKING;
    }

}
