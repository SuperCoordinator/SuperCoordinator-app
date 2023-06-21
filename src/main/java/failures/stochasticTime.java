package failures;

import models.sfe_x.*;
import models.base.SFEI;
import models.base.part;
import models.partDescription;
import utility.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class stochasticTime {
    private enum SM_conv {
        INIT,
        STOPPED,
        RESUMING,
        END
    }

    private enum SM_pusher {
        INIT,
        MOVE,
        STOP,
        RESUME,
        END

    }

    private enum SM_mach {
        WAITING,
        LOADING,
        UNLOADING,
        FORCE_PAUSE,
        RELEASE_PAUSE,
        END
    }

    private enum SM_trans {
        INIT,
        REMOVING,
        WAITING,
        EMITTING,
        END
    }

    private SM_conv smConv;
    private SM_pusher smPusher;
    private SM_mach smMach;
    private SM_trans smTrans;

    public enum timeOptions {
        GAUSSIAN,
        LINEAR
    }

    private final SFEI sfei;
    private final int sfei_idx;
    private SFEM_transport.configuration transportConfiguration;
    private final part part;
    private final double delay;

    public stochasticTime(SFEI sfei, int sfei_idx, part part, double delay) {

        this.sfei = sfei;
        this.sfei_idx = sfei_idx;
        this.part = part;

        this.delay = delay;

        this.smConv = SM_conv.INIT;
        this.smPusher = SM_pusher.INIT;
        this.smMach = SM_mach.WAITING;
        this.smTrans = SM_trans.INIT;

    }

    public int getSfei_idx() {
        return sfei_idx;
    }

    public models.base.part getPart() {
        return part;
    }

    public boolean isConveyorFinished() {
        return smConv.equals(SM_conv.END);
    }

    public boolean isPusherFinished() {
        return smPusher.equals(SM_pusher.END);
    }

    public boolean isMachineFinished() {
        return smMach.equals(SM_mach.END);
    }

    public boolean isTransportFinished() {
        return smTrans.equals(SM_trans.END);
    }

    /**
     * For the parts introduced in the middle of the production, in order to this task can be removed when expires
     */
    public boolean isPartProduced() {
        return part.getState().equals(models.base.part.status.PRODUCED);
    }

    public boolean isPartRemovedInProduction() {
        return part.getState().equals(models.base.part.status.REMOVED);
    }

    public void setTransportConfiguration(SFEM_transport.configuration configuration) {

        try {
            this.transportConfiguration = configuration;
            if (Objects.requireNonNull(transportConfiguration).equals(SFEM_transport.configuration.WH2SFEI)
                    || Objects.requireNonNull(transportConfiguration).equals(SFEM_transport.configuration.WH2RealSFEI)) {
                initial_t = Instant.now();
                this.smTrans = SM_trans.WAITING;
                this.old_state = SM_trans.INIT;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void loop(ArrayList<List<Object>> sensorsState, ArrayList<List<Object>> actuatorsState) {
        try {
            switch (sfei.getSfeiType()) {
                case CONVEYOR -> {
                    if (sfei.isSimulation()) {
                        // F_IO scene
                        injectFailureF_IOConv(sensorsState.get(0), actuatorsState.get(0));
                    } else {
                        //No simulation, so have only a STOP bit
                        injectFailureConveyor(sensorsState.get(0), actuatorsState.get(0));
                    }
                }
                case PUSHER -> injectFailurePusher(sensorsState.get(0), actuatorsState.get(0));
                case MACHINE -> {
                    if (sfei.isSimulation())
                        injectFailuresSimMachine(sensorsState.get(0), actuatorsState.get(0));
                    else
                        injectFailuresMachine(sensorsState.get(0), actuatorsState.get(0));
                }
                case TRANSPORT -> transportBetweenSFEIs(sensorsState.get(0), actuatorsState.get(0),
                        sensorsState.get(1), actuatorsState.get(1), actuatorsState.get(2));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean firstRun = true;

    private boolean old_sRemover;
    private Instant initial_t;

    private void injectFailureF_IOConv(List<Object> sensorsState, List<Object> actuatorsState) {
        try {
            SFEI_conveyor sfeiConveyor = (SFEI_conveyor) sfei;
            if (firstRun) {
                old_sRemover = (int) sensorsState.get(sfeiConveyor.getsRemover().getOffset()) == 1;
                firstRun = false;
            }
            boolean sensor;
            switch (smConv) {
                case INIT -> {
                    sensor = (int) sensorsState.get(sfeiConveyor.getsRemover().getOffset()) == 1;
                    if (sfeiConveyor.getPartsATM().size() > 0) {
                        if (utils.getInstance().getLogicalOperator().RE_detector(sensor, old_sRemover)) {
                            actuatorsState.set(sfeiConveyor.getaConveyorMotor().getOffset(), 1);
                            initial_t = Instant.now();
                            smConv = SM_conv.STOPPED;
                        }
                    }
                    old_sRemover = sensor;
                }
                case STOPPED -> {
                    if (Duration.between(initial_t, Instant.now()).toMillis() >= delay) {
                        actuatorsState.set(sfeiConveyor.getaConveyorMotor().getOffset(), 0);
                        smConv = SM_conv.RESUMING;
                    }

                }
                case RESUMING -> {
                    sensor = (int) sensorsState.get(sfeiConveyor.getsRemover().getOffset()) == 1;
                    if (utils.getInstance().getLogicalOperator().FE_detector(sensor, old_sRemover)) {
                        smConv = SM_conv.END;
                    }
                    old_sRemover = sensor;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void injectFailureConveyor(List<Object> sensorsState, List<Object> actuatorsState) {
    }

    private Instant safety_margin_start, stopped_at;

    /**
     * Not TESTED
     * @param sensorsState
     * @param actuatorsState
     */
    private void injectFailurePusher(List<Object> sensorsState, List<Object> actuatorsState) {
        try {
            SFEI_pusher sfeiPusher = (SFEI_pusher) sfei;
            switch (smPusher) {
                case INIT -> {
                    boolean b_s_pusher_back = (int) sensorsState.get(sfeiPusher.getsPusherBack().getOffset()) == 1;
                    if (sfeiPusher.getPartsATM().size() > 0) {
                        if (b_s_pusher_back && part.getId() == sfeiPusher.getPartsATM().first().getId()) {
                            safety_margin_start = Instant.now();
                            smPusher = SM_pusher.MOVE;
                        }
                    }
                }
                case MOVE -> {
                    double safety_margin = 500.0;
                    if (Duration.between(safety_margin_start, Instant.now()).toMillis() >= safety_margin) {
                        smPusher = SM_pusher.STOP;
                        stopped_at = Instant.now();
                    }
                }
                case STOP -> {
                    if (Duration.between(stopped_at, Instant.now()).toMillis() >= delay) {
                        smPusher = SM_pusher.RESUME;
                    }
                    actuatorsState.set(sfeiPusher.getaForwardMotor().getOffset(), 1);

                }
                case RESUME -> {
                    actuatorsState.set(sfeiPusher.getaForwardMotor().getOffset(), 0);
                    smPusher = SM_pusher.END;

                }
                case END -> {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean old_sMachine_door;

    private void injectFailuresSimMachine(List<Object> sensorsState, List<Object> actuatorsState) {

        try {
            SFEI_machine sfeiMachine = (SFEI_machine) sfei;
            switch (smMach) {
                case WAITING -> {
                    boolean b_machine_door = (int) sensorsState.get(sfeiMachine.getsDoor().getOffset()) == 1;
                    if (sfeiMachine.getPartsATM().size() > 0) {
                        if (sfeiMachine.getPartsATM().first().getId() == part.getId() && utils.getInstance().getLogicalOperator().FE_detector(b_machine_door, old_sMachine_door)) {
                            smMach = SM_mach.LOADING;
                        }
                    }
                    old_sMachine_door = b_machine_door;
                }
                case LOADING -> {
                    boolean b_machine_door = (int) sensorsState.get(sfeiMachine.getsDoor().getOffset()) == 1;
                    if (utils.getInstance().getLogicalOperator().RE_detector(b_machine_door, old_sMachine_door)) {
                        smMach = SM_mach.UNLOADING;
                        safety_margin_start = Instant.now();
                    }
                    old_sMachine_door = b_machine_door;
                }
                case UNLOADING -> {
                    double safety_margin = 2000.0;
                    if (Duration.between(safety_margin_start, Instant.now()).toMillis() >= safety_margin) {
                        smMach = SM_mach.FORCE_PAUSE;
                        safety_margin_start = null;
                        stopped_at = Instant.now();
                    }
                }
                case FORCE_PAUSE -> {
                    if (Duration.between(stopped_at, Instant.now()).toMillis() >= delay) {
                        stopped_at = null;
                        smMach = SM_mach.RELEASE_PAUSE;
                    }
                    actuatorsState.set(sfeiMachine.getaStop().getOffset(), 1);
                }
                case RELEASE_PAUSE -> {
                    actuatorsState.set(sfeiMachine.getaStop().getOffset(), 0);
                    smMach = SM_mach.END;
                }
                case END -> {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * NOT TESTED
     * @param sensorsState
     * @param actuatorsState
     */
    private void injectFailuresMachine(List<Object> sensorsState, List<Object> actuatorsState) {
        try {
            SFEI_machine sfeiMachine = (SFEI_machine) sfei;
            if (firstRun) {
                old_sMachine_door = (int) sensorsState.get(sfeiMachine.getsDoor().getOffset()) == 1;
                firstRun = false;
            }

            switch (smMach) {
                case WAITING -> {
                    if (sfeiMachine.getPartsATM().size() > 0) {
                        if (sfeiMachine.getPartsATM().first().getId() == part.getId()) {
                            smMach = SM_mach.LOADING;
                        }
                    }
                }
                case LOADING -> {
                    boolean part_in_machine = (int) sensorsState.get(sfeiMachine.getsDoor().getOffset()) == 1;
                    if (utils.getInstance().getLogicalOperator().RE_detector(part_in_machine, old_sMachine_door)) {
                        smMach = SM_mach.UNLOADING;
                        safety_margin_start = Instant.now();
                    }
                    old_sMachine_door = part_in_machine;
                }
                case UNLOADING -> {
                    int safety_margin = 500;
                    if (Duration.between(safety_margin_start, Instant.now()).toMillis() >= safety_margin) {
                        smMach = SM_mach.FORCE_PAUSE;
                        stopped_at = Instant.now();
                    }
                }
                case FORCE_PAUSE -> {
                    if (Duration.between(stopped_at, Instant.now()).toMillis() >= delay) {
                        stopped_at = null;
                        smMach = SM_mach.RELEASE_PAUSE;
                    }
                    actuatorsState.set(sfeiMachine.getaStop().getOffset(), 1);
                }
                case RELEASE_PAUSE -> {
                    actuatorsState.set(sfeiMachine.getaStop().getOffset(), 0);
                    smMach = SM_mach.END;
                }
                case END -> {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SM_trans old_state = smTrans;
    private boolean old_sEmitter;
    boolean isRemoverON = false, isEmitterON = false;

    private void transportBetweenSFEIs(List<Object> discreteInputs_inMB, List<Object> coils_inMB, List<Object> discreteInputs_outMB, List<Object> coilsState_outMB, List<Object> holdRegs_outMB) {
        try {
            SFEI_transport sfeiTransport = (SFEI_transport) sfei;
            if (firstRun) {
                if (!transportConfiguration.equals(SFEM_transport.configuration.WH2RealSFEI) && !transportConfiguration.equals(SFEM_transport.configuration.WH2SFEI))
                    old_sRemover = (int) discreteInputs_inMB.get(sfeiTransport.getInSensor().getOffset()) == 1;
                if (!transportConfiguration.equals(SFEM_transport.configuration.RealSFEI2WH) && !transportConfiguration.equals(SFEM_transport.configuration.SFEI2WH))
                    old_sEmitter = (int) discreteInputs_outMB.get(sfeiTransport.getOutSensor().getOffset()) == 1;
                firstRun = false;
            }
            boolean sensor;
            switch (smTrans) {
                case INIT -> {
                    sensor = (int) discreteInputs_inMB.get(sfeiTransport.getInSensor().getOffset()) == 1;
                    if (sfeiTransport.getPartsATM().size() > 0) {
                        if (sfeiTransport.getPartsATM().first().getId() == part.getId() /*&& utility.getLogicalOperator().RE_detector(sensor, old_sRemover)*/) {
                            smTrans = SM_trans.REMOVING;
                        }
                    }
                    old_sRemover = sensor;
                }
                case REMOVING -> {
                    if (!isRemoverON) {
                        if (!transportConfiguration.equals(SFEM_transport.configuration.RealSFEI2WH)
                                && !transportConfiguration.equals(SFEM_transport.configuration.RealSFEI2SFEI)) {
                            coils_inMB.set(sfeiTransport.getaRemover().getOffset(), 1);
                        }
                        initial_t = Instant.now();
                        isRemoverON = true;
                        // PREPARE THE NEXT EMITTER for correct type
                        if (transportConfiguration.equals(SFEM_transport.configuration.SFEI2SFEI))
                            holdRegs_outMB.set(sfeiTransport.getaEmitPart().getOffset(), (int) Math.pow(2, getNumberbyPartAspect(part.getReality()) + 4 - 1));

                    }
                    sensor = (int) discreteInputs_inMB.get(sfeiTransport.getInSensor().getOffset()) == 1;

                    if (utils.getInstance().getLogicalOperator().FE_detector(sensor, old_sRemover)) {
                        if (!transportConfiguration.equals(SFEM_transport.configuration.RealSFEI2WH)
                                && !transportConfiguration.equals(SFEM_transport.configuration.RealSFEI2SFEI)) {
                            coils_inMB.set(sfeiTransport.getaRemover().getOffset(), 0);
                        }
                        isRemoverON = false;
                        smTrans = SM_trans.WAITING;
                    }
                    old_sRemover = sensor;
                }
                case WAITING -> {
                    if (Duration.between(initial_t, Instant.now()).toMillis() >= delay) {
                        if (transportConfiguration.equals(SFEM_transport.configuration.SFEI2WH)
                                || transportConfiguration.equals(SFEM_transport.configuration.RealSFEI2WH)) {
                            part.setState(models.base.part.status.PRODUCED);
                            smTrans = SM_trans.END;
                        } else {
                            smTrans = SM_trans.EMITTING;
                        }
                    } else {
                        if (transportConfiguration.equals(SFEM_transport.configuration.WH2SFEI)) {
                            // It will execute only once !
                            if (old_state.equals(SM_trans.INIT)) {
                                holdRegs_outMB.set(sfeiTransport.getaEmitPart().getOffset(), (int) Math.pow(2, getNumberbyPartAspect(part.getReality()) + 4 - 1));
                            }
                        }
                    }
                }
                case EMITTING -> {
                    if (!isEmitterON) {
                        coilsState_outMB.set(sfeiTransport.getaEmit().getOffset(), 1);
                        isEmitterON = true;
                    }
                    sensor = (int) discreteInputs_outMB.get(sfeiTransport.getOutSensor().getOffset()) == 1;
                    if (transportConfiguration.equals(SFEM_transport.configuration.WH2RealSFEI)
                            || transportConfiguration.equals(SFEM_transport.configuration.SFEI2RealSFEI)) {
                        if (utils.getInstance().getLogicalOperator().RE_detector(sensor, old_sEmitter)) {
                            coilsState_outMB.set(sfeiTransport.getaEmit().getOffset(), 0);
                            smTrans = SM_trans.END;
                        }

                    } else {
                        if (utils.getInstance().getLogicalOperator().FE_detector(sensor, old_sEmitter)) {
                            // The part was removed in this cycle by the SFEM_monitor, by the SFEI outSensor Activation
                            // To prove it, the execution of the following is done because the FE_detector
                            coilsState_outMB.set(sfeiTransport.getaEmit().getOffset(), 0);
                            smTrans = SM_trans.END;
                        }
                    }
                    old_sEmitter = sensor;
                }
            }
            old_state = smTrans;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private int getNumberbyPartAspect(partDescription aspect) {

        int num = -1;

        if (aspect.form().equals(partDescription.form.RAW)) {
            num = 0;
        } else if (aspect.form().equals(partDescription.form.BASE)) {
            num = 3;
        } else if (aspect.form().equals(partDescription.form.LID)) {
            num = 6;
        }
        if (aspect.material().equals(partDescription.material.BLUE))
            num++;
        else if (aspect.material().equals(partDescription.material.GREEN)) {
            num += 2;
        } else if (aspect.material().equals(partDescription.material.METAL)) {
            num += 3;
        }

        if (num == -1)
            throw new RuntimeException("Part Description == -1");
        return num;
    }

}
