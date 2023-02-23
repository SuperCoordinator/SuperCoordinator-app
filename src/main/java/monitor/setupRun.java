package monitor;

import communication.modbus;
import models.SFEI.SFEI;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;

public class setupRun implements Callable {

    private final SFEI sfei;
    private final modbus mb;
    private final utils utility;

    public setupRun(SFEI sfei, modbus mb) {
        this.sfei = sfei;
        this.mb = mb;
        this.utility = new utils();
    }

    @Override
    public Long call() {
        System.out.println(sfei.getName() + " " + Thread.currentThread().getName());
        try {
            while (true) {
                sStartTrigger();
                if (sEndTrigger())
                    break;
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return sfei.getMinOperationTime();
    }

    private Instant start_t = null;
    private boolean old_sStart = false;

    private void sStartTrigger() {
        boolean sStart = Boolean.parseBoolean(mb.readState(sfei.getInSensor()));
        if (utility.getLogicalOperator().RE_detector(sStart, old_sStart)) {
            if (start_t == null) {
                start_t = Instant.now();
            }
        }
        old_sStart = sStart;
    }

    private boolean old_sEnd = false;

    private boolean sEndTrigger() {
        boolean sEnd = Boolean.parseBoolean(mb.readState(sfei.getOutSensor()));
        if (utility.getLogicalOperator().RE_detector(sEnd, old_sEnd)) {
            if (start_t != null) {

                Instant end_t = Instant.now();
                sfei.setMinOperationTime(Duration.between(start_t, end_t).toSeconds());
                return true;
            }
        }
        old_sEnd = sEnd;
        return false;
    }

}
