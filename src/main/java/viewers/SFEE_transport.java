package viewers;

import utility.customCalculator;

import java.util.Scanner;

public class SFEE_transport {

    private final Scanner in;

    public SFEE_transport() {
        this.in = new Scanner(System.in);
    }

    public String[] associateSensor2Actuator(int repeat, String sensorName) {

        String[] str = new String[repeat];
        if (repeat == 1) {
            System.out.println("What is the actuator for the " + sensorName + " sensor ?");
            str[0] = in.nextLine();
        } else if (repeat == 3) {
            System.out.println("What is the actuator for the " + sensorName + " sensor (Emit) ?");
            str[0] = in.nextLine();
            System.out.println(" And for the part ? ");
            str[1] = in.nextLine();
            System.out.println(" And for the base ? ");
            str[2] = in.nextLine();
        }

        return str;
    }

    public String[] SFEE_stochasticTime() {

        String str = null;
        customCalculator customCalculator = new customCalculator();
        boolean retry = false;

        System.out.println("SFEE Transport Operation Time?");
        System.out.println("Valid variables: n - number of pieces moved / a - age of the machine in minutes / m - time since last maintenance in minutes");
        System.out.println("Valid operator: + - * / % or gauss[ mean ; dev ] linear[ value ] ");
        System.out.println("Please insert a space between each character/number (p.e: gauss [ 65 + ( 0.001 * n) ; 3.5 + 0.1 * a ]");
        System.out.print("Enter expression:");
        do {
            if (retry)
                System.out.println("Msg: " + customCalculator.errorMsg(str));
            str = in.nextLine();
            retry = customCalculator.evalStochasticTimeExpression(str);
        } while (retry);

        return customCalculator.getStochasticTimeFormulaElements();
    }
}
