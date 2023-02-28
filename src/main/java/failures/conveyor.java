/*
package failures;

import communication.modbus;
import models.sensor_actuator;
//import models.missingPart;
import models.part;
import utils.utils;

import java.stochastic.Duration;
import java.stochastic.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

public class conveyor implements Runnable {

    public enum ERROR_TYPE {
        INCREASE_LINEAR
    }

    private final String name;
    private modbus MB;

    private final ERROR_TYPE errorType;
    private double time_adjust_param;

    private final ArrayList<missingPart> missingParts;

    private final String aRemover;
    private final String aEmmiter;
    private final String sRemover;
    private final String sEmitter;

    private final boolean sRemover_inv;
    private final boolean sEmitter_inv;

    */
/**
     * @param errorType type of error to inject
     * @param param     value in percentage to increase the stochastic ]0-2]
     * @param senAct    mapping of the input/output following: sRemover,sEmitter,aRemover,aRemover
     * @param invValue  sensors value follows inverse logic? following:sRemover,sEmitter
     *//*

    public conveyor(String name, ERROR_TYPE errorType, double param, String[] senAct, boolean[] invValue) {
        this.name = name;
        this.errorType = errorType;
        this.time_adjust_param = param;
        this.missingParts = new ArrayList<>();
        this.sRemover = senAct[0];
        this.sEmitter = senAct[1];
        this.aRemover = senAct[2];
        this.aEmmiter = senAct[3];
        this.sRemover_inv = invValue[0];
        this.sEmitter_inv = invValue[1];
    }

    public String getName() {
        return name;
    }

    public ERROR_TYPE getErrorType() {
        return errorType;
    }

    public double getTime_adjust_param() {
        return time_adjust_param;
    }

    public void setTime_adjust_param(double param) {
        this.time_adjust_param = param;
    }

    public void setMB(modbus MB) {
        this.MB = MB;
    }

    @Override
    public void run() {

        try {
            if (MB != null)
                synchronized (MB) {
                    removeParts();
                    placeParts();
                }
            else throw new RuntimeException("Modbus Connection not defined!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final utils utils = new utils();
    private boolean old_b_remover = true;

    private void removeParts() {
        boolean b_remover = Boolean.parseBoolean(MB.readState(sRemover));
        if (sRemover_inv)
            b_remover = !b_remover;

        if (utils.getLogicalOperator().RE_detector(b_remover, old_b_remover)) {
            missingParts.add(new missingPart(new part(missingParts.size()), calculateDelay(), Instant.now()));
            MB.writeState(aRemover, "1");
        }
        if (utils.getLogicalOperator().FE_detector(b_remover, old_b_remover)) {
            // Turn off the remover
            MB.writeState(aRemover, "0");
        }
        old_b_remover = b_remover;
    }

    private boolean old_b_emitter = true;

    private void placeParts() {

        // Using the iterator because remove an object from the arrayList while search on it
        // can lead to memory errors
*/
/*
        if (missingPartsArrayList.size() > 0)
            System.out.println("missingParts len: " + missingPartsArrayList.size());
*//*

        try {
            Iterator<missingPart> iterator = missingParts.iterator();
            while (iterator.hasNext()) {
                missingPart part = iterator.next();
                if (part.isDisappear())
                    continue;
                Instant t_now = Instant.now();
                if (Duration.between(part.getT_init(), t_now).toSeconds() >= part.getDelay()) {
                    MB.writeState(aEmmiter, "1");
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean b_emitter = Boolean.parseBoolean(MB.readState(sEmitter));
        if (sEmitter_inv)
            b_emitter = !b_emitter;

        if (utils.getLogicalOperator().FE_detector(b_emitter, old_b_emitter)) {
            // Turn off the emitter
            MB.writeState(aEmmiter, "0");
        }
        old_b_emitter = b_emitter;
    }

    private double calculateDelay() {

        double rTime = 2; // stochastic in seconds

        switch (errorType) {
            case INCREASE_LINEAR -> {
                rTime = (rTime + time_adjust_param * rTime) - rTime;
            }
            default -> {
            }
        }

        return rTime;

    }

}
*/
