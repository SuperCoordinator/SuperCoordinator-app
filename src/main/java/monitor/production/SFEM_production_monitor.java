package monitor.production;

import models.base.SFEE;
import models.base.SFEI;
import models.SFEx.SFEM_production;
import models.base.SFEM;
import models.base.part;
import models.part_prodTime;
import monitor.SFEM_monitor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEM_production_monitor extends SFEM_monitor  {


    public SFEM_production_monitor() {
        super();
    }

    public SFEM_production_monitor(SFEM sfem) {
        super(sfem);
    }

    // Avaliador de peças defeituosas baseada no tipo e no tempo de processamento

    public void loop(List<Long> runtime) {
        try {
            if (/*Duration.between(getInit_t(), Instant.now()).toSeconds() % 5 == 0*/false) {
                if (!isPrintedStats()) {
                    SFEM_production sfem = (SFEM_production) getSfem();
                    // will check the parts from the SFEE and save them into history
                    for (Map.Entry<Integer, SFEE> entry : sfem.getSFEEs().entrySet()) {
                        SFEI lastSFEI_of_SFEE = entry.getValue().getSFEIbyIndex(entry.getValue().getSFEIs().size() - 1);
                        Iterator<part> iterator = lastSFEI_of_SFEE.getPartsATM().iterator();
                        while (iterator.hasNext()) {
                            part p = iterator.next();
                            if (p.isProduced()) {
                                int prod_t = /*calculateProductionTime(p);*/0;
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
                                if (lastSFEI_of_SFEE.isLine_end())
                                    iterator.remove();
                            }
                        }
                    }
//                    printStats(runtime);
                    setPrintedStats(true);
//
                    updateGraphs();

                }
            } else {
                setPrintedStats(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int calculateProductionTime(part p) {

        SFEM_production sfem = (SFEM_production) getSfem();

        // search for inOut sensor of SFEM : in from SFEE(0) / out from SFEE(size-1) ;
        String inSFEM_sensor = sfem.getSFEEbyIndex(0).getInSensor().getName();

        // Who is the last SFEI of this SFEM
        SFEE lastSFEE = sfem.getSFEEbyIndex(sfem.getSFEEs().size() - 1);
        String outSFEM_sensor = lastSFEE.getSFEIbyIndex(lastSFEE.getSFEIs().size() - 1).getOutSensor().getName();

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


}
