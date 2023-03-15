import org.apache.commons.math3.util.Pair;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;
import viewers.graphs.histogram;

import java.util.*;

public class formulasTest3 {

    public static void main(String[] args) {

        double mean = 100.0;
        double dev = 5.5;

        Random random = new Random();
        random.setSeed(3587214);

        TreeMap<Double, Double> discreteGaussDistr = new TreeMap<>();
        ArrayList<Double> unOrder = new ArrayList<>();

        for (int i = 0; i < 1024*2; i++) {
            double gauss = random.nextGaussian() * dev + mean;
            double r = (double) Math.round(gauss);
            if (discreteGaussDistr.keySet().contains(r)) {
                discreteGaussDistr.replace(r, discreteGaussDistr.get(r), discreteGaussDistr.get(r) + 1);
            } else {
                discreteGaussDistr.put(r, 1.0);
            }
            unOrder.add(r);
        }
        System.out.println(Arrays.toString(discreteGaussDistr.keySet().toArray()));
        System.out.println(Arrays.toString(discreteGaussDistr.values().toArray()));
        System.out.println(Arrays.toString(unOrder.toArray()));

        List<Double> xData = new ArrayList<>(discreteGaussDistr.keySet());
        List<Double> yData = new ArrayList<>(discreteGaussDistr.values());

        CategoryChart categoryChart = new CategoryChartBuilder().width(600).height(600).title("All Gaussian Distr").xAxisTitle("Production time (s)").yAxisTitle("# parts").build();
        categoryChart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        categoryChart.addSeries("All Gaussian Distr", xData, yData);
//            categoryChart.addSeries(seriesNames[i], data.get(i).xData(), data.get(i).yData());


        SwingWrapper<CategoryChart> sw = new SwingWrapper<CategoryChart>(categoryChart);
//        sw.displayChartMatrix();
        sw.displayChart();


    }

}
