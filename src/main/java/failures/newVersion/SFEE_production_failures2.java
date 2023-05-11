package failures.newVersion;

import failures.*;
import models.base.SFEE;
import models.base.SFEI;
import models.SFEx.SFEI_conveyor;
import models.SFEx.SFEI_machine;
import utility.utils;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEE_production_failures2 {
    private enum SM {
        STOCHASTIC,
        BREAKDOWN,
        BREAKDOWN_WITH_REPAIR,
        PRODUCE_FAULTY,
        PRODUCE_MORE
    }

    private SM sm_state;
    private SFEE sfee;
    private LinkedList<stochasticTime> stochasticTimeTasks = new LinkedList<>();
    @XmlElement
    private stochasticTime.timeOptions stochasticType;
    @XmlElement
    private String[] stochasticFormulas;
    @XmlElement
    private ArrayList<String[]> failuresFormulas;

    private breakdown_repair2 breakdownRepair2;
    private breakdown2 breakdown2;
    private produce_faulty2 produceFaulty2;
    private produce_more2 produceMore2;

    private int oldPartID = -1;

    public SFEE_production_failures2() {
    }

    public SFEE_production_failures2(SFEE sfee, stochasticTime.timeOptions stochasticType, String[] stochasticTime_f, ArrayList<String[]> failures_f) {
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

        System.out.println("*** Failures Information in " + sfee.getName() + "***");
        this.breakdownRepair2 = new breakdown_repair2(
                failuresFormulas.get(0),
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures),
                failuresFormulas.get(1));

        this.breakdown2 = new breakdown2(
                failuresFormulas.get(2),
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures));

        this.produceFaulty2 = new produce_faulty2(
                failuresFormulas.get(3),
                (SFEI_machine) sfee.getSFEIbyIndex(sfeiMachine_idx_failures));

        this.produceMore2 = new produce_more2(
                failuresFormulas.get(4),
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures),
                ((SFEI_machine) sfee.getSFEIbyIndex(sfeiMachine_idx_failures)).getPartDescription().material());

        System.out.println("************************");

        this.sm_state = SM.STOCHASTIC;
    }

    private boolean first_run = true;

    public void loop(ArrayList<List<Object>> sensorsState, ArrayList<List<Object>> actuatorsState) {
        if (first_run) {
            init();
            first_run = false;
        }
        try {
            // Evaluate of the transitions
            switch (sm_state) {
                case STOCHASTIC -> {
                    breakdownRepair2.loop(sensorsState.get(0), actuatorsState.get(0));
                    if (breakdownRepair2.isActive()) {
                        sm_state = SM.BREAKDOWN_WITH_REPAIR;
                    } else {
                        breakdown2.loop(sensorsState.get(0), actuatorsState.get(0));
                        if (breakdown2.isActive()) {
                            sm_state = SM.BREAKDOWN;
                        } else {
                            produceFaulty2.loop(sensorsState.get(0), actuatorsState.get(0));
                            if (produceFaulty2.isActive()) {
                                sm_state = SM.PRODUCE_FAULTY;
                            } else {
                                produceMore2.loop(sensorsState.get(0), actuatorsState.get(0), actuatorsState.get(1));
                                if (produceMore2.isActive()) {
                                    sm_state = SM.PRODUCE_MORE;
                                } /*ELSE state = SM.STOCHASTIC; */
                            }
                        }
                    }
                }
                case BREAKDOWN_WITH_REPAIR -> {
                    if (!breakdownRepair2.isActive()) {
                        sm_state = SM.STOCHASTIC;
                    }
                }
                case BREAKDOWN -> {
                    if (!breakdown2.isActive()) {
                        sm_state = SM.STOCHASTIC;
                    }
                }
                case PRODUCE_FAULTY -> {
                    if (!produceFaulty2.isActive()) {
                        sm_state = SM.STOCHASTIC;
                    }
                }
                case PRODUCE_MORE -> {
                    if (!produceMore2.isActive()) {
                        sm_state = SM.STOCHASTIC;
                    }
                }
            }

            // Execute tasks
            switch (sm_state) {
                case BREAKDOWN_WITH_REPAIR -> breakdownRepair2.loop(sensorsState.get(0), actuatorsState.get(0));
                case BREAKDOWN -> breakdown2.loop(sensorsState.get(0), actuatorsState.get(0));
                case PRODUCE_FAULTY -> produceFaulty2.loop(sensorsState.get(0), actuatorsState.get(0));
                case PRODUCE_MORE ->
                        produceMore2.loop(sensorsState.get(0), actuatorsState.get(0), actuatorsState.get(1));
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

            // The M_part is in the initial SFEI, so it is needed to select the M_part and
            // associate with the correct SFEI to manipulate the time
            if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {
                int minSFEEminOperation_t = 0;
                if (sfee.getSFEE_function().equals(SFEE.SFEE_role.PRODUCTION)) {
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
        stochasticTimeTasks.removeIf(object -> object.isConveyorFinished() || object.isMachineFinished());

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
            oldPartID = currID;
            return true;
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
