package failures;

import communication.modbus;
import models.SFEE;
import models.SFEI.SFEI;
import org.apache.commons.math3.dfp.DfpField;
import org.apache.commons.math3.util.Precision;
import utils.utils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class SFEE_failures {

    public enum timeOptions {
        GAUSSIAN,
        LINEAR
    }

    private final SFEE sfee;

    private final LinkedList<stochasticTime> failuresTasks;
    private final timeOptions timeType;

    // IN case of STOCHASTIC
    private String mean;
    private String std_dev;

    private final utils utility;

/*    private final ScheduledExecutorService executorService;
    private final ExecutorService service;*/

    public SFEE_failures(SFEE sfee, timeOptions timeType) {
        this.sfee = sfee;
        this.failuresTasks = new LinkedList<>();

        this.utility = new utils();
        this.timeType = timeType;

/*        this.executorService = Executors.newScheduledThreadPool(sfee.getSFEIs().size());
        this.service = Executors.newFixedThreadPool(1);*/
    }

    public void setMean(String mean) {
        this.mean = mean;
    }

    public void setStd_dev(String std_dev) {
        this.std_dev = std_dev;
    }


    public String loop(String sensorsState,/* modbus mb*/ String actuatorsState) {
        String ret = actuatorsState;
        // Depends on the piece at the emitter of SFEE
        try {
            boolean newPiece = checkNewPiece();
            if (newPiece) {
                int pickSFEI = pickSFEI(false);
//                int pickSFEI = 2;

                // The part is in the initial SFEI, so it is needed to select the partID and
                // associate with the correct SFEI to manipulate the time
                if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {

                    int delay = calculateDelay(pickSFEI);
                    System.out.println("SFEI index chosen: " + pickSFEI + " to delay: " + delay);

                    stochasticTime stochasticTime = new stochasticTime(sfee.getSFEIbyIndex(pickSFEI), sfee.getSFEIbyIndex(0).getPartsATM().first().getId(), delay/*,mb*/);
                    failuresTasks.add(stochasticTime);
/*                    Thread t1 = new Thread(stochasticTime);
                    t1.start();*/

                }
            }

//            String[] res = ret.split(" ");
            // Runs the tasks
            for (stochasticTime object : failuresTasks) {
                ret = object.loop(sensorsState, ret);
//                String[] obj_set_ = obj_ret.split(" ");
//                for (int i = 0; i < obj_set_.length; i++) {
//                    res[i] = String.valueOf(Boolean.parseBoolean(String.valueOf(Boolean.parseBoolean(res[i]) ^ Boolean.parseBoolean(obj_set_[i]))));
//                }
            }

            // Delete the completed tasks
            failuresTasks.removeIf(object -> object.isConveyorFinished() || object.isMachineFinished());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private int oldPartID = -1;

    private boolean checkNewPiece() {
        int currID = oldPartID;
//        System.out.println(Thread.currentThread().getName());
        if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {
            //System.out.println("*** " + sfee.getName() + " #parts at entryConv: " + sfee.getSFEIbyIndex(0).getPartsATM().size());
            currID = sfee.getSFEIbyIndex(0).getPartsATM().first().getId();
        }
        if (currID != oldPartID) {
            oldPartID = currID;
            return true;
        }
        return false;
    }

    private int pickSFEI(boolean re_entrant) {

        Random random = new Random();
        OptionalInt optionalInt;
        do {
            // NOT WORKING FOR THE LAST SFEI, WHY???
            optionalInt = random.ints(0, sfee.getSFEIs().size()).findAny();
        }
        while (optionalInt.isEmpty());
        int sfei_id = optionalInt.getAsInt();
        if (sfee.getSFEIbyIndex(sfei_id).getSfeiType().equals(SFEI.SFEI_type.MACHINE) && !re_entrant)
            sfei_id = pickSFEI(true);
        return sfei_id;

    }

    private final Random random = new Random();

    private int calculateDelay(int sfei_id) {

        SFEI sfei = sfee.getSFEIbyIndex(sfei_id);


        double m = utility.getCustomCalc().calcExpression(mean,
                sfei.getnPiecesMoved(),
                (double) Duration.between(sfei.getDayOfBirth(), Instant.now()).toDays(),
                (double) Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toDays());

        double total_Time;
        if (timeType.equals(SFEE_failures.timeOptions.GAUSSIAN)) {

            double dev = utility.getCustomCalc().calcExpression(std_dev,
                    sfei.getnPiecesMoved(),
                    (double) Duration.between(sfei.getDayOfBirth(), Instant.now()).toDays(),
                    (double) Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toDays());


//            total_Time = random.nextGaussian() * Math.sqrt(dev) + m;
            total_Time = random.nextGaussian() * dev + m;
            System.out.println("Calculated Mean: " + m + " and dev:" + dev + " with total time of: " + total_Time);
        } else {
            total_Time = m;
            System.out.println("Calculated Mean: " + m + " with total time of: " + total_Time);
        }

        for (Map.Entry<Integer, SFEI> entry : sfee.getSFEIs().entrySet()) {
            total_Time = total_Time - entry.getValue().getMinOperationTime();
        }
        if (total_Time < 0)
            return 0;
        return (int) Precision.round(total_Time, 2, BigDecimal.ROUND_HALF_UP);
    }

}
