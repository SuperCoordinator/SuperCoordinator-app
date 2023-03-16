/*
package monitor;

import communication.modbus;
import models.sensor_actuator;
import models.SFEI.SFEI;
import utils.utils;

import java.util.TreeMap;

public class SFEI_monitor implements Runnable {

    private final SFEI sfei;

    private final utils utility;
    private String[] iBits;

    private TreeMap<String, sensor_actuator> io;

    public SFEI_monitor(SFEI sfei, String[] iBits) {
        this.sfei = sfei;
        this.iBits = iBits;
        this.utility = new utils();
        io = new TreeMap<>();
    }

    public void setiBits(String[] iBits) {
        this.iBits = iBits;
    }

    @Override
    public void run() {
        try {
            synchronized (iBits) {
                // Just look for the IN/OUT sensors and stamp to the part its progress
                inSensorTrigger();
                outSensorTrigger();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean old_inSensor = false;

    public void inSensorTrigger() {

        boolean b_inSensor = Boolean.parseBoolean(iBits[sfei.getInSensor().bit_offset()]);
        if (utility.getLogicalOperator().RE_detector(b_inSensor, old_inSensor)) {
            if (sfei.getPartsATM().size() > 0)
                sfei.getPartsATM().last().addTimestamp(sfei.getName() + "-" + sfei.getInSensor().name());
           */
/* else
                System.out.println(Thread.currentThread().getName() + " " + sfei.getName());*//*

        }
        old_inSensor = b_inSensor;
    }

    private boolean old_outSensor = false;

    public void outSensorTrigger() {

        boolean b_outSensor = Boolean.parseBoolean(iBits[sfei.getOutSensor().bit_offset()]);
        if (utility.getLogicalOperator().RE_detector(b_outSensor, old_outSensor)) {
            if (sfei.getPartsATM().size() > 0) {
                sfei.getPartsATM().last().addTimestamp(sfei.getName() + "-" + sfei.getOutSensor().name());
                sfei.setnPiecesMoved(sfei.getnPiecesMoved() + 1);
            }
        }
        old_outSensor = b_outSensor;
    }

}
*/
