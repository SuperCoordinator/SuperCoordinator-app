package failures;

import models.SFEE;
import models.SFEI.SFEI;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Random;

public class SFEE_failures implements Runnable {

    public enum timeOptions {
        GAUSSIAN,
        LINEAR
    }

    private final SFEE sfee;

    private final timeOptions timeType;

    // IN case of STOCHASTIC
    private String mean;
    private String std_dev;

    private final utils utility;

/*    private final ScheduledExecutorService executorService;
    private final ExecutorService service;*/

    public SFEE_failures(SFEE sfee, timeOptions timeType) {
        this.sfee = sfee;
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

    @Override
    public void run() {

        // Launch new thread for interfering on SFEE time
        // Depends on the piece at the emitter os SFEE
        try {
            boolean newPiece = checkNewPiece();

            if (newPiece) {
                int pickSFEI = pickSFEI(false);
                //int pickSFEI = 2;

                // The part is in the initial SFEI, so it is needed to select the partID and
                // associate with the correct SFEI to manipulate the time
                if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {

                    int delay = calculateDelay(pickSFEI);
                    System.out.println("SFEI index chosen: " + pickSFEI + " to delay: " + delay);

                    stochasticTime stochasticTime = new stochasticTime(sfee.getSFEIbyIndex(pickSFEI), sfee.getSFEIbyIndex(0).getPartsATM().first().getId(), delay, sfee.getMb());

                    Thread t1 = new Thread(stochasticTime);
                    t1.start();

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private int calculateDelay(int sfei_id) {

        SFEI sfei = sfee.getSFEIbyIndex(sfei_id);
        double dev = utility.getCustomCalc().calcExpression(std_dev,
                sfei.getnPiecesMoved(),
                (double) Duration.between(sfei.getDayOfBirth(), Instant.now()).toDays(),
                (double) Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toDays());

        double m = utility.getCustomCalc().calcExpression(mean,
                sfei.getnPiecesMoved(),
                (double) Duration.between(sfei.getDayOfBirth(), Instant.now()).toDays(),
                (double) Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toDays());

        double total_Time;
        if (timeType.equals(SFEE_failures.timeOptions.GAUSSIAN)) {
            Random random = new Random();
            total_Time = random.nextGaussian() * Math.sqrt(dev) + m;
        } else {
            total_Time = utility.getCustomCalc().calcExpression(mean,
                    sfei.getnPiecesMoved(),
                    (double) Duration.between(sfei.getDayOfBirth(), Instant.now()).toDays(),
                    (double) Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toDays());
        }

        for (Map.Entry<Integer, SFEI> entry : sfee.getSFEIs().entrySet()) {
            total_Time = total_Time - entry.getValue().getMinOperationTime();
        }
        if (total_Time < 0)
            return 0;
        return (int) total_Time;
    }

}
