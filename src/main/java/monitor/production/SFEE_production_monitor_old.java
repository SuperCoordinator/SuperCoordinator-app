package monitor.production;

import communication.database.dbConnection;
import models.SFEx.SFEI_conveyor;
import models.SFEx.SFEI_machine;
import models.base.SFEE;
import models.base.SFEI;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEE_production_monitor_old {


    private SFEE sfee;
    private boolean[] SFEIs_old_inSensors;
    private boolean[] SFEIs_old_outSensors;

    @XmlElement
    private TreeMap<Integer, sensor_actuator> visionSensorLocation;

    private partDescription.form default_partForm;
    private boolean printedDBG = false;

    public SFEE_production_monitor_old() {
    }

    public SFEE_production_monitor_old(SFEE sfee) {
        this.sfee = sfee;
    }

    public void setSfee(SFEE sfee) {
        this.sfee = sfee;
    }

    public void setVisionSensorLocation(TreeMap<Integer, sensor_actuator> visionSensorLocation) {
        this.visionSensorLocation = visionSensorLocation;
    }

    private void init_oldSensorsValues(List<Object> sensorsState) {

        for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {

            sensor_actuator sfei_inSensor = sfei.getValue().getInSensor();
            sensor_actuator sfei_outSensor = sfei.getValue().getOutSensor();

            boolean b_inSensor = (int) sensorsState.get(sfei_inSensor.getBit_offset()) == 1;
            boolean b_outSensor = (int) sensorsState.get(sfei_outSensor.getBit_offset()) == 1;
            SFEIs_old_inSensors[sfei.getKey()] = b_inSensor;
            SFEIs_old_outSensors[sfei.getKey()] = b_outSensor;
        }
    }

    private void extractPartsType() {

        this.SFEIs_old_inSensors = new boolean[sfee.getSFEIs().size()];
        this.SFEIs_old_outSensors = new boolean[sfee.getSFEIs().size()];

        // GET SFEI_MACHINE for get the type of M_part to be produced
        // default -> LID
        default_partForm = partDescription.form.LID;

        if (sfee.getSFEE_function().equals(SFEE.SFEE_role.PRODUCTION)) {
            for (Map.Entry<Integer, SFEI> sfeiEntry : sfee.getSFEIs().entrySet())
                if (sfeiEntry.getValue().getSfeiType().equals(SFEI.SFEI_type.MACHINE)) {
                    SFEI_machine temp = (SFEI_machine) sfeiEntry.getValue();
                    default_partForm = temp.getPartDescription().form();
                    break;
                }
        }

    }

    private boolean setup_run = true;

    public void loop(List<Object> sensorsState, List<Object> inputRegsValue, List<Object> actuatorsState) {
        try {
            if (setup_run) {
                extractPartsType();

                init_oldSensorsValues(sensorsState);
                updateSFEI_machinePartType(actuatorsState);
                setup_run = false;
            }

            monitorProduction(sensorsState, inputRegsValue);
            if (visionSensorLocation != null)
                validateProducedParts(sensorsState, inputRegsValue);

            printDBG();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void monitorProduction(List<Object> sensorsState, List<Object> inputRegsValue) {
        try {
            for (int sfei_idx = 0; sfei_idx < sfee.getSFEIs().size(); sfei_idx++) {

                SFEI sfei = sfee.getSFEIbyIndex(sfei_idx);

                sensor_actuator sfei_inSensor = sfei.getInSensor();
                sensor_actuator sfei_outSensor = sfei.getOutSensor();

                boolean b_inSensor = (int) sensorsState.get(sfei_inSensor.getBit_offset()) == 1;
                boolean b_outSensor = (int) sensorsState.get(sfei_outSensor.getBit_offset()) == 1;


                if (sfee.getSFEE_function().equals(SFEE.SFEE_role.SORTING_STATION)) {
                    if (utils.getInstance().getLogicalOperator().RE_detector(b_inSensor, SFEIs_old_inSensors[sfei_idx])) {
                        // New part arriving at specific conveyor -> result of separation

                        // include timestamp of the end of SFEI at index 0
                        // move M_part instance to current SFEI at index idx
                        if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {
                            part p = Objects.requireNonNull(sfee.getSFEIbyIndex(0).getPartsATM().pollFirst());
//                            System.out.println("[" + SFEE_production_monitor.class + "] removed " + p + "and size is : " + sfee.getSFEIbyIndex(0).getPartsATM().size());
                            String itemName = sfei.getName();
                            itemName = itemName.concat("-");
                            itemName = itemName.concat(sfei.getInSensor().getName());

                            p.addTimestamp(itemName);

                            sfei.addNewPartATM(p);
                            sfee.getSFEIbyIndex(0).setnPiecesMoved(sfee.getSFEIbyIndex(0).getnPiecesMoved() + 1);


                            // Database space
                            // add production history
                            dbConnection.getInstance().getProduction_history().insert(
                                    p.getId(),
                                    sfei.getInSensor().getName(),
                                    p.getReality().material().toString(),
                                    p.getReality().form().toString());

                        }

                    }

                    if (utils.getInstance().getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei_idx])) {

                        if (sfei.getPartsATM().size() > 0) {
                            /* ATENÇAO -> antes tava last(), mas para o caso de a peça ser introduzida, pela P_MORE
                             * iria dar o stamp na ultima e portanto o TRANSPORT nao iniciava*/
                            sfei.getPartsATM().first().setWaitTransport(!sfei.isLine_end());
                            // In both cases set produced TRUE
                            // In that way the SFEM_monitor will put the part in their statistic
                            // But as is not the end_line the transport will be done
                            sfei.getPartsATM().first().setProduced(true);

                        }
                    }

                    if (utils.getInstance().getLogicalOperator().FE_detector(b_outSensor, SFEIs_old_outSensors[sfei_idx])) {
                        sfei.setnPiecesMoved(sfei.getnPiecesMoved() + 1);
                    }


                } else {

                    // Only register on the end (end of item[i-1] = start of item[i])
                    // If SFEIs outSensor RE, then timestamp that event
                    if (utils.getInstance().getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei_idx])) {
                        part oldest_part = Objects.requireNonNull(selectedOldestPartInSFEI(sfei_idx));

                        String itemName = sfei.getName();
                        itemName = itemName.concat("-");
                        itemName = itemName.concat(sfei_outSensor.getName());

                        oldest_part.addTimestamp(itemName);

                        // Database space
                        // add production history
                        dbConnection.getInstance().getProduction_history().insert(
                                oldest_part.getId(),
                                sfei.getOutSensor().getName(),
                                oldest_part.getReality().material().toString(),
                                oldest_part.getReality().form().toString());

                        sfei.setnPiecesMoved(sfei.getnPiecesMoved() + 1);
                    }
                    // Shift the part among SFEIs
                    if (utils.getInstance().getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei_idx])) {
                        if (sfei_idx + 1 < sfee.getSFEIs().size()) {
                            // Then it is not in the last SFEI, so move the piece!
                            // remove from the previous
                            if (sfei.getPartsATM().size() > 0) {
                                part p = sfei.getPartsATM().pollFirst();
                                sfee.getSFEIbyIndex(sfei_idx + 1).addNewPartATM(p);
                            }
                        }
                    }

                    // End of the SFEE, set M_part produced flag to TRUE
                    if (sfei_idx == sfee.getSFEIs().size() - 1) {
                        boolean sfee_outSensor = (int) sensorsState.get(sfee.getOutSensor().getBit_offset()) == 1;
                        if (utils.getInstance().getLogicalOperator().RE_detector(sfee_outSensor, SFEIs_old_outSensors[sfei_idx])) {

                            if (sfei.getPartsATM().size() > 0) {
                                /* ATENÇAO -> antes tava last(), mas para o caso de a peça ser introduzida, pela P_MORE
                                 * iria dar o stamp na ultima e portanto o TRANSPORT nao iniciava*/
                                sfei.getPartsATM().first().setWaitTransport(!sfei.isLine_end());
                                // In both cases set produced TRUE
                                // In that way the SFEM_monitor will put the M_part in their statistic
                                // But as is not the end_line the transport will be done
                                sfei.getPartsATM().first().setProduced(true);

                            }
                        }
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

    private part selectedOldestPartInSFEI(int sfei_idx) {
        SFEI sfei = sfee.getSFEIbyIndex(sfei_idx);
        part oldest_part = null;
        if (sfei.getPartsATM().size() > 0) {
            // Timestamp the M_part that is on that SFEI for more time
            if (sfei_idx == 0 || sfei.getPartsATM().size() == 1) {
                // The oldest M_part is the first
                oldest_part = sfei.getPartsATM().first();
            } else {
                // Select the M_part
                long time_sec = 0;
                for (part p : sfei.getPartsATM()) {
                    for (Map.Entry<String, Instant> entry : p.getTimestamps().entrySet()) {
                        // Instant related to timestamp of leaving previous SFEI
                        if (entry.getKey().contains(sfee.getSFEIbyIndex(sfei_idx - 1).getOutSensor().getName())) {

                            long temp_time = Duration.between(entry.getValue(), Instant.now()).toSeconds();
                            if (temp_time > time_sec) {
                                time_sec = temp_time;
                                oldest_part = p;
                            }
                        }
                    }
                }

            }
        }
        return oldest_part;
    }

    private void updateSFEI_machinePartType(List<Object> actuatorsState) {
        for (Map.Entry<Integer, SFEI> sfeiEntry : sfee.getSFEIs().entrySet()) {

            if (sfeiEntry.getValue().getSfeiType().equals(SFEI.SFEI_type.MACHINE) && sfee.getSFEE_type().equals(SFEE.SFEE_environment.SIMULATION)) {

                SFEI_machine sfei = (SFEI_machine) sfeiEntry.getValue();

                if (default_partForm.equals(partDescription.form.LID)) {
                    actuatorsState.set(sfei.getaProduce().getBit_offset(), 1);
                } else if (default_partForm.equals(partDescription.form.BASE)) {
                    actuatorsState.set(sfei.getaProduce().getBit_offset(), 0);
                }
            }
        }
    }

    private void validateProducedParts(List<Object> sensorsState, List<Object> inputRegsValue) {

        for (Map.Entry<Integer, sensor_actuator> entry : visionSensorLocation.entrySet()) {
            if (sfee.getSFEIbyIndex(entry.getKey()).getSfeiType().equals(SFEI.SFEI_type.CONVEYOR)) {

                SFEI_conveyor sfeiConveyor = (SFEI_conveyor) sfee.getSFEIbyIndex(entry.getKey());
                // Verify if there is M_part in vision sensor range
                int visionSensor_number = (int) inputRegsValue.get(entry.getValue().getBit_offset());

                if (visionSensor_number > 0) {
                    if (sfeiConveyor.getPartsATM().size() > 0) {
                        partDescription reality = getPartsAspectByNumber(visionSensor_number);
//                        M_part lastPart = selectedOldestPartInSFEI(entry.getKey());
//                        M_part lastPart = sfeiConveyor.getPartsATM().last();
                        part lastPart = sfeiConveyor.getPartsATM().first();
/*                        if (!lastPart.getExpectation().equals(reality)) {
                            lastPart.setDefect();
                        }*/
                        lastPart.setReality(reality);
                    }
                }
            } else {
                throw new RuntimeException("(" + SFEE_production_monitor_old.class + " )The vision sensor should be placed on the conveyors");
            }


        }
    }

    private partDescription getPartsAspectByNumber(int num) {

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

    }

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
