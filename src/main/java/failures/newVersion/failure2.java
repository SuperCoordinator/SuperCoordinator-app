package failures.newVersion;

import models.SFEI.SFEI;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class failure2 {

    public enum type {
        BREAKDOWN,
        BREAKDOWN_WITH_REPAIR,
        PRODUCE_FAULTY,
        PRODUCE_MORE
    }

    private final type type;

    private final utils utility;

    public failure2(failure2.type type) {
        this.type = type;

        this.utility = new utils();

    }

    public failure2.type getType() {
        return type;
    }

    public utils getUtility() {
        return utility;
    }

    public int[] getLastFailureOccurrence(SFEI sfei) {
        int[] ret = new int[]{0, 0, 0};
        ret[0] = getLastFailureOccurrenceByVariable(failure_occurrence.activationVariable.N, sfei);
        ret[1] = getLastFailureOccurrenceByVariable(failure_occurrence.activationVariable.A, sfei);
        ret[2] = getLastFailureOccurrenceByVariable(failure_occurrence.activationVariable.M, sfei);
//        if (ret[0] > 0 || ret[1] > 0 || ret[2] > 0)
//            System.out.println(Arrays.toString(ret));
        return ret;
    }

    private int getLastFailureOccurrenceByVariable(failure_occurrence.activationVariable actVar, SFEI sfei) {
        if (sfei.getFailuresHistory().size() == 0)
            return 0;

        int ret_value = 0;

        for (Map.Entry<Integer, failure_occurrence> fOcurr : sfei.getFailuresHistory().entrySet()) {

            if (fOcurr.getValue().getActVariable().equals(actVar)) {

                switch (actVar) {
                    case N -> {
                        if (fOcurr.getValue().getnPartsMoved_at_time() > ret_value)
                            ret_value = fOcurr.getValue().getnPartsMoved_at_time();
                    }
                    case A -> {
                        if (ret_value == 0) {
                            ret_value = (int) Duration.between(fOcurr.getValue().getStart_t(), Instant.now()).toMinutes();
                        } else if (Duration.between(fOcurr.getValue().getStart_t(), Instant.now()).toMinutes() < ret_value) {
                            ret_value = (int) Duration.between(fOcurr.getValue().getStart_t(), Instant.now()).toMinutes();
                        }
                    }
                    case M -> {
                        if (fOcurr.getValue().getEnd_t() != null) {

                            if (ret_value == 0) {
                                ret_value = (int) Duration.between(fOcurr.getValue().getStart_t(), Instant.now()).toMinutes();
                            } else if (Duration.between(fOcurr.getValue().getEnd_t(), Instant.now()).toMinutes() < ret_value) {
                                ret_value = (int) Duration.between(fOcurr.getValue().getStart_t(), Instant.now()).toMinutes();
                            }
                        }
                    }
                }
            }
        }

        return ret_value;
    }

}