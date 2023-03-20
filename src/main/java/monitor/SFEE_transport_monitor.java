package monitor;

import models.base.SFEE;
import models.base.SFEI;
import models.base.part;
import models.partsAspect;
import models.sensor_actuator;
import utils.utils;

import java.util.List;
import java.util.Map;

public class SFEE_transport_monitor {

    private SFEE sfee;
    private final utils utility;
    private final boolean[] SFEIs_old_inSensors;
    private final boolean[] SFEIs_old_outSensors;

    public SFEE_transport_monitor(SFEE sfee) {
        this.sfee = sfee;
        this.utility = new utils();
        this.SFEIs_old_inSensors = new boolean[sfee.getSFEIs().size()];
        this.SFEIs_old_outSensors = new boolean[sfee.getSFEIs().size()];

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

    private boolean setup_run = true;

    private void loop(List<Object> sensorsState) {

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

    private void monitorPartsMovements(List<Object> sensorsState) {
        for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {

            sensor_actuator sfei_inSensor = sfei.getValue().getInSensor();
            sensor_actuator sfei_outSensor = sfei.getValue().getOutSensor();

            boolean b_inSensor = (int) sensorsState.get(sfei_inSensor.bit_offset()) == 1;
            boolean b_outSensor = (int) sensorsState.get(sfei_outSensor.bit_offset()) == 1;

            // inSFEI
/*            // SFEE entry, should get part object from inSFEI
            if (sfei.getKey() == 0) {

                boolean sfee_inSensor = (int) sensorsState.get(sfee.getInSensor().bit_offset()) == 1;
                if (utility.getLogicalOperator().RE_detector(sfee_inSensor, SFEIs_old_inSensors[sfei.getKey()])) {
                    part p = null;
                    if (sfei.getValue().getPartsATM().size() > 0) {
                            if(sfei.getValue().getPartsATM().last().isProduced()){
                                p = sfei.getValue().getPartsATM().pollLast();

                            }
                    }
                    // This operation of concat is faster than + operation
                    String itemName = sfee.getName();
                    itemName = itemName.concat("-");
                    itemName = itemName.concat(sfee.getInSensor().name());

                    assert p != null;
                    p.addTimestamp(itemName);
                    sfei.getValue().addNewPartATM(p);

                }
            }*/
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

        }
    }

}
