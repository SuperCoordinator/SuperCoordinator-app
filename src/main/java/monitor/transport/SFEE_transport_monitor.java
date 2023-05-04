package monitor.transport;

import communication.database.dbConnection;
import models.SFEx.SFEI_transport;
import models.base.SFEE;
import models.base.SFEI;
import models.base.part;
import models.sensor_actuator;
import utility.serialize.serializer;
import utility.utils;
import viewers.SFEE_transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SFEE_transport_monitor {

    private enum operation {
        SFEI2SFEI,
        WH2SFEI,
        SFEI2WH
    }

    private operation operationMode;

    private SFEE sfee;
    private SFEI previousSFEI;
    private SFEI nextSFEI;

    private boolean SFEI_old_inSensors = false;
    private boolean SFEI_old_outSensors = false;

    private boolean firstRun = true;
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

    private void init_oldSensorsValues(ArrayList<List<Object>> sensorsState) {

        try {
            switch (operationMode) {
                case WH2SFEI -> {
                    sensor_actuator sfei_outSensor = sfee.getSFEIbyIndex(0).getOutSensor();
                    SFEI_old_outSensors = (int) sensorsState.get(1).get(sfei_outSensor.getBit_offset()) == 1;
                }
                case SFEI2SFEI -> {
                    sensor_actuator sfei_inSensor = sfee.getSFEIbyIndex(0).getInSensor();
                    SFEI_old_inSensors = (int) sensorsState.get(0).get(sfei_inSensor.getBit_offset()) == 1;
                    sensor_actuator sfei_outSensor = sfee.getSFEIbyIndex(0).getOutSensor();
                    SFEI_old_outSensors = (int) sensorsState.get(1).get(sfei_outSensor.getBit_offset()) == 1;
                }
                case SFEI2WH -> {
                    sensor_actuator sfei_inSensor = sfee.getSFEIbyIndex(0).getInSensor();
                    SFEI_old_inSensors = (int) sensorsState.get(0).get(sfei_inSensor.getBit_offset()) == 1;

                }
                default -> throw new RuntimeException(SFEE_transport.class + " operation mode not defined!");
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void setupPrevNextSFEI(SFEI previousSFEI, SFEI nextSFEI) {
        this.previousSFEI = previousSFEI;
        this.nextSFEI = nextSFEI;
    }

    public void loop(ArrayList<List<Object>> sensorsState) {
        try {
            if (firstRun) {
                if (previousSFEI.getSfeiType().equals(SFEI.SFEI_type.WAREHOUSE)) {
                    operationMode = operation.WH2SFEI;
                } else if (nextSFEI.getSfeiType().equals(SFEI.SFEI_type.WAREHOUSE)) {
                    operationMode = operation.SFEI2WH;
                } else {
                    operationMode = operation.SFEI2SFEI;
                }
                init_oldSensorsValues(sensorsState);
                firstRun = false;

                sm_state = SFEE_transport_monitor.state.T1;
            }

            switch (operationMode) {
                case SFEI2SFEI -> moveParts(sensorsState);
                case WH2SFEI -> placeNewParts(sensorsState);
                case SFEI2WH -> removeProducedParts(sensorsState);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void moveParts(ArrayList<List<Object>> sensorsState) {

        SFEI_transport sfeiTransport = (SFEI_transport) sfee.getSFEIbyIndex(0);

        boolean b_inSensor = (int) sensorsState.get(0).get(sfeiTransport.getInSensor().getBit_offset()) == 1;
        boolean b_outSensor = (int) sensorsState.get(1).get(sfeiTransport.getOutSensor().getBit_offset()) == 1;

        if (utils.getInstance().getLogicalOperator().RE_detector(b_inSensor, SFEI_old_inSensors)) {
            // Detect part from the inSFEI waiting for transport
            if (previousSFEI.getPartsATM().size() > 0) {
                // The first one is waiting for the most time
                part movingPart = Objects.requireNonNull(previousSFEI.getPartsATM().pollFirst());
                movingPart.setState(part.status.IN_TRANSPORT);
                sfeiTransport.addNewPartATM(movingPart);
            }
        }

        if (utils.getInstance().getLogicalOperator().RE_detector(b_outSensor, SFEI_old_inSensors)) {
            // Detect part from the inSFEI waiting for transport
            if (sfeiTransport.getPartsATM().size() > 0) {
                // The first one is waiting for the most time
                part movingPart = Objects.requireNonNull(sfeiTransport.getPartsATM().pollFirst());
                movingPart.setState(part.status.IN_PRODUCTION);
                nextSFEI.addNewPartATM(movingPart);

                sfeiTransport.setnPiecesMoved(sfeiTransport.getnPiecesMoved() + 1);
            }
        }

        // Only update in the end in order to all functions see the values at the read moment
        SFEI_old_inSensors = b_inSensor;
        SFEI_old_outSensors = b_outSensor;
    }

    private enum state {
        T1, T2, T3
    }

    private state sm_state;

    private void placeNewParts(ArrayList<List<Object>> sensorsState) {
        // Only have 1 SFEI
        SFEI_transport sfeiTransport = (SFEI_transport) sfee.getSFEIbyIndex(0);

        boolean b_outSensor = (int) sensorsState.get(1).get(sfeiTransport.getOutSensor().getBit_offset()) == 1;

        switch (sm_state) {
            case T1 -> {
                if (sfeiTransport.getPartsATM().size() == 0) {
                    if (previousSFEI.getPartsATM().size() > 0) {
                        part movingPart = Objects.requireNonNull(previousSFEI.getPartsATM().pollFirst());
                        movingPart.setState(part.status.IN_TRANSPORT);
                        sfeiTransport.addNewPartATM(movingPart);
                        sm_state = state.T2;
                    }
                }
            }
            case T2 -> {
                if (utils.getInstance().getLogicalOperator().FE_detector(b_outSensor, SFEI_old_outSensors)) {
                    if (sfeiTransport.getPartsATM().size() > 0) {
                        part movingPart = Objects.requireNonNull(sfeiTransport.getPartsATM().pollFirst());
                        movingPart.setState(part.status.IN_PRODUCTION);
                        nextSFEI.getPartsATM().add(movingPart);

                        updateDB(movingPart, sfeiTransport);

                        sm_state = state.T1;
                    }
                }
            }
        }

        // Only update in the end in order to all functions see the values at the read moment
        SFEI_old_outSensors = b_outSensor;

    }

    private void updateDB(part movingPart, SFEI_transport sfeiTransport) {

        // update part status
        dbConnection.getInstance().getParts().update_status(
                movingPart.getId(),
                serializer.getInstance().scene.toString(),
                movingPart.getState().toString());

        // new record in production_history
        dbConnection.getInstance().getProduction_history().insert(
                movingPart.getId(),
                sfeiTransport.getOutSensor().getName(),
                movingPart.getReality().material().toString(),
                movingPart.getReality().form().toString());
    }

    private void removeProducedParts(ArrayList<List<Object>> sensorsState) {

    }


}
