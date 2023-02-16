package monitor;

import communication.modbus;
import models.sensor_actuator;
import models.part;
import utils.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

public class conveyors_timers extends Thread {

    private final String[] I = new String[]{
            "s_emitter",
            "s_entry_remover",
            "s_entry_emitter",
            "s_lids_at_entry",
            "s_lids_at_exit",
            "s_exit_remover",
            "s_exit_emitter",
            "s_remover"
    };
    private Instant[] timestamps;

    private final Boolean[] old_I;
    private final modbus MB;
    private final TreeMap<String, sensor_actuator> fieldObjs;
    private final ArrayList<part> productionParts;

    public conveyors_timers(modbus MB, TreeMap<String, sensor_actuator> fieldObjs, ArrayList<part> productionParts) {
        this.MB = MB;
        this.fieldObjs = fieldObjs;
        this.productionParts = productionParts;
        this.timestamps = new Instant[8];
        this.old_I = new Boolean[8];
        Arrays.fill(old_I, true);
    }

    @Override
    public void run() {
        /*conveyorTime("s_emitter", "s_lids_at_entry", 1);
        conveyorTime("s_lids_at_exit", "s_remover", 1);*/
        registerTimestamps();
        associateTimestamps();

        if (productionParts.size() > 0) {
            for (part p : productionParts)
                System.out.println(p.getId() + " " + Arrays.toString(p.getTimestamps()));
            System.out.println();
        }
    }

    private final utils utils = new utils();

    private void registerTimestamps() {
        try {
            for (int idx = 0; idx < I.length; idx++) {
                String sensor = I[idx];
                boolean sensValue = Boolean.parseBoolean(MB.readState(sensor));

                if (utils.getLogicalOperator().FE_detector(sensValue, old_I[idx]))
                    timestamps[idx] = Instant.now();

                old_I[idx] = sensValue;
//            System.out.println("(idx): " + idx + " s: " + sensor + " val:" + sensValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void associateTimestamps() {
        try {
            for (int idx = 0; idx < timestamps.length; idx++) {

                // Part in the s_emitter implies new part instance
                if (idx == 0 && timestamps[idx] != null) {
                    part newPart = new part(productionParts.size());
                    newPart.addTimestamp(Instant.now(), 0);
                    productionParts.add(newPart);
                    continue;
                }

                if (timestamps[idx] != null) {
                    // A part passed thought sensor idx
                    // That part is the first in the parts array that do not have the corresponding
                    // timestamp related to the idx
                    for (part p : productionParts) {
                        if (p.getTimestamps()[idx] == null) {
                            p.addTimestamp(timestamps[idx], idx);
                            break;
                        }
                    }
                }
            }
            // reset timestamps
            timestamps = new Instant[8];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Instant start_t = null;
    private boolean measuring = false;

    public void conveyorTime(String inSensor, String outSensor, int n_measures) {
        try {

            int runs = 0;
            while (runs < n_measures) {
                Instant end_t;
                while (true) {
                    boolean b_inSensor = Boolean.parseBoolean(MB.readState(inSensor));
                    boolean b_outSensor = Boolean.parseBoolean(MB.readState(outSensor));
                    if (!b_inSensor && !measuring) {
                        start_t = Instant.now();
                        measuring = true;
                    }

                    if (!b_outSensor && measuring) {
                        end_t = Instant.now();
                        measuring = false;
                        assert start_t != null;
                        System.out.println("(t) " + inSensor + "-" + outSensor + ": " + Duration.between(start_t, end_t).toSeconds());
                        break;
                    }
                }
                ++runs;
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

}
