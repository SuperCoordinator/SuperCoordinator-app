package failures.supportedTypes;

import failures.evaluations.failure_occurrence;
import failures.evaluations.failures_conditions;
import models.sfe_x.SFEI_conveyor;
import models.base.part;
import models.partDescription;
import utility.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;


public class produce_more extends failures_conditions {

    private enum SM {
        UNDEFINED,
        WORKING,
        TURN_ON,
        WAITING,
        TURN_OFF

    }

    private SM state;
    private SM old_state;

    private final SFEI_conveyor sfeiConveyor;
    private final int sfei_idx;
    private boolean old_sEmitter = false;
    private partDescription.material material;
    private int faulty_partID = Integer.MAX_VALUE / 2;

    public produce_more(String[] formulas, SFEI_conveyor sfeiConveyor, int sfei_idx, partDescription.material mat) {
        super(formulas, type.PRODUCE_MORE);
        this.sfeiConveyor = sfeiConveyor;
        this.sfei_idx = sfei_idx;
        this.material = mat;

        this.state = SM.WORKING;
        this.old_state = state;

        if (getnCondition() == null && getaCondition() == null && getmCondition() == null) {
            this.state = SM.UNDEFINED;
            return;
        }
        System.out.println("Produce More on -> " + sfeiConveyor.getName());
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

    public void loop(List<Object> sensorsState, List<Object> actuatorsState, List<Object> holdRegs) {

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
                    state = SM.TURN_ON;
                }
            }
            case TURN_ON -> state = SM.WAITING;
            case WAITING -> {
                boolean sensor = (int) sensorsState.get(sfeiConveyor.getsEmitter().getOffset()) == 1;
                if (utils.getInstance().getLogicalOperator().FE_detector(sensor, old_sEmitter)) {
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
            case WORKING -> {
                if (state != old_state) {
                }
            }
            case TURN_ON -> {
                if (state != old_state) {

                    part newPart = new part(faulty_partID, new partDescription(material, partDescription.form.RAW));
                    faulty_partID++;
                    sfeiConveyor.addNewPartATM(newPart);

//                    // For the overflow, just in case
//                    if (faulty_partID > 0)
//                        faulty_partID = Integer.MAX_VALUE / 2;

                    holdRegs.set(sfeiConveyor.getaEmitPart().getOffset(), (int) Math.pow(2, getNumberbyPartAspect(newPart.getReality()) + 4 - 1));
                    actuatorsState.set(sfeiConveyor.getaEmit().getOffset(), 1);

                    failure_occurrence.activationVariable actVar = null;
                    if (wasActivated_by_N()) {
                        actVar = failure_occurrence.activationVariable.N;
                    } else if (wasActivated_by_A()) {
                        actVar = failure_occurrence.activationVariable.A;
                    } else if (wasActivated_by_M()) {
                        actVar = failure_occurrence.activationVariable.M;
                    }
                    if (actVar != null)
                        newOccurrence = new failure_occurrence(sfeiConveyor.getName(), type.PRODUCE_MORE, actVar, sfeiConveyor.getnPartsMoved(), Instant.now());
                    else
                        throw new RuntimeException("(Produce More) Activation Variable null but evalConditions was TRUE");

                    // Produce More happened
                    System.out.println("********************");
                    System.out.println("   Failure " + sfeiConveyor.getFailuresHistory().size() + " on " + sfeiConveyor.getName() + " " + newOccurrence);
                    System.out.println("********************");
                }
            }
            case WAITING -> {
                holdRegs.set(sfeiConveyor.getaEmitPart().getOffset(), (int) Math.pow(2, getNumberbyPartAspect(new partDescription(material, partDescription.form.RAW)) + 4 - 1));
            }

            case TURN_OFF -> {
                if (state != old_state) {
                    actuatorsState.set(sfeiConveyor.getaEmit().getOffset(), 0);

                    Instant t = Instant.now();
                    newOccurrence.setEnd_t(t);

                    sfeiConveyor.addNewFailureOccurrence(newOccurrence);
                    // Produce More happened
                    System.out.println("********************");
                    System.out.println("   Failure " + (sfeiConveyor.getFailuresHistory().size() - 1) + " on " + sfeiConveyor.getName() + " solved at " + newOccurrence.getEnd_t());
                    System.out.println("********************");
                    newOccurrence = new failure_occurrence();

                }
            }
        }
//        if (old_state != state) {
//            System.out.println("*** Produce More on " + sfeiConveyor.getName() + " -> [" + state + "]");
//        }
        old_state = state;
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
