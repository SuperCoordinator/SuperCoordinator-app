package utils;

import models.sensor_actuator;

import java.io.FileReader;
import java.util.*;

import com.opencsv.*;

public class csv_reader {
    private record innerForTreeMap(String name, sensor_actuator sensorActuator) {
    }

    public TreeMap<String, sensor_actuator> readModbusTags(String path, boolean dbg) {
        TreeMap<String, sensor_actuator> treeMap = new TreeMap<>();
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

            ArrayList<innerForTreeMap> tempTreeMap = new ArrayList<>();
            // print Data
            for (String[] row : allData) {

                if (row.length < 4)
                    throw new Exception("Cannot parse the object " + Arrays.toString(row));

                // TreeMap with object name as KEY and fieldObj as VALUE
                tempTreeMap.add(new innerForTreeMap(row[0], createObj(row)));

                if (dbg) {
                    for (String cell : row) {
                        System.out.print(cell + "\t");
                    }
                    System.out.println();
                }
            }
            System.out.println("*** All imported IOs ***");
            for (int i = 0; i < tempTreeMap.size(); i++) {
                System.out.println("   " + i + " - " + tempTreeMap.get(i).name());
            }
            System.out.println("From the IO which are in inverse logic?");

            Scanner in = new Scanner(System.in);

            //String input = in.nextLine();
            // System.out.println("Enter following the example pattern: 2,3,1,5");
            String input = "6,7,8,9,10,12,13";
            System.out.println(input);
            for (String str : input.split(",")) {
                innerForTreeMap inner = tempTreeMap.get(Integer.parseInt(str));
                treeMap.put(inner.name(), inner.sensorActuator().changeInvLogic(true));
            }
            // Copy the remaining elements

            for (innerForTreeMap inner : tempTreeMap) {
                treeMap.putIfAbsent(inner.name(), inner.sensorActuator());
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

}
