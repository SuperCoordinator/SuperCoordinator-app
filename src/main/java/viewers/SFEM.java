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

    public String[] SFEE_params(int num, String SFEM_name) {

        String[] str = new String[3];

        System.out.println("Name Element " + num + " of " + SFEM_name);
        System.out.print("> ");
        str[0] = in.nextLine();

//        System.out.println("1 - Modbus");
//        System.out.println("2 - OPC UA");
//        str[1] = in.nextLine();
        str[1] = "1";

        System.out.println("Element Environment");
        System.out.println("    1 - Simulation");
        System.out.println("    2 - Real");
        int opt = 1;
        do {
            if (opt != 1 && opt != 2) {
                System.out.println("Invalid option! Try again");
            }
            System.out.print("> ");
            opt = Integer.parseInt(in.nextLine());
        } while (opt != 1 && opt != 2);
        str[2] = String.valueOf(opt);

        return str;
    }

    public String[] communicationParams(int opt, String sfeeName) {

        String[] str = new String[3];

        // MODBUS
        if (opt == 0) {
            System.out.println("MODBUS Connection of " + sfeeName);
            // Modbus
            System.out.print("   IP: ");
            str[0] = in.nextLine();

            System.out.print("   Port: ");
            str[1] = in.nextLine();

            System.out.print("   Slave ID: ");
            str[2] = in.nextLine();
        }
        return str;
    }


}
