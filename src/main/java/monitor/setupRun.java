package monitor;

import communication.modbus;
import models.base.SFEI;
import utility.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;

public class setupRun implements Callable {

    private final SFEI sfei;
    private final modbus mb;

    public setupRun(SFEI sfei, modbus mb) {
        this.sfei = sfei;
        this.mb = mb;

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
//        boolean sStart = Boolean.parseBoolean(mb.readState(sfei.getInSensor()));
        //int obj = (int) mb.readState(sfei.getInSensor()).get(0);
        int obj = (int) mb.readCoils().get(sfei.getInSensor().getOffset());
        boolean sStart = (obj == 1);

        if (utils.getInstance().getLogicalOperator().RE_detector(sStart, old_sStart)) {
            if (start_t == null) {
                start_t = Instant.now();
            }
        }
        old_sStart = sStart;
    }

    private boolean old_sEnd = false;

    private boolean sEndTrigger() {
//        boolean sEnd = Boolean.parseBoolean(mb.readState(sfei.getOutSensor()));
//        int obj = (int) mb.readState(sfei.getOutSensor()).get(0);
        int obj = (int) mb.readCoils().get(sfei.getOutSensor().getOffset());
        boolean sEnd = (obj == 1);
        if (utils.getInstance().getLogicalOperator().RE_detector(sEnd, old_sEnd)) {
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
