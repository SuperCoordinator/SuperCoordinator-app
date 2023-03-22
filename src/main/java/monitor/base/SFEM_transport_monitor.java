package monitor.base;

import models.SFEx_particular.SFEM_production;
import models.SFEx_particular.SFEM_transport;
import models.base.SFEE;
import models.base.SFEI;
import models.base.part;
import models.part_prodTime;
import viewers.graphs.histogram;
import viewers.graphs.histogram.intPair;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class SFEM_transport_monitor {


    private final SFEM_transport sfem;

    private final TreeMap<Integer, Integer> productionTime_cnt;

    private histogram graphs;

    private final Instant init_t = Instant.now();

    public SFEM_transport_monitor(SFEM_transport sfem) {
        this.sfem = sfem;
        this.productionTime_cnt = new TreeMap<>();
    }

    private boolean isGraphsInited = false;

    private void initGraphs() {
        if (productionTime_cnt.size() > 0) {
            List<intPair> list = getGraphLists();
            assert list != null;
            graphs = new histogram(list, "All production", "Last 10 parts");
            graphs.createWindow(list);
            isGraphsInited = true;
        }
    }

    // Avaliador de peças defeituosas baseada no tipo e no tempo de processamento

    public void loop() {
        try {
            if (Duration.between(init_t, Instant.now()).toSeconds() % 5 == 0) {
                if (!printedStats) {
                    // will check the parts from the SFEE and save them into history

                    SFEI lastSFEI_of_SFEE = sfem.getSfeeTransport().getSFEIbyIndex(0);
                    Iterator<part> iterator = lastSFEI_of_SFEE.getPartsATM().iterator();
                    while (iterator.hasNext()) {
                        part p = iterator.next();
                        if (!p.isProduced() && !p.isWaitTransport()) {
                            part_prodTime pp = new part_prodTime(p, calculateProductionTime(p));
                            sfem.addPartToProductionHistory(pp);
                            if (productionTime_cnt.containsKey(pp.production_time())) {
                                int old_value = productionTime_cnt.get(pp.production_time());
                                productionTime_cnt.replace(pp.production_time(), old_value, old_value + 1);
                            } else {
                                productionTime_cnt.put(pp.production_time(), 1);
                            }

                            iterator.remove();
                        }
                    }

/*                    printStats(runtime);*/
                    printedStats = true;

                    if (!isGraphsInited)
                        initGraphs();
                    else updateGraphs();

                }
            } else {
                printedStats = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean printedStats = false;
    private part_prodTime min_time = null;
    private part_prodTime max_time = null;
    private boolean firstRun = true;

    private void printStats(List<Long> runtime) {

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

    private int calculateProductionTime(part p) {

        String sfeiName = sfem.getSfeeTransport().getSFEIbyIndex(0).getName();
        // Who is the last SFEI of this SFEM
        // Calculate the time between those SFEIs
        TreeMap<String, Instant> subTree = new TreeMap<>();
        for (Map.Entry<String, Instant> entry : p.getTimestamps().entrySet()) {
            if (entry.getKey().contains(sfeiName)) {
                subTree.put(entry.getKey(), entry.getValue());
            }
        }
        Object[] orderArray = subTree.values().toArray();
        Arrays.sort(orderArray);

        long duration = Duration.between((Instant) orderArray[0], (Instant) orderArray[orderArray.length - 1]).toMillis();
        return (int) Math.round(duration * 0.001);

       /* Object[] orderArray = p.getTimestamps().values().toArray();
        Arrays.sort(orderArray);

        // Who is the first SFEI of this SFEM
        String firstSFEI_name = sfem.getSFEEbyIndex(0).getSFEIbyIndex(0).getName();
        // Who is the last SFEI of this SFEM
        SFEE lastSFEE = sfem.getSFEEbyIndex(sfem.getSFEEs().size() - 1);
        String lastSFEI_name = lastSFEE.getSFEIbyIndex(lastSFEE.getSFEIs().size() - 1).getName();
        // Calculate the time between those SFEIs
        Instant start = null, end = null;

        for (Map.Entry<String, Instant> entry : p.getTimestamps().entrySet()) {
            if (entry.getKey().contains(firstSFEI_name)) {
                start = entry.getValue();
                break;
            }
        }

        for (Map.Entry<String, Instant> entry : p.getTimestamps().entrySet()) {
            if (entry.getKey().contains(lastSFEI_name)) {
                end = entry.getValue();
            }
        }

        if (start == null || end == null)
            throw new RuntimeException("Did not found start or end Instant related to part " + p.getId() + "in SFEM" + sfem.getName());

        long duration = Duration.between(start, end).toMillis();
        return (int) Math.round(duration * 0.001);*/
    }


    private void updateGraphs() {
//        getGraphLists();
        if (graphs != null)
            graphs.updateSeries(getGraphLists());
    }


    private List<intPair> getGraphLists() {
        if (productionTime_cnt.size() == 0)
            return null;


        intPair pair = new intPair(productionTime_cnt.keySet().toArray(new Integer[0]), productionTime_cnt.values().toArray(new Integer[0]));
        intPair lastNparts = getLastNParts(20);

        List<intPair> list = new ArrayList<>();
        list.add(pair);
        list.add(lastNparts);

        System.out.println("All parts:" + Arrays.toString(pair.xData()) + " " + (Arrays.toString(pair.yData())));
        System.out.println("Last n parts:" + Arrays.toString(lastNparts.xData()) + " " + (Arrays.toString(lastNparts.yData())));
        return list;

    }

    private intPair getLastNParts(int n) {

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

        return new intPair(lastNParts.keySet().toArray(new Integer[0]), lastNParts.values().toArray(new Integer[0]));
    }
}
