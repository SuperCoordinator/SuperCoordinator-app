package models;

import communication.modbus;
//import monitor.time.conveyor;
import utils.utils;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SFEM {

    private String name;

    private final TreeMap<Integer, SFEE> SFEEs;

    private final ArrayList<part> productionHistory;
    utils utility;
    private final viewers.SFEM viewer;

    public SFEM(String name) {

        this.name = name;
        this.SFEEs = new TreeMap<>();
        this.productionHistory = new ArrayList<>();

        this.viewer = new viewers.SFEM();
        this.utility = new utils();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeMap<Integer, SFEE> getSFEEs() {
        return SFEEs;
    }

    public SFEE getSFEEbyIndex(int index) {
        return SFEEs.get(index);
    }

    public int addNewSFEE(SFEE newSFEE) {
        int index = SFEEs.size();
        SFEEs.put(index, newSFEE);
        return index;
    }


}
