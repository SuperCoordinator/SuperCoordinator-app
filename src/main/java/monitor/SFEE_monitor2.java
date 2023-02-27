package monitor;

import communication.modbus;
import models.SFEE;
import models.SFEI.SFEI;
import models.part;
import models.sensor_actuator;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SFEE_monitor2 implements Runnable {

    private final SFEE sfee;
    private final utils utility;
    private final boolean[] SFEIs_old_inSensors;
    private final boolean[] SFEIs_old_outSensors;
    private final modbus mb;

    public SFEE_monitor2(SFEE sfee) {
        this.sfee = sfee;
        this.utility = new utils();
        this.SFEIs_old_inSensors = new boolean[sfee.getSFEIs().size()];
        this.SFEIs_old_outSensors = new boolean[sfee.getSFEIs().size()];
        this.mb = sfee.getMb();

        init_oldSensorsValues();
    }

    public synchronized SFEE getSFEE() {
        return sfee;
    }

    private void init_oldSensorsValues() {
        String sensorsState = mb.readMultipleRegisters(sfee.getIo());
        String[] iBits = sensorsState.split(" ");

        for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
            sensor_actuator sfei_inSensor = sfei.getValue().getInSensor();
            sensor_actuator sfei_outSensor = sfei.getValue().getOutSensor();
            boolean b_inSensor = Boolean.parseBoolean(iBits[sfei_inSensor.bit_offset()]);
            boolean b_outSensor = Boolean.parseBoolean(iBits[sfei_outSensor.bit_offset()]);
            SFEIs_old_inSensors[sfei.getKey()] = b_inSensor;
            SFEIs_old_outSensors[sfei.getKey()] = b_outSensor;
        }
    }

    private int pieceCnt = 0;

    @Override
    public void run() {
        try {
            synchronized (sfee) {
                String sensorsState = mb.readMultipleRegisters(sfee.getIo());
                String[] iBits = sensorsState.split(" ");

                for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {

                    sensor_actuator sfei_inSensor = sfei.getValue().getInSensor();
                    sensor_actuator sfei_outSensor = sfei.getValue().getOutSensor();

                    boolean b_inSensor = Boolean.parseBoolean(iBits[sfei_inSensor.bit_offset()]);
                    boolean b_outSensor = Boolean.parseBoolean(iBits[sfei_outSensor.bit_offset()]);

                    // SFEE entry, should create new part object
                    if (sfei.getKey() == 0) {
                        boolean sfee_inSensor = Boolean.parseBoolean(iBits[sfee.getInSensor().bit_offset()]);
                        if (utility.getLogicalOperator().RE_detector(sfee_inSensor, SFEIs_old_inSensors[sfei.getKey()])) {
                            part p = new part(pieceCnt, "Blue base");
                            p.addTimestamp(sfei.getValue().getName() + "-" + sfee.getInSensor().name());
                            sfee.getSFEIbyIndex(0).addNewPartATM(p);
                            pieceCnt++;
                        }
                    }

/*                    // If SFEIs inSensor RE, then timestamp that event
                    if (utility.getLogicalOperator().RE_detector(b_inSensor, SFEIs_old_inSensors[sfei.getKey()])) {
                        if (sfei.getValue().getPartsATM().size() > 0)
                            sfei.getValue().getPartsATM().last().addTimestamp(sfei.getValue().getName() + "-" + sfei_inSensor.name());
                    }*/

                    // Only register on the end (end of item[i-1] = start of item[i])
                    // If SFEIs outSensor RE, then timestamp that event
                    if (utility.getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei.getKey()])) {
                        if (sfei.getValue().getPartsATM().size() > 0) {
                            if (sfei.getKey() == sfee.getSFEIs().size() - 1)
                                sfei.getValue().getPartsATM().last().addTimestamp(sfei.getValue().getName() + "-" + sfei_outSensor.name());
                            else
                                sfei.getValue().getPartsATM().first().addTimestamp(sfei.getValue().getName() + "-" + sfei_outSensor.name());
                            sfei.getValue().setnPiecesMoved(sfei.getValue().getnPiecesMoved() + 1);
                        }
                    }

                    // Shift the part among SFEIs
                    if (utility.getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei.getKey()])) {
                        if (sfei.getKey() + 1 < sfee.getSFEIs().size()) {
                            // Then it is not in the last SFEI, so move the piece!
                            // remove from the previous
                            if (sfei.getValue().getPartsATM().size() > 0) {
                                part p = sfei.getValue().getPartsATM().pollFirst();
                                sfee.getSFEIbyIndex(sfei.getKey() + 1).addNewPartATM(p);
                            }
                        }
                    }

                    // End of the SFEE, set part produced flag to TRUE
                    if (sfei.getKey() == sfee.getSFEIs().size() - 1) {
                        boolean sfee_outSensor = Boolean.parseBoolean(iBits[sfee.getOutSensor().bit_offset()]);
                        if (utility.getLogicalOperator().RE_detector(sfee_outSensor, SFEIs_old_outSensors[sfei.getKey()])) {
                            if (sfei.getValue().getPartsATM().size() > 0) {
                                sfei.getValue().getPartsATM().last().setProduced();
                            }
                        }
                    }

                    // Only update in the end in order to all functions see the values at the read moment
                    SFEIs_old_inSensors[sfei.getKey()] = b_inSensor;
                    SFEIs_old_outSensors[sfei.getKey()] = b_outSensor;

                }

/*                // Check pieces in the SFEE start -> create part to for tracking
                //      could also write in the piece on F_IO by the RFID sensor
                inSensorTrigger();

                // Shift pieces between SFEI
                shiftParts();

                // Check pieces in the SFEE end -> change part produced attribute
                outSensorTrigger();
*/

                printDBG();

                /* <-- The order is important to not skip parts that just entered the start SFEI (w/ the emitter) */

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*

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
            if (utility.getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei.getKey()])) {
                if (sfei.getKey() + 1 < sfee.getSFEIs().size()) {
                    // Then it is not in the last SFEI, so move the piece!
                    // remove from the previous
                    if (sfei.getValue().getPartsATM().size() > 0) {
                        part p = sfei.getValue().getPartsATM().pollFirst();
                        sfee.getSFEIbyIndex(sfei.getKey() + 1).addNewPartATM(p);
                    }
                }
            }

            SFEIs_old_outSensors[sfei.getKey()] = b_outSensor;
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
*/

    private boolean printedDBG = false;

    private void printDBG() {
        if (Duration.between(sfee.getSFEIbyIndex(0).getDayOfBirth(), Instant.now()).toSeconds() % 5 == 0) {
            if (!printedDBG) {
                for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
                    System.out.println("(" + sfei.getKey() + ") " + sfei.getValue().getName());
                    for (part p : sfei.getValue().getPartsATM()) {
                        System.out.println("  part ID:" + p.getId());
                        p.getTimestamps().forEach((key, value) -> {
                            System.out.println("  -> " + key + " " + value.toString());
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
