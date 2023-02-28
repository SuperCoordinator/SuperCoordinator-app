package viewers;

import models.sensor_actuator;

import java.time.Instant;
import java.util.Scanner;
import java.util.TreeMap;

public class SFEE {

    private final Scanner in;

    public SFEE() {
        this.in = new Scanner(System.in);
    }

    public String[] setupComunication(int opt) {

        String[] str = new String[3];

        if (opt == 0) {
            // Modbus
            System.out.print("IP: ");
            str[0] = in.nextLine();

            System.out.print("Port: ");
            str[1] = in.nextLine();

            System.out.print("Slave ID: ");
            str[2] = in.nextLine();
        }

        return str;
    }

    public String readIOpath() {
        String str;
        System.out.print("IO CSV file path: ");
        str = in.nextLine();
        return str;
    }

    public void listAllIO(TreeMap<String, sensor_actuator> io) {
        io.forEach((key, value) -> System.out.println(value.toString()));
    }

    public String opMode(){
        String str;
        System.out.println("SFEE Operation Mode?");

        System.out.println("1 - Normal");
        System.out.println("2 - Programmed Failures");
        str = in.nextLine();


        return str;
    }

    public String nSFEI() {
        String str;

        System.out.print("How many SFEI? ");
        str = in.nextLine();

        return str;
    }

    public String SFEI_type() {
        String str;
        System.out.println("SFEI Type?");

        System.out.println("1 - Conveyor");
        System.out.println("2 - Machine");
        str = in.nextLine();


        return str;
    }

    public String[] SFEI_params(int num, int type, boolean isSimulation) {
        String[] str = new String[0];
        if (type == 1 && isSimulation) {
            str = new String[9];

            System.out.print("SFEI " + num + " name? ");
            str[0] = in.nextLine();

            System.out.print("inSensor: ");
            str[1] = in.nextLine();

            System.out.print("outSensor: ");
            str[2] = in.nextLine();

            // day of birth
            str[3] = Instant.now().toString();
            // day of last maintenance
            str[4] = Instant.now().toString();

            System.out.print("Remover actuator: ");
            str[5] = in.nextLine();

            System.out.print("Emitter actuator: ");
            str[6] = in.nextLine();

            System.out.print("Remover sensor: ");
            str[7] = in.nextLine();

            System.out.print("Emitter sensor: ");
            str[8] = in.nextLine();

        } else if (type == 1) {
            str = new String[6];

            System.out.print("SFEI " + num + " name? ");
            str[0] = in.nextLine();

            System.out.print("inSensor: ");
            str[1] = in.nextLine();

            System.out.print("outSensor: ");
            str[2] = in.nextLine();

            // day of birth
            str[3] = Instant.now().toString();
            // day of last maintenance
            str[4] = Instant.now().toString();

            System.out.print("Conveyor actuator: ");
            str[5] = in.nextLine();

        } else if (type == 2) {
            str = new String[6];

            System.out.print("SFEI " + num + " name? ");
            str[0] = in.nextLine();

            System.out.print("inSensor: ");
            str[1] = in.nextLine();

            System.out.print("outSensor: ");
            str[2] = in.nextLine();

            // day of birth
            str[3] = Instant.now().toString();
            // day of last maintenance
            str[4] = Instant.now().toString();

            System.out.print("Stop actuator: ");
            str[5] = in.nextLine();

        }
        return str;
    }

    public String[] SFEEtime(){
        String[] str = new String[3];
        System.out.println("SFEE Operation Time?");

        System.out.println("1 - Stochastic N(u,s) ");
        System.out.println("2 - Linear ");
        str[0] = in.nextLine();

        if(Integer.parseInt(str[0]) == 1){
            System.out.println("Mean u :");
            str[1] = in.nextLine();
            System.out.println("Deviation s :");
            str[2] = in.nextLine();

        }else if (Integer.parseInt(str[0]) == 2){
            System.out.println("Value x :");
            str[1] = in.nextLine();
        }
        return str;

    }

}
