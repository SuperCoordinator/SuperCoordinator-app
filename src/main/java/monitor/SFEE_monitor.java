package monitor;

import communication.modbus;
import models.SFEE;
import models.SFEI.SFEI;
import models.part;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SFEE_monitor implements Runnable {

    private final SFEE sfee;
    private final utils utility;

    private final boolean[] SFEI_old_outSensors;
    private String[] iBits;

    public SFEE_monitor(SFEE sfee) {
        this.sfee = sfee;
        this.utility = new utils();
        this.SFEI_old_outSensors = new boolean[sfee.getSFEIs().size()];
        Arrays.fill(SFEI_old_outSensors, false);
//        createSFEI_monitor();
    }

    ArrayList<SFEI_monitor> SFEI_monitors = new ArrayList<>();

    private void createSFEI_monitor() {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(sfee.getSFEIs().size());

        for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
            SFEI_monitors.add(sfei.getKey(), new SFEI_monitor(sfei.getValue(), iBits));
            scheduler.scheduleAtFixedRate(SFEI_monitors.get(sfei.getKey()), 0, 50, TimeUnit.MILLISECONDS);
        }
//        ?????????????????????
//        scheduler.shutdown();
    }

    // PROBLEM: COMUNICAÇÃO ENTRE THREADS
    // PRINCIPALMENTE na parte do SHIFT das peças

    private boolean first_exe = true;

    @Override
    public void run() {
        try {
            modbus mb = sfee.getMb();
            synchronized (mb) {

                String sensorsState = mb.readMultipleRegisters(sfee.getIo());
                iBits = sensorsState.split(" ");

                if (first_exe) {
                    createSFEI_monitor();
                    first_exe = false;
                } else {
                    for (SFEI_monitor sfeiMonitor : SFEI_monitors) {
                        sfeiMonitor.setiBits(iBits);
                    }
                }
                // Check pieces in the SFEE start -> create part to for tracking
                //      could also write in the piece on F_IO by the RFID sensor
                inSensorTrigger();

                // Shift pieces between SFEI
                shiftParts();

                // Check pieces in the SFEE end -> change part produced attribute
                outSensorTrigger();


                printDBG();
                /* <-- The order is important to not skip parts that just entered the start SFEI (w/ the emitter) */

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean old_inSensor = false;
    private int pieceCnt = 0;

    private void inSensorTrigger() {

        boolean b_inSensor = Boolean.parseBoolean(iBits[sfee.getInSensor().bit_offset()]);
//        System.out.println("inSensorTrigger: " + b_inSensor + " bit: " + iBits[sfee.getInSensor().bit_offset()]);
        if (utility.getLogicalOperator().RE_detector(b_inSensor, old_inSensor)) {
            sfee.getSFEIbyIndex(0).addNewPartATM(new part(pieceCnt, "Blue base"));
            pieceCnt++;
        }
        old_inSensor = b_inSensor;

    }


    private void shiftParts() {
        // For loop looking for the SFEI outSensor
        // if it RE, then move the part for the next SFEI
        for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {

            boolean b_outSensor = Boolean.parseBoolean(iBits[sfei.getValue().getOutSensor().bit_offset()]);
            if (utility.getLogicalOperator().RE_detector(b_outSensor, SFEI_old_outSensors[sfei.getKey()])) {
                if (sfei.getKey() + 1 < sfee.getSFEIs().size()) {
                    // Then it is not in the last SFEI, so move the piece!
                    // remove from the previous
                    if (sfei.getValue().getPartsATM().size() > 0) {
                        part p = sfei.getValue().getPartsATM().pollFirst();
                        sfee.getSFEIbyIndex(sfei.getKey() + 1).addNewPartATM(p);
                    }
                }
            }

            SFEI_old_outSensors[sfei.getKey()] = b_outSensor;
        }

    }

    private boolean old_outSensor = false;

    private void outSensorTrigger() {
        boolean b_outSensor = Boolean.parseBoolean(iBits[sfee.getOutSensor().bit_offset()]);
        if (utility.getLogicalOperator().RE_detector(b_outSensor, old_outSensor)) {
            if (sfee.getSFEIbyIndex(sfee.getSFEIs().size() - 1).getPartsATM().size() > 0)
                sfee.getSFEIbyIndex(sfee.getSFEIs().size() - 1).getPartsATM().last().setProduced();
        }
        old_outSensor = b_outSensor;
    }

    private boolean printedDBG = false;

    private void printDBG() {
        if (Duration.between(sfee.getSFEIbyIndex(0).getDayOfBirth(), Instant.now()).toSeconds() % 5 == 0) {
            if (!printedDBG) {
                for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
                    System.out.println("(" + sfei.getKey() + ") " + sfei.getValue().getName());
                    Iterator<part> itr = sfei.getValue().getPartsATM().descendingIterator();
                    while (itr.hasNext()) {
                        part p = itr.next();
                        System.out.print("    " + p.getId() + " ");
                        p.getTimestamps().forEach((key, value) -> {
                            System.out.println(key + " " + value.toString());
                        });


                    }
                    System.out.println();
                }
                printedDBG = true;
            }
        } else {
            printedDBG = false;
        }

    }


}
