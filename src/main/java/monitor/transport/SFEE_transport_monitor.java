package monitor.transport;

import communication.database.dbConnection;
import failures.SFEE_transport_failures;
import models.SFEx.SFEI_transport;
import models.SFEx.SFEM_transport;
import models.base.SFEE;
import models.base.SFEI;
import models.base.part;
import models.sensor_actuator;
import utility.serialize.serializer;
import utility.utils;
import viewers.SFEE_transport;

import java.time.Duration;
import java.time.Instant;
import java.util.*;


public class SFEE_transport_monitor {

    private SFEM_transport.configuration operationMode;
    private SFEE sfee;
    private SFEI previousSFEI;
    private SFEI nextSFEI;

    private boolean SFEI_old_inSensors = false;
    private boolean SFEI_old_outSensors = false;

    private boolean firstRun = true;
    private boolean printedDBG = false;

    public SFEE_transport_monitor() {
    }

    public SFEE_transport_monitor(SFEE sfee, SFEI previousSFEI, SFEI nextSFEI, SFEM_transport.configuration configuration) {
        this.sfee = sfee;
        this.previousSFEI = previousSFEI;
        this.nextSFEI = nextSFEI;
        this.operationMode = configuration;

    }

    private void init_oldSensorsValues(ArrayList<List<Object>> sensorsState) {

        try {
            switch (operationMode) {
                case WH2SFEI, WH2RealSFEI -> {
                    sensor_actuator sfei_outSensor = sfee.getSFEIbyIndex(0).getOutSensor();
                    SFEI_old_outSensors = (int) sensorsState.get(1).get(sfei_outSensor.getBit_offset()) == 1;
                }
                case SFEI2SFEI, SFEI2RealSFEI, RealSFEI2SFEI -> {
                    sensor_actuator sfei_inSensor = sfee.getSFEIbyIndex(0).getInSensor();
                    SFEI_old_inSensors = (int) sensorsState.get(0).get(sfei_inSensor.getBit_offset()) == 1;
                    sensor_actuator sfei_outSensor = sfee.getSFEIbyIndex(0).getOutSensor();
                    SFEI_old_outSensors = (int) sensorsState.get(1).get(sfei_outSensor.getBit_offset()) == 1;
                }
                case SFEI2WH, RealSFEI2WH -> {
                    sensor_actuator sfei_inSensor = sfee.getSFEIbyIndex(0).getInSensor();
                    SFEI_old_inSensors = (int) sensorsState.get(0).get(sfei_inSensor.getBit_offset()) == 1;

                }
                default -> throw new RuntimeException(SFEE_transport.class + " operation mode not defined!");
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void loop(ArrayList<List<Object>> sensorsState) {
        try {
            if (firstRun) {
                init_oldSensorsValues(sensorsState);
                firstRun = false;
                sm_state = SFEE_transport_monitor.state.T1;
            }

            switch (operationMode) {
                case WH2SFEI, WH2RealSFEI -> placeNewParts(sensorsState);
                case SFEI2SFEI, SFEI2RealSFEI, RealSFEI2SFEI -> moveParts(sensorsState);
                case SFEI2WH, RealSFEI2WH -> removeProducedParts(sensorsState);
            }

//            printDBG();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void moveParts(ArrayList<List<Object>> sensorsState) {

        SFEI_transport sfeiTransport = (SFEI_transport) sfee.getSFEIbyIndex(0);


        boolean b_inSensor = (int) sensorsState.get(0).get(sfeiTransport.getInSensor().getBit_offset()) == 1;
        boolean b_outSensor = (int) sensorsState.get(1).get(sfeiTransport.getOutSensor().getBit_offset()) == 1;

        switch (sm_state) {
            case T1 -> {
                if (utils.getInstance().getLogicalOperator().RE_detector(b_inSensor, SFEI_old_inSensors) || b_inSensor) {
//                    System.out.println("SFEE_transport_monitor RE on " + sfeiTransport.getInSensor().getName() + " with " + previousSFEI.getPartsATM().size() + " parts on previous SFEI");
                    // The first one is waiting for the most time
                    Iterator<part> iterator = previousSFEI.getPartsATM().iterator();
                    while (iterator.hasNext()) {
                        part movingPart = iterator.next();
//                        System.out.println(movingPart);
                        if (movingPart.getState().equals(part.status.WAIT_TRANSPORT)) {
//                    movingPart.setState(part.status.IN_TRANSPORT);
                            sfeiTransport.addNewPartATM(movingPart);
                            iterator.remove();
                            sm_state = state.T2;
                            break;
                        }
                    }

                }
            }
            case T2 -> {
                if (utils.getInstance().getLogicalOperator().RE_detector(b_outSensor, SFEI_old_inSensors) || b_outSensor) {

                    Iterator<part> iterator = sfeiTransport.getPartsATM().iterator();
                    while (iterator.hasNext()) {
                        part movingPart = iterator.next();
                        if (movingPart.getState().equals(part.status.IN_TRANSPORT)) {
                            movingPart.setState(part.status.IN_PRODUCTION);
                            nextSFEI.addNewPartATM(movingPart);

                            sfeiTransport.setnPiecesMoved(sfeiTransport.getnPiecesMoved() + 1);

                            iterator.remove();
                            sm_state = state.T1;
                            break;
                        }
                    }

                }
            }
        }

        // Only update in the end in order to all functions see the values at the read moment
        SFEI_old_inSensors = b_inSensor;
        SFEI_old_outSensors = b_outSensor;
    }

    private enum state {
        T1, T2
    }

    private state sm_state;

    private void placeNewParts(ArrayList<List<Object>> sensorsState) {

        SFEI_transport sfeiTransport = (SFEI_transport) sfee.getSFEIbyIndex(0);

        boolean b_outSensor = (int) sensorsState.get(1).get(sfeiTransport.getOutSensor().getBit_offset()) == 1;

        switch (sm_state) {
            case T1 -> {
//                System.out.println("sfei transport partsATM " + sfeiTransport.getPartsATM().size());
                if (sfeiTransport.getPartsATM().size() == 0) {
                    for (part movingPart : previousSFEI.getPartsATM()) {
                        if (movingPart.getState().equals(part.status.IN_STOCK)) {
                            movingPart.setState(part.status.WAIT_TRANSPORT);
                            sfeiTransport.addNewPartATM(movingPart);
                            previousSFEI.getPartsATM().remove(movingPart);
                            sm_state = state.T2;
                            break;
                        }
                    }
                }
            }
            case T2 -> {
                if (utils.getInstance().getLogicalOperator().FE_detector(b_outSensor, SFEI_old_outSensors)) {

                    Iterator<part> iterator = sfeiTransport.getPartsATM().iterator();
                    while (iterator.hasNext()) {
                        part movingPart = iterator.next();
                        if (movingPart.getState().equals(part.status.IN_TRANSPORT)) {
                            movingPart.setState(part.status.IN_PRODUCTION);
                            nextSFEI.addNewPartATM(movingPart);
                            updateDB(movingPart, sfeiTransport);

                            iterator.remove();
                            sm_state = state.T1;
                            break;
                        }
                    }
                }
            }
        }

        // Only update in the end in order to all functions see the values at the read moment
        SFEI_old_outSensors = b_outSensor;

    }

    private void removeProducedParts(ArrayList<List<Object>> sensorsState) {

        SFEI_transport sfeiTransport = (SFEI_transport) sfee.getSFEIbyIndex(0);

        boolean b_inSensor = (int) sensorsState.get(0).get(sfeiTransport.getInSensor().getBit_offset()) == 1;

        if (utils.getInstance().getLogicalOperator().RE_detector(b_inSensor, SFEI_old_inSensors)) {
            Iterator<part> iterator = previousSFEI.getPartsATM().iterator();
            while (iterator.hasNext()) {
                part movingPart = iterator.next();
//                System.out.println(SFEE_transport_monitor.class + " " + operationMode + " " + movingPart);
                if (movingPart.getState().equals(part.status.WAIT_TRANSPORT)) {
//                    movingPart.setState(part.status.IN_TRANSPORT);
                    sfeiTransport.addNewPartATM(movingPart);
                    iterator.remove();
                    break;
                }
            }
        }

        Iterator<part> iterator = sfeiTransport.getPartsATM().iterator();
        while (iterator.hasNext()) {
            part movingPart = iterator.next();
            if (movingPart.getState().equals(part.status.PRODUCED)) {
                nextSFEI.addNewPartATM(movingPart);
                // update part status
                dbConnection.getInstance().getParts().update_status(
                        movingPart.getId(),
                        serializer.getInstance().scene,
                        movingPart.getState().toString());

                iterator.remove();
                break;
            }
        }
    }

    private void updateDB(part movingPart, SFEI_transport sfeiTransport) {

        // update part status
        dbConnection.getInstance().getParts().update_status(
                movingPart.getId(),
                serializer.getInstance().scene,
                movingPart.getState().toString());

        // new record in production_history
        dbConnection.getInstance().getProduction_history().insert(
                movingPart.getId(),
                sfeiTransport.getOutSensor().getName(),
                movingPart.getReality().material().toString(),
                movingPart.getReality().form().toString(),
                Instant.now());
    }

    private void printDBG() {
        try {
            if (Duration.between(sfee.getSFEIbyIndex(0).getDayOfBirth(), Instant.now()).toSeconds() % 5 == 0) {
                if (!printedDBG) {
                    for (Map.Entry<Integer, SFEI> sfei : sfee.getSFEIs().entrySet()) {
                        if (sfei.getValue().getPartsATM().size() > 0) {
                            System.out.println("(" + sfei.getKey() + ") " + sfei.getValue().getName() + " moved: " + sfei.getValue().getnPiecesMoved() + " parts");
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
            throw new RuntimeException(e);
        }

    }


}
