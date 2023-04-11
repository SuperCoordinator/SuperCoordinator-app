package monitor.base;


import models.base.SFEM;
import models.part_prodTime;
import monitor.production.SFEM_production_monitor;
import monitor.transport.SFEM_transport_monitor;
import viewers.graphs.histogram;

import javax.xml.bind.annotation.*;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Instant;
import java.util.*;

public class SFEM_monitor  {
    private SFEM sfem;
    private TreeMap<Integer, Integer> productionTime_cnt = new TreeMap<>();

    private histogram graphs;
    private boolean isGraphsInited = false;
    private Instant init_t = Instant.now();

    public SFEM_monitor() {
    }

    public SFEM_monitor(SFEM sfem) {
        this.sfem = sfem;
    }

//    @XmlElement(name = "SFEM")
    public SFEM getSfem() {
        return sfem;
    }

    public TreeMap<Integer, Integer> getProductionTime_cnt() {
        return productionTime_cnt;
    }

    public histogram getGraphs() {
        return graphs;
    }

//    @XmlTransient
    public boolean isGraphsInited() {
        return isGraphsInited;
    }

    public void setGraphsInited(boolean graphsInited) {
        isGraphsInited = graphsInited;
    }

    public Instant getInit_t() {
        return init_t;
    }

    private boolean printedStats = false;

//    @XmlTransient
    public boolean isPrintedStats() {
        return printedStats;
    }

    public void setPrintedStats(boolean printedStats) {
        this.printedStats = printedStats;
    }

    private part_prodTime min_time = null;
    private part_prodTime max_time = null;
    private boolean firstRun = true;

    public void printStats(List<Long> runtime) {

        // One statistic (production mean-stochasticTime, max-stochasticTime and min-stochasticTime)
        System.out.println("Number of running Threads: " + Thread.activeCount());
        System.out.println("Cycle duration (ms): " + calculateRuntime(runtime));

        long total_time = 0;
        if (sfem.getProductionHistory().size() > 0) {
            for (Map.Entry<Integer, part_prodTime> entry : sfem.getProductionHistory().entrySet()) {
                part_prodTime pp = entry.getValue();
                if (firstRun) {
                    min_time = sfem.getProductionHistory().get(0);
                    max_time = sfem.getProductionHistory().get(0);
                    firstRun = false;
                }
                long p_prodTime = pp.production_time();
                if (p_prodTime < min_time.production_time()) {
                    min_time = pp;
//                            System.out.println("Min: " + Arrays.toString(min_time.getTimestamps().values().toArray()));
                }
                if (p_prodTime > max_time.production_time()) {
                    max_time = pp;
//                            System.out.println("Max: " + Arrays.toString(max_time.getTimestamps().values().toArray()));
                }
                total_time = total_time + p_prodTime;
            }
            assert max_time != null;
            System.out.println("Produced " + sfem.getProductionHistory().size() + " parts [min mean max] (s): "
                    + min_time.production_time() + " "
                    + total_time / sfem.getProductionHistory().size() + " "
                    + max_time.production_time());

        }

    }

    private long calculateRuntime(List<Long> runtime) {

        if (runtime.size() == 0)
            return 0;

        long totalRuntime = 0;
        for (Long run_t : runtime) {
            totalRuntime = totalRuntime + run_t;
        }
        return totalRuntime / runtime.size();
    }


    private void initGraphs() {
        if (productionTime_cnt.size() > 0) {
            List<histogram.intPair> list = getGraphLists();
            assert list != null;
            graphs = new histogram(sfem.getName(), list, "All production", "Last 20 parts");
            graphs.createWindow(list);
            isGraphsInited = true;
        }
    }

    public void updateGraphs() {
        if (!isGraphsInited)
            initGraphs();
        else
            graphs.updateSeries(getGraphLists());
    }


    private List<histogram.intPair> getGraphLists() {
        if (productionTime_cnt.size() == 0)
            return null;


        histogram.intPair pair = new histogram.intPair(productionTime_cnt.keySet().toArray(new Integer[0]), productionTime_cnt.values().toArray(new Integer[0]));
        histogram.intPair lastNparts = getLastNParts(20);

        List<histogram.intPair> list = new ArrayList<>();
        list.add(pair);
        list.add(lastNparts);

/*        System.out.println("All parts:" + Arrays.toString(pair.xData()) + " " + (Arrays.toString(pair.yData())));
        System.out.println("Last n parts:" + Arrays.toString(lastNparts.xData()) + " " + (Arrays.toString(lastNparts.yData())));*/
        return list;

    }

    private histogram.intPair getLastNParts(int n) {

        TreeMap<Integer, Integer> lastNParts = new TreeMap<>();


        ListIterator<part_prodTime> iterator = new ArrayList<>(sfem.getProductionHistory().values()).listIterator(sfem.getProductionHistory().size());
        int cnt = n;
        while (iterator.hasPrevious() && cnt > 0) {
            part_prodTime part = iterator.previous();
            if (cnt == n) {
                lastNParts.put(part.production_time(), 1);
            } else if (lastNParts.containsKey(part.production_time())) {
                int old_value = lastNParts.get(part.production_time());
                lastNParts.replace(part.production_time(), old_value, old_value + 1);
            } else {
                lastNParts.put(part.production_time(), 1);
            }
            cnt--;
        }

        return new histogram.intPair(lastNParts.keySet().toArray(new Integer[0]), lastNParts.values().toArray(new Integer[0]));
    }


}
