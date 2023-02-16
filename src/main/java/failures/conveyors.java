package failures;

import communication.modbus;
import models.sensor_actuator;
import models.missingParts;
import models.part;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

public class conveyors extends Thread {

    public enum ERROR_TYPE {
        INCREASE_LINEAR
    }

    private final modbus MB;
    private final TreeMap<String, sensor_actuator> fieldObjs;

    private final ERROR_TYPE errorType;

    private final ArrayList<part> productionParts;
    private final ArrayList<missingParts> missingPartsArrayList;

    private final String aRemover;
    private final String aEmmiter;

    private final String sRemover;
    private final String sEmitter;

    public conveyors(modbus MB, TreeMap<String, sensor_actuator> fieldObjs, String prefix, ArrayList<part> productionParts) {
        this.MB = MB;
        this.fieldObjs = fieldObjs;
        this.errorType = ERROR_TYPE.INCREASE_LINEAR;
        this.productionParts = productionParts;
        this.missingPartsArrayList = new ArrayList<>();
        this.sRemover = "s_" + prefix + "_remover";
        this.sEmitter = "s_" + prefix + "_emitter";
        this.aRemover = prefix + "_remover";
        this.aEmmiter = prefix + "_emitter";

    }

    @Override
    public void run() {
        removeParts();
        placeParts();
    }

    private final utils utils = new utils();
    private boolean old_b_remover = true;

    private void removeParts() {
        boolean b_remover = Boolean.parseBoolean(MB.readState(sRemover));

        if (utils.getLogicalOperator().FE_detector(b_remover, old_b_remover)) {
            missingPartsArrayList.add(new missingParts(productionParts.get(productionParts.size() - 1), Instant.now()));
            MB.writeState(aRemover, "1");
        }
        if (utils.getLogicalOperator().RE_detector(b_remover, old_b_remover)) {
            // Turn off the remover
            MB.writeState(aRemover, "0");
        }
        old_b_remover = b_remover;
    }

    private boolean old_b_emitter = true;

    private void placeParts() {

        // Using the iterator because remove an object from the arrayList while search on it
        // can lead to memory errors
/*
        if (missingPartsArrayList.size() > 0)
            System.out.println("missingParts len: " + missingPartsArrayList.size());
*/
        try {
            Iterator<missingParts> iterator = missingPartsArrayList.iterator();
            while (iterator.hasNext()) {
                missingParts part = iterator.next();
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

        if (utils.getLogicalOperator().RE_detector(b_emitter, old_b_emitter)) {
            // Turn off the emitter
            MB.writeState(aEmmiter, "0");
        }
        old_b_emitter = b_emitter;
    }

}
