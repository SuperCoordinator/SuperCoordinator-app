package viewers;

import models.sensor_actuator;

import java.util.Arrays;
import java.util.Scanner;
import java.util.TreeMap;

public class SFEM {

    private final Scanner in;

    public SFEM() {
        this.in = new Scanner(System.in);
    }

    public String nSFEE() {
        String str;

        System.out.print("How many SFEE? ");
        str = in.nextLine();

        return str;
    }

    public String[] SFEE_params(int num) {

        String[] str = new String[3];

        System.out.print("SFEE " + num + " name? ");
        str[0] = in.nextLine();

        System.out.println("1 - Modbus");
        System.out.println("2 - OPC UA");
        str[1] = in.nextLine();

        System.out.println("1 - Simulation");
        System.out.println("2 - Real");
        str[2] = in.nextLine();

        return str;
    }

}
