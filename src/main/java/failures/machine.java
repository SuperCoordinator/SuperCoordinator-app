/*
package failures;

import communication.modbus;
import models.sensor_actuator;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.TreeMap;

public class machine extends Thread {

    private enum grafState {
        WAITING,
        LOADING,
        UNLOADING,
        FORCE_PAUSE,
        RELEASE_PAUSE
    }

    public enum ERROR_TYPE {
        INCREASE_LINEAR
    }

    private final modbus MB;
    private final TreeMap<String, sensor_actuator> fieldObjs;

    private final machine.ERROR_TYPE errorType;

    */
/**
     * Default value 2s
     *//*

    private final int stopping_time;
    private final int safety_margin = 2;
    private final String sMachine_door;
    private final String aMachine_stop;
    private grafState currState;

    public machine(modbus MB, TreeMap<String, sensor_actuator> fieldObjs, String prefix, int stopping_time) {
        this.MB = MB;
        this.fieldObjs = fieldObjs;
        this.errorType = ERROR_TYPE.INCREASE_LINEAR;
        this.sMachine_door = prefix + "_opened";
        this.aMachine_stop = prefix + "_stop";
        this.stopping_time = stopping_time;
        this.currState = grafState.WAITING;
    }

    public machine(modbus MB, TreeMap<String, sensor_actuator> fieldObjs, String prefix) {
        this.MB = MB;
        this.fieldObjs = fieldObjs;
        this.errorType = ERROR_TYPE.INCREASE_LINEAR;
        this.sMachine_door = prefix + "_opened";
        this.aMachine_stop = prefix + "_stop";
        this.stopping_time = 2;
        this.currState = grafState.WAITING;
    }

    @Override
    public void run() {
        evalTransitions();
        executeActions();

    }

    private final utils utils = new utils();
    private Instant safety_margin_start, stopped_at;
    private boolean old_sMachine_door = true;

    private void evalTransitions() {
//        System.out.println("(failures/machine): " + currState);
        if (currState == grafState.WAITING) {
            boolean b_machine_door = Boolean.parseBoolean(MB.readState(sMachine_door));
            if (utils.getLogicalOperator().FE_detector(b_machine_door, old_sMachine_door)) {
                currState = grafState.LOADING;
            }
            old_sMachine_door = b_machine_door;

        } else if (currState == grafState.LOADING) {
            boolean b_machine_door = Boolean.parseBoolean(MB.readState(sMachine_door));
            if (utils.getLogicalOperator().RE_detector(b_machine_door, old_sMachine_door)) {
                currState = grafState.UNLOADING;
                safety_margin_start = Instant.now();
            }
            old_sMachine_door = b_machine_door;

        } else if (currState == grafState.UNLOADING) {
            if (Duration.between(safety_margin_start, Instant.now()).toSeconds() >= safety_margin) {
                currState = grafState.FORCE_PAUSE;
                safety_margin_start = null;
                stopped_at = Instant.now();
            }

        } else if (currState == grafState.FORCE_PAUSE) {
            if (Duration.between(stopped_at, Instant.now()).toSeconds() >= stopping_time) {
                stopped_at = null;
                currState = grafState.RELEASE_PAUSE;
            }

        } else if (currState == grafState.RELEASE_PAUSE) {
            currState = grafState.WAITING;
        }
    }

    private void executeActions() {
        switch (currState) {
            case FORCE_PAUSE -> {
                MB.writeState(aMachine_stop, "1");
            }
            case RELEASE_PAUSE -> {
                MB.writeState(aMachine_stop, "0");
            }
            default -> {
            }
        }
    }

}
*/
