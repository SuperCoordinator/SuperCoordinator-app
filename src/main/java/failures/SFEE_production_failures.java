package failures;

import failures.supportedTypes.breakdown;
import failures.supportedTypes.breakdown_repair;
import failures.supportedTypes.produce_faulty;
import failures.supportedTypes.produce_more;
import models.base.SFEE;
import models.base.SFEI;
import models.SFEx.SFEI_conveyor;
import models.SFEx.SFEI_machine;
import utility.utils;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEE_production_failures {
    private enum SM {
        STOCHASTIC,
        BREAKDOWN,
        BREAKDOWN_WITH_REPAIR,
        PRODUCE_FAULTY,
        PRODUCE_MORE
    }

    private SM sm_state;
    private SFEE sfee;
    private final LinkedList<stochasticTime> stochasticTimeTasks = new LinkedList<>();
    @XmlElement
    private stochasticTime.timeOptions stochasticType;
    @XmlElement
    private String[] stochasticFormulas;
    @XmlElement
    private ArrayList<String[]> failuresFormulas;

    private breakdown_repair breakdownRepair;
    private failures.supportedTypes.breakdown breakdown;
    private produce_faulty produceFaulty;
    private produce_more produceMore;

    private int oldPartID = -1;

    public SFEE_production_failures() {
    }

    public SFEE_production_failures(SFEE sfee, stochasticTime.timeOptions stochasticType, String[] stochasticTime_f, ArrayList<String[]> failures_f) {
        this.sfee = sfee;
        this.stochasticType = stochasticType;
        this.stochasticFormulas = stochasticTime_f;
        this.failuresFormulas = failures_f;

    }

    public void setSfee(SFEE sfee) {
        this.sfee = sfee;
    }

    public stochasticTime.timeOptions getStochasticType() {
        return stochasticType;
    }

    public String[] getStochasticFormulas() {
        return stochasticFormulas;
    }

    public ArrayList<String[]> getFailuresFormulas() {
        return failuresFormulas;
    }

    private void init() {
        int sfeiConveyor_idx_failures = pickSFEI(false);
        int sfeiMachine_idx_failures = pickSFEIMachine();

        System.out.println("*** Failures Information in " + sfee.getName() + " ***");
        this.breakdownRepair = new breakdown_repair(
                failuresFormulas.get(0),
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures),
                failuresFormulas.get(1));

        this.breakdown = new breakdown(
                failuresFormulas.get(2),
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures));

        this.produceFaulty = new produce_faulty(
                failuresFormulas.get(3),
                (SFEI_machine) sfee.getSFEIbyIndex(sfeiMachine_idx_failures));

        this.produceMore = new produce_more(
                failuresFormulas.get(4),
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures),
                ((SFEI_machine) sfee.getSFEIbyIndex(sfeiMachine_idx_failures)).getPartDescription().material());

        System.out.println("*******************************************");
        System.out.println();
        this.sm_state = SM.STOCHASTIC;
    }

    private boolean firstRun = true;

    public void loop(ArrayList<List<Object>> sensorsState, ArrayList<List<Object>> actuatorsState) {
        if (firstRun) {
            init();
            firstRun = false;
        }
        try {
            // Evaluate of the transitions
            switch (sm_state) {
                case STOCHASTIC -> {
                    breakdownRepair.loop(sensorsState.get(0), actuatorsState.get(0));
                    if (breakdownRepair.isActive()) {
                        sm_state = SM.BREAKDOWN_WITH_REPAIR;
                    } else {
                        breakdown.loop(sensorsState.get(0), actuatorsState.get(0));
                        if (breakdown.isActive()) {
                            sm_state = SM.BREAKDOWN;
                        } else {
                            produceFaulty.loop(sensorsState.get(0), actuatorsState.get(0));
                            if (produceFaulty.isActive()) {
                                sm_state = SM.PRODUCE_FAULTY;
                            } else {
                                produceMore.loop(sensorsState.get(0), actuatorsState.get(0), actuatorsState.get(1));
                                if (produceMore.isActive()) {
                                    sm_state = SM.PRODUCE_MORE;
                                } /*ELSE state = SM.STOCHASTIC; */
                            }
                        }
                    }
                }
                case BREAKDOWN_WITH_REPAIR -> {
                    if (!breakdownRepair.isActive()) {
                        sm_state = SM.STOCHASTIC;
                    }
                }
                case BREAKDOWN -> {
                    if (!breakdown.isActive()) {
                        sm_state = SM.STOCHASTIC;
                    }
                }
                case PRODUCE_FAULTY -> {
                    if (!produceFaulty.isActive()) {
                        sm_state = SM.STOCHASTIC;
                    }
                }
                case PRODUCE_MORE -> {
                    if (!produceMore.isActive()) {
                        sm_state = SM.STOCHASTIC;
                    }
                }
            }

            // Execute tasks
            switch (sm_state) {
                case BREAKDOWN_WITH_REPAIR -> breakdownRepair.loop(sensorsState.get(0), actuatorsState.get(0));
                case BREAKDOWN -> breakdown.loop(sensorsState.get(0), actuatorsState.get(0));
                case PRODUCE_FAULTY -> produceFaulty.loop(sensorsState.get(0), actuatorsState.get(0));
                case PRODUCE_MORE ->
                        produceMore.loop(sensorsState.get(0), actuatorsState.get(0), actuatorsState.get(1));
                case STOCHASTIC -> stochasticTimeMode(sensorsState, actuatorsState);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void stochasticTimeMode(ArrayList<List<Object>> sensorsState, ArrayList<List<Object>> actuatorsState) {
        // Depends on the piece at the emitter of SFEE
        boolean newPiece = checkNewPiece();
        if (newPiece) {
            int pickSFEI = pickSFEI(false);
//                int pickSFEI = 0;

            // The part is in the initial SFEI, so it is needed to select the part and
            // associate with the correct SFEI to manipulate the time
            if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {
                int minSFEEminOperation_t = 0;
                if (sfee.getSFEE_role().equals(SFEE.SFEE_role.PRODUCTION)) {
                    minSFEEminOperation_t = calculateSFEEMinOperationTime();
                }
                stochasticTime stochasticTime = new stochasticTime(
                        sfee.getSFEIbyIndex(pickSFEI),
                        sfee.getSFEIbyIndex(0).getPartsATM().first(),
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
        stochasticTimeTasks.removeIf(object -> object.isConveyorFinished() || object.isPusherFinished() || object.isMachineFinished() || object.isPartProduced());

    }

    private int calculateSFEEMinOperationTime() {
        long total_t = 0;

        for (Map.Entry<Integer, SFEI> entry : sfee.getSFEIs().entrySet()) {
            total_t = total_t + entry.getValue().getMinOperationTime();
        }
        return Math.round(total_t);
    }


    private boolean checkNewPiece() {
        int currID = oldPartID;

        if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {
            currID = sfee.getSFEIbyIndex(0).getPartsATM().first().getId();
        }
        if (currID != oldPartID) {
            if (currID >= 0) {
                oldPartID = currID;
                return true;
            }
        }
        return false;
    }


    private int pickSFEI(boolean isMachineValid) {

        OptionalInt optionalInt;
        do {
            optionalInt = utils.getInstance().getRandom().ints(0, sfee.getSFEIs().size()).findAny();
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
            optionalInt = utils.getInstance().getRandom().ints(0, sfee.getSFEIs().size()).findAny();
        }
        while (optionalInt.isEmpty());

        int sfei_id = optionalInt.getAsInt();
        if (sfee.getSFEIbyIndex(sfei_id).getSfeiType().equals(SFEI.SFEI_type.MACHINE))
            return sfei_id;
        else
            return pickSFEIMachine();

    }

}
