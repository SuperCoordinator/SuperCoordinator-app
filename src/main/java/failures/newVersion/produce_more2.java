package failures.newVersion;

import models.SFEx.SFEI_conveyor;
import models.base.part;
import models.partDescription;
import utility.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;


public class produce_more2 extends failures_conditions {

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
    private partDescription.material material;
    private int faulty_partID = -1;

    public produce_more2(String[] formulas, SFEI_conveyor sfeiConveyor, partDescription.material mat) {
        super(formulas, type.PRODUCE_MORE);
        this.sfeiConveyor = sfeiConveyor;
        this.material = mat;

        this.state = SM.WORKING;
        this.old_state = state;

        System.out.println("Produce More on -> " + sfeiConveyor.getName());
    }

    public boolean isActive() {
        return state != SM.WORKING;
    }

    private failure_occurrence newOccurrence = new failure_occurrence();

    public void loop(List<Object> sensorsState, List<Object> actuatorsState,List<Object> holdRegs) {

        int nParts = 0, age = 0, maintenance = 0;
        if (state == SM.WORKING || state == SM.TURN_OFF) {
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
/*                    if (sfeiConveyor.getPartsATM().size() > 0) {
                        if (sfeiConveyor.getPartsATM().last().getId() >= sfeiConveyor.getnPiecesMoved()) {
                            id = sfeiConveyor.getPartsATM().last().getId() + 1;
                        }
                    } else
                        id = sfeiConveyor.getnPiecesMoved();*/


                    // This operation of concat is faster than + operation
/*                    String itemName = sfeiConveyor.getName();
                    itemName = itemName.concat("-");
                    itemName = itemName.concat(sfeiConveyor.getInSensor().getName());

                    p.addTimestamp(itemName);*/


                    state = SM.TURN_OFF;

                }
                old_sEmitter = sensor;
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
                if (state != old_state) {
                    System.out.println("P_More -> " + state);
                }
            }
            case TURN_ON -> {
                if (state != old_state) {

                    part newPart = new part(faulty_partID, new partDescription(material, partDescription.form.RAW));
                    faulty_partID--;
                    sfeiConveyor.addNewPartATM(newPart);

                    // For the overflow, just in case
                    if (faulty_partID > 0)
                        faulty_partID = -1;

                    holdRegs.set(sfeiConveyor.getaEmitPart().getBit_offset(),(int) Math.pow(2, getNumberbyPartAspect(newPart.getReality()) + 4 - 1));
                    actuatorsState.set(sfeiConveyor.getaEmit().getBit_offset(), 1);


                    failure_occurrence.activationVariable actVar = null;
                    if (wasActivated_by_N()) {
                        actVar = failure_occurrence.activationVariable.N;
                    } else if (wasActivated_by_A()) {
                        actVar = failure_occurrence.activationVariable.A;
                    } else if (wasActivated_by_M()) {
                        actVar = failure_occurrence.activationVariable.M;
                    }
                    if (actVar != null)
                        newOccurrence = new failure_occurrence(type.PRODUCE_MORE, actVar, sfeiConveyor.getnPiecesMoved(), Instant.now());
                    else
                        throw new RuntimeException("(breakdown) Activation Variable null but evalConditions was TRUE");


                    System.out.println("P_More -> " + state);
                }
            }
            case TURN_OFF -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiConveyor.getaEmit().getBit_offset(), 0);

                    Instant t = Instant.now();
                    newOccurrence.setEnd_t(t);

                    sfeiConveyor.addNewFailureOccurrence(newOccurrence);

                    newOccurrence = new failure_occurrence();

                    System.out.println("P_More -> " + state);
                }
            }
        }

        old_state = state;
//        return state != SM.WORKING;
    }

    private int getNumberbyPartAspect(partDescription aspect) {

        int num = -1;

        if (aspect.form().equals(partDescription.form.RAW)) {
            num = 0;
        } else if (aspect.form().equals(partDescription.form.BASE)) {
            num = 3;
        } else if (aspect.form().equals(partDescription.form.LID)) {
            num = 6;
        }
        if (aspect.material().equals(partDescription.material.BLUE))
            num++;
        else if (aspect.material().equals(partDescription.material.GREEN)) {
            num += 2;
        } else if (aspect.material().equals(partDescription.material.METAL)) {
            num += 3;
        }

        if (num == -1)
            throw new RuntimeException("Part Description == -1");
        return num;

    }

}
