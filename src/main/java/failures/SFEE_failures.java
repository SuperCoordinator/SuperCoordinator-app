package failures;

import models.SFEE;
import models.SFEI.SFEI;
import utils.RunnableWrap;

import java.util.Map;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SFEE_failures implements Runnable {

    public enum timeType {
        STOCHASTIC,
        LINEAR
    }

    private final SFEE sfee;

    private timeType timeType;

    // IN case of STOCHASTIC
    private double mean;
    private double std_dev;

/*    private final ScheduledExecutorService executorService;
    private final ExecutorService service;*/

    public SFEE_failures(SFEE sfee) {
        this.sfee = sfee;

/*        this.executorService = Executors.newScheduledThreadPool(sfee.getSFEIs().size());
        this.service = Executors.newFixedThreadPool(1);*/
    }

    public void setTimeType(timeType timeType) {
        this.timeType = timeType;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public void setStd_dev(double std_dev) {
        this.std_dev = std_dev;
    }


    @Override
    public void run() {

        // Launch new thread for interfering on SFEE time
        // Depends on the piece at the emitter os SFEE
        try {
            boolean newPiece = checkNewPiece();

            if (newPiece) {
                int pickSFEI = pickSFEI();
                //int pickSFEI = 2;

                // The part is in the initial SFEI, so it is needed to select the partID and
                // associate with the correct SFEI to manipulate the time
                if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {
                    AtomicBoolean stop = new AtomicBoolean(false);
                    int delay = calculateDelay();
                    System.out.println("SFEI index chosen: " + pickSFEI + " to delay: " + delay);
/*                new RunnableWrap().runNTimes(
                        new stochastic(sfee.getSFEIbyIndex(pickSFEI), stop, sfee.getSFEIbyIndex(0).getPartsATM().first().getId(), delay, sfee.getMb()),
                        stop,
                        50,
                        TimeUnit.MILLISECONDS,
                        executorService);*/

                    stochastic2 stochastic2 = new stochastic2(sfee.getSFEIbyIndex(pickSFEI), sfee.getSFEIbyIndex(0).getPartsATM().first().getId(), delay, sfee.getMb());

/*                    ExecutorService service = Executors.newFixedThreadPool(1);
                    service.submit(stochastic2);*/

                    Thread t1 = new Thread(stochastic2);
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

    private int pickSFEI() {

        Random random = new Random();
        OptionalInt optionalInt;
        do {
            // NOT WORKING FOR THE LAST SFEI, WHY???
            optionalInt = random.ints(0, sfee.getSFEIs().size() - 1).findAny();
        }
        while (optionalInt.isEmpty());
        return optionalInt.getAsInt();

    }

    private int calculateDelay() {

        Random random = new Random();
        double total_Time = random.nextGaussian() * Math.sqrt(std_dev) + mean;

        for (Map.Entry<Integer, SFEI> entry : sfee.getSFEIs().entrySet()) {
            total_Time = total_Time - entry.getValue().getMinOperationTime();
        }
        if (total_Time < 0)
            return 0;
        return (int) total_Time;
    }

}
