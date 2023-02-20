import models.eduBlock;
import monitor.time.conveyor;
import utils.utils;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {

        int state = 0;
        String in;

        ArrayList<eduBlock> eduBlocks = new ArrayList<>();
        int current_block = -1;

        Scanner input = new Scanner(System.in);

        while (state != -1) {
            try {
                switch (state) {
                    case 0 -> {
                        System.out.println("*********************************************");
                        System.out.println("**** SuperCoordinator Terminal Interface ****");
                        System.out.println("*********************************************");
                        System.out.println();
                        System.out.println("    1 - Create new block; ");
                        System.out.println("    2 - List all blocks;  ");
//                        System.out.println("    3 - List block i;  ");
                        System.out.println("    4 - Active Threads;");
                        System.out.println("   -1 - Exit program;");
                        in = input.nextLine();


                        if (Integer.parseInt(in) == 1)
                            ++state;
                        else if (Integer.parseInt(in) == 2) {
                            for (eduBlock block : eduBlocks) {
                                System.out.println("    " + "->" + block.getName());
                            }
                        } /*else if (Integer.parseInt(in) == 3) {
                            System.out.print("  Block name: ");
                            in = input.nextLine();
                            eduBlock temp;
                            for (eduBlock block : eduBlocks) {
                                if (block.getName().equalsIgnoreCase(in)) {
                                    temp = block;
                                    break;
                                }
                            }


                        }*/ else if (Integer.parseInt(in) == 4) {
                            System.out.println("#Threads: " + Thread.activeCount());
                        } else
                            state = -1;
                    }
                    case 1 -> {
                        System.out.println("**** New Educational Block ****");
                        System.out.println();
                        System.out.print(" Name: ");
                        String name = input.nextLine();

                        System.out.println("**** Communication Protocol ****");
                        System.out.println();
                        System.out.println("    1 - Modbus TCP/IP; ");
                        System.out.println("    2 - OPC UA;");
                        System.out.println("    0 - Go back;");
                        in = input.nextLine();
                        if (Integer.parseInt(in) == 1)
                            state++;
                        else if (Integer.parseInt(in) == 0) {
                            state = 0;
                            break;
                        }

                        eduBlocks.add(new eduBlock(name, eduBlock.communication.MODBUS));
                        current_block = eduBlocks.size() - 1;
                    }
                    case 2 -> {
                        System.out.println("**** Communication Protocol -> Modbus ****");
                        System.out.println();
                        System.out.print(" IP: ");
                        String ip = input.nextLine();

                        System.out.print(" Port: ");
                        int port = Integer.parseInt(input.nextLine());
                        System.out.print(" Slave ID (0-255): ");
                        int slaveID = Integer.parseInt(input.nextLine());

                        eduBlocks.get(current_block).openCommunication(ip, port, slaveID);

                        System.out.println();
                        System.out.println("Connection Established!");
                        ++state;
                    }
                    case 3 -> {
                        System.out.println("**** Define IO mapping ****");
                        System.out.println();
                        System.out.println("    1 - Import from CSV file; ");
                        System.out.println("    2 - Add manually;");
                        in = input.nextLine();

                        if (Integer.parseInt(in) == 1) {
                            System.out.print(" File location (path):");
                            String io_file_path = input.nextLine();
                            eduBlocks.get(current_block).importIO(io_file_path);
                            ++state;
                        }

                        eduBlocks.get(current_block).printAllIO();

                    }
                    case 4 -> {
                        System.out.println("**** Monitoring Configuration  ****");
                        System.out.print("    How many elements to monitor?");
                        in = input.nextLine();
                        int nElements = Integer.parseInt(in);
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

                            scheduler.scheduleAtFixedRate(
                                    new conveyor(eduBlocks.get(current_block).getMb(),
                                            name,
                                            sStart,
                                            invLogic_start,
                                            sEnd,
                                            invLogic_end,
                                            true,
                                            ""), 0, 100, TimeUnit.MILLISECONDS);
                        }
                        state = 0;
                    }
                    default -> {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Exit program
        if (current_block != -1)
            eduBlocks.get(current_block).closeCommunication();

        /*try {

            TreeMap<String, fieldObj> fieldObjs = new TreeMap<>();
            csvReader.readModbusTags(csv_path, fieldObjs, false);

            fieldObjs.forEach((key, value) -> System.out.println(
                    key + " " + value.getType() + " " + value.getDataType() + " " + value.getAddressType() + " " + value.getBit_offset()));

            utils utils = new utils();
        *//*
        utils.getSensorsOrActuators(fieldObjs, false).forEach(
                (key, value) -> System.out.println(
                        key + " " + value.getType() + " " + value.getDataType() + " " + value.getAddressType() + " " + value.getBit_offset()));

         *//*
            System.out.println();
            modbus MB = new modbus();

            MB.openConnection("192.168.216.130", Modbus.DEFAULT_PORT);

            ArrayList<part> productionParts = new ArrayList<>();

            ScheduledExecutorService measScheduler = Executors.newScheduledThreadPool(4);
            measScheduler.scheduleAtFixedRate(new conveyors_timers(MB, fieldObjs, productionParts), 0, 10, TimeUnit.MILLISECONDS);
            measScheduler.scheduleAtFixedRate(new conveyors(MB, fieldObjs, "entry", productionParts), 0, 10, TimeUnit.MILLISECONDS);
            measScheduler.scheduleAtFixedRate(new failures.machine(MB, fieldObjs, "machine_center_1", 10), 0, 10, TimeUnit.MILLISECONDS);
            measScheduler.scheduleAtFixedRate(new conveyors(MB, fieldObjs, "exit", productionParts), 0, 10, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
