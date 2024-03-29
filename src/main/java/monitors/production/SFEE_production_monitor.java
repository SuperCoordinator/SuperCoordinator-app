package monitors.production;

import communication.database.dbConnection;
import models.base.SFEE;
import models.base.SFEI;
import models.base.SFE_role;
import models.sfe_x.SFEI_conveyor;
import models.sfe_x.SFEI_machine;
import models.base.part;
import models.partDescription;
import models.sensor_actuator;
import utility.utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEE_production_monitor {


    private SFEE sfee;
    private boolean[] SFEIs_old_inSensors;
    private boolean[] SFEIs_old_outSensors;

    @XmlElement
    private TreeMap<Integer, sensor_actuator> visionSensorLocation;

    private partDescription.form default_partForm;
    private boolean printedDBG = false;

    public SFEE_production_monitor() {
    }

    public SFEE_production_monitor(SFEE sfee) {
        this.sfee = sfee;
    }

    public void setSfee(SFEE sfee) {
        this.sfee = sfee;
    }

    public void setVisionSensorLocation(TreeMap<Integer, sensor_actuator> visionSensorLocation) {
        this.visionSensorLocation = visionSensorLocation;
    }

    private void init_oldSensorsValues(List<Object> sensorsState) {

        try {
            for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {

                sensor_actuator sfei_inSensor = sfei.getValue().getInSensor();
                sensor_actuator sfei_outSensor = sfei.getValue().getOutSensor();

                boolean b_inSensor = (int) sensorsState.get(sfei_inSensor.getOffset()) == 1;
                boolean b_outSensor = (int) sensorsState.get(sfei_outSensor.getOffset()) == 1;

                SFEIs_old_inSensors[sfei.getKey()] = b_inSensor;
                SFEIs_old_outSensors[sfei.getKey()] = b_outSensor;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void extractPartsType() {

        try {
            this.SFEIs_old_inSensors = new boolean[sfee.getSFEIs().size()];
            this.SFEIs_old_outSensors = new boolean[sfee.getSFEIs().size()];

            // GET SFEI_MACHINE for get the type of M_part to be produced
            // default -> LID
            default_partForm = partDescription.form.LID;

            if (sfee.getSFEE_role().equals(SFE_role.PRODUCTION)) {
                for (Map.Entry<Integer, SFEI> sfeiEntry : sfee.getSFEIs().entrySet())
                    if (sfeiEntry.getValue().getSfeiType().equals(SFEI.SFEI_type.MACHINE)) {
                        SFEI_machine temp = (SFEI_machine) sfeiEntry.getValue();
                        default_partForm = temp.getPartDescription().form();
                        break;
                    }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private boolean firstRun = true;

    public void loop(List<Object> sensorsState, List<Object> inputRegsValue, List<Object> actuatorsState) {
        try {
            if (firstRun) {
                extractPartsType();
                init_oldSensorsValues(sensorsState);
                updateSFEI_machinePartType(actuatorsState);
                firstRun = false;
            }
            switch (sfee.getSfeeEnvironment()) {
                case SIMULATION -> simulationProductionMonitor(sensorsState);
                case REAL -> realProductionMonitor(sensorsState);
            }

            if (visionSensorLocation != null)
                updatePartDescription(inputRegsValue);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void simulationProductionMonitor(List<Object> sensorsState) {

        try {
            boolean isLineStart = false;

            for (int sfei_idx = 0; sfei_idx < sfee.getSFEIs().size(); sfei_idx++) {
                SFEI sfei = sfee.getSFEIbyIndex(sfei_idx);

                sensor_actuator sfei_inSensor = sfei.getInSensor();
                sensor_actuator sfei_outSensor = sfei.getOutSensor();

                boolean b_inSensor = (int) sensorsState.get(sfei_inSensor.getOffset()) == 1;
                boolean b_outSensor = (int) sensorsState.get(sfei_outSensor.getOffset()) == 1;

                if (sfei_idx == 0 && sfei.isLine_start()) {
                    /* First conveyor of the Shop Floor.
                     * All the next SFEIs is remove parts from this one. */
                    isLineStart = true;

                } else if (sfei_idx == 0) {
                    /* It is not the start of the line,
                     * so SFEI with idx == 1 will accept the part from this one. */
                    if (utils.getInstance().getLogicalOperator().RE_detector(b_inSensor, SFEIs_old_inSensors[sfei_idx]) || b_inSensor) {
                        Iterator<part> iterator = sfee.getSFEIbyIndex(sfei_idx).getPartsATM().iterator();
                        if (iterator.hasNext()) {
                            part movingPart = iterator.next();
                            /* DATABASE save -> production_history table */
                            dbConnection.getInstance().getProduction_history().insert(
                                    movingPart.getId(),
                                    sfei.getInSensor().getName(),
                                    movingPart.getReality().material().toString(),
                                    movingPart.getReality().form().toString(),
                                    Instant.now());
                        }
                    }

                    if (utils.getInstance().getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei_idx])) {
                        /* Remove the part from the previous SFEI and place on the next one. */
                        movePart(sfei_idx, sfei_idx + 1, sfei.getOutSensor().getName());
                    }

                } else if (sfei_idx < sfee.getSFEIs().size() - 1) {
                    /* Intermediate SFEIs, only move part between them
                     * Move part to the next SFEI. */

                    if (isLineStart) {
                        if (utils.getInstance().getLogicalOperator().RE_detector(b_inSensor, SFEIs_old_inSensors[sfei_idx])) {
                            /* Remove the part from the first SFEI. */
                            movePart(0, sfei_idx, sfei.getInSensor().getName());
                        }
                        if (utils.getInstance().getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei_idx])) {
                            markPartForTransport(sfei_idx);
                        }
                    } else {
                        if (utils.getInstance().getLogicalOperator().FE_detector(b_outSensor, SFEIs_old_outSensors[sfei_idx])) {
                            /* Remove the part from the previous SFEI and place on the next one. */
                            movePart(sfei_idx, sfei_idx + 1, sfei.getOutSensor().getName());
                        }
                    }

                } else if (sfei_idx == sfee.getSFEIs().size() - 1) {
                    /* This is the last SFEI of this SFEE ?:
                     *      1 - YES. It is line end, so part is PRODUCED (wait for the transport to be shipped)
                     *      2 - NO. Wait for the transport. It will remove the part from SFEI partsATM. */

                    if (utils.getInstance().getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei_idx])) {
                        markPartForTransport(sfei_idx);
                    }
                }
                // Only update in the end in order to all functions see the values at the read moment
                SFEIs_old_inSensors[sfei_idx] = b_inSensor;
                SFEIs_old_outSensors[sfei_idx] = b_outSensor;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void realProductionMonitor(List<Object> sensorsState) {
        try {
            for (int sfei_idx = 0; sfei_idx < sfee.getSFEIs().size(); sfei_idx++) {
                SFEI sfei = sfee.getSFEIbyIndex(sfei_idx);

                sensor_actuator sfei_inSensor = sfei.getInSensor();
                sensor_actuator sfei_outSensor = sfei.getOutSensor();

                boolean b_inSensor = (int) sensorsState.get(sfei_inSensor.getOffset()) == 1;
                boolean b_outSensor = (int) sensorsState.get(sfei_outSensor.getOffset()) == 1;

                if (sfei_idx == 0) {
                    /* Can be or not the startLine,
                     * so SFEI with idx == 1 will accept the part from this one. */
                    if (utils.getInstance().getLogicalOperator().RE_detector(b_inSensor, SFEIs_old_inSensors[sfei_idx]) || b_inSensor) {

                        Iterator<part> iterator = sfee.getSFEIbyIndex(sfei_idx).getPartsATM().iterator();

                        if (iterator.hasNext()) {
                            part movingPart = iterator.next();
                            /* DATABASE save -> production_history table */
                            dbConnection.getInstance().getProduction_history().insert(
                                    movingPart.getId(),
                                    sfei.getInSensor().getName(),
                                    movingPart.getReality().material().toString(),
                                    movingPart.getReality().form().toString(),
                                    Instant.now());
                        }
                    }

                    if (utils.getInstance().getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei_idx])) {
                        /* Remove the part from the previous SFEI and place on the next one. */
                        movePart(sfei_idx, sfei_idx + 1, sfei.getOutSensor().getName());
                    }

                } else if (sfei_idx < sfee.getSFEIs().size() - 1) {
                    /* Intermediate SFEIs, only move part between them
                     * Move part to the next SFEI. */

                    if (utils.getInstance().getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei_idx])) {
                        /* Remove the part from the previous SFEI and place on the next one. */
                        movePart(sfei_idx, sfei_idx + 1, sfei.getOutSensor().getName());
                    }


                } else if (sfei_idx == sfee.getSFEIs().size() - 1) {
                    /* This is the last SFEI of this SFEE ?:
                     *      1 - YES. It is line end, so part is PRODUCED (wait for the transport to be shipped)
                     *      2 - NO. Wait for the transport. It will remove the part from SFEI partsATM. */

                    if (utils.getInstance().getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei_idx])) {
                        markPartForTransport(sfei_idx);
                    }
                }
                // Only update in the end in order to all functions see the values at the read moment
                SFEIs_old_inSensors[sfei_idx] = b_inSensor;
                SFEIs_old_outSensors[sfei_idx] = b_outSensor;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void movePart(int origin_sfei, int destination_sfei, String sensor_name) {

        try {
            if (sfee.getSFEIbyIndex(origin_sfei).getPartsATM().size() > 0) {
                part movingPart = Objects.requireNonNull(sfee.getSFEIbyIndex(origin_sfei).getPartsATM().pollFirst());
                sfee.getSFEIbyIndex(destination_sfei).addNewPartATM(movingPart);


                /* DATABASE save -> production_history table */
                dbConnection.getInstance().getProduction_history().insert(
                        movingPart.getId(),
                        sensor_name,
                        movingPart.getReality().material().toString(),
                        movingPart.getReality().form().toString(),
                        Instant.now());

                /* Increment the number of parts moved by the SFEI sfei_idx */
                sfee.getSFEIbyIndex(origin_sfei).setnPartsMoved(sfee.getSFEIbyIndex(origin_sfei).getnPartsMoved() + 1);

                /* DATABASE update nParts -> sfei table */
                dbConnection.getInstance().getSfeis().update_nMovedParts(
                        sfee.getSFEIbyIndex(origin_sfei).getName(),
                        sfee.getName(),
                        sfee.getSFEIbyIndex(origin_sfei).getnPartsMoved());

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void markPartForTransport(int sfei_idx) {

        try {
            SFEI sfei = sfee.getSFEIbyIndex(sfei_idx);
            if (sfei.getPartsATM().size() > 0) {
                // In the case of Transport, the most recent part that arrives in the out sensor is the last()
                part movingPart = Objects.requireNonNull(sfei.getPartsATM().last());

                movingPart.setState(part.status.WAIT_TRANSPORT);

                /* DATABASE save -> production_history table */
                dbConnection.getInstance().getProduction_history().insert(
                        movingPart.getId(),
                        sfei.getOutSensor().getName(),
                        movingPart.getReality().material().toString(),
                        movingPart.getReality().form().toString(),
                        Instant.now());

                /* Increment the number of parts moved by the SFEI sfei_idx. */
                sfei.setnPartsMoved(sfei.getnPartsMoved() + 1);
                /* DATABASE update nParts -> sfei table */
                dbConnection.getInstance().getSfeis().update_nMovedParts(
                        sfei.getName(),
                        sfee.getName(),
                        sfei.getnPartsMoved());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void updateSFEI_machinePartType(List<Object> actuatorsState) {
        try {
            for (Map.Entry<Integer, SFEI> sfeiEntry : sfee.getSFEIs().entrySet()) {

                if (sfeiEntry.getValue().getSfeiType().equals(SFEI.SFEI_type.MACHINE) && sfee.getSfeeEnvironment().equals(SFEE.SFEE_environment.SIMULATION)) {

                    SFEI_machine sfei = (SFEI_machine) sfeiEntry.getValue();

                    if (default_partForm.equals(partDescription.form.LID)) {
                        actuatorsState.set(sfei.getaProduce().getOffset(), 1);
                    } else if (default_partForm.equals(partDescription.form.BASE)) {
                        actuatorsState.set(sfei.getaProduce().getOffset(), 0);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updatePartDescription(List<Object> inputRegsValue) {

        try {
            for (Map.Entry<Integer, sensor_actuator> entry : visionSensorLocation.entrySet()) {
                if (sfee.getSFEIbyIndex(entry.getKey()).getSfeiType().equals(SFEI.SFEI_type.CONVEYOR)) {

                    SFEI_conveyor sfeiConveyor = (SFEI_conveyor) sfee.getSFEIbyIndex(entry.getKey());
                    // Verify if there is a part in the vision sensor range
                    int visionSensor_number = (int) inputRegsValue.get(entry.getValue().getOffset());

                    if (visionSensor_number > 0) {
                        if (sfeiConveyor.getPartsATM().size() > 0) {
                            partDescription actualDescription = Objects.requireNonNull(getPartsAspectByNumber(visionSensor_number));

                            part p = Objects.requireNonNull(sfeiConveyor.getPartsATM().last());
                            if (!p.getReality().form().equals(actualDescription.form())) {
                                p.setReality(actualDescription);
                            }
                        }
                    }
                } else {
                    throw new RuntimeException("(" + SFEE_production_monitor.class + " )The vision sensor should be placed on the conveyors");
                }


            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private partDescription getPartsAspectByNumber(int num) {

        try {
            if (num == 0 || num > 9)
                return null;
            partDescription.material mat;
            if (num > 0 && num <= 3) {
                mat = partDescription.material.BLUE;
            } else if (num > 3 && num <= 6) {
                mat = partDescription.material.GREEN;
            } else {
                mat = partDescription.material.METAL;
            }
            partDescription.form f;
            if (num % 3 == 1)
                f = partDescription.form.RAW;
            else if (num % 3 == 2) {
                f = partDescription.form.LID;
            } else {
                f = partDescription.form.BASE;
            }

            return new partDescription(mat, f);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void printDBG() {
        try {
            if (Duration.between(sfee.getSFEIbyIndex(0).getDayOfBirth(), Instant.now()).toSeconds() % 5 == 0) {
                if (!printedDBG) {
                    for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
                        if (sfei.getValue().getPartsATM().size() > 0) {
                            System.out.println("(" + sfei.getKey() + ") " + sfei.getValue().getName() + " moved: " + sfei.getValue().getnPartsMoved() + " parts");
                            for (part p : sfei.getValue().getPartsATM()) {
                                System.out.println(p);
//                                p.getTimestamps().forEach((key, value) -> {
//                                    System.out.println("  -> " + key + " " + value.toString());
//                                });
                            }
                            System.out.println();
                        }
                    }
                    printedDBG = true;
                }
            } else {
                printedDBG = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Print Monitor and Tracking information, such as the part position (in which SFEI)
     * and the number of parts moved by the SFEIs
     */
    public void monitorSFEE() {
        try {
            for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
                if (sfei.getValue().getPartsATM().size() > 0) {
                    System.out.println("SFEI [ idx =" + sfei.getKey() + "] " + sfei.getValue().getName() + " moved: " + sfei.getValue().getnPartsMoved() + " parts");
                    for (part p : sfei.getValue().getPartsATM()) {
                        System.out.println(p);
                    }
                    System.out.println();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
