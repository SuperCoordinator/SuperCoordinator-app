package failures;

import communication.modbus;
import models.SFEI.SFEI;
import models.SFEI.SFEI_conveyor;
import models.SFEI.SFEI_machine;
import utils.utils;

import java.time.Duration;
import java.time.Instant;

public class stochastic implements Runnable {

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

    private final SFEI_conveyor sfeiConveyor;
    private final SFEI_machine sfeiMachine;
    private final SFEI.SFEI_type sfeiType;
    private final int partID;
    private final long delay;
    private final modbus mb;


    private final utils utility;

    public stochastic(SFEI sfei, int partID, int delay, modbus mb) {

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
        this.delay = delay;
        this.mb = mb;

        this.utility = new utils();
        this.smConv = SM_conv.INIT;
        this.smMach = SM_mach.WAITING;
    }

    @Override
    public void run() {
        while (!smConv.equals(SM_conv.END) && !smMach.equals(SM_mach.END)) {
            synchronized (mb) {

                if (sfeiType.equals(SFEI.SFEI_type.CONVEYOR)) {

                    if (sfeiConveyor.isSimulation()) {
                        // F_IO scene, so have REMOVER and EMITTER
                        injectFailureF_IOConv();
                    } else {
                        // OTHER simulation, so have only a STOP bit
                        injectFailureSimConv();
                    }

                } else if (sfeiType.equals(SFEI.SFEI_type.MACHINE)) {
                    injectFailuresMach();
                }

            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private boolean old_sRemover = false, old_sEmitter = false;
    private Instant initial_t;
    boolean isRemoverON = false, isEmitterON = false;

    private void injectFailureF_IOConv() {

        boolean sensor;
        switch (smConv) {
            case INIT -> {
                sensor = Boolean.parseBoolean(mb.readState(sfeiConveyor.getsRemover()));
                if (sfeiConveyor.getPartsATM().size() > 0) {
                    if (sfeiConveyor.getPartsATM().first().getId() == partID && utility.getLogicalOperator().RE_detector(sensor, old_sRemover)) {
                        smConv = SM_conv.REMOVING;
                    }
                }
                old_sRemover = sensor;
            }
            case REMOVING -> {
                sensor = Boolean.parseBoolean(mb.readState(sfeiConveyor.getsRemover()));
                if (!isRemoverON) {
                    mb.writeState(sfeiConveyor.getaRemover(), "1");
                    initial_t = Instant.now();
                    isRemoverON = true;
                }

                if (utility.getLogicalOperator().FE_detector(sensor, old_sRemover)) {
                    mb.writeState(sfeiConveyor.getaRemover(), "0");
                    isRemoverON = false;
                    smConv = SM_conv.WAITING;
                }
                old_sRemover = sensor;
            }
            case WAITING -> {
                if (Duration.between(initial_t, Instant.now()).toSeconds() >= delay)
                    smConv = SM_conv.EMITTING;

            }
            case EMITTING -> {
                sensor = Boolean.parseBoolean(mb.readState(sfeiConveyor.getsEmitter()));
                if (!isEmitterON) {
                    mb.writeState(sfeiConveyor.getaEmitter(), "1");
                    isEmitterON = true;
                }
                if (utility.getLogicalOperator().FE_detector(sensor, old_sEmitter)) {
                    // Turn off the emitter
                    mb.writeState(sfeiConveyor.getaEmitter(), "0");
                    smConv = SM_conv.END;

                }
                old_sEmitter = sensor;
            }
            case END -> {

            }
            default -> {
            }
        }
/*
        do {
            oldSensor = Boolean.parseBoolean(mb.readState(sfeiConveyor.getsRemover()));

            sensor = Boolean.parseBoolean(mb.readState(sfeiConveyor.getsRemover()));
        } while (sfeiConveyor.getPartsATM().first().getId() != partID && !utility.getLogicalOperator().RE_detector(sensor, oldSensor));


        do {
            if (isRemoverON) {
                oldSensor = Boolean.parseBoolean(mb.readState(sfeiConveyor.getsRemover()));

                sensor = Boolean.parseBoolean(mb.readState(sfeiConveyor.getsRemover()));
                if (utility.getLogicalOperator().FE_detector(sensor, oldSensor)) {
                    mb.writeState(sfeiConveyor.getaRemover(), "0");
                    isRemoverON = false;
                }
            }
        } while (Duration.between(initial_T, Instant.now()).toSeconds() < delay);

        mb.writeState(sfeiConveyor.getaEmitter(), "1");

        do {
            oldSensor = Boolean.parseBoolean(mb.readState(sfeiConveyor.getsEmitter()));

            sensor = Boolean.parseBoolean(mb.readState(sfeiConveyor.getsEmitter()));
        }
        while (!utility.getLogicalOperator().FE_detector(sensor, oldSensor));
        // Turn off the emitter
        mb.writeState(sfeiConveyor.getaEmitter(), "0");*/

    }

    private void injectFailureSimConv() {

    }

    private Instant safety_margin_start, stopped_at;
    private boolean old_sMachine_door = false;

    private void injectFailuresMach() {

        switch (smMach) {
            case WAITING -> {
                boolean b_machine_door = Boolean.parseBoolean(mb.readState(sfeiMachine.getsDoor()));
                if (sfeiMachine.getPartsATM().size() > 0) {
                    if (sfeiMachine.getPartsATM().first().getId() == partID && utility.getLogicalOperator().FE_detector(b_machine_door, old_sMachine_door)) {
                        smMach = SM_mach.LOADING;
                    }
                }
                old_sMachine_door = b_machine_door;
            }
            case LOADING -> {
                boolean b_machine_door = Boolean.parseBoolean(mb.readState(sfeiMachine.getsDoor()));
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

                mb.writeState(sfeiMachine.getaStop(), "1");
            }
            case RELEASE_PAUSE -> {
                mb.writeState(sfeiMachine.getaStop(), "0");
                smMach = SM_mach.END;
            }
            case END -> {

            }
        }


    }

}
