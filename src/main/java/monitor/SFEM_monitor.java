package monitor;

import models.SFEE;
import models.SFEI.SFEI;
import models.SFEM;
import models.part;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class SFEM_monitor implements Runnable {

    private final SFEM sfem;

    private final Instant init_t = Instant.now();

    public SFEM_monitor(SFEM sfem) {
        this.sfem = sfem;
    }

    @Override
    public void run() {
        try {
            synchronized (this) {

                // will check the parts from the SFEE and save them into history
                for (Map.Entry<Integer, SFEE> entry : sfem.getSFEEs().entrySet()) {
                    SFEI lastSFEI_of_SFEE = entry.getValue().getSFEIbyIndex(entry.getValue().getSFEIs().size() - 1);
                    Iterator<part> iterator = lastSFEI_of_SFEE.getPartsATM().iterator();
                    while (iterator.hasNext()) {
                        part p = iterator.next();
                        if (p.isProduced()) {
                            sfem.addPartToProductionHistory(p);
                            iterator.remove();
                        }
                    }
                }
                printStats();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private long calculateProductionTime(part p) {

        // The sorting is probably wrong (inversely ordered) because returns negative difference
        Object[] orderArray = p.getTimestamps().values().toArray();
        Arrays.sort(orderArray);
        return Duration.between((Instant) orderArray[0], (Instant) orderArray[orderArray.length - 1]).toSeconds();
    }

    private boolean printedStats = false;
    private part min_time = null;
    private part max_time = null;
    private boolean firstRun = true;

    private void printStats() {

        if (Duration.between(init_t, Instant.now()).toSeconds() % 5 == 0) {
            if (!printedStats) {
                // One statistic (production mean-stochasticTime, max-stochasticTime and min-stochasticTime)
                long total_time = 0;
                if (sfem.getProductionHistory().size() > 0) {
                    for (part p : sfem.getProductionHistory()) {
                        if (firstRun) {
                            min_time = sfem.getProductionHistory().get(0);
                            max_time = sfem.getProductionHistory().get(0);
                            firstRun = false;
                        }
                        long p_prodTime = calculateProductionTime(p);
                        if (p_prodTime < calculateProductionTime(min_time)) {
                            min_time = p;
//                            System.out.println("Min: " + Arrays.toString(min_time.getTimestamps().values().toArray()));
                        }
                        if (p_prodTime > calculateProductionTime(max_time)) {
                            max_time = p;
//                            System.out.println("Max: " + Arrays.toString(max_time.getTimestamps().values().toArray()));
                        }
                        total_time = total_time + p_prodTime;
                    }

                    System.out.println("Produced " + sfem.getProductionHistory().size() + " with stochasticTime mean of " + total_time / sfem.getProductionHistory().size() + "s");
                    assert max_time != null;
                    System.out.println("Max production stochasticTime " + calculateProductionTime(max_time) + "s");
                    System.out.println("Min production stochasticTime " + calculateProductionTime(min_time) + "s");
                }
                printedStats = true;
            }
        } else {
            printedStats = false;
        }
    }

}
