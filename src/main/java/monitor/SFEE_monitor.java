package monitor;

import communication.modbus;
import models.SFEE;
import models.SFEI.SFEI;
import models.part;
import utils.utils;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SFEE_monitor implements Runnable {

    private SFEE sfee;
    private final utils utility;

    public SFEE_monitor(SFEE sfee) {
        this.sfee = sfee;
        createSFEI_monitor();
        this.utility = new utils();
    }

    private void createSFEI_monitor() {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(sfee.getSFEIs().size());

        for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
            scheduler.scheduleAtFixedRate(new SFEI_monitor(sfei.getValue(), sfee.getMb()), 0, 100, TimeUnit.MILLISECONDS);
        }
//        ?????????????????????
//        scheduler.shutdown();
    }

    // PROBLEM: COMUNICAÇÃO ENTRE THREADS
    // PRINCIPALMENTE na parte do SHIFT das peças

    @Override
    public void run() {
        try {
            modbus mb = sfee.getMb();
            synchronized (mb) {
                // Check pieces in the SFEE start -> create part to for tracking
                //      could also write in the piece on F_IO by the RFID sensor
                inSensorTrigger();
                // Shift pieces between SFEI
                shiftParts();
                // Check pieces in the SFEE end -> change part produced attribute
                outSensorTrigger();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean old_inSensor = false;

    private void inSensorTrigger() {

        boolean b_inSensor = Boolean.parseBoolean(sfee.getMb().readState(sfee.getInSensor()));
        if (utility.getLogicalOperator().RE_detector(b_inSensor, old_inSensor)) {
           sfee.getSFEIbyIndex(0).getPartsATM().add(new part(0,"Blue base"));
        }
        old_inSensor = b_inSensor;

    }

    private void shiftParts() {

    }

    private void outSensorTrigger() {

    }


}
