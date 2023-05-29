package viewers;

import models.SFEx.SFEI_conveyor;
import models.SFEx.SFEI_machine;
import models.SFEx.SFEI_pusher;
import models.base.SFEI;
import models.partDescription;
import models.sensor_actuator;
import utility.customCalculator;
import utility.utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
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

    public String getIOpath(String sfeeName) {
        System.out.println("Path to I/O CSV file");
        System.out.print(">");
        return in.nextLine();
    }

    public String opMode(String sfeeName) {

        System.out.println("Operation Mode for " + sfeeName);
        System.out.println("    1 - Normal");
        System.out.println("    2 - Programmed Failures");
        int opt;
        do {
            System.out.print("> ");
            opt = Integer.parseInt(in.nextLine());
        } while (opt != 1 && opt != 2);

        return String.valueOf(opt);
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

    public String[] associateVisionSensors(models.base.SFEE sfee) {
        String[] str = new String[]{"", "", ""};
        System.out.println("Associate F_IO Vision Sensor in numerical configuration (y/n)?");
        System.out.print("> ");
        str[0] = in.nextLine();
        if (str[0].contains("y")) {

            System.out.println("Vision sensor index");
            str[1] = String.valueOf(utils.getInstance().validateUserOption(0, sfee.getIo().size() - 1));

            listElementItems(sfee);

            System.out.println("Placed on Item (SFEI) number");
            int opt = utils.getInstance().validateUserOption(0, sfee.getSFEIs().size() - 1);
            str[2] = String.valueOf(opt);

        } else {
            str[0] = "no";
        }

        return str;
    }

    private void listElementItems(models.base.SFEE sfee) {
        System.out.println("Element " + sfee.getName() + " items");
        for (Map.Entry<Integer, SFEI> entry : sfee.getSFEIs().entrySet()) {
            System.out.println("   " + entry.getKey() + " - " + entry.getValue().getName());
        }
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

    public ArrayList<String[]> SFEE_failures(String sfeeName) {

        ArrayList<String[]> formulas = new ArrayList<>();

        System.out.println("*** Failures Definition per runtime variable for Element " + sfeeName + " ***");
        System.out.println("""
                Supported formulas
                    > gauss  [ A ; B ]          (A: mean / B: deviation)
                    > prob   [ A ] (signal) B   (signal: > OR = OR < OR <= OR >= ),(B belongs to [0, 100])
                    > det    [ A ]
                A,B : ONLY constant values (can be floating point)
                Example:
                    > m : prob [ 70 ] >= 90
                Interpretation
                    -> When time since last maintenance (m) equal to 70 minutes, if the current value of the probability
                    is bigger or equal to 90 then the failure happen.
                NOTE 1 : Insert a space between each character/number, i.e.: gauss [ 70 ; 5 ]
                NOTE 2 : 'no' to skip expression!""");

        System.out.println("BREAKDOWN WITH REPAIR ");
        System.out.println("   BREAK ");
        String[] f_BDwR = variablesFormulas();

        String repair;
        boolean retry = false;
        do {
            if (retry) {
                System.out.println("Msg (repair): " + utils.getInstance().getCustomCalculator().errorMsg());
            }
            if (!(f_BDwR[0].equalsIgnoreCase("no") && f_BDwR[1].equalsIgnoreCase("no") && f_BDwR[2].equalsIgnoreCase("no"))) {
                System.out.print("   REPAIR (time, in minutes, since breakdown occurred in min): gauss [ ");
                repair = "gauss [ ";
                repair = repair.concat(in.nextLine());
                retry = utils.getInstance().getCustomCalculator().evalFailureFormula(repair);
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

        System.out.println("PRODUCE LESS");
        String[] f_PL = variablesFormulas();

        formulas.add(f_PL);

        return formulas;
    }

    private String[] variablesFormulas() {
        String[] str = new String[3];

        boolean retry = false;
        do {
            if (retry)
                System.out.println("Error Message: " + utils.getInstance().getCustomCalculator().errorMsg());
            System.out.print("  > n : ");
            str[0] = in.nextLine();
            if (containsOperator(str[0]))
                retry = utils.getInstance().getCustomCalculator().evalStochasticTimeExpression(str[0]);
            else
                retry = utils.getInstance().getCustomCalculator().evalFailureFormula(str[0]);
        } while (retry);

        do {
            if (retry)
                System.out.println("Error Message: " + utils.getInstance().getCustomCalculator().errorMsg());
            System.out.print("  > a : ");
            str[1] = in.nextLine();
            if (containsOperator(str[1]))
                retry = utils.getInstance().getCustomCalculator().evalStochasticTimeExpression(str[1]);
            else
                retry = utils.getInstance().getCustomCalculator().evalFailureFormula(str[1]);
        } while (retry);

        do {
            if (retry)
                System.out.println("Error Message: " + utils.getInstance().getCustomCalculator().errorMsg());
            System.out.print("  > m : ");
            str[2] = in.nextLine();
            if (containsOperator(str[2]))
                retry = utils.getInstance().getCustomCalculator().evalStochasticTimeExpression(str[2]);
            else
                retry = utils.getInstance().getCustomCalculator().evalFailureFormula(str[2]);
        } while (retry);


        return str;
    }

    private boolean containsOperator(String str) {

        return !str.contains(">") && !str.contains("<") && !str.contains(">=") && !str.contains("<=") && !str.contains("=");
    }

    public ArrayList<SFEI> createSFEIs(models.base.SFEE sfee, boolean supportFailures) {
        System.out.println("Note 1 : Instant for the item age and maintenance will be the same and equal to this precise moment!");
        System.out.println("         It will be possible to change in the XML configuration file.");
        System.out.println("Note 2 : For the inputs and outputs please check the window with Element I/Os.");
        System.out.println("         Enter -1 to leave empty!");
        System.out.println("How many Items (SFEIs) for Element " + sfee.getName());
        System.out.print("> ");
        int nSFEIS = Integer.parseInt(in.nextLine());
        ArrayList<SFEI> sfeis = new ArrayList<>();

        for (int i = 0; i < nSFEIS; i++) {
            System.out.println("Item " + i + " type");
            System.out.println("    1 - Conveyor");
            System.out.println("    2 - Machine");
            System.out.println("    3 - Pusher (only for the MC Staudinger)");
            int opt = utils.getInstance().validateUserOption(1, 3);

            switch (opt) {
                case 1 -> sfeis.add(newConveyor(sfee, supportFailures));
                case 2 -> sfeis.add(newMachine(sfee, supportFailures));
                case 3 -> sfeis.add(newPusher(sfee));
            }
        }

        return sfeis;
    }

    private SFEI_conveyor newConveyor(models.base.SFEE sfee, boolean supportFailures) {

        System.out.print("Item conveyor name: ");
        String name = in.nextLine();

        sensor_actuator inSensor = getSensorActuator("Input sensor: ", sfee);
        sensor_actuator outSensor = getSensorActuator("Output sensor: ", sfee);

        boolean isSimulation = sfee.getSFEE_environment().equals(models.base.SFEE.SFEE_environment.SIMULATION);

//        System.out.println("Is simulation (y/n)?");
//        boolean isSimulation = utils.getInstance().validateUserOption();
//        System.out.println("Supports failures (y/n) ?");
//        boolean supportsFailures = utils.getInstance().validateUserOption();
//        System.out.println("Is line start (y/n)? (First item of all Modules combined)");
//        boolean isLineStart = utils.getInstance().validateUserOption();
//        boolean isLineEnd = false;
//        if (!isLineStart) {
//            System.out.println("Is line end (y/n) ? (Last item of all Modules combined)");
//            isLineEnd = utils.getInstance().validateUserOption();
//        }

        sensor_actuator[] vector = new sensor_actuator[7];

        if (supportFailures) {

            vector[0] = getSensorActuator("Remover actuator: ", sfee);
            vector[1] = getSensorActuator("Emit actuator: ", sfee);
            vector[2] = getSensorActuator("Emit Part register: ", sfee);
            vector[3] = getSensorActuator("Emit Base register: ", sfee);
            vector[4] = getSensorActuator("Remover sensor: ", sfee);
            vector[5] = getSensorActuator("Emitter sensor: ", sfee);
        }
        vector[6] = getSensorActuator("Conveyor Motor actuator: ", sfee);

        return new SFEI_conveyor(name, inSensor, outSensor,
                Instant.now(), Instant.now(),
                isSimulation, supportFailures,
                vector);
    }

    private sensor_actuator getSensorActuator(String msg, models.base.SFEE sfee) {

        int max_io_idx = sfee.getIo().size() - 1;
        System.out.println(msg);
        int io = utils.getInstance().validateUserOption(-1, max_io_idx);
        return io > 0 ? sfee.getIo().get(io) : null;
    }

    private SFEI_machine newMachine(models.base.SFEE sfee, boolean supportFailures) {

        System.out.print("Item machine name: ");
        String name = in.nextLine();
        System.out.println("Note : The UNKNOWN is only for the Staudinger Machine");
        System.out.println("Part Material");
        for (partDescription.material material : partDescription.material.values()) {
            System.out.println("    " + material.ordinal() + " - " + material.name());
        }
        int opt = utils.getInstance().validateUserOption(0, partDescription.material.values().length - 1);
        partDescription.material mat = partDescription.material.values()[opt];

        System.out.println("Part Form");
        for (partDescription.form form : partDescription.form.values()) {
            System.out.println("    " + form.ordinal() + " - " + form.name());
        }
        opt = utils.getInstance().validateUserOption(0, partDescription.form.values().length - 1);
        partDescription.form f = partDescription.form.values()[opt];

        sensor_actuator inSensor = getSensorActuator("Input sensor: ", sfee);
        sensor_actuator outSensor = getSensorActuator("Output sensor: ", sfee);

//        System.out.println("Is simulation (y/n) ?");
//        boolean isSimulation = utils.getInstance().validateUserOption();
//        System.out.println("Supports failures (y/n) ?");
//        boolean supportsFailures = utils.getInstance().validateUserOption();
        boolean isSimulation = sfee.getSFEE_environment().equals(models.base.SFEE.SFEE_environment.SIMULATION);
//        System.out.println("Is line start (y/n)? (First item of all Modules combined)");
//        boolean isLineStart = utils.getInstance().validateUserOption();
//        System.out.println("Supports failures (y/n) ? (Last item of all Modules combined)");
//        boolean isLineEnd = utils.getInstance().validateUserOption();
        sensor_actuator[] vector = new sensor_actuator[3];
        if (isSimulation) {
            vector[0] = getSensorActuator("Produce register: ", sfee);
            if (supportFailures) {
                vector[1] = getSensorActuator("Machine Door sensor: ", sfee);
                vector[2] = getSensorActuator("Stop Machine actuator: ", sfee);
            }
        } else {
            vector[2] = getSensorActuator("Stop Machine actuator: ", sfee);
        }

        return new SFEI_machine(name, new partDescription(mat, f), inSensor, outSensor,
                Instant.now(), Instant.now(),
                isSimulation, supportFailures,
                vector);
    }

    private SFEI_pusher newPusher(models.base.SFEE sfee) {

        System.out.print("Item Staudinger pusher name: ");
        String name = in.nextLine();

        sensor_actuator inSensor = getSensorActuator("Pusher back sensor: ", sfee);
        sensor_actuator outSensor = getSensorActuator("Pusher forward sensor: ", sfee);


//        System.out.println("Is line start (y/n)? (First item of all Modules combined)");
//        boolean isLineStart = utils.getInstance().validateUserOption();
//        System.out.println("Is line end (y/n) ? (Last item of all Modules combined)");
//        boolean isLineEnd = utils.getInstance().validateUserOption();

        sensor_actuator aBackMotor = getSensorActuator("Pusher back actuator: ", sfee);
        sensor_actuator aForwardMotor = getSensorActuator("Pusher forward actuator: ", sfee);

        return new SFEI_pusher(name, inSensor, outSensor,
                Instant.now(), Instant.now(), false, false,
                aBackMotor, aForwardMotor);
    }

    public int[] startEnd_sfeis(models.base.SFEE sfee) {
        int[] vector = new int[2];

        listElementItems(sfee);
        System.out.println("   " + sfee.getSFEIs().size() + " - NONE");
        System.out.println("Which Item (SFEI) is line START ?");
        vector[0] = utils.getInstance().validateUserOption(0, sfee.getSFEIs().size());

        System.out.println("Which Item (SFEI) is line END ?");
        vector[1] = utils.getInstance().validateUserOption(0, sfee.getSFEIs().size());
        return vector;
    }
}
