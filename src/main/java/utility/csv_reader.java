package utility;

import de.erichseifert.vectorgraphics2d.VectorHints;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.sensor_actuator;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.opencsv.*;

import javax.swing.*;

public class csv_reader {

    public TreeMap<Integer, sensor_actuator> readModbusTags(String path, String sfee_name, boolean openIOwindow) {
        TreeMap<Integer, sensor_actuator> treeMap = new TreeMap<>();
        try {

            // Create an object of file-reader class
            // with CSV file as a parameter.
            FileReader filereader = new FileReader(path);

            // create csvReader object
            // and skip first Line
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                    .withSkipLines(1)
                    .build();
            List<String[]> allData = csvReader.readAll();

            // print Data
            for (String[] row : allData) {

                if (row.length < 4)
                    throw new Exception("Cannot parse the object " + Arrays.toString(row));

                // TreeMap with object name as KEY and fieldObj as VALUE
                treeMap.put(treeMap.size(), createObj(row));

            }
            if (openIOwindow) {
                openWindow(treeMap, sfee_name);
                System.out.print("There are inputs with inverse logic for " + sfee_name + " (y/n)?");
                if (utils.getInstance().validateUserOption()) {
                    System.out.println("Enter following the example pattern: 2,3,1,5");
                    String input = new Scanner(System.in).nextLine();
                    if (!input.isEmpty()) {
                        for (String str : input.split(",")) {
                            int key = Integer.parseInt(str);
                            treeMap.replace(key, treeMap.get(key), treeMap.get(key).changeInvLogic(true));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return treeMap;
    }

    private sensor_actuator createObj(String[] row) {

        /*
         * Retrieve sensor/actuator name
         */

        String objName = row[0];

        /*
         *  Define if it is a sensor or actuator
         */

        sensor_actuator.Type objType = sensor_actuator.Type.OUTPUT;
        if (row[1].equalsIgnoreCase("Input"))
            objType = sensor_actuator.Type.INPUT;

        /*
         * Define object Data Type
         */

        sensor_actuator.DataType objDataType = sensor_actuator.DataType.BOOL;
        if (row[2].equalsIgnoreCase("Int"))
            objDataType = sensor_actuator.DataType.INT;
        else if (row[2].equalsIgnoreCase("Real"))
            objDataType = sensor_actuator.DataType.REAL;

        /*
         * Define object Address Type and the bit offset
         */

        sensor_actuator.AddressType objAddressType = null;
        String[] addType = row[3].split(" ");
        int bit_off = Integer.parseInt(addType[addType.length - 1]);
        addType[addType.length - 1] = " ";
        String address = Arrays.toString(addType);
        address = address.toLowerCase();

        if (address.contains("coil"))
            objAddressType = sensor_actuator.AddressType.COIL;
        else if (address.contains("input") && address.contains("reg"))
            objAddressType = sensor_actuator.AddressType.INPUT_REGISTER;
        else if (address.contains("holding") && address.contains("reg"))
            objAddressType = sensor_actuator.AddressType.HOLDING_REGISTER;
        else if (address.contains("input"))
            objAddressType = sensor_actuator.AddressType.DISCRETE_INPUT;

        return new sensor_actuator(objName, objType, false, objDataType, objAddressType, 0, bit_off);
    }

    private void openWindow(TreeMap<Integer, sensor_actuator> treeMap, String sfeeName) {

        JFrame mainFrame = new JFrame(sfeeName);
        JPanel controlPanel = new JPanel();
        mainFrame.setSize(500, 500);
        mainFrame.add(controlPanel);

        String[] columnNames = {"Index", "Name", "Bit", "Data Type"};
        Object[][] data = new Object[treeMap.size()][4];
        treeMap.forEach((key, value) -> {
            data[key] = new Object[]{String.valueOf(key), value.getName(), String.valueOf(value.getBit_offset()), value.getDataType().toString()};
        });

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setSize(400, 500);
        table.setFillsViewportHeight(true);
        controlPanel.add(scrollPane);
        mainFrame.setVisible(true);
    }

}
