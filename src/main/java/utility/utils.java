package utility;

import java.util.Random;
import java.util.Scanner;

public class utils {

    // Declare random seed
    private final Random random = new Random();
    private long rnd_seed = 3587214;

    private static final csv_reader reader = new csv_reader();
    private static final search search = new search();
    private static final logicalOperators logicalOp = new logicalOperators();

    private static final customCalculator customCalc = new customCalculator();


    public utils() {
        random.setSeed(rnd_seed);
    }

    public static utils getInstance() {
        return utils.utilsHolder.INSTANCE;
    }


    private static class utilsHolder {
        private static final utils INSTANCE = new utils();
    }

    public csv_reader getReader() {
        return reader;
    }

    public search getSearch() {
        return search;
    }

    public logicalOperators getLogicalOperator() {
        return logicalOp;
    }

    public customCalculator getCustomCalculator() {
        return customCalc;
    }

    public Random getRandom() {
        return random;
    }

    public long getRnd_seed() {
        return rnd_seed;
    }

    public void setRnd_seed(long rnd_seed) {
        this.rnd_seed = rnd_seed;
    }

    public int validateUserOption(int min, int max) {
        int opt = min;
        Scanner in = new Scanner(System.in);
        do {
            if (opt < min || opt > max)
                System.out.println("Invalid option. Try again!");
            System.out.print("> ");
            opt = Integer.parseInt(in.nextLine());
        } while (opt < min || opt > max);

        return opt;
    }

    public boolean validateUserOption() {
        String input = "y";
        do {
            if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n"))
                System.out.println("Invalid option! Try again");
            System.out.print("> ");
            input = new Scanner(System.in).nextLine();
        } while (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n"));

        return input.equalsIgnoreCase("y");
    }

}
