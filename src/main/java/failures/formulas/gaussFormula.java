package failures.formulas;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;
import utility.utils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class gaussFormula extends formula {

    private double mean;
    private double dev;

    public gaussFormula() {
        super(-1);
    }

    public gaussFormula(double mean, double dev, boolean showHistogram) {
        super(0);
        this.mean = mean;
        this.dev = dev;
        init(showHistogram);
        setNextValue();
    }

    public double getMean() {
        return mean;
    }

    public double getDev() {
        return dev;
    }

    private final ArrayList<Double> array = new ArrayList<>();

    private void init(boolean showHist) {

        TreeMap<Double, Double> discreteGaussDistr = null;
        if (showHist) {
            discreteGaussDistr = new TreeMap<>();
        }

        for (int i = 0; i < 1024 * 2; i++) {
            double r = utils.getInstance().getRandom().nextGaussian() * dev + mean;
            r = (double) Math.round(r);
            if (showHist) {
                if (discreteGaussDistr.containsKey(r)) {
                    discreteGaussDistr.replace(r, discreteGaussDistr.get(r), discreteGaussDistr.get(r) + 1);
                } else {
                    discreteGaussDistr.put(r, 1.0);
                }
            }
            array.add(r);
        }

        if (showHist) {

            System.out.println(Arrays.toString(array.toArray()));

            List<Double> xData = new ArrayList<>(discreteGaussDistr.keySet());
            List<Double> yData = new ArrayList<>(discreteGaussDistr.values());

            CategoryChart categoryChart = new CategoryChartBuilder().width(600).height(600).title("Gaussian [" + mean + ";" + dev + "]").xAxisTitle("Production time (s)").yAxisTitle("# parts").build();
            categoryChart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
            categoryChart.addSeries("Gaussian [" + mean + ";" + dev + "]", xData, yData);

            SwingWrapper<CategoryChart> sw = new SwingWrapper<>(categoryChart);
            sw.displayChart().setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }
    }

    private int array_index = -1;

    public void setNextValue() {
        do {
            if (array_index < array.size())
                array_index++;
            else
                array_index = 0;

//            System.out.println("GAUSSIAN nextValue: " + array.get(array_index).intValue());
            setCurrentValue(array.get(array_index).intValue());
        } while (getCurrentValue() <= 0);
    }


}
