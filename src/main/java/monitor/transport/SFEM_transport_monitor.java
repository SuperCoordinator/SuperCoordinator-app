package monitor.transport;

import models.SFEx_particular.SFEM_production;
import models.SFEx_particular.SFEM_transport;
import models.base.SFEE;
import models.base.SFEI;
import models.base.SFEM;
import models.base.part;
import models.part_prodTime;
import monitor.base.SFEM_monitor;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class SFEM_transport_monitor extends SFEM_monitor {

    public SFEM_transport_monitor(SFEM sfem) {
        super(sfem);
    }

    public void loop() {
        try {
            if (Duration.between(getInit_t(), Instant.now()).toSeconds() % 5 == 0) {
                if (!isPrintedStats()) {
                    // will check the parts from the SFEE and save them into history
                    SFEM_transport sfem = (SFEM_transport) getSfem();
                    SFEI lastSFEI_of_SFEE = sfem.getSfeeTransport().getSFEIbyIndex(0);
                    Iterator<part> iterator = lastSFEI_of_SFEE.getPartsATM().iterator();
                    while (iterator.hasNext()) {
                        part p = iterator.next();
                        if (!p.isProduced() && !p.isWaitTransport()) {
                            int prod_t = calculateProductionTime(p);
                            if (prod_t == -1)
                                continue;
                            part_prodTime pp = new part_prodTime(p, prod_t);
                            sfem.addPartToProductionHistory(pp);
                            if (getProductionTime_cnt().containsKey(pp.production_time())) {
                                int old_value = getProductionTime_cnt().get(pp.production_time());
                                getProductionTime_cnt().replace(pp.production_time(), old_value, old_value + 1);
                            } else {
                                getProductionTime_cnt().put(pp.production_time(), 1);
                            }
                            iterator.remove();
                        }
                    }

                    updateGraphs();

                    printStats(new ArrayList<>());
                    setPrintedStats(true);

                }
            } else {
                setPrintedStats(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int calculateProductionTime(part p) {

        SFEM_transport sfem = (SFEM_transport) getSfem();

        // search for inOut sensor of SFEM : in from SFEE(0) / out from SFEE(size-1) ;

        String inSFEM_sensor = sfem.getSfeeTransport().getInSensor().name();

        String outSFEM_sensor = sfem.getSfeeTransport().getOutSensor().name();

        // Calculate the time between those SFEIs
        TreeMap<String, Instant> subTree = new TreeMap<>();
        for (Map.Entry<String, Instant> entry : p.getTimestamps().entrySet()) {
            if (entry.getKey().contains(inSFEM_sensor) || entry.getKey().contains(outSFEM_sensor)) {
                subTree.put(entry.getKey(), entry.getValue());
            }
        }
        if (subTree.size() == 0)
            return -1;
        Object[] orderArray = subTree.values().toArray();
        Arrays.sort(orderArray);

        long duration = Duration.between((Instant) orderArray[0], (Instant) orderArray[orderArray.length - 1]).toMillis();
        return (int) Math.round(duration * 0.001);

    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
