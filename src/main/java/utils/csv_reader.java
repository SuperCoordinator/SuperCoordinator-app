package utils;

import models.sensor_actuator;

import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import com.opencsv.*;

import java.util.TreeMap;

public class csv_reader {

    public void readModbusTags(String path, TreeMap<String, sensor_actuator> treeMap, boolean dbg) {

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
                treeMap.put(row[0], createObj(row));

                if (dbg) {
                    for (String cell : row) {
                        System.out.print(cell + "\t");
                    }
                    System.out.println();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        return new sensor_actuator(objName, objType, objDataType, objAddressType, 0, bit_off);
    }

}
