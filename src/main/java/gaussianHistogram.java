import communication.database.dbConnection;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;
import utility.utils;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.sql.Connection;

public class gaussianHistogram {

    public static void main(String[] args) {

        printQueryRes();
//        double mean = 60.0;
//        double dev = 8.0;
//
//        TreeMap<Double, Double> discreteGaussDistr = new TreeMap<>();
//        ArrayList<Double> unOrder = new ArrayList<>();
//
//        for (int i = 0; i < 51; i++) {
//            double gauss = utils.getInstance().getRandom().nextGaussian() * dev + mean;
//            double r = (double) Math.round(gauss);
//
//            if (discreteGaussDistr.containsKey(r)) {
//                discreteGaussDistr.replace(r, discreteGaussDistr.get(r), discreteGaussDistr.get(r) + 1);
//            } else {
//                discreteGaussDistr.put(r, 1.0);
//            }
//            unOrder.add(r);
//        }
//        System.out.println(Arrays.toString(discreteGaussDistr.keySet().toArray()));
//        System.out.println(Arrays.toString(discreteGaussDistr.values().toArray()));
//        System.out.println(Arrays.toString(unOrder.toArray()));
//
//        List<Double> xData = new ArrayList<>(discreteGaussDistr.keySet());
//        List<Double> yData = new ArrayList<>(discreteGaussDistr.values());
//
//        CategoryChart categoryChart = new CategoryChartBuilder().width(600).height(600).title("Gaussian Distribution").xAxisTitle("Production time (s)").yAxisTitle("# parts").build();
//        categoryChart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
//        categoryChart.addSeries("All Gaussian Distr", xData, yData);
////            categoryChart.addSeries(seriesNames[i], data.get(i).xData(), data.get(i).yData());
//
//
//        SwingWrapper<CategoryChart> sw = new SwingWrapper<>(categoryChart);
////        sw.displayChartMatrix();
//        sw.displayChart();


    }

    public static void printQueryRes() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "");
            con.setAutoCommit(true);
            con.setCatalog("wh_ss_3cmc_wh");
            String query = "SELECT " +
                    "    TIMESTAMPDIFF(SECOND, ph_E0.time_stamp, ph_remover.time_stamp) AS metal_prod_time " +
                    "FROM " +
                    "    production_history AS ph " +
                    "    INNER JOIN production_history AS ph_E0 " +
                    "        ON ph.fk_part_id = ph_E0.fk_part_id " +
                    "        AND ph_E0.fk_sensor_name = 's_E0' " +
                    "    INNER JOIN production_history AS ph_remover " +
                    "        ON ph.fk_part_id = ph_remover.fk_part_id " +
                    "        AND ph_remover.fk_sensor_name = 's_R0' " +
                    "WHERE " +
                    "    ph_E0.time_stamp < ph_remover.time_stamp " +
                    "GROUP BY " +
                    "    ph.fk_part_id " +
                    "HAVING " +
                    "    metal_prod_time < 500 " +
                    "ORDER BY " +
                    "    metal_prod_time; ";
            ResultSet rs = con.prepareStatement(query).executeQuery();
            TreeMap<Double, Double> discreteGaussDistr = new TreeMap<>();
            ArrayList<Double> unOrder = new ArrayList<>();
            while (rs.next()) {
                double res = Double.parseDouble(rs.getString(1));
                if (discreteGaussDistr.containsKey(res)) {
                    discreteGaussDistr.replace(res, discreteGaussDistr.get(res), discreteGaussDistr.get(res) + 1);
                } else {
                    discreteGaussDistr.put(res, 1.0);
                }
                unOrder.add(res);
            }


            System.out.println(Arrays.toString(discreteGaussDistr.keySet().toArray()));
            System.out.println(Arrays.toString(discreteGaussDistr.values().toArray()));
            System.out.println(Arrays.toString(unOrder.toArray()));

            List<Double> xData = new ArrayList<>(discreteGaussDistr.keySet());
            List<Double> yData = new ArrayList<>(discreteGaussDistr.values());

            CategoryChart categoryChart = new CategoryChartBuilder().width(600).height(600).title("Gaussian Distribution").xAxisTitle("Production time (s)").yAxisTitle("# parts").build();
            categoryChart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
            categoryChart.addSeries("All Gaussian Distr", xData, yData);

            SwingWrapper<CategoryChart> sw = new SwingWrapper<>(categoryChart);
            sw.displayChart();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
