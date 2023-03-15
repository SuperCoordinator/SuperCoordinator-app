package viewers;

import models.sensor_actuator;
import utils.customCalculator;

import java.time.Instant;
import java.util.Scanner;
import java.util.TreeMap;

public class SFEE {

    private final Scanner in;

    public SFEE() {
        this.in = new Scanner(System.in);
    }


    public String readIOpath() {
        String str;
        System.out.print("IO CSV file path: ");
        str = in.nextLine();
        return str;
    }

    public void listAllIO(TreeMap<Integer, sensor_actuator> io) {
        io.forEach((key, value) -> System.out.println(value.toString()));
    }

    public String opMode() {
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

    public String[] SFEE_stochasticTime() {

        String str = null;
        customCalculator customCalculator = new customCalculator();
        boolean retry = false;

        System.out.println("SFEE Operation Time?");
        System.out.println("Valid variables: n - number of pieces moved / a - age of the machine in minutes / m - time since last maintenance in minutes");
        System.out.println("Valid operator: + - * / % or gauss[ mean ; dev ] linear[ value ] ");
        System.out.println("Please add a space between each character/number (p.e: gauss [ 65 + ( 0.001 * n) ; 3.5 + 0.1 * a ]");
        System.out.println("Enter expression:");
        do {
            if (retry)
                System.out.println("Msg: " + customCalculator.errorMsg(str));
            str = in.nextLine();
            retry = customCalculator.evalStochasticTimeExpression(str);
        } while (retry);

        return customCalculator.getStochasticTimeFormulaElements();

        /*String[] str = new String[3];
        customCalculator customCalculator = new customCalculator();
        boolean retry = false;
        System.out.println("SFEE Operation Time?");

        System.out.println("1 - Stochastic N(u,s) ");
        System.out.println("2 - Linear ");
        str[0] = in.nextLine();

        if (Integer.parseInt(str[0]) == 1) {
            do {
                if (retry)
                    System.out.println(customCalculator.errorMsg(str[1]));
                System.out.println("Mean u :");
                str[1] = in.nextLine();
                retry = customCalculator.evalExpression(str[1]);
            } while (retry);

            do {
                if (retry)
                    System.out.println(customCalculator.errorMsg(str[2]));
                System.out.println("Deviation s :");
                str[2] = in.nextLine();
                retry = customCalculator.evalExpression(str[2]);
            } while (retry);

        } else if (Integer.parseInt(str[0]) == 2) {
            do {
                if (retry)
                    System.out.println(customCalculator.errorMsg(str[1]));
                System.out.println("Value x :");
                str[1] = in.nextLine();
                retry = customCalculator.evalExpression(str[1]);
            } while (retry);

        }
        return str;
*/
    }

    public String[] SFEEFailures() {
        String[] str = new String[]{"no", "no", "no", "no", "no"};
        customCalculator customCalculator = new customCalculator();
        boolean retry = false;

        System.out.println("SFEE Failures ?");
        System.out.println("Valid variables: n - number of pieces moved / a - age of the machine in minutes / m - time since last maintenance in minutes");
        System.out.println("Valid operator: + - * / %  or random [ x ], where x [0, 100] ");
        System.out.println("Defined variables: n - number of pieces moved / a - age of the machine in minutes / m - time since last maintenance in minutes");
        System.out.println("Please add a space between each character/number (p.e: random [ 20 ] OR n % 100 = 0 ");
        do {
            if (retry) {
                System.out.println("break error: " + customCalculator.errorMsg(str[0]));
                System.out.println("repair error: " + customCalculator.errorMsg(str[4]));
            }
            System.out.println("BREAKDOWN WITH REPAIR ");
            System.out.print(" BREAK: ");
            str[0] = in.nextLine();
            retry = customCalculator.evalFailureFormula(str[0]);
            if (!str[0].equalsIgnoreCase("no")) {
                System.out.print("REPAIR: ");
                str[4] = in.nextLine();
                retry = retry || customCalculator.evalFailureFormula(str[4]);
            } else {
                str[4] = "no";
            }

        } while (retry);

        do {
            if (retry)
                System.out.println(customCalculator.errorMsg(str[1]));
            System.out.print("BREAKDOWN: ");
            str[1] = in.nextLine();
            retry = customCalculator.evalFailureFormula(str[1]);
        } while (retry);


        do {
            if (retry)
                System.out.println(customCalculator.errorMsg(str[2]));
            System.out.print("PRODUCE FAULTY: ");
            str[2] = in.nextLine();
            retry = customCalculator.evalFailureFormula(str[2]);
        } while (retry);

        do {
            if (retry)
                System.out.println(customCalculator.errorMsg(str[3]));
            System.out.print("PRODUCE MORE: ");
            str[3] = in.nextLine();
            retry = customCalculator.evalFailureFormula(str[3]);
        } while (retry);


        return str;
    }

    public String[] breakdown2() {
        String[] str = new String[3];
        customCalculator customCalculator = new customCalculator();
        boolean retry = false;
        do {
            if (retry)
                System.out.println("Msg: " + customCalculator.errorMsg(str[0]));
            System.out.print("      [n] -> ");
            str[0] = in.nextLine();
            if (containsOperator(str[0]))
                retry = customCalculator.evalStochasticTimeExpression(str[0]);
            else
                retry = customCalculator.evalFailureFormula(str[0]);
        } while (retry);

        do {
            if (retry)
                System.out.println("Msg: " + customCalculator.errorMsg(str[1]));
            System.out.print("      [a] -> ");
            str[1] = in.nextLine();
            if (containsOperator(str[1]))
                retry = customCalculator.evalStochasticTimeExpression(str[1]);
            else
                retry = customCalculator.evalFailureFormula(str[1]);
        } while (retry);

        do {
            if (retry)
                System.out.println("Msg: " + customCalculator.errorMsg(str[2]));
            System.out.print("      [m] -> ");
            str[2] = in.nextLine();
            if (containsOperator(str[2]))
                retry = customCalculator.evalStochasticTimeExpression(str[2]);
            else
                retry = customCalculator.evalFailureFormula(str[2]);
        } while (retry);


        return str;
    }

    private boolean containsOperator(String str) {

        return !str.contains(" > ") && !str.contains(" < ") && !str.contains(" >= ") && !str.contains(" <= ") && !str.contains(" = ");
    }

}
