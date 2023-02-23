import communication.modbus;
import models.sensor_actuator;
import utils.utils;

import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class test {

    public static void main(String[] args) {

        /*try {
            modbus mb = new modbus();
            TreeMap<String, sensor_actuator> io = new TreeMap<>();
            utils util = new utils();
            util.getReader().readModbusTags("C:\\Users\\danie\\Desktop\\CMC-block\\Tags_CMC_Modbus.csv", io, false);
            mb.openConnection("192.168.240.1", 502, 1, io);
            mb.writeState("FACTORY I/O (Pause)", "0");
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        /*int test_value = 0;

        try {

            ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
            test_thread thread = new test_thread(test_value);
            executorService.scheduleAtFixedRate(thread, 0, 1000, TimeUnit.MILLISECONDS);

            while (true) {
                //test_value.getAndIncrement();
                int temp = thread.getValue();
                if (temp > 10)
                    thread.setValue(0);
                System.out.println("Value: " + thread.getValue());
                Thread.sleep(2000);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/


    }
}
