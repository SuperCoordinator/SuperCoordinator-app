package failures;

import models.SFEx_particular.SFEI_transport;
import models.base.SFEI;
import models.SFEx_particular.SFEI_conveyor;
import models.SFEx_particular.SFEI_machine;
import models.base.part;
import models.partDescription;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class stochasticTime {

    private enum SM_conv {
        INIT,
        REMOVING,
        WAITING,
        EMITTING,
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
    private SM_mach smMach;
    private SM_trans smTrans;

    public enum timeOptions {
        GAUSSIAN,
        LINEAR
    }

    private final timeOptions timeType;

    // timeOptions  == GAUSSIAN -> [ mean, std_dev ]
    //                 LINEAR   -> mean
    private String mean;
    private String std_dev;

    private final SFEI_conveyor sfeiConveyor;
    private final SFEI_machine sfeiMachine;

    private final SFEI_transport sfeiTransport;
    private final SFEI.SFEI_type sfeiType;
    private final part part;
    private final long delay;

    private final utils utility;

    public stochasticTime(SFEI sfei, part part, timeOptions timeType, String[] formulas, int minSFEEOperationTime) {

        if (sfei.getSfeiType().equals(SFEI.SFEI_type.CONVEYOR)) {
            this.sfeiConveyor = (SFEI_conveyor) sfei;
            this.sfeiMachine = null;
            this.sfeiTransport = null;
            this.sfeiType = sfeiConveyor.getSfeiType();
        } else if (sfei.getSfeiType().equals(SFEI.SFEI_type.MACHINE)) {
            this.sfeiMachine = (SFEI_machine) sfei;
            this.sfeiConveyor = null;
            this.sfeiTransport = null;
            this.sfeiType = sfeiMachine.getSfeiType();
        } else if (sfei.getSfeiType().equals(SFEI.SFEI_type.TRANSPORT)) {
            this.sfeiTransport = (SFEI_transport) sfei;
            this.sfeiMachine = null;
            this.sfeiConveyor = null;
            this.sfeiType = sfeiTransport.getSfeiType();
        } else {
            this.sfeiConveyor = null;
            this.sfeiMachine = null;
            this.sfeiType = null;
            this.sfeiTransport = null;
        }

        this.part = part;
        this.timeType = timeType;

        if (timeType.equals(timeOptions.GAUSSIAN)) {
            this.mean = formulas[0];
            this.std_dev = formulas[1];
        } else if (timeType.equals(timeOptions.LINEAR)) {
            this.mean = formulas[0];
            this.std_dev = " ";
        }
        this.utility = new utils();

        this.delay = calculateDelay(minSFEEOperationTime);
        this.smConv = SM_conv.INIT;
        this.smMach = SM_mach.WAITING;
        this.smTrans = SM_trans.INIT;
    }


    public boolean isConveyorFinished() {
        return smConv.equals(SM_conv.END);
    }

    public boolean isMachineFinished() {
        return smMach.equals(SM_mach.END);
    }

    public boolean isTransportFinished() {
        return smTrans.equals(SM_trans.END);
    }

    public void loop(ArrayList<List<Object>> sensorsState, ArrayList<List<Object>> actuatorsState) {
        try {
            if (sfeiType.equals(SFEI.SFEI_type.CONVEYOR)) {

                if (sfeiConveyor.isSimulation()) {
                    // F_IO scene, so have REMOVER and EMITTER
                    injectFailureF_IOConv(sensorsState.get(0), actuatorsState.get(0));
                } else {
                    // OTHER simulation, so have only a STOP bit
                    injectFailureSimConv();
                }

            } else if (sfeiType.equals(SFEI.SFEI_type.MACHINE)) {
                injectFailuresMach(sensorsState.get(0), actuatorsState.get(0));
            } else if (sfeiType.equals(SFEI.SFEI_type.TRANSPORT)) {
                transportBetweenSFEis(sensorsState.get(0), actuatorsState.get(0),
                        sensorsState.get(1), actuatorsState.get(1), actuatorsState.get(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean old_sRemover = false, old_sEmitter = false;
    private Instant initial_t;
    boolean isRemoverON = false, isEmitterON = false;

    private void injectFailureF_IOConv(List<Object> sensorsState, List<Object> actuatorsState) {
        try {
            boolean sensor;
            switch (smConv) {
                case INIT -> {
                    sensor = (int) sensorsState.get(sfeiConveyor.getsRemover().getBit_offset()) == 1;
                    if (sfeiConveyor.getPartsATM().size() > 0) {
                        if (sfeiConveyor.getPartsATM().first().getId() == part.getId() && utility.getLogicalOperator().RE_detector(sensor, old_sRemover)) {
                            smConv = SM_conv.REMOVING;
                        }
                    }
                    old_sRemover = sensor;
                }
                case REMOVING -> {
                    if (!isRemoverON) {
                        actuatorsState.set(sfeiConveyor.getaRemover().getBit_offset(), 1);
                        initial_t = Instant.now();
                        isRemoverON = true;
                    }
                    sensor = (int) sensorsState.get(sfeiConveyor.getsRemover().getBit_offset()) == 1;
                    if (utility.getLogicalOperator().FE_detector(sensor, old_sRemover)) {
                        actuatorsState.set(sfeiConveyor.getaRemover().getBit_offset(), 0);
                        isRemoverON = false;
                        smConv = SM_conv.WAITING;
                    }
                    old_sRemover = sensor;
                }
                case WAITING -> {
                    if (Duration.between(initial_t, Instant.now()).toSeconds() >= delay) {
                        smConv = SM_conv.EMITTING;
                    }
                }
                case EMITTING -> {
                    if (!isEmitterON) {
                        actuatorsState.set(sfeiConveyor.getaEmitter().getBit_offset(), 1);
                        isEmitterON = true;
                    }
                    sensor = (int) sensorsState.get(sfeiConveyor.getsEmitter().getBit_offset()) == 1;
                    if (sfeiConveyor.getPartsATM().last().getId() == part.getId() && utility.getLogicalOperator().FE_detector(sensor, old_sEmitter)) {
                        actuatorsState.set(sfeiConveyor.getaEmitter().getBit_offset(), 0);
                        smConv = SM_conv.END;
                    }
                    old_sEmitter = sensor;
                }
                default -> {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void injectFailureSimConv() {
    }

    private Instant safety_margin_start, stopped_at;
    private boolean old_sMachine_door = false;

    private void injectFailuresMach(List<Object> sensorsState, List<Object> actuatorsState) {

        switch (smMach) {
            case WAITING -> {
                boolean b_machine_door = (int) sensorsState.get(sfeiMachine.getsDoor().getBit_offset()) == 1;
                if (sfeiMachine.getPartsATM().size() > 0) {
                    if (sfeiMachine.getPartsATM().first().getId() == part.getId() && utility.getLogicalOperator().FE_detector(b_machine_door, old_sMachine_door)) {
                        smMach = SM_mach.LOADING;
                    }
                }
                old_sMachine_door = b_machine_door;
            }
            case LOADING -> {
                boolean b_machine_door = (int) sensorsState.get(sfeiMachine.getsDoor().getBit_offset()) == 1;
                if (utility.getLogicalOperator().RE_detector(b_machine_door, old_sMachine_door)) {
                    smMach = SM_mach.UNLOADING;
                    safety_margin_start = Instant.now();
                }
                old_sMachine_door = b_machine_door;
            }
            case UNLOADING -> {
                int safety_margin = 2;
                if (Duration.between(safety_margin_start, Instant.now()).toSeconds() >= safety_margin) {
                    smMach = SM_mach.FORCE_PAUSE;
                    safety_margin_start = null;
                    stopped_at = Instant.now();
                }
            }
            case FORCE_PAUSE -> {
                if (Duration.between(stopped_at, Instant.now()).toSeconds() >= delay) {
                    stopped_at = null;
                    smMach = SM_mach.RELEASE_PAUSE;
                }
                actuatorsState.set(sfeiMachine.getaStop().getBit_offset(), 1);
            }
            case RELEASE_PAUSE -> {
                actuatorsState.set(sfeiMachine.getaStop().getBit_offset(), 0);
                smMach = SM_mach.END;
            }
            case END -> {

            }
        }

    }

    private int calculateDelay(int sumSFEEminOperationTime) {

        Random random = new Random();
        random.setSeed(3587214);
        SFEI sfei;
        if (sfeiType.equals(SFEI.SFEI_type.CONVEYOR)) {
            sfei = sfeiConveyor;
        } else if (sfeiType.equals(SFEI.SFEI_type.MACHINE)) {
            sfei = sfeiMachine;
        } else {
            sfei = sfeiTransport;
        }

        double m = utility.getCustomCalc().calcExpression(mean,
                sfei.getnPiecesMoved(),
                (double) Duration.between(sfei.getDayOfBirth(), Instant.now()).toDays(),
                (double) Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toDays());

        double total_Time;
        if (timeType.equals(timeOptions.GAUSSIAN)) {

            double dev = utility.getCustomCalc().calcExpression(std_dev,
                    sfei.getnPiecesMoved(),
                    (double) Duration.between(sfei.getDayOfBirth(), Instant.now()).toDays(),
                    (double) Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toDays());

//            total_Time = random.nextGaussian() * Math.sqrt(dev) + m;
            total_Time = random.nextGaussian() * dev + m;
            System.out.println("Calculated Mean: " + m + " and dev:" + dev + " with total time of: " + total_Time);
        } else {
            total_Time = m;
            System.out.println("Calculated Mean: " + m + " with total time of: " + total_Time);
        }

        total_Time = total_Time - sumSFEEminOperationTime;

        System.out.println("Delay " + total_Time + " on SFEI:" + sfei.getName());
        if (total_Time < 0)
            return 0;

        return (int) Math.round(total_Time);
    }

    private SM_trans old_state = smTrans;

    private void transportBetweenSFEis(List<Object> discreteInputs_inMB, List<Object> coils_inMB, List<Object> discreteInputs_outMB, List<Object> coilsState_outMB, List<Object> holdRegs_outMB) {
        try {
            boolean sensor;
            switch (smTrans) {
                case INIT -> {
                    sensor = (int) discreteInputs_inMB.get(sfeiTransport.getInSensor().getBit_offset()) == 1;
                    if (sfeiTransport.getPartsATM().size() > 0) {
                        if (sfeiTransport.getPartsATM().first().getId() == part.getId() /*&& utility.getLogicalOperator().RE_detector(sensor, old_sRemover)*/) {
                            smTrans = SM_trans.REMOVING;
                        }
                    }
                    old_sRemover = sensor;
                }
                case REMOVING -> {
                    if (!isRemoverON) {
                        coils_inMB.set(sfeiTransport.getaRemover().getBit_offset(), 1);
                        initial_t = Instant.now();
                        isRemoverON = true;
                        // PREPARE THE NEXT EMITTER for correct type
                        // +5 to ignore boxes [1;4] boxes, as well as 14
                        holdRegs_outMB.set(sfeiTransport.getaEmitterPart().getBit_offset(), (int) Math.pow(2, getNumberbyPartAspect(part.getReality()) + 4 - 1));

                    }
                    sensor = (int) discreteInputs_inMB.get(sfeiTransport.getInSensor().getBit_offset()) == 1;
                    if (utility.getLogicalOperator().FE_detector(sensor, old_sRemover)) {
                        coils_inMB.set(sfeiTransport.getaRemover().getBit_offset(), 0);
                        isRemoverON = false;
                        smTrans = SM_trans.WAITING;
                    }
                    old_sRemover = sensor;
                }
                case WAITING -> {
                    if (Duration.between(initial_t, Instant.now()).toSeconds() >= delay) {
                        smTrans = SM_trans.EMITTING;
                    }
                }
                case EMITTING -> {
                    if (!isEmitterON) {
                        // NO BASE -> testing if 0 works....
                        holdRegs_outMB.set(sfeiTransport.getaEmitterBase().getBit_offset(), 0);
                        // +5 to ignore boxes [1;4] boxes, as well as 14
                        holdRegs_outMB.set(sfeiTransport.getaEmitterPart().getBit_offset(), (int) Math.pow(2, getNumberbyPartAspect(part.getReality()) + 4 - 1));

                        coilsState_outMB.set(sfeiTransport.getaEmitter().getBit_offset(), 1);
                        isEmitterON = true;
                    }
                    sensor = (int) discreteInputs_outMB.get(sfeiTransport.getOutSensor().getBit_offset()) == 1;
/*                if (sfeiTransport.getPartsATM().size() > 0) {
                    if (sfeiTransport.getPartsATM().last().getId() == part.getId() && utility.getLogicalOperator().FE_detector(sensor, old_sEmitter)) {
                        coilsState_outMB.set(sfeiTransport.getaEmitter().bit_offset(), 0);
                        smTrans = SM_trans.END;
                    }
                } else*/
                    if (/*sfeiTransport.getPartsATM().size() == 0 &&*/ utility.getLogicalOperator().FE_detector(sensor, old_sEmitter)) {
                        // The part was removed in this cycle by the SFEM_monitor, by the SFEI outSensor Activation
                        // To prove it, the execution of the following is done because the FE_detector
                        coilsState_outMB.set(sfeiTransport.getaEmitter().getBit_offset(), 0);
                        smTrans = SM_trans.END;
                    }
                    old_sEmitter = sensor;
                }
                default -> {
                }
            }
/*        if (old_state != smTrans)
            System.out.println(smTrans);*/

            old_state = smTrans;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getNumberbyPartAspect(partDescription aspect) {

        int num;

        if (aspect.form().equals(partDescription.form.RAW)) {
            num = 0;
        } else if (aspect.form().equals(partDescription.form.BASE)) {
            num = 3;
        } else {
            num = 6;
        }
        if (aspect.material().equals(partDescription.material.BLUE))
            num++;
        else if (aspect.material().equals(partDescription.material.GREEN)) {
            num += 2;
        } else {
            num += 3;
        }

        return num;

    }

}
