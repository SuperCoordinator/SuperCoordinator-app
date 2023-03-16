import utils.customCalculator;

import java.util.Scanner;

public class formulasTest {

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        customCalculator customCalculator = new customCalculator();
        String str = " ";
        boolean retry = true, error = false;
        try {
            do {
                if (error)
                    System.out.println("Msg: " + customCalculator.errorMsg(str));
                System.out.println("Write formula for evaluation: ");
                str = in.nextLine();
                if (str.equalsIgnoreCase("exit"))
                    retry = false;
                error = customCalculator.evalExpression(str);
                System.out.println("Errors? " + error + customCalculator.calcExpression(str, 1, 1, 1));

            } while (retry);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
