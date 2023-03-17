import viewers.graphs.histogram;

import java.util.*;

public class formulasTest2 {

    public static void main(String[] args) {

        double mean = 100.0;
        double dev =  1.5;

        Random random = new Random();
//        random.setSeed(3000);

        int[] discreteGaussDistr = new int[1024];

        for (int i = 0; i < 1024; i++) {
            discreteGaussDistr[i] = (int) ((int) random.nextGaussian() * dev + mean);
        }

        histogram.intPair coisePair = getLastNParts(discreteGaussDistr);

        List<histogram.intPair> list = new ArrayList<>();
        list.add(coisePair);
        list.add(coisePair);

        histogram graphs = new histogram(list, "All Gaussian Distr", "All Gaussian Distr");
        graphs.createWindow(list);


    }

    private static histogram.intPair getLastNParts(int[] discreteGaussDistr) {

        TreeMap<Integer, Integer> lastNParts = new TreeMap<>();

        //ListIterator<part_prodTime> iterator = sfem.getProductionHistory().listIterator(sfem.getProductionHistory().size());


        for (int i = 0; i < discreteGaussDistr.length; i++) {

            if (i == 0) {
                lastNParts.put(discreteGaussDistr[i], 1);
            } else if (lastNParts.containsKey(discreteGaussDistr[i])) {
                int old_value = lastNParts.get(discreteGaussDistr[i]);
                lastNParts.replace(discreteGaussDistr[i], old_value, old_value + 1);
            } else {
                lastNParts.put(discreteGaussDistr[i], 1);
            }

        }

        return new histogram.intPair(
                lastNParts.keySet().toArray(new Integer[0]),
                lastNParts.values().toArray(new Integer[0]));
    }

}
