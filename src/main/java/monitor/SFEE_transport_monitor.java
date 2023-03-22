package monitor;

import models.base.SFEE;
import models.base.SFEI;
import models.base.part;
import models.partsAspect;
import models.sensor_actuator;
import utils.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SFEE_transport_monitor {

    private SFEE sfee;
    private SFEI previousSFEI;
    private SFEI nextSFEI;
    private final utils utility;
    private final boolean[] SFEIs_old_inSensors;
    private final boolean[] SFEIs_old_outSensors;

    public SFEE_transport_monitor(SFEE sfee, SFEI previousSFEI, SFEI nextSFEI) {
        this.sfee = sfee;
        this.previousSFEI = previousSFEI;
        this.nextSFEI = nextSFEI;
        this.utility = new utils();
        this.SFEIs_old_inSensors = new boolean[sfee.getSFEIs().size()];
        this.SFEIs_old_outSensors = new boolean[sfee.getSFEIs().size()];
    }

    private void init_oldSensorsValues(ArrayList<List<Object>> sensorsState) {

        for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {

            sensor_actuator sfei_inSensor = sfei.getValue().getInSensor();
            sensor_actuator sfei_outSensor = sfei.getValue().getOutSensor();

            boolean b_inSensor = (int) sensorsState.get(0).get(sfei_inSensor.bit_offset()) == 1;
            boolean b_outSensor = (int) sensorsState.get(1).get(sfei_outSensor.bit_offset()) == 1;
            SFEIs_old_inSensors[sfei.getKey()] = b_inSensor;
            SFEIs_old_outSensors[sfei.getKey()] = b_outSensor;
        }
    }

    private boolean setup_run = true;
    private part currPart = null;

    public void loop(ArrayList<List<Object>> sensorsState) {

        try {
            if (setup_run) {
                init_oldSensorsValues(sensorsState);
                setup_run = false;
            }
            monitorPartsMovements(sensorsState);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void monitorPartsMovements(ArrayList<List<Object>> sensorsState) {
        try {
            for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {

                sensor_actuator sfei_inSensor = sfei.getValue().getInSensor();
                sensor_actuator sfei_outSensor = sfei.getValue().getOutSensor();

                boolean b_inSensor = (int) sensorsState.get(0).get(sfei_inSensor.bit_offset()) == 1;
                boolean b_outSensor = (int) sensorsState.get(1).get(sfei_outSensor.bit_offset()) == 1;

                // inSFEI
                // SFEE entry, should get part object from inSFEI
                if (sfei.getKey() == 0) {
                    if (currPart != null) {
                        // It means that previously a part was detected but not had the isWaitTransport flag TRUE
                        if (currPart.isWaitTransport()) {
                            System.out.println("IS FK waitingTransport");

                            part p = previousSFEI.getPartsATM().pollFirst();
                            if (p == null)
                                System.out.println("PART IS FK NULL");
                            else
//                                    assert p != null;
                                System.out.println("part from previousSFEI " + p.getId() + " exp: " + p.getExpectation() + " real: " + p.getReality());

                            // setWaitTransport(FALSE), but the produced is TRUE
                            assert p != null;
                            p.setWaitTransport(false);
                            // This operation of concat is faster than + operation
                            String itemName = sfee.getName();
                            itemName = itemName.concat("-");
                            itemName = itemName.concat(sfee.getInSensor().name());

                            p.addTimestamp(itemName);
                            sfei.getValue().addNewPartATM(p);

                            currPart = null;
                        }

//                boolean sfee_inSensor = (int) sensorsState.get(sfee.getInSensor().bit_offset()) == 1;
                    } else if (utility.getLogicalOperator().RE_detector(b_inSensor, SFEIs_old_inSensors[sfei.getKey()])) {
                        System.out.println("Detected the fk RE");
                        if (previousSFEI.getPartsATM().size() > 0) {
                            System.out.println("FK size > 0");
                            if (previousSFEI.getPartsATM().first().isWaitTransport()) {
                                part p = previousSFEI.getPartsATM().pollFirst();
                                if (p == null)
                                    System.out.println("PART IS FK NULL");
                                else
//                                    assert p != null;
                                    System.out.println("part from previousSFEI " + p.getId() + " exp: " + p.getExpectation() + " real: " + p.getReality());

                                // setWaitTransport(FALSE), but the produced is TRUE
                                assert p != null;
                                p.setWaitTransport(false);
                                // This operation of concat is faster than + operation
                                String itemName = sfee.getName();
                                itemName = itemName.concat("-");
                                itemName = itemName.concat(sfee.getInSensor().name());

                                p.addTimestamp(itemName);
                                sfei.getValue().addNewPartATM(p);
                            } else {
                                System.out.println("IS NOT WAITING TRANSPORT, FK");
                                currPart = previousSFEI.getPartsATM().first();
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

/*                // Shift the part among SFEIs
                if (utility.getLogicalOperator().RE_detector(b_outSensor, SFEIs_old_outSensors[sfei.getKey()])) {
                    if (sfei.getKey() + 1 < sfee.getSFEIs().size()) {
                        // Then it is not in the last SFEI, so move the piece!
                        // remove from the previous
                        if (sfei.getValue().getPartsATM().size() > 0) {
                            part p = sfei.getValue().getPartsATM().pollFirst();
                            sfee.getSFEIbyIndex(sfei.getKey() + 1).addNewPartATM(p);
                        }
                    }
                }*/

                if (sfei.getKey() == sfee.getSFEIs().size() - 1) {
                    boolean sfee_outSensor = (int) sensorsState.get(1).get(sfee.getOutSensor().bit_offset()) == 1;
                    if (utility.getLogicalOperator().RE_detector(sfee_outSensor, SFEIs_old_outSensors[sfei.getKey()])) {
                        if (sfei.getValue().getPartsATM().size() == 1) {
                            // setProduced(FALSE)
                            // before, setWaitTransport(FALSE)
                            // So the SFEM_transport will be triggered and remove the part from the SFEI partsATM
                            part p = sfei.getValue().getPartsATM().first();
                            p.setProduced(false);
                            nextSFEI.getPartsATM().add(p);
                        } /*else if (sfei.getValue().getPartsATM().size() > 1) {
                            // É suposto fazer isto no monitor deste SFEM de transporte
                            sfei.getValue().getPartsATM().pollFirst();
                            nextSFEI.getPartsATM().add(sfei.getValue().getPartsATM().first());
                        }*/
                    }
                }

                // Only update in the end in order to all functions see the values at the read moment
                SFEIs_old_inSensors[sfei.getKey()] = b_inSensor;
                SFEIs_old_outSensors[sfei.getKey()] = b_outSensor;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
