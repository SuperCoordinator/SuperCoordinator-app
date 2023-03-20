package viewers;

import java.util.Scanner;

public class SFEM_transport {

    private final Scanner in;

    public SFEM_transport() {
        this.in = new Scanner(System.in);
    }

    public String[] init_SFEE_transport() {

        String[] str = new String[4];

        System.out.print("SFEE name? ");
        str[0] = in.nextLine();
        System.out.println("1 - Simulation");
        System.out.println("2 - Real");
        str[1] = in.nextLine();
        System.out.println("Connect to input SFEI via?");
        System.out.println("1 - Modbus");
        System.out.println("2 - OPC UA");
        str[2] = in.nextLine();
        System.out.println("Connect to output SFEI via?");
        System.out.println("1 - Modbus");
        System.out.println("2 - OPC UA");
        str[3] = in.nextLine();

        return str;
    }


}
