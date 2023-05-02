/*
package failures.oldVersion;

import failures.stochasticTime;
import models.base.SFEE;
import models.base.SFEI;
import models.SFEx_particular.SFEI_conveyor;
import models.SFEx_particular.SFEI_machine;

import java.util.*;

public class SFEE_failures {

    private enum SM {
        STOCHASTIC,
        BREAKDOWN,
        BREAKDOWN_WITH_REPAIR,
        PRODUCE_FAULTY,
        PRODUCE_MORE
    }

    private SM state;

    private final SFEE sfee;
    private final LinkedList<stochasticTime> stochasticTimeTasks;
    private final stochasticTime.timeOptions stochasticType;
    private final String[] stochasticFormulas;

    public SFEE_failures(SFEE sfee, stochasticTime.timeOptions stochasticType, String[] stochasticTime_f, String[] failures_f) {
        this.sfee = sfee;
        this.stochasticTimeTasks = new LinkedList<>();
        this.stochasticType = stochasticType;
        this.stochasticFormulas = stochasticTime_f;
        this.state = SM.STOCHASTIC;

        int sfeiConveyor_idx_failures = pickSFEI(false);
        int sfeiMachine_idx_failures = pickSFEIMachine();

        this.breakdownRepair = new breakdown_repair(
                failure.type.BREAKDOWN_WITH_REPAIR,
                failures_f[0],
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures),
                failures_f[4]);

        this.breakdown = new breakdown(
                failure.type.BREAKDOWN,
                failures_f[1],
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures));


        this.produceFaulty = new produce_faulty(
                failure.type.PRODUCE_FAULTY,
                failures_f[2],
                (SFEI_machine) sfee.getSFEIbyIndex(sfeiMachine_idx_failures));

        this.produceMore = new produce_more(
                failure.type.PRODUCE_MORE,
                failures_f[3],
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures));
    }

    private final breakdown_repair breakdownRepair;
    private final breakdown breakdown;
    private final produce_faulty produceFaulty;
    private final produce_more produceMore;

    public void loop(List<Object> sensorsState, List<Object> actuatorsState) {

        try {
            // Evaluate of the transitions
            switch (state) {
                case STOCHASTIC -> {
                    breakdownRepair.loop(sensorsState, actuatorsState);
                    if (breakdownRepair.isActive()) {
                        state = SM.BREAKDOWN_WITH_REPAIR;
                    } else {
                        breakdown.loop(sensorsState, actuatorsState);
                        if (breakdown.isActive()) {
                            state = SM.BREAKDOWN;
                        } else {
                            produceFaulty.loop(sensorsState, actuatorsState);
                            if (produceFaulty.isActive()) {
                                state = SM.PRODUCE_FAULTY;
                            } else {
                                produceMore.loop(sensorsState, actuatorsState);
                                if (produceMore.isActive()) {
                                    state = SM.PRODUCE_MORE;
                                } */
/*else {
                                    state = SM.STOCHASTIC;
                                }*//*

                            }
                        }
                    }
                }
                case BREAKDOWN_WITH_REPAIR -> {
                    if (!breakdownRepair.isActive()) {
                        state = SM.STOCHASTIC;
                    }
                }
                case BREAKDOWN -> {
                    if (!breakdown.isActive()) {
                        state = SM.STOCHASTIC;
                    }
                }
                case PRODUCE_FAULTY -> {
                    if (!produceFaulty.isActive()) {
                        state = SM.STOCHASTIC;
                    }
                }
                case PRODUCE_MORE -> {
                    if (!produceMore.isActive()) {
                        state = SM.STOCHASTIC;
                    }
                }
            }

            // Execute tasks
            switch (state) {
                case BREAKDOWN_WITH_REPAIR -> breakdownRepair.loop(sensorsState, actuatorsState);
                case BREAKDOWN -> breakdown.loop(sensorsState, actuatorsState);
                case PRODUCE_FAULTY -> produceFaulty.loop(sensorsState, actuatorsState);
                case PRODUCE_MORE -> produceMore.loop(sensorsState, actuatorsState);
                case STOCHASTIC -> stochasticTimeMode(sensorsState, actuatorsState);
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

            // The db_part is in the initial SFEI, so it is needed to select the partID and
            // associate with the correct SFEI to manipulate the time
            if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {

                int minSFEEminOperation_t = calculateSFEEMinOperationTime();

                stochasticTime stochasticTime = new stochasticTime(
                        sfee.getSFEIbyIndex(pickSFEI),
                        sfee.getSFEIbyIndex(0).getPartsATM().first(),
                        stochasticType,
                        stochasticFormulas,
                        minSFEEminOperation_t);
                stochasticTimeTasks.insert(stochasticTime);

            }
        }

        // Runs the tasks
        for (stochasticTime object : stochasticTimeTasks) {
            object.loop(sensorsState, actuatorsState);
        }

        // Delete the completed tasks
        stochasticTimeTasks.removeIf(object -> object.isConveyorFinished() || object.isMachineFinished());

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

}
*/
