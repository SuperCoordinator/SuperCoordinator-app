package failures;

import failures.formulas.gaussFormula;
import failures.supportedTypes.*;
import models.base.SFEE;
import models.base.SFEI;
import models.SFEx.SFEI_conveyor;
import models.SFEx.SFEI_machine;
import utility.utils;

import javax.xml.bind.annotation.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEE_production_failures {

    private enum SM {
        STOCHASTIC,
        BREAKDOWN,
        BREAKDOWN_WITH_REPAIR,
        PRODUCE_FAULTY,
        PRODUCE_MORE,
        PRODUCE_LESS
    }

    private SM sm_state;
    private SFEE sfee;
    private final ArrayList<stochasticTime> stochasticTimeTasks = new ArrayList<>();
    @XmlElement
    private stochasticTime.timeOptions stochasticType;
    @XmlElement
    private String[] stochasticFormulas;
    @XmlElement
    private ArrayList<String[]> failuresFormulas;


    private gaussFormula gaussFormula;
    private breakdown_repair breakdownRepair;
    private breakdown breakdown;
    private produce_faulty produceFaulty;
    private produce_more produceMore;
    private produce_less produceLess;

    private int oldPartID = -1;

    public SFEE_production_failures() {
    }

    public SFEE_production_failures(SFEE sfee, stochasticTime.timeOptions stochasticType, String[] stochasticTime_f, ArrayList<String[]> failures_f) {
        this.sfee = sfee;
        this.stochasticType = stochasticType;
        this.stochasticFormulas = stochasticTime_f;
        this.failuresFormulas = failures_f;

        if (stochasticType.equals(stochasticTime.timeOptions.GAUSSIAN))
            this.gaussFormula = new gaussFormula();

    }

    public void setSfee(SFEE sfee) {
        this.sfee = sfee;

        if (stochasticType.equals(stochasticTime.timeOptions.GAUSSIAN))
            this.gaussFormula = new gaussFormula();
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
                sfeiConveyor_idx_failures,
                failuresFormulas.get(1));

        this.breakdown = new breakdown(
                failuresFormulas.get(2),
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures),
                sfeiConveyor_idx_failures);

        this.produceFaulty = new produce_faulty(
                failuresFormulas.get(3),
                (SFEI_machine) sfee.getSFEIbyIndex(sfeiMachine_idx_failures),
                sfeiMachine_idx_failures);

        this.produceMore = new produce_more(
                failuresFormulas.get(4),
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures),
                sfeiConveyor_idx_failures,
                ((SFEI_machine) sfee.getSFEIbyIndex(sfeiMachine_idx_failures)).getPartDescription().material());

        this.produceLess = new produce_less(
                failuresFormulas.get(5),
                (SFEI_conveyor) sfee.getSFEIbyIndex(sfeiConveyor_idx_failures),
                sfeiConveyor_idx_failures);

        System.out.println("*******************************************");
        System.out.println();
        this.sm_state = SM.STOCHASTIC;
    }

    private boolean firstRun = true;
    private SM old_state;

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
                                } else {
                                    produceLess.loop(sensorsState.get(0), actuatorsState.get(0));
                                    if (produceLess.isActive()) {
                                        sm_state = SM.PRODUCE_LESS;
                                    }/*ELSE state = SM.STOCHASTIC; */
                                }
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
                case PRODUCE_LESS -> {
                    if (!produceLess.isActive()) {
                        sm_state = SM.STOCHASTIC;
                    }
                }
            }

            stochasticTimeMode();

            // Execute tasks
            switch (sm_state) {
                case BREAKDOWN_WITH_REPAIR -> {
                    for (stochasticTime object : stochasticTimeTasks) {
                        if (breakdownRepair.getSfei_idx() > object.getSfei_idx())
                            object.loop(sensorsState, actuatorsState);
                    }
                    breakdownRepair.loop(sensorsState.get(0), actuatorsState.get(0));
                }
                case BREAKDOWN -> {
                    for (stochasticTime object : stochasticTimeTasks) {
                        if (breakdown.getSfei_idx() > object.getSfei_idx())
                            object.loop(sensorsState, actuatorsState);
                    }
                    breakdown.loop(sensorsState.get(0), actuatorsState.get(0));
                }
                case PRODUCE_FAULTY -> {
                    for (stochasticTime object : stochasticTimeTasks) {
                        // Here must execute always the stochastic tasks, due to the part location (in which SFEI)
                        // and taking into account the time trigger (as for the event trigger all goes well)
                        // For the time, the part can be in the SFEI_idx 2 sleeping and the PRODUCE FAULTY turns on

//                        if (produceFaulty.getSfei_idx() > object.getSfei_idx())
                        object.loop(sensorsState, actuatorsState);
                    }
                    produceFaulty.loop(sensorsState.get(0), actuatorsState.get(0));
                }
                case PRODUCE_MORE -> {
                    for (stochasticTime object : stochasticTimeTasks) {
                        if (produceMore.getSfei_idx() > object.getSfei_idx())
                            object.loop(sensorsState, actuatorsState);
                    }
                    produceMore.loop(sensorsState.get(0), actuatorsState.get(0), actuatorsState.get(1));
                }
                case PRODUCE_LESS -> {
                    for (stochasticTime object : stochasticTimeTasks) {
                        if (produceLess.getSfei_idx() > object.getSfei_idx())
                            object.loop(sensorsState, actuatorsState);
                    }
                    produceLess.loop(sensorsState.get(0), actuatorsState.get(0));
                }
                case STOCHASTIC -> {
                    // Runs the tasks
                    for (stochasticTime object : stochasticTimeTasks) {
                        object.loop(sensorsState, actuatorsState);
                    }
                    // Delete the completed tasks
                    if (stochasticTimeTasks.removeIf(object -> object.isConveyorFinished() || object.isPusherFinished() || object.isMachineFinished()
                            || object.isPartProduced() || object.isPartRemovedInProduction())) {
                        System.out.println(" >>> " + sfee.getName() + " stochasticTasks: " + stochasticTimeTasks.size());
                        if (stochasticTimeTasks.size() == 1) {
                            System.out.println(stochasticTimeTasks.get(0).getPart());
                        }
                    }
                }
            }
            if (sm_state != old_state) {
                System.out.println("   ---> " + sfee.getName() + " failure state: " + sm_state);
            }
            old_state = sm_state;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void stochasticTimeMode() {
        // Depends on the piece at the emitter of SFEE
        boolean newPiece = checkNewPiece();
        if (newPiece) {
            int pickedSFEI = pickSFEI(false);
//                int pickedSFEI = 0;

            // The part is in the initial SFEI, so it is needed to select the part and
            // associate with the correct SFEI to manipulate the time
            if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {
//                int minSFEEminOperation_t = 0;
//                if (sfee.getSFEE_role().equals(SFEE.SFEE_role.PRODUCTION)) {
//                    minSFEEminOperation_t = calculateSFEEMinOperationTime();
//                }
                stochasticTime stochasticTime = new stochasticTime(
                        sfee.getSFEIbyIndex(pickedSFEI),
                        pickedSFEI,
                        sfee.getSFEIbyIndex(0).getPartsATM().first(),
                        calculateDelay(pickedSFEI)
/*                        stochasticType,
                        stochasticFormulas,
                        minSFEEminOperation_t*/);
                stochasticTimeTasks.add(stochasticTime);
            }

        }
    }

    private double calculateSFEEMinOperationTime() {
        double total_t = 0;
        if (sfee.getSFEE_role().equals(SFEE.SFEE_role.PRODUCTION))
            for (Map.Entry<Integer, SFEI> entry : sfee.getSFEIs().entrySet()) {
                total_t = total_t + entry.getValue().getMinOperationTime();
            }
        return total_t;
    }

    private double calculateDelay(int sfei_idx) {

        // Calculate mean and dev
        // If different from previous value, new formula (of any type)
        try {
            SFEI sfei = sfee.getSFEIbyIndex(sfei_idx);

            double m = utils.getInstance().getCustomCalculator().calcExpression(stochasticFormulas[0],
                    sfei.getnPiecesMoved(),
                    (double) Duration.between(sfei.getDayOfBirth(), Instant.now()).toMinutes(),
                    (double) Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toMinutes());

            double total_Time = m;

            if (stochasticType.equals(stochasticTime.timeOptions.GAUSSIAN)) {

                double dev = utils.getInstance().getCustomCalculator().calcExpression(stochasticFormulas[1],
                        sfei.getnPiecesMoved(),
                        (double) Duration.between(sfei.getDayOfBirth(), Instant.now()).toMinutes(),
                        (double) Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toMinutes());

                // if mean and/or dev change value (due to dependency of variables like n,a,m)
                if (gaussFormula.getCurrentValue() == -1) {
                    // First execution
                    gaussFormula = new gaussFormula(m, dev, true);
                } else {
                    // Others executions
                    double mean = gaussFormula.getMean();
                    double deviation = gaussFormula.getDev();
                    if (!(mean - 1.0 < m && m < mean + 1.0) || !(deviation - 1.0 < dev && dev < deviation + 1.0)) {
                        // If is a new value for mean or deviation
                        gaussFormula = new gaussFormula(m, dev, true);
                    }
                }

                total_Time = gaussFormula.getCurrentValue();
                gaussFormula.setNextValue();
            }

            total_Time = total_Time - /*sumSFEEminOperationTime*/ calculateSFEEMinOperationTime();

            if (!sfei.getSfeiType().equals(SFEI.SFEI_type.TRANSPORT))
                System.out.println(sfee.getSFEIbyIndex(0).getPartsATM().first() + " delay " + total_Time * 1000 + " (ms) on SFEI:" + sfei.getName());
            if (total_Time < 0)
                return 0;
            // For the result in millis
            return total_Time * 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
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
