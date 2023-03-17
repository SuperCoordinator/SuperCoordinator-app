package failures.newVersion;

import failures.*;
import models.base.SFEE;
import models.base.SFEI;
import models.SFEx_particular.SFEI_conveyor;
import models.SFEx_particular.SFEI_machine;

import java.util.*;

public class SFEE_failures2 {

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


    public SFEE_failures2(SFEE sfee, stochasticTime.timeOptions stochasticType, String[] stochasticTime_f, ArrayList<String[]> failures_f) {
        this.sfee = sfee;
        this.stochasticTimeTasks = new LinkedList<>();
        this.stochasticType = stochasticType;
        this.stochasticFormulas = stochasticTime_f;
        this.state = SM.STOCHASTIC;

        int sfeiConveyor_idx_failures = /*pickSFEI(false)*/ 2;
        int sfeiMachine_idx_failures = pickSFEIMachine();

        this.breakdownRepair2 = new breakdown_repair2(
                failures_f.get(0),
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures),
                failures_f.get(1));

        this.breakdown2 = new breakdown2(
                failures_f.get(2),
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures));

        this.produceFaulty2 = new produce_faulty2(
                failures_f.get(3),
                (SFEI_machine) sfee.getSFEIbyIndex(sfeiMachine_idx_failures));

        this.produceMore2 = new produce_more2(
                failures_f.get(4),
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures));
    }

    private final breakdown_repair2 breakdownRepair2;
    private final breakdown2 breakdown2;
    private final produce_faulty2 produceFaulty2;

    private final produce_more2 produceMore2;

    public void loop(List<Object> sensorsState, List<Object> actuatorsState) {

        try {
            // Evaluate of the transitions
            switch (state) {
                case STOCHASTIC -> {
                    breakdownRepair2.loop(sensorsState, actuatorsState);
                    if (breakdownRepair2.isActive()) {
                        state = SM.BREAKDOWN_WITH_REPAIR;
                    } else {
                        breakdown2.loop(sensorsState, actuatorsState);
                        if (breakdown2.isActive()) {
                            state = SM.BREAKDOWN;
                        } else {
                            produceFaulty2.loop(sensorsState, actuatorsState);
                            if (produceFaulty2.isActive()) {
                                state = SM.PRODUCE_FAULTY;
                            } else {
                                produceMore2.loop(sensorsState, actuatorsState);
                                if (produceMore2.isActive()) {
                                    state = SM.PRODUCE_MORE;
                                } /*else {
                                    state = SM.STOCHASTIC;
                                }*/
                            }
                        }
                    }
                }
                case BREAKDOWN_WITH_REPAIR -> {
                    if (!breakdownRepair2.isActive()) {
                        state = SM.STOCHASTIC;
                    }
                }
                case BREAKDOWN -> {
                    if (!breakdown2.isActive()) {
                        state = SM.STOCHASTIC;
                    }
                }
                case PRODUCE_FAULTY -> {
                    if (!produceFaulty2.isActive()) {
                        state = SM.STOCHASTIC;
                    }
                }
                case PRODUCE_MORE -> {
                    if (!produceMore2.isActive()) {
                        state = SM.STOCHASTIC;
                    }
                }
            }

            // Execute tasks
            switch (state) {
                case BREAKDOWN_WITH_REPAIR -> breakdownRepair2.loop(sensorsState, actuatorsState);
                case BREAKDOWN -> breakdown2.loop(sensorsState, actuatorsState);
                case PRODUCE_FAULTY -> produceFaulty2.loop(sensorsState, actuatorsState);
                case PRODUCE_MORE -> produceMore2.loop(sensorsState, actuatorsState);
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

            // The part is in the initial SFEI, so it is needed to select the partID and
            // associate with the correct SFEI to manipulate the time
            if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {

                int minSFEEminOperation_t = calculateSFEEMinOperationTime();

                stochasticTime stochasticTime = new stochasticTime(
                        sfee.getSFEIbyIndex(pickSFEI),
                        sfee.getSFEIbyIndex(0).getPartsATM().first().getId(),
                        stochasticType,
                        stochasticFormulas,
                        minSFEEminOperation_t);
                stochasticTimeTasks.add(stochasticTime);

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

        random.setSeed(3587214);

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
