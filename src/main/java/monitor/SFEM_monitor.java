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
    private final ArrayList<SFEE_monitor2> SFEE_monitors;

    private final Instant init_t = Instant.now();

    public SFEM_monitor(SFEM sfem, ArrayList<SFEE_monitor2> SFEE_monitors) {
        this.sfem = sfem;
        this.SFEE_monitors = SFEE_monitors;
    }

    @Override
    public void run() {
        try {
            // update value of SFEE_monitor
            for (int i = 0; i < SFEE_monitors.size(); i++) {
                sfem.updateSFEE(i, SFEE_monitors.get(i).getSFEE());
            }

            // will check the parts from the SFEE and save them into history
            for (Map.Entry<Integer, SFEE> entry : sfem.getSFEEs().entrySet()) {
                SFEI lastSFEI_of_SFEE = entry.getValue().getSFEIbyIndex(entry.getValue().getSFEIs().size() - 1);
                Iterator<part> iterator = lastSFEI_of_SFEE.getPartsATM().descendingIterator();
                while (iterator.hasNext()) {
                    part p = iterator.next();
                    if (p.isProduced())
                        sfem.addPartToProductionHistory(p);
                    iterator.remove();
                }
            }

            printStats();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private long calculateProductionTime(part p) {

        // The sorting is probably wrong (inversely ordered) because returns negative difference

        Object[] orderArray = p.getTimestamps().values().toArray();
        return Duration.between((Instant) orderArray[0], (Instant) orderArray[orderArray.length - 1]).toSeconds();
    }

    private boolean printedStats = false;

    private void printStats() {

        if (Duration.between(init_t, Instant.now()).toSeconds() % 5 == 0) {
            if (!printedStats) {
                // One statistic (production mean-time, max-time and min-time)
                long total_time = 0;
                part min_time = null;
                part max_time = null;
                boolean firstRun = true;
                if (sfem.getProductionHistory().size() > 0) {
                    for (part p : sfem.getProductionHistory()) {
                        if (firstRun) {
                            min_time = sfem.getProductionHistory().get(0);
                            max_time = sfem.getProductionHistory().get(0);
                            firstRun = false;
                        }
                        if (calculateProductionTime(p) < calculateProductionTime(min_time))
                            min_time = p;
                        if (calculateProductionTime(p) > calculateProductionTime(max_time))
                            max_time = p;

                        total_time = total_time + calculateProductionTime(p);
                    }

                    System.out.println("Produced " + sfem.getProductionHistory().size() + " with mean time of " + total_time / sfem.getProductionHistory().size());
                    assert max_time != null;
                    System.out.println("Max production time" + calculateProductionTime(max_time));
                    System.out.println("Min production time" + calculateProductionTime(min_time));
                }

                printedStats = true;
            }
        } else {
            printedStats = false;
        }
    }

}
