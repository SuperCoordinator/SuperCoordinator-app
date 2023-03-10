package failures;

import models.SFEE;
import models.SFEI.SFEI;
import models.SFEI.SFEI_conveyor;
import models.SFEI.SFEI_machine;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class SFEE_failures {

    private enum SM_Failures {
        STOCHASTIC,
        BREAKDOWN,
        BREAKDOWN_WITH_REPAIR,
        PRODUCE_FAULTY,
        PRODUCE_MORE
    }

    private final SFEE sfee;
    private final LinkedList<stochasticTime> failuresTasks;
    private final stochasticTime.timeOptions stochasticType;
    private final String[] stochasticFormulas;
    private final String[] failuresFormulas;
    private final utils utility;

    private SM_Failures state;
    private final int sfeiConveyor_idx_failures;
    private final int sfeiMachine_idx_failures;

    public SFEE_failures(SFEE sfee, stochasticTime.timeOptions stochasticType, String[] stochasticTime_f, String[] failures_f) {
        this.sfee = sfee;
        this.failuresTasks = new LinkedList<>();
        this.stochasticType = stochasticType;
        this.stochasticFormulas = stochasticTime_f;
        this.failuresFormulas = failures_f;
        this.utility = new utils();

        this.state = SM_Failures.STOCHASTIC;

        this.sfeiConveyor_idx_failures = pickSFEI(false);
        this.sfeiMachine_idx_failures = pickSFEIMachine();

    }

    // Memorize the SFEI in case of BREAKDOWN_WITH_REPAIR
    private int bd_repair_sfei = -1;
    private boolean bd_repair_completed = false;
    private int bd_sfei = -1;
    private int produce_faulty = -1;
    private boolean produce_faulty_completed = false;
    private boolean old_sMachine_door = false;
    private int produce_more = -1;
    private boolean produce_more_completed = false;
    private boolean isEmitterON = false;
    private boolean old_sEmitter = false;

    public void loop(List<Object> sensorsState, List<Object> actuatorsState) {

        try {
            switch (state) {
                case STOCHASTIC -> {

                    boolean bd_repair_formula = evalFormula(0);
                    boolean bd_formula = evalFormula(1);
                    boolean produce_faulty_formula = evalFormula(2);
                    boolean produce_more_formula = evalFormula(3);

                    if (bd_repair_formula || bd_formula || produce_faulty_formula || produce_more_formula) {
                        if (bd_repair_formula && !bd_repair_completed) {
                            state = SM_Failures.BREAKDOWN_WITH_REPAIR;
                        }
                        if (bd_formula) {
                            state = SM_Failures.BREAKDOWN;
                        }
                        if (produce_faulty_formula && !produce_faulty_completed) {
                            state = SM_Failures.PRODUCE_FAULTY;
                        }
                        if (produce_more_formula && !produce_more_completed) {
                            state = SM_Failures.PRODUCE_MORE;
                        }
                    } else {
                        // LESS PRIORITY THAN THE OTHER FAILURES
                        stochasticTimeMode(sensorsState, actuatorsState);
                    }

                    // RESET completed indicators
                    if (!bd_repair_formula && bd_repair_completed)
                        bd_repair_completed = false;
                    if (!produce_faulty_formula && produce_faulty_completed)
                        produce_faulty_completed = false;
                    if (!produce_more_formula && produce_more_completed)
                        produce_more_completed = false;
                }

                case BREAKDOWN_WITH_REPAIR -> {
                    // Stop the motor of the conveyor
                    if (bd_repair_sfei != -1) {
                        if (sfee.getSFEIbyIndex(bd_repair_sfei).getSfeiType().equals(SFEI.SFEI_type.CONVEYOR)) {
                            SFEI_conveyor sfeiConveyor = (SFEI_conveyor) sfee.getSFEIbyIndex(bd_repair_sfei);
                            // inverse way of thinking for the actuator because of the control implementation
                            actuatorsState.set(sfeiConveyor.getaConveyorMotor().bit_offset(), 1);
                            System.out.println("STOPPED the " + sfeiConveyor.getName());
                        } else if (sfee.getSFEIbyIndex(bd_repair_sfei).getSfeiType().equals(SFEI.SFEI_type.MACHINE)) {
                            // Nothing for now
                        }

                        // The last Formula is to go back to default operation mode
                        if (evalFormula(4)) {
                            System.out.println("TIME PASSED");
                            // Maintenance completed
                            // Resume operation
                            if (sfee.getSFEIbyIndex(bd_repair_sfei).getSfeiType().equals(SFEI.SFEI_type.CONVEYOR)) {
                                SFEI_conveyor sfeiConveyor = (SFEI_conveyor) sfee.getSFEIbyIndex(bd_repair_sfei);
                                sfeiConveyor.setDayOfLastMaintenance(Instant.now());
                                // inverse way of thinking for the actuator because of the control implementation
                                actuatorsState.set(sfeiConveyor.getaConveyorMotor().bit_offset(), 0);
                                System.out.println("RUN the " + sfeiConveyor.getName());

                                bd_repair_completed = true;
                                state = SM_Failures.STOCHASTIC;
                                bd_repair_sfei = -1;
                            }
                        }
                    }
                }

                case BREAKDOWN -> {

                    if (bd_sfei != -1) {
                        // Will stay forever STOPPED
                        if (sfee.getSFEIbyIndex(bd_sfei).getSfeiType().equals(SFEI.SFEI_type.CONVEYOR)) {

                            SFEI_conveyor sfeiConveyor = (SFEI_conveyor) sfee.getSFEIbyIndex(bd_sfei);
                            // inverse way of thinking for the actuator because of the control implementation
                            actuatorsState.set(sfeiConveyor.getaConveyorMotor().bit_offset(), 1);

                        } else if (sfee.getSFEIbyIndex(bd_repair_sfei).getSfeiType().equals(SFEI.SFEI_type.MACHINE)) {

                            SFEI_machine sfeiMachine = (SFEI_machine) sfee.getSFEIbyIndex(bd_sfei);
                            // inverse way of thinking for the actuator because of the control implementation
                            actuatorsState.set(sfeiMachine.getaStop().bit_offset(), 1);
                        }
                    }
                }

                case PRODUCE_FAULTY -> {

                    if (produce_faulty != -1) {
                        SFEI_machine sfeiMachine = (SFEI_machine) sfee.getSFEIbyIndex(produce_faulty);

                        boolean b_machine_door = (int) sensorsState.get(sfeiMachine.getsDoor().bit_offset()) == 1;
                        if (sfeiMachine.getPartsATM().size() > 0) {
                            if (utility.getLogicalOperator().FE_detector(b_machine_door, old_sMachine_door)) {
                                actuatorsState.set(sfeiMachine.getaStop().bit_offset(), 1);

                                state = SM_Failures.STOCHASTIC;
                                produce_faulty = -1;
                                produce_faulty_completed = true;
                            }
                        }
                        old_sMachine_door = b_machine_door;
                    }

                }

                case PRODUCE_MORE -> {
                    if (produce_more != -1) {

                        SFEI_conveyor sfeiConveyor = (SFEI_conveyor) sfee.getSFEIbyIndex(produce_more);

                        if (!isEmitterON) {
                            actuatorsState.set(sfeiConveyor.getaEmitter().bit_offset(), 1);
                            isEmitterON = true;
                        }

                        boolean sensor = (int) sensorsState.get(sfeiConveyor.getsEmitter().bit_offset()) == 1;

                        if (utility.getLogicalOperator().FE_detector(sensor, old_sEmitter)) {
                            actuatorsState.set(sfeiConveyor.getaEmitter().bit_offset(), 0);

                            state = SM_Failures.STOCHASTIC;
                            produce_more = -1;
                            produce_more_completed = true;
                        }
                        old_sEmitter = sensor;
                    }
                }
                default -> {
                }
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    private void stochasticTimeMode(List<Object> sensorsState, List<Object> actuatorsState) {
        // Depends on the piece at the emitter of SFEE
        boolean newPiece = checkNewPiece();
        if (newPiece) {
            int pickSFEI = pickSFEI(false);
//                int pickSFEI = 0;

            // The part is in the initial SFEI, so it is needed to select the partID and
            // associate with the correct SFEI to manipulate the time
            if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {

                int minSFEEminOperation_t = calculateSFEEMinOperationTime();
//                    System.out.println("SFEI chosen: " + pickSFEI + " to delay: " + delay);

                stochasticTime stochasticTime = new stochasticTime(
                        sfee.getSFEIbyIndex(pickSFEI),
                        sfee.getSFEIbyIndex(0).getPartsATM().first().getId(),
                        stochasticType,
                        stochasticFormulas,
                        minSFEEminOperation_t);
                failuresTasks.add(stochasticTime);

            }
        }

        // Runs the tasks
        for (stochasticTime object : failuresTasks) {
            object.loop(sensorsState, actuatorsState);
        }

        // Delete the completed tasks
        failuresTasks.removeIf(object -> object.isConveyorFinished() || object.isMachineFinished());

    }

    private int calculateSFEEMinOperationTime() {
        long total_t = 0;

        for (Map.Entry<Integer, SFEI> entry : sfee.getSFEIs().entrySet()) {
            total_t = total_t + entry.getValue().getMinOperationTime();
        }
        return Math.round(total_t);
    }

    private int oldPartID = -1;

    private boolean checkNewPiece() {
        int currID = oldPartID;

        if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {
            currID = sfee.getSFEIbyIndex(0).getPartsATM().first().getId();
        }
        if (currID != oldPartID) {
            oldPartID = currID;
            return true;
        }
        return false;
    }

    private final Random random = new Random();

    private int pickSFEI(boolean isMachineValid) {

        OptionalInt optionalInt;
        do {
            optionalInt = random.ints(0, sfee.getSFEIs().size()).findAny();
        }
        while (optionalInt.isEmpty());

        int sfei_id = optionalInt.getAsInt();
        if (!sfee.getSFEIbyIndex(sfei_id).getSfeiType().equals(SFEI.SFEI_type.MACHINE))
            return sfei_id;
        if (sfee.getSFEIbyIndex(sfei_id).getSfeiType().equals(SFEI.SFEI_type.MACHINE) && isMachineValid) {
            return sfei_id;
        }
        return pickSFEI(isMachineValid);

    }

    private int pickSFEIMachine() {
        OptionalInt optionalInt;
        do {
            optionalInt = random.ints(0, sfee.getSFEIs().size()).findAny();
        }
        while (optionalInt.isEmpty());

        int sfei_id = optionalInt.getAsInt();
        if (sfee.getSFEIbyIndex(sfei_id).getSfeiType().equals(SFEI.SFEI_type.MACHINE))
            return sfei_id;
        else
            return pickSFEIMachine();

    }

    private boolean evalFormula(int idx) {

        String[] members;
        String op;
        String formula = failuresFormulas[idx];
        if (formula.contains(" > ")) {
            op = ">";
        } else if (formula.contains(" < ")) {
            op = "<";
        } else if (formula.contains(" = ")) {
            op = "=";
        } else if (formula.contains(" <= ")) {
            op = "<=";
        } else if (formula.contains(" >= ")) {
            op = ">=";
        } else {
            return false;
        }
        members = formula.split(op);
        SFEI sfei = null;
        // Only if the SM_Failures is not in the BREAKDOWN_WITH_REPAIR MODE
        if (idx != 4) {
            // Regarding the type of the Failure, could be in different SFEIs
            // With the index, the formula is available and the corresponding SFEI
            // is extrapolated
            switch (idx) {
                case 0 -> {
                    // BREAKDOWN_WITH_REPAIR
                    // For now, do not break down the machines, because it is not clear the
                    // correct way to proceed
//                    bd_repair_sfei = pickSFEI(false);
                    bd_repair_sfei = sfeiConveyor_idx_failures;
                    sfei = sfee.getSFEIbyIndex(bd_repair_sfei);
                }
                case 1 -> {
                    // BREAKDOWN
//                    bd_sfei = pickSFEI(true);
                    bd_sfei = sfeiConveyor_idx_failures;
                    sfei = sfee.getSFEIbyIndex(bd_sfei);
                }
                case 2 -> {
                    // PRODUCE_FAULTY -> Obligated for SFEI Machine
//                    produce_faulty = pickSFEIMachine();
                    produce_faulty = sfeiMachine_idx_failures;
                    sfei = sfee.getSFEIbyIndex(produce_faulty);
                }
                case 3 -> {
                    // PRODUCE_MORE -> Obligated for SFEI Conveyor
//                    produce_more = pickSFEI(false);
                    produce_more = sfeiConveyor_idx_failures;
                    sfei = sfee.getSFEIbyIndex(produce_more);
                }
            }
        } else {
            sfei = sfee.getSFEIbyIndex(bd_repair_sfei);
        }

        assert sfei != null;
        double leftMember = utility.getCustomCalc().calcExpression(
                members[0],
                sfei.getnPiecesMoved(),
                Duration.between(sfei.getDayOfBirth(), Instant.now()).toMinutes(),
                Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toMinutes());
        double rightMember = utility.getCustomCalc().calcExpression(
                members[1],
                sfei.getnPiecesMoved(),
                Duration.between(sfei.getDayOfBirth(), Instant.now()).toMinutes(),
                Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toMinutes());

        return switch (op) {
            case ">" -> leftMember > rightMember;
            case "<" -> leftMember < rightMember;
            case "=" -> leftMember == rightMember;
            case "<=" -> leftMember <= rightMember;
            case ">=" -> leftMember >= rightMember;
            default -> false;
        };
    }


}
