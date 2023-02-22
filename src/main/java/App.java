import models.eduBlock;
import monitor.time.conveyor;
import monitor.timestamp_pair;
import utils.utils;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

public class App {

    public static void main(String[] args) {

        int state = 0;
        String in;

        ArrayList<eduBlock> eduBlocks = new ArrayList<>();
        int iCurrBlock = -1;

        Scanner input = new Scanner(System.in);

        // Running time phase
        eduBlock currBlock;

        while (state != -1) {
            try {
                switch (state) {
                    case 0 -> {
                        // Clean memory
                        if (iCurrBlock != -1)
                            eduBlocks.clear();

                        System.out.println("*********************************************");
                        System.out.println("**** SuperCoordinator Terminal Interface ****");
                        System.out.println("******** STEP 1 - Configuration Phase *******");
                        System.out.println("*********************************************");
                        System.out.println();
                        System.out.println("    1 - Create new block; ");
                        System.out.println("    0 - Exit program;");
                        in = input.nextLine();

                        if (Integer.parseInt(in) == 1)
                            state = 1;
                        else if (Integer.parseInt(in) == 0) {
                            state = -1;
                        }

                    }
                    case 1 -> {
                        System.out.println("**** New Educational Block ****");
                        System.out.println();
                        System.out.print(" Name: ");
                        String name = input.nextLine();

                        System.out.println("**** Communication Protocol ****");
                        System.out.println();
                        System.out.println("    1 - Modbus TCP/IP; ");
                        //System.out.println("    2 - OPC UA;");
                        System.out.println("    0 - Go back;");
                        in = input.nextLine();
                        if (Integer.parseInt(in) == 1)
                            state = 2;
                        else if (Integer.parseInt(in) == 0) {
                            state = 0;
                            break;
                        }

                        eduBlocks.add(new eduBlock(name, eduBlock.blockType.SIMULATION, eduBlock.communication.MODBUS));
                        iCurrBlock = eduBlocks.size() - 1;
                    }
                    case 2 -> {
                        System.out.println("**** Communication Protocol -> Modbus ****");
                        System.out.println();
                        System.out.print("    IP: ");
                        String ip = input.nextLine();

                        System.out.print("    Port: ");
                        int port = Integer.parseInt(input.nextLine());
                        System.out.print("    Slave ID (0-255): ");
                        int slaveID = Integer.parseInt(input.nextLine());

                        eduBlocks.get(iCurrBlock).openCommunication(ip, port, slaveID);

                        System.out.println();
                        System.out.println("Connection Established!");
                        state = 3;
                    }
                    case 3 -> {
                        System.out.println("**** Define IO mapping ****");
                        System.out.println();
                        System.out.println("    1 - Import from CSV file; ");
                        System.out.println("    0 - Go back;");
                        //System.out.println("    2 - Add manually;");
                        in = input.nextLine();

                        if (Integer.parseInt(in) == 1) {
                            System.out.print("    File location (path):");
                            String io_file_path = input.nextLine();
                            eduBlocks.get(iCurrBlock).importIO(io_file_path);
                            ++state;
                        } else if (Integer.parseInt(in) == 0) {
                            state = 0;
                        }

                        System.out.print("    Print imported IO (y/n)? ");
                        String ans = input.nextLine();
                        if (ans.contains("y"))
                            eduBlocks.get(iCurrBlock).printAllIO();

                    }
                    case 4 -> {
                        System.out.println("**** Monitoring Configuration  ****");
                        System.out.print("    How many elements to monitor?");
                        in = input.nextLine();
                        int nElements = Integer.parseInt(in);
/*
                        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(nElements);

                        for (int i = 0; i < nElements; ++i) {
                            System.out.print("  Name:");
                            String name = input.nextLine();
                            System.out.print("  Start sensor:");
                            String sStart = input.nextLine();
                            System.out.print("    Inverse logic (y/n)? ");
                            String ans = input.nextLine();
                            boolean invLogic_start = ans.contains("y");
                            System.out.print("  End sensor:");
                            String sEnd = input.nextLine();
                            System.out.print("    Inverse logic (y/n)? ");
                            ans = input.nextLine();
                            boolean invLogic_end = ans.contains("y");
                            // VERIFICAR SE OS SENSORES EXISTEM ?

                           Runnable task = new conveyor(eduBlocks.get(current_block).getMb(),
                                    name,
                                    sStart,
                                    invLogic_start,
                                    sEnd,
                                    invLogic_end,
                                    true,
                                    ""

                            );

                            scheduler.scheduleAtFixedRate(task, 0, 100, TimeUnit.MILLISECONDS);
                        }*/


                        for (int i = 0; i < nElements; ++i) {
                            System.out.print("  Name:");
                            String name = input.nextLine();
                            System.out.print("  Start sensor:");
                            String sStart = input.nextLine();
                            System.out.print("    Inverse logic (y/n)? ");
                            String ans = input.nextLine();
                            boolean invLogic_start = ans.contains("y");
                            System.out.print("  End sensor:");
                            String sEnd = input.nextLine();
                            System.out.print("    Inverse logic (y/n)? ");
                            ans = input.nextLine();
                            boolean invLogic_end = ans.contains("y");
                            // VERIFICAR SE OS SENSORES EXISTEM ?

                            eduBlocks.get(iCurrBlock).addMonitorTimeConveyor(
                                    new conveyor(name,
                                            sStart,
                                            invLogic_start,
                                            sEnd,
                                            invLogic_end,
                                            true,
                                            ""));
                        }

                        state++;

                    }
                    case 5 -> {
                        System.out.println("**** Failure Configuration  ****");
                        System.out.print("    How many conveyors to manipulate its time ?");
                        in = input.nextLine();
                        int nElements = Integer.parseInt(in);

                        for (int i = 0; i < nElements; ++i) {
                            System.out.print("  Name:");
                            String name = input.nextLine();

                            String[] sensAct = new String[4];
                            boolean[] invSensAct = new boolean[2];

                            // VERIFICAR SE OS SENSORES EXISTEM ?
                            System.out.print("  Remover sensor:");
                            sensAct[0] = input.nextLine();
                            System.out.print("    Inverse logic (y/n)? ");
                            String ans = input.nextLine();
                            invSensAct[0] = ans.contains("y");

                            System.out.print("  Emitter sensor:");
                            sensAct[1] = input.nextLine();
                            System.out.print("    Inverse logic (y/n)? ");
                            ans = input.nextLine();
                            invSensAct[1] = ans.contains("y");

                            System.out.print("  Remover actuator:");
                            sensAct[2] = input.nextLine();

                            System.out.print("  Emitter actuator:");
                            sensAct[3] = input.nextLine();

                            eduBlocks.get(iCurrBlock).addFailureConveyor(
                                    new failures.conveyor(name,
                                            failures.conveyor.ERROR_TYPE.INCREASE_LINEAR,
                                            1.5,
                                            sensAct,
                                            invSensAct));
                        }

                        state = 6;
                    }
                    case 6 -> {
                        System.out.println("    1 - Swap to running mode; ");
                        System.out.println("    2 - Add more blocks");
                        in = input.nextLine();
                        if (Integer.parseInt(in) == 1) {
                            eduBlocks.get(iCurrBlock).startBlock();
                            state = 10;
                        } else if (Integer.parseInt(in) == 2) {
                            state = 1;
                        }
                    }
                    case 10 -> {
                        System.out.println("*********************************************");
                        System.out.println("**** SuperCoordinator Terminal Interface ****");
                        System.out.println("*********** STEP 2 - Running Phase **********");
                        System.out.println("*********************************************");
                        System.out.println();
                        System.out.println("    1 - List running blocks;");
                        System.out.println("    2 - List/Change running time parameters;");
                        System.out.println("    0 - Exit program;");

                        in = input.nextLine();
                        if (Integer.parseInt(in) == 0) {
                            state = -1;
                        } else if (Integer.parseInt(in) == 1) {
                            for (eduBlock block : eduBlocks) {
                                System.out.println("    -> " + block.getName());
                            }
                        } else if (Integer.parseInt(in) == 2) {
                            state = 11;
                        }
                    }
                    case 11 -> {
                        currBlock = null;
                        System.out.println("**** Running time Parameters  ****");
                        System.out.print("    From each block (name)?");
                        in = input.nextLine();

                        for (eduBlock block : eduBlocks) {
                            if (block.getName().equalsIgnoreCase(in)) {
                                currBlock = block;
                                break;
                            }
                        }
                        if (currBlock == null) {
                            System.out.println("Educational Block not found!");
                            System.out.println("Please enter name again.");
                        } else {
                            System.out.println("**** Monitoring Tasks  ****");
                            System.out.print("      Print associated timestamps (y/n)?");
                            boolean print = input.nextLine().contains("y");

                            for (monitor.time.conveyor convMon : currBlock.getMonitorTimeConveyors()) {
                                System.out.println("   " + convMon.getName());
                                if (print) {
                                    long duration = 0;
                                    int discount = 0;
                                    for (Map.Entry<Integer, timestamp_pair> entry : convMon.getTimestamps().entrySet()) {
                                        System.out.println("      (" + entry.getKey() + ") " + Arrays.toString(entry.getValue().getPair()));

                                        if (entry.getValue().getPair()[0] != null && entry.getValue().getPair()[1] != null) {
                                            duration = duration + entry.getValue().getDuration().toSeconds();
                                        } else
                                            discount++;
                                    }

                                    DecimalFormat df = new DecimalFormat("#.###");
                                    System.out.println(convMon.getTimestamps().size() + " parts moved with mean time " + df.format(duration / (convMon.getTimestamps().size() - discount)) + " s");
                                    System.out.println();
                                }
                            }
                        }

                        System.out.println("**** Failure Tasks  ****");
                        assert currBlock != null;
                        for (failures.conveyor convFail : currBlock.getFailureConveyors()) {
                            System.out.println("   " + convFail.getName());
                        }

                        System.out.print("     Change stop time (y/n)?");
                        if (input.nextLine().contains("y")) {
                            System.out.print("      Which from (name)?");
                            String name = input.nextLine();
                            failures.conveyor temp = null;
                            for (failures.conveyor convFail : currBlock.getFailureConveyors()) {
                                if (convFail.getName().equalsIgnoreCase(name)) {
                                    temp = convFail;
                                    break;
                                }
                            }
                            if (temp == null)
                                System.out.println();
                            else {
                                System.out.println("       Current time value: " + temp.getTime_adjust_param());
                                System.out.print("       New time value:");

                                temp.setTime_adjust_param(Double.parseDouble(input.nextLine()));
                                System.out.println("Value set to :" + temp.getTime_adjust_param());
                            }

                        }

                    }
                    default -> {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Exit program
        if (iCurrBlock != -1) {
            eduBlocks.get(iCurrBlock).closeCommunication();
        }

    }
}
