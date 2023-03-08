import org.apache.commons.math3.util.Precision;
import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.util.ArrayList;
import java.util.List;

public class SimpleRealTime {

    public static void main(String[] args) throws Exception {

        double phase = 0;
        double[][] initdata = getSineData(phase);

/*        // Create Chart
        final XYChart chart = QuickChart.getChart("Simple XChart Real-time Demo", "Radians", "Sine", "sine", initdata[0], initdata[1]);
        chart.getStyler().setZoomEnabled(true);
        chart.getStyler().setZoomResetByButton(true);
        chart.getStyler().setXAxisTickMarkSpacingHint(200);
        // Show it
        final SwingWrapper<XYChart> sw = new SwingWrapper<>(chart);

        sw.displayChart();*/

        int numCharts = 2;

        List<XYChart> charts = new ArrayList<XYChart>();

        for (int i = 0; i < numCharts; i++) {
            XYChart chart = new XYChartBuilder().xAxisTitle("X").yAxisTitle("Y").width(600).height(400).build();
/*            chart.getStyler().setYAxisMin(-10.0);
            chart.getStyler().setYAxisMax(10.0);*/
            XYSeries series = chart.addSeries("sine " + i, initdata[0], initdata[1], null);
            series.setMarker(SeriesMarkers.NONE);
            charts.add(chart);
        }
        final SwingWrapper<XYChart> sw = new SwingWrapper<XYChart>(charts);
        sw.displayChartMatrix();


        while (true) {

            phase += 2 * Math.PI * 2 / 20.0;

            Thread.sleep(500);

            final double[][] data = getSineData(phase);

/*            javax.swing.SwingUtilities.invokeLater(() -> {

                for (int i = 0; i < numCharts; i++) {
                    charts.get(i).updateXYSeries("sine " + i, data[0], data[1], null);
                    sw.repaintChart(i);
                }

            });*/

            for (int i = 0; i < numCharts; i++) {
                charts.get(i).updateXYSeries("sine " + i, data[0], data[1], null);
                sw.repaintChart(i);
            }
        }

    }

    private static double[][] getSineData(double phase) {

        double[] xData = new double[100];
        double[] yData = new double[100];
        for (int i = 0; i < xData.length; i++) {
            double radians = phase + (2 * Math.PI / xData.length * i);
            xData[i] = Precision.round(radians, 2);
            yData[i] = Math.sin(radians);
        }
        return new double[][]{xData, yData};
    }
}