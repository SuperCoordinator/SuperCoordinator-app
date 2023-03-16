package failures;

import communication.modbus;
import models.SFEI.SFEI;
import models.SFEI.SFEI_conveyor;
import models.SFEI.SFEI_machine;
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

    private SM_conv smConv;
    private SM_mach smMach;

    public enum timeOptions {
        GAUSSIAN,
        LINEAR
    }

    private final timeOptions timeType;

    // IN case of STOCHASTIC
    private String mean;
    private String std_dev;

    private final SFEI_conveyor sfeiConveyor;
    private final SFEI_machine sfeiMachine;
    private final SFEI.SFEI_type sfeiType;
    private final int partID;
    private final long delay;

    private final utils utility;

    public stochasticTime(SFEI sfei, int partID, timeOptions timeType, String[] formulas, int minSFEEOperationTime) {

        if (sfei.getSfeiType().equals(SFEI.SFEI_type.CONVEYOR)) {
            this.sfeiConveyor = (SFEI_conveyor) sfei;
            this.sfeiMachine = null;
            this.sfeiType = sfeiConveyor.getSfeiType();
        } else if (sfei.getSfeiType().equals(SFEI.SFEI_type.MACHINE)) {
            this.sfeiMachine = (SFEI_machine) sfei;
            this.sfeiConveyor = null;
            this.sfeiType = sfeiMachine.getSfeiType();
        } else {
            this.sfeiConveyor = null;
            this.sfeiMachine = null;
            this.sfeiType = null;
        }

        this.partID = partID;
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
    }


    public boolean isConveyorFinished() {
        return smConv.equals(SM_conv.END);
    }

    public boolean isMachineFinished() {
        return smMach.equals(SM_mach.END);
    }

    public void loop(List<Object> sensorsState, List<Object> actuatorsState) {

        if (sfeiType.equals(SFEI.SFEI_type.CONVEYOR)) {

            if (sfeiConveyor.isSimulation()) {
                // F_IO scene, so have REMOVER and EMITTER
                injectFailureF_IOConv(sensorsState, actuatorsState);
            } else {
                // OTHER simulation, so have only a STOP bit
                injectFailureSimConv();
            }

        } else if (sfeiType.equals(SFEI.SFEI_type.MACHINE)) {
            injectFailuresMach(sensorsState, actuatorsState);
        }

    }

    private boolean old_sRemover = false, old_sEmitter = false;
    private Instant initial_t;
    boolean isRemoverON = false, isEmitterON = false;

    private void injectFailureF_IOConv(List<Object> sensorsState, List<Object> actuatorsState) {

        boolean sensor;
        switch (smConv) {
            case INIT -> {
                sensor = (int) sensorsState.get(sfeiConveyor.getsRemover().bit_offset()) == 1;
                if (sfeiConveyor.getPartsATM().size() > 0) {
                    if (sfeiConveyor.getPartsATM().first().getId() == partID && utility.getLogicalOperator().RE_detector(sensor, old_sRemover)) {
                        smConv = SM_conv.REMOVING;
                    }
                }
                old_sRemover = sensor;
            }
            case REMOVING -> {
                if (!isRemoverON) {
                    actuatorsState.set(sfeiConveyor.getaRemover().bit_offset(), 1);
                    initial_t = Instant.now();
                    isRemoverON = true;
                }
                sensor = (int) sensorsState.get(sfeiConveyor.getsRemover().bit_offset()) == 1;
                if (utility.getLogicalOperator().FE_detector(sensor, old_sRemover)) {
                    actuatorsState.set(sfeiConveyor.getaRemover().bit_offset(), 0);
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
                    actuatorsState.set(sfeiConveyor.getaEmitter().bit_offset(), 1);
                    isEmitterON = true;
                }
                sensor = (int) sensorsState.get(sfeiConveyor.getsEmitter().bit_offset()) == 1;
                if (sfeiConveyor.getPartsATM().last().getId() == partID && utility.getLogicalOperator().FE_detector(sensor, old_sEmitter)) {
                    actuatorsState.set(sfeiConveyor.getaEmitter().bit_offset(), 0);
                    smConv = SM_conv.END;
                }
                old_sEmitter = sensor;
            }
            default -> {
            }
        }
    }

    private void injectFailureSimConv() {
    }

    private Instant safety_margin_start, stopped_at;
    private boolean old_sMachine_door = false;

    private void injectFailuresMach(List<Object> sensorsState, List<Object> actuatorsState) {

        switch (smMach) {
            case WAITING -> {
                boolean b_machine_door = (int) sensorsState.get(sfeiMachine.getsDoor().bit_offset()) == 1;
                if (sfeiMachine.getPartsATM().size() > 0) {
                    if (sfeiMachine.getPartsATM().first().getId() == partID && utility.getLogicalOperator().FE_detector(b_machine_door, old_sMachine_door)) {
                        smMach = SM_mach.LOADING;
                    }
                }
                old_sMachine_door = b_machine_door;
            }
            case LOADING -> {
                boolean b_machine_door = (int) sensorsState.get(sfeiMachine.getsDoor().bit_offset()) == 1;
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
                actuatorsState.set(sfeiMachine.getaStop().bit_offset(), 1);
            }
            case RELEASE_PAUSE -> {
                actuatorsState.set(sfeiMachine.getaStop().bit_offset(), 0);
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
        } else {
            sfei = sfeiMachine;
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

}
