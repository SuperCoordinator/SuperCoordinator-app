package monitor.production;

import models.base.SFEE;
import models.base.SFEI;
import models.SFEx_particular.SFEI_conveyor;
import models.SFEx_particular.SFEI_machine;
import models.base.part;
import models.partsAspect;
import models.sensor_actuator;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class SFEE_production_monitor {

    private final SFEE sfee;
    private final utils utility;
    private final boolean[] SFEIs_old_inSensors;
    private final boolean[] SFEIs_old_outSensors;

    private TreeMap<Integer, sensor_actuator> visionSensorLocation;

    public SFEE_production_monitor(SFEE sfee) {
        this.sfee = sfee;
        this.utility = new utils();
        this.SFEIs_old_inSensors = new boolean[sfee.getSFEIs().size()];
        this.SFEIs_old_outSensors = new boolean[sfee.getSFEIs().size()];

        extractPartsType();
    }

    public void setVisionSensorLocation(TreeMap<Integer, sensor_actuator> visionSensorLocation) {
        this.visionSensorLocation = visionSensorLocation;
    }

    private void init_oldSensorsValues(List<Object> sensorsState) {

        for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {

            sensor_actuator sfei_inSensor = sfei.getValue().getInSensor();
            sensor_actuator sfei_outSensor = sfei.getValue().getOutSensor();

            boolean b_inSensor = (int) sensorsState.get(sfei_inSensor.bit_offset()) == 1;
            boolean b_outSensor = (int) sensorsState.get(sfei_outSensor.bit_offset()) == 1;
            SFEIs_old_inSensors[sfei.getKey()] = b_inSensor;
            SFEIs_old_outSensors[sfei.getKey()] = b_outSensor;
        }
    }

    private partsAspect.form default_partForm;

    private void extractPartsType() {
        // GET SFEI_MACHINE for get the type of part to be produced
        // default -> LID
        default_partForm = partsAspect.form.LID;

        if (sfee.getSfeeFunction().equals(SFEE.SFEE_function.PRODUCTION)) {
            for (Map.Entry<Integer, SFEI> sfeiEntry : sfee.getSFEIs().entrySet())
                if (sfeiEntry.getValue().getSfeiType().equals(SFEI.SFEI_type.MACHINE)) {
                    SFEI_machine temp = (SFEI_machine) sfeiEntry.getValue();
                    default_partForm = temp.getPartForm();
                    break;
                }
        }
    }

    private int pieceCnt = 0;

    private boolean setup_run = true;

    public void loop(List<Object> sensorsState, List<Object> inputRegsValue, List<Object> actuatorsState) {
        try {
            if (setup_run) {
                init_oldSensorsValues(sensorsState);
                updateSFEI_machinePartType(actuatorsState);
                setup_run = false;
            }

            monitorPartsMovements(sensorsState);
            if (visionSensorLocation != null)
                validateProducedParts(sensorsState, inputRegsValue);
            printDBG();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void monitorPartsMovements(List<Object> sensorsState) {
        for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {

            sensor_actuator sfei_inSensor = sfei.getValue().getInSensor();
            sensor_actuator sfei_outSensor = sfei.getValue().getOutSensor();

            boolean b_inSensor = (int) sensorsState.get(sfei_inSensor.bit_offset()) == 1;
            boolean b_outSensor = (int) sensorsState.get(sfei_outSensor.bit_offset()) == 1;

            // SFEE entry, should create new part object
            if (sfei.getKey() == 0) {

                boolean sfee_inSensor = (int) sensorsState.get(sfee.getInSensor().bit_offset()) == 1;
                if (utility.getLogicalOperator().RE_detector(sfee_inSensor, SFEIs_old_inSensors[sfei.getKey()])) {

                    int id = 0;
                    if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {
                        if (sfee.getSFEIbyIndex(0).getPartsATM().last().getId() >= sfee.getSFEIbyIndex(0).getnPiecesMoved()) {
                            id = sfee.getSFEIbyIndex(0).getPartsATM().last().getId() + 1;
                        }
                    } else
                        id = sfee.getSFEIbyIndex(0).getnPiecesMoved();

                    if (sfei.getValue().isLine_start()) {
                        part p = new part(id, new partsAspect(partsAspect.material.BLUE, default_partForm));
                        // This operation of concat is faster than + operation
                        String itemName = sfei.getValue().getName();
                        itemName = itemName.concat("-");
                        itemName = itemName.concat(sfee.getInSensor().name());

                        p.addTimestamp(itemName);
                        sfee.getSFEIbyIndex(0).addNewPartATM(p);
                        pieceCnt++;
                    } else {
                        // Is not the production line start, so wait for the transport bring the part
                        if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {
                            part p = sfee.getSFEIbyIndex(0).getPartsATM().last();
                            String itemName = sfei.getValue().getName();
                            itemName = itemName.concat("-");
                            itemName = itemName.concat(sfee.getInSensor().name());

                            p.addTimestamp(itemName);
                        }
                    }

                }
            }

            // Only register on the end (end of item[i-1] = start of item[i])
            // If SFEIs outSensor RE, then timestamp that event
            if (utility.getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei.getKey()])) {
                if (sfei.getValue().getPartsATM().size() > 0) {
                    // This operation of concat is faster than + operation
                    String itemName = sfei.getValue().getName();
                    itemName = itemName.concat("-");
                    itemName = itemName.concat(sfei_outSensor.name());

                    if (sfei.getKey() == sfee.getSFEIs().size() - 1)
                        sfei.getValue().getPartsATM().last().addTimestamp(itemName);
                    else
                        sfei.getValue().getPartsATM().first().addTimestamp(itemName);

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
                boolean sfee_outSensor = (int) sensorsState.get(sfee.getOutSensor().bit_offset()) == 1;
                if (utility.getLogicalOperator().RE_detector(sfee_outSensor, SFEIs_old_outSensors[sfei.getKey()])) {

                    if (sfei.getValue().getPartsATM().size() > 0) {
                        if (sfei.getValue().isLine_end()) {
                            sfei.getValue().getPartsATM().last().setWaitTransport(false);
                        } else {
                            sfei.getValue().getPartsATM().last().setWaitTransport(true);
                        }
                        // In both cases set produced TRUE
                        // In that way the SFEM_monitor will put the part in their statistic
                        // But as is not the end_line the transport will be done
                        sfei.getValue().getPartsATM().last().setProduced(true);
                    }
                    if (sfei.getValue().getName().equals("entry_conveyor2")) {
                        part p = sfei.getValue().getPartsATM().last();
                        System.out.println("part ID: " + p.getId() + " exp: " + p.getExpectation() + " real: " + p.getReality());
                    }
                }
            }

            // Only update in the end in order to all functions see the values at the read moment
            SFEIs_old_inSensors[sfei.getKey()] = b_inSensor;
            SFEIs_old_outSensors[sfei.getKey()] = b_outSensor;

        }
    }

    private void updateSFEI_machinePartType(List<Object> actuatorsState) {
        for (Map.Entry<Integer, SFEI> sfeiEntry : sfee.getSFEIs().entrySet()) {

            if (sfeiEntry.getValue().getSfeiType().equals(SFEI.SFEI_type.MACHINE)) {

                SFEI_machine sfei = (SFEI_machine) sfeiEntry.getValue();

                if (default_partForm.equals(partsAspect.form.LID)) {
                    actuatorsState.set(sfei.getaProduce().bit_offset(), 1);
                } else if (default_partForm.equals(partsAspect.form.BASE)) {
                    actuatorsState.set(sfei.getaProduce().bit_offset(), 0);
                }
            }
        }
    }

    private void validateProducedParts(List<Object> sensorsState, List<Object> inputRegsValue) {

        for (Map.Entry<Integer, sensor_actuator> entry : visionSensorLocation.entrySet()) {
            if (sfee.getSFEIbyIndex(entry.getKey()).getSfeiType().equals(SFEI.SFEI_type.CONVEYOR)) {

                SFEI_conveyor sfeiConveyor = (SFEI_conveyor) sfee.getSFEIbyIndex(entry.getKey());
                // Verify if there is part in vision sensor range
                int visionSensor_number = (int) inputRegsValue.get(entry.getValue().bit_offset());

                if (visionSensor_number > 0) {
                    if (sfeiConveyor.getPartsATM().size() > 0) {
                        partsAspect reality = getPartsAspectByNumber(visionSensor_number);
                        part lastPart = sfeiConveyor.getPartsATM().last();
                        if (!lastPart.getExpectation().equals(reality)) {
                            lastPart.setDefect();
                        }
                        lastPart.setReality(reality);
                    }
                }
            } else {
                throw new RuntimeException("The vision sensor should be placed on the conveyors");
            }


        }
    }

    private partsAspect getPartsAspectByNumber(int num) {

        if (num == 0 || num > 9)
            return null;
        partsAspect.material mat;
        if (num > 0 && num <= 3) {
            mat = partsAspect.material.BLUE;
        } else if (num > 3 && num <= 6) {
            mat = partsAspect.material.GREEN;
        } else {
            mat = partsAspect.material.METAL;
        }
        partsAspect.form f;
        if (num % 3 == 1)
            f = partsAspect.form.RAW;
        else if (num % 3 == 2) {
            f = partsAspect.form.LID;
        } else {
            f = partsAspect.form.BASE;
        }

        return new partsAspect(mat, f);

    }

    private boolean printedDBG = false;

    private void printDBG() {
        if (Duration.between(sfee.getSFEIbyIndex(0).getDayOfBirth(), Instant.now()).toSeconds() % 5 == 0) {
            if (!printedDBG) {
                for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
                    System.out.println("(" + sfei.getKey() + ") " + sfei.getValue().getName() + " moved: " + sfei.getValue().getnPiecesMoved() + " parts");
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
