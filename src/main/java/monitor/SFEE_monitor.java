package monitor;

import communication.modbus;
import models.SFEE;
import models.SFEI.SFEI;
import models.part;
import models.sensor_actuator;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class SFEE_monitor {

    private final SFEE sfee;
    private final utils utility;
    private final boolean[] SFEIs_old_inSensors;
    private final boolean[] SFEIs_old_outSensors;

    public SFEE_monitor(SFEE sfee, String sensorsState) {
        this.sfee = sfee;
        this.utility = new utils();
        this.SFEIs_old_inSensors = new boolean[sfee.getSFEIs().size()];
        this.SFEIs_old_outSensors = new boolean[sfee.getSFEIs().size()];

        init_oldSensorsValues(sensorsState);
    }

    private void init_oldSensorsValues(String sensorsState) {
//        String sensorsState = mb.readMultipleInputs(sfee.getIo());
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

    public void loop(String sensorsState) {
        try {
            synchronized (sfee) {
//                String sensorsState = mb.readMultipleInputs(sfee.getIo());
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
                printDBG();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean printedDBG = false;

    private void printDBG() {
        if (Duration.between(sfee.getSFEIbyIndex(0).getDayOfBirth(), Instant.now()).toSeconds() % 5 == 0) {
            if (!printedDBG) {
                System.out.println("Number of running Threads: " + Thread.activeCount());
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
