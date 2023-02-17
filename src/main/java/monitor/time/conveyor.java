package monitor.time;

import communication.modbus;
import communication.opcua;
import monitor.timestamp_pair;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class conveyor extends Thread {

    private String name;
    private final TreeMap<Integer, timestamp_pair> timestamps;
    private final String sStart;
    private final boolean invLogic_sStart;
    private final String sEnd;
    private final boolean invLogic_sEnd;
    private final String bitPartID;
    private int current_partID;
    private modbus mb;
    private opcua opcua;

    public conveyor(modbus mb, String name, String sStart, boolean invLogic_sStart, String sEnd, boolean invLogic_sEnd, boolean simulatePartsID, String partID_bitName) {
        this.mb = mb;
        this.name = name;
        this.timestamps = new TreeMap<>();
        this.sStart = sStart;
        this.invLogic_sStart = invLogic_sStart;
        this.invLogic_sEnd = invLogic_sEnd;
        this.sEnd = sEnd;

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
        this.invLogic_sStart = invLogic_sStart;
        this.invLogic_sEnd = invLogic_sEnd;
        this.sEnd = sEnd;

        if (!simulatePartsID) {
            bitPartID = partID_bitName;
        } else {
            bitPartID = "";
            current_partID = -1;
        }

    }

    @Override
    public void run() {
        System.out.println("Running Thread:" + Thread.currentThread().getName());
        // Assume-se que as peças são sequenciais
        // Caso contrário é necessário um identificador das mesmas
        // Para 1a implementação, o sensor de Start introduz um novo objeto no vetor
        // O sensor de End termina completa essa instancia
        try {
            synchronized (mb) {
                if (bitPartID.isBlank()) current_partID++;
                else current_partID = Integer.parseInt(mb.readState(bitPartID));

                // Assumindo sempre SEQUENCIAL a ordem das peças
                entryLook();
                exitLook();

                // Probably here see how much time passed and save the mean in some database
                // and clean local memory

                timestamps.forEach((key, value) -> System.out.println("(" + key + ") " + Arrays.toString(value.getPair())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void entryLook() {
        boolean sensvalue = Boolean.parseBoolean(mb.readState(sStart));
        if (invLogic_sStart)
            sensvalue = !sensvalue;

        if (sensvalue) timestamps.put(current_partID, new timestamp_pair(Instant.now()));
    }

    private void exitLook() {
        boolean sensvalue = Boolean.parseBoolean(mb.readState(sEnd));
        if (invLogic_sEnd)
            sensvalue = !sensvalue;
        if (sensvalue)
            timestamps.get(current_partID - 1).setSecondValue(Instant.now());
    }

//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public ArrayList<timestamp_pair> getTimestamps() {
//        return timestamps;
//    }
//
//    public void addTimestamp(timestamp_pair timestamp) {
//        this.timestamps.add(timestamp);
//    }
//
//    public String getsStart() {
//        return sStart;
//    }
//
//    public void setsStart(String sStart) {
//        this.sStart = sStart;
//    }
//
//    public String getsEnd() {
//        return sEnd;
//    }
//
//    public void setsEnd(String sEnd) {
//        this.sEnd = sEnd;
//    }
}
