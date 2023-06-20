package viewers.graphs;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class histogram {

    public record intPair(Integer[] xData, Integer[] yData) {

    }

    private final String windowTitle;
    private final String[] seriesNames;
    private final List<CategoryChart> chartList;

    public histogram(String title, List<intPair> data, String... seriesName) {
        this.windowTitle = title;
        this.seriesNames = new String[data.size()];
        System.arraycopy(seriesName, 0, seriesNames, 0, data.size());

        this.chartList = new ArrayList<>();

    }

    private SwingWrapper<CategoryChart> sw = null;

    public void createWindow(List<intPair> data) {

        for (int i = 0; i < data.size(); i++) {
            CategoryChart categoryChart = new CategoryChartBuilder().width(600).height(600).title(seriesNames[i]).xAxisTitle("Production time (s)").yAxisTitle("# parts").build();
            categoryChart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
            categoryChart.addSeries(seriesNames[i], Arrays.asList(data.get(i).xData()), Arrays.asList(data.get(i).yData()));

            chartList.add(categoryChart);
        }
        sw = new SwingWrapper<>(chartList);
        sw.setTitle(windowTitle);
        sw.displayChartMatrix();

    }

    public void updateSeries(List<intPair> data) {

        for (int i = 0; i < chartList.size(); i++) {
            chartList.get(i).updateCategorySeries(seriesNames[i],
                    Arrays.stream(data.get(i).xData()).toList(),
                    Arrays.stream(data.get(i).yData()).toList(),
                    null);
            sw.repaintChart(i);
        }
    }


}
