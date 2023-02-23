package monitor;

import communication.modbus;
import models.SFEI.SFEI;
import utils.utils;

public class SFEI_monitor implements Runnable {

    private final SFEI sfei;
    private final modbus mb;
    private final utils utility;

    public SFEI_monitor(SFEI sfei, modbus mb) {
        this.sfei = sfei;
        this.mb = mb;
        this.utility = new utils();
    }

    @Override
    public void run() {
        try {
            synchronized (mb) {
                // Just look for the IN/OUT sensors and stamp to the part the progress
                inSensorTrigger();
                outSensorTrigger();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean old_inSensor = false;

    public void inSensorTrigger() {

        boolean b_inSensor = Boolean.parseBoolean(mb.readState(sfei.getInSensor()));
        if (utility.getLogicalOperator().RE_detector(b_inSensor, old_inSensor)) {
            if (sfei.getPartsATM().last() != null)
                sfei.getPartsATM().last().addTimestamp(sfei.getName() + "-" + sfei.getInSensor());
            else
                System.out.println(Thread.currentThread().getName() + " " + sfei.getName() + " " + sfei.getPartsATM().toString());
        }
        old_inSensor = b_inSensor;
    }

    private boolean old_outSensor = false;

    public void outSensorTrigger() {

        boolean b_outSensor = Boolean.parseBoolean(mb.readState(sfei.getOutSensor()));
        if (utility.getLogicalOperator().RE_detector(b_outSensor, old_outSensor)) {
            if (sfei.getPartsATM().last() != null)
                sfei.getPartsATM().last().addTimestamp(sfei.getName() + "-" + sfei.getOutSensor());
        }
        old_outSensor = b_outSensor;
    }

}
