package viewers;

import models.sensor_actuator;
import utility.customCalculator;

import java.time.Instant;
import java.util.ArrayList;
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

    public boolean isSFEI_simulated() {
        String str;
        System.out.println("The SFEI is simulated (y(n)?");
        do {
            str = in.nextLine();
        } while (!str.contains("y") && !str.contains("n"));

        return str.contains("y");
    }

    public String[] SFEI_params(int num, int type, boolean isSimulation) {
        String[] str = new String[0];
        if (type == 1 && isSimulation) {
            str = new String[12];

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

            System.out.print("Remover actuator: ");
            str[6] = in.nextLine();

            System.out.print("Emitter actuator: ");
            str[7] = in.nextLine();

            System.out.print("Remover sensor: ");
            str[8] = in.nextLine();

            System.out.print("Emitter sensor: ");
            str[9] = in.nextLine();

            System.out.print("Is start of line (y/n)?");
            str[10] = in.nextLine();

            System.out.print("Is end of line (y/n)?");
            str[11] = in.nextLine();

        } else if (type == 1) {
            str = new String[8];

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

            System.out.print("Is start of line (y/n)?");
            str[6] = in.nextLine();

            System.out.print("Is end of line (y/n)?");
            str[7] = in.nextLine();

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

    public String[] associateVisionSensors() {
        String[] str = new String[]{"", "", ""};
        System.out.print("Associate Vision Sensor in numerical configuration (y/n) ?");

        str[0] = in.nextLine();
        if (str[0].contains("y")) {
            System.out.print("Vision sensor name: ");
            str[1] = in.nextLine();
            System.out.print("Placed on: ");
            str[2] = in.nextLine();
        } else {
            str[0] = "no";
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
        System.out.println("Please insert a space between each character/number (p.e: gauss [ 65 + ( 0.001 * n) ; 3.5 + 0.1 * a ]");
        System.out.print("Enter expression:");
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

    public /*String[] */    ArrayList<String[]> SFEEFailures() {
//        String[] str = new String[]{"no", "no", "no"};

        ArrayList<String[]> formulas = new ArrayList<>();
        customCalculator customCalculator = new customCalculator();

        System.out.println("SFEE Failures ?");
        System.out.println("Defined variables x[ n, a, m]: n - number of pieces moved / a - age of the machine in minutes / m - time since last maintenance in minutes");
        System.out.println("Valid operations");
        System.out.println("   -- gauss  [ x ; y ]        => x,y are the numbers related to mean and dev, respectively ");
        System.out.println("   -- prob   [ x ]    (op)  p => x is a number related to the variable [x] / op [>,=,<,<=,>=] / p [0, 100] ");
        System.out.println("   -- linear [ x ]            => x is a number related to the variable [x]");
        System.out.println("That example is interpreted as: the probability of failures after x units (parts or minutes ) is op than p");
        System.out.println("Please insert a space between each character/number (p.e: prob [ 20 ] >= 50 )");


        System.out.println("BREAKDOWN WITH REPAIR ");
        System.out.println("   BREAK ");
        String[] f_BDwR = variablesFormulas();

        String repair = "gauss [ ";
        boolean retry = false;
        do {
            if (retry) {
                System.out.println("Msg (repair): " + customCalculator.errorMsg(repair));
            }
            if (!(f_BDwR[0].equalsIgnoreCase("no") && f_BDwR[1].equalsIgnoreCase("no") && f_BDwR[2].equalsIgnoreCase("no"))) {
                System.out.print("   REPAIR (regarding time since break in min): gauss [ ");
                repair = "gauss [ ";
                repair = repair.concat(in.nextLine());
                retry = customCalculator.evalFailureFormula(repair);
            } else {
                repair = "no";
            }

        } while (retry);

        formulas.add(f_BDwR);
        formulas.add(new String[]{repair});

        System.out.println("BREAKDOWN");
        String[] f_BD = variablesFormulas();

        formulas.add(f_BD);

        System.out.println("PRODUCE FAULTY");
        String[] f_PF = variablesFormulas();

        formulas.add(f_PF);

        System.out.println("PRODUCE MORE");
        String[] f_PM = variablesFormulas();

        formulas.add(f_PM);


//        return str;
        return formulas;
    }

    private String[] variablesFormulas() {
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

        return !str.contains(">") && !str.contains("<") && !str.contains(">=") && !str.contains("<=") && !str.contains("=");
    }

}
