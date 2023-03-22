package viewers;

import java.util.Scanner;

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

    public String[] communicationParams(int opt) {

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


}
