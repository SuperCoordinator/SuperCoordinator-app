package viewers;

import models.base.SFEE;
import utility.customCalculator;
import utility.utils;

import java.util.Scanner;

public class SFEE_transport {

    private final Scanner in;

    public SFEE_transport() {
        this.in = new Scanner(System.in);
    }

    public String[] associateSensor2Actuator(boolean isEntry, SFEE sfee, String sensorName) {

        String[] str = new String[isEntry ? 3 : 1];
        if (isEntry) {

            System.out.println("Emit Coil actuator of sensor " + sensorName);
            str[0] = sfee.getIo().get(utils.getInstance().validateUserOption(0, sfee.getIo().size() - 1)).getName();
            System.out.println("Emit Part register");
            str[1] = sfee.getIo().get(utils.getInstance().validateUserOption(0, sfee.getIo().size() - 1)).getName();
            System.out.println("Emit Base register");
            str[2] = sfee.getIo().get(utils.getInstance().validateUserOption(0, sfee.getIo().size() - 1)).getName();
        } else {
            System.out.println("Remover Coil actuator of sensor " + sensorName);
            str[0] = sfee.getIo().get(utils.getInstance().validateUserOption(0, sfee.getIo().size() - 1)).getName();
        }

        return str;
    }

    public String[] SFEE_stochasticTime(String sfeeName) {

        String str;
        customCalculator customCalculator = new customCalculator();
        boolean retry = false;

        System.out.println("*** Stochastic Operation Time for Element " + sfeeName + " ***");
        System.out.println("""
                Runtime variables:
                    > n - number of pieces moved
                    > a - age of the machine in minutes
                    > m - time since last maintenance in minutes
                Basic Operator: + - * / %
                Supported formulas:
                    > gauss  [ A ; B ]
                    > linear [ A ]
                A,B : constant values or expressions depending the runtime variables
                Please insert a space between each character/number (p.e: gauss [ 65 + ( 0.001 * n) ; 3.5 + 0.1 * a ]""");
        System.out.print("Enter expression:");
        do {
            if (retry)
                System.out.println("Error Message: " + customCalculator.errorMsg());
            str = in.nextLine();
            retry = customCalculator.evalStochasticTimeExpression(str);
        } while (retry);

        return customCalculator.getStochasticTimeFormulaElements();
    }
}
