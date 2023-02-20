package monitor.time;

import communication.modbus;
import communication.opcua;
import monitor.timestamp_pair;
import utils.logicalOperators;
import utils.utils;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

public class conveyor implements Runnable {

    private final String name;
    private final TreeMap<Integer, timestamp_pair> timestamps;

    private boolean old_sStart;
    private final String sStart;
    private final boolean invLogic_sStart;
    private boolean old_sEnd;
    private final String sEnd;
    private final boolean invLogic_sEnd;
    private final String bitPartID;
    private int current_partID;
    private modbus mb;
    private opcua opcua;

    private final utils util = new utils();

    public conveyor(modbus mb, String name, String sStart, boolean invLogic_sStart, String sEnd, boolean invLogic_sEnd, boolean simulatePartsID, String partID_bitName) {
        this.mb = mb;
        this.name = name;
        this.timestamps = new TreeMap<>();
        this.sStart = sStart;
        this.old_sStart = invLogic_sStart;
        this.invLogic_sStart = invLogic_sStart;
        this.sEnd = sEnd;
        this.old_sEnd = invLogic_sEnd;
        this.invLogic_sEnd = invLogic_sEnd;

        if (!simulatePartsID) {
            bitPartID = partID_bitName;
        } else {
            bitPartID = "";
            current_partID = -1;
        }
    }

    public conveyor(opcua opcua, String name, String sStart, boolean invLogic_sStart, String sEnd, boolean invLogic_sEnd, boolean simulatePartsID, String partID_bitName) {
        this.opcua = opcua;
        this.name = name;
        this.timestamps = new TreeMap<>();
        this.sStart = sStart;
        this.old_sStart = invLogic_sStart;
        this.invLogic_sStart = invLogic_sStart;
        this.sEnd = sEnd;
        this.old_sEnd = invLogic_sEnd;
        this.invLogic_sEnd = invLogic_sEnd;

        if (!simulatePartsID) {
            bitPartID = partID_bitName;
        } else {
            bitPartID = "";
            current_partID = -1;
        }

    }

    @Override
    public void run() {
//        System.out.println("Running Thread:" + Thread.currentThread().getName());
        // Assume-se que as peças são sequenciais
        // Caso contrário é necessário um identificador das mesmas
        // Para 1a implementação, o sensor de Start introduz um novo objeto no vetor
        // O sensor de End termina completa essa instancia
        try {
            synchronized (mb) {

                // Assumindo sempre SEQUENCIAL a ordem das peças
                entryLook();
                exitLook();

                // Probably here see how much time passed and save the mean in some database
                // and clean local memory

                infoMessage();

//                timestamps.forEach((key, value) -> System.out.println("(" + key + ") " + Arrays.toString(value.getPair())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void entryLook() {
        boolean sensvalue = Boolean.parseBoolean(mb.readState(sStart));
        if (invLogic_sStart) sensvalue = !sensvalue;

        boolean RE = util.getLogicalOperator().RE_detector(sensvalue, old_sStart);

        if (RE) {
            if (bitPartID.isBlank()) current_partID++;
            else current_partID = Integer.parseInt(mb.readState(bitPartID));
            timestamps.put(current_partID, new timestamp_pair(Instant.now()));
        }
        old_sStart = sensvalue;
    }

    private void exitLook() {
        boolean sensvalue = Boolean.parseBoolean(mb.readState(sEnd));
        if (invLogic_sEnd) sensvalue = !sensvalue;
        boolean RE = util.getLogicalOperator().RE_detector(sensvalue, old_sEnd);

        if (RE) timestamps.get(timestamps.size() - 1).setSecondValue(Instant.now());
        old_sEnd = sensvalue;
    }

    private boolean showed;

    private void infoMessage() {

        // Every 5 pieces show message

        if (timestamps.size() % 5 == 0 && !showed) {
            if (timestamps.size() > 0) {
                long duration = 0;
                int discount = 0;
                for (Map.Entry<Integer, timestamp_pair> entry : timestamps.entrySet()) {
                    if (entry.getValue().getPair()[0] != null && entry.getValue().getPair()[1] != null) {
                        duration = duration + entry.getValue().getDuration().toSeconds();
                    } else
                        discount++;

                    System.out.println("(" + entry.getKey() + ") " + Arrays.toString(entry.getValue().getPair()));
                }
                DecimalFormat df = new DecimalFormat("#.###");
                System.out.println("(" + name + ")Thread:" + Thread.currentThread().getName());
                System.out.println(timestamps.size() + " parts moved with mean time " + df.format(duration / (timestamps.size() - discount)) + " s");
                System.out.println();
                showed = true;
            }
        } else if (timestamps.size() % 5 != 0)
            showed = false;
    }

}
