package monitor.transport;

import models.SFEx_particular.SFEI_transport;
import models.base.SFEE;
import models.base.SFEI;
import models.base.part;
import models.sensor_actuator;
import utility.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class SFEE_transport_monitor {
    private SFEE sfee;
    private SFEI previousSFEI;
    private SFEI nextSFEI;

    private boolean SFEI_old_inSensors = false;
    private boolean SFEI_old_outSensors = false;

    private boolean setup_run = true;
    private part currPart = null;

    public SFEE_transport_monitor() {
    }

    public SFEE_transport_monitor(SFEE sfee, SFEI previousSFEI, SFEI nextSFEI) {
        this.sfee = sfee;
        this.previousSFEI = previousSFEI;
        this.nextSFEI = nextSFEI;

    }

    public SFEI getNextSFEI() {
        return nextSFEI;
    }

    private void init_oldSensorsValues(ArrayList<List<Object>> sensorsState, boolean isSFEI_warehouse) {

        if (!isSFEI_warehouse) {
            sensor_actuator sfei_inSensor = sfee.getSFEIbyIndex(0).getInSensor();
            SFEI_old_inSensors = (int) sensorsState.get(0).get(sfei_inSensor.getBit_offset()) == 1;
        }
        sensor_actuator sfei_outSensor = sfee.getSFEIbyIndex(0).getOutSensor();
        SFEI_old_outSensors = (int) sensorsState.get(1).get(sfei_outSensor.getBit_offset()) == 1;

/*        sensor_actuator sfei_inSensor = sfee.getSFEIbyIndex(0).getInSensor();
        boolean b_inSensor = (int) sensorsState.get(0).get(sfei_inSensor.getBit_offset()) == 1;
        SFEI_old_inSensors = b_inSensor;*/

    }

    public void setupPrevNextSFEI(SFEI previousSFEI, SFEI nextSFEI) {
        this.previousSFEI = previousSFEI;
        this.nextSFEI = nextSFEI;
    }

    public void loop(ArrayList<List<Object>> sensorsState, boolean waitNewPart) {
        try {
            if (previousSFEI.getSfeiType().equals(SFEI.SFEI_type.WAREHOUSE)) {
                if (setup_run) {
                    // If it is line start, then the connection is established with the warehouse
                    init_oldSensorsValues(sensorsState, true);
                    setup_run = false;
                    sm_state = state.T1;
                    old_sm_state = sm_state;
                }
                placeNewParts(sensorsState, waitNewPart);
            } else {
                // Normal Transport between SFEIs
                if (setup_run) {
                    // If it is line start, then the connection is established with the warehouse
                    init_oldSensorsValues(sensorsState, false);
                    setup_run = false;
                }
                monitorPartsMovements(sensorsState);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    private enum state {
        T1, T2, T3
    }

    private state sm_state, old_sm_state;

    private void placeNewParts(ArrayList<List<Object>> sensorsState, boolean waitNewPart) {
        // Only have 1 SFEI
        SFEI_transport sfeiTransport = (SFEI_transport) sfee.getSFEIbyIndex(0);

        boolean b_outSensor = (int) sensorsState.get(1).get(sfeiTransport.getOutSensor().getBit_offset()) == 1;
        switch (sm_state) {
            case T1 -> {
                if (previousSFEI.getPartsATM().size() > 0 && !b_outSensor) {
                    part p = Objects.requireNonNull(previousSFEI.getPartsATM().pollFirst());
                    p.setWaitTransport(false);
                    // This operation of concat is faster than + operation
                    String itemName = sfee.getName();
                    itemName = itemName.concat("-");
//            itemName = itemName.concat(sfee.getInSensor().getName());
                    itemName = itemName.concat("warehouse");

                    p.addTimestamp(itemName);
                    sfeiTransport.addNewPartATM(p);
                    System.out.println(p);
                    sm_state = state.T2;
                }
            }
            case T2 -> {
//                boolean sfee_outSensor = (int) sensorsState.get(1).get(sfee.getOutSensor().getBit_offset()) == 1;
                if (utils.getInstance().getLogicalOperator().FE_detector(b_outSensor, SFEI_old_outSensors)) {
                    if (sfeiTransport.getPartsATM().size() > 0) {
                        part p = Objects.requireNonNull(sfeiTransport.getPartsATM().pollFirst());
                        p.setProduced(false);
                        nextSFEI.getPartsATM().add(p);
                        sm_state = state.T3;
                    }
                }
            }
            case T3 -> {
                if (waitNewPart )
                    sm_state = state.T1;
            }
        }

/*        if (old_sm_state != sm_state)
            System.out.println("state: " + sm_state);*/

        old_sm_state = sm_state;
        // Only update in the end in order to all functions see the values at the read moment
        SFEI_old_outSensors = b_outSensor;

    }

    private void monitorPartsMovements(ArrayList<List<Object>> sensorsState) {
        try {
            for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {

                sensor_actuator sfei_inSensor = sfei.getValue().getInSensor();
                sensor_actuator sfei_outSensor = sfei.getValue().getOutSensor();

                boolean b_inSensor = (int) sensorsState.get(0).get(sfei_inSensor.getBit_offset()) == 1;
                boolean b_outSensor = (int) sensorsState.get(1).get(sfei_outSensor.getBit_offset()) == 1;

                // inSFEI
                // SFEE entry, should get part object from inSFEI

                if (currPart != null) {
                    // It means that previously a part was detected but not had the isWaitTransport flag TRUE
                    if (currPart.isWaitTransport() && currPart.isProduced()) {

                        part p = Objects.requireNonNull(previousSFEI.getPartsATM().pollFirst());

                        // setWaitTransport(FALSE), but the produced is TRUE
                        p.setWaitTransport(false);
                        // This operation of concat is faster than + operation
                        String itemName = sfee.getName();
                        itemName = itemName.concat("-");
                        itemName = itemName.concat(sfee.getInSensor().getName());

                        p.addTimestamp(itemName);
                        sfei.getValue().addNewPartATM(p);

                        currPart = null;
                    }

//                boolean sfee_inSensor = (int) sensorsState.get(sfee.getInSensor().bit_offset()) == 1;
                } else if (utils.getInstance().getLogicalOperator().RE_detector(b_inSensor, SFEI_old_inSensors)) {
                    if (previousSFEI.getPartsATM().size() > 0) {
                        if (previousSFEI.getPartsATM().first().isWaitTransport() && previousSFEI.getPartsATM().first().isProduced()) {
                            part p = Objects.requireNonNull(previousSFEI.getPartsATM().pollFirst());

                            p.setWaitTransport(false);
                            // This operation of concat is faster than + operation
                            String itemName = sfee.getName();
                            itemName = itemName.concat("-");
                            itemName = itemName.concat(sfee.getInSensor().getName());

                            p.addTimestamp(itemName);
                            sfei.getValue().addNewPartATM(p);
                        } else {
                            currPart = previousSFEI.getPartsATM().first();
                        }
                    } else
                        throw new RuntimeException(previousSFEI.getName() + " RE out_sensor but partsATM size is 0");

                }

                // Only register on the end (end of item[i-1] = start of item[i])
                // If SFEIs outSensor RE, then timestamp that event
                if (utils.getInstance().getLogicalOperator().RE_detector(b_outSensor, SFEI_old_outSensors)) {
                    if (sfei.getValue().getPartsATM().size() > 0) {
                        // This operation of concat is faster than + operation
                        String itemName = sfei.getValue().getName();
                        itemName = itemName.concat("-");
                        itemName = itemName.concat(sfei_outSensor.getName());

                        if (sfei.getKey() == sfee.getSFEIs().size() - 1)
                            sfei.getValue().getPartsATM().last().addTimestamp(itemName);
                        else
                            sfei.getValue().getPartsATM().first().addTimestamp(itemName);

                        sfei.getValue().setnPiecesMoved(sfei.getValue().getnPiecesMoved() + 1);
                    }
                }


                boolean sfee_outSensor = (int) sensorsState.get(1).get(sfee.getOutSensor().getBit_offset()) == 1;
                if (utils.getInstance().getLogicalOperator().RE_detector(sfee_outSensor, SFEI_old_outSensors)) {
                    if (sfei.getValue().getPartsATM().size() > 0) {
                        part p = sfei.getValue().getPartsATM().first();
                        // setProduced(FALSE)
                        // before, setWaitTransport(FALSE)
                        // So the SFEM_transport will be triggered and remove the part from the SFEI partsATM
                        p.setProduced(false);
                        nextSFEI.getPartsATM().add(p);
                    }
                }

                // Only update in the end in order to all functions see the values at the read moment
                SFEI_old_inSensors = b_inSensor;
                SFEI_old_outSensors = b_outSensor;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
