package controllers.production;

import communication.modbus;
import models.base.SFEE;
import models.sfe_x.SFEM_production;
import monitor.production.SFEM_production_monitor;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class cSFEM_production implements Runnable {

    @XmlElement
    private SFEM_production sfem;
    private SFEM_production_monitor sfemMonitor;
    @XmlElement
    private ArrayList<cSFEE_production> sfeeControllers;
    private final viewers.SFEM viewer = new viewers.SFEM();

    public cSFEM_production() {
    }

    public cSFEM_production(SFEM_production sfem) {
        this.sfem = sfem;
        this.sfeeControllers = new ArrayList<>();
    }


    public SFEM_production getSfem() {
        return sfem;
    }

    public ArrayList<cSFEE_production> getSfeeControllers() {
        return sfeeControllers;
    }

    public void init_SFEEs(int nSFEEs, String sfemName) {

        try {

            for (int i = 0; i < nSFEEs; i++) {
                String[] inputs = viewer.SFEE_params(i, sfemName);

                SFEE sfee = new SFEE(
                        inputs[0],
                        Integer.parseInt(inputs[2]) == 1 ? SFEE.SFEE_environment.SIMULATION : SFEE.SFEE_environment.REAL,
                        SFEE.SFEE_role.PRODUCTION,
                        Integer.parseInt(inputs[1]) == 1 ? SFEE.communicationOption.MODBUS : SFEE.communicationOption.OPC_UA);

                sfem.addNewSFEE(sfee);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        init_SFEM_production_monitor();
    }

    private void init_SFEM_production_monitor() {
        sfemMonitor = new SFEM_production_monitor(sfem);
    }

    public void init_SFEE_controllers(/*int scene, int SFEM_idx*/) {
        try {
//            int i = 0;
            for (Map.Entry<Integer, SFEE> sfee : sfem.getSFEEs().entrySet()) {

                String[] comConfig = viewer.communicationParams(sfee.getValue().getCom().ordinal(), sfee.getValue().getName());

                modbus mb = new modbus(comConfig[0], Integer.parseInt(comConfig[1]), Integer.parseInt(comConfig[2]));
                cSFEE_production sfeeController = new cSFEE_production(sfee.getValue(), mb);

                sfeeController.init();
//                if (scene == 0)
//                    sfeeController.init(i == 0 ? scene : -1);
//                else if (scene == 1) {
//                    if (SFEM_idx == 0)
//                        sfeeController.init(3);
//                    else
//                        sfeeController.init(4);
//                } else if (scene == 2) {
//                    if (i == 0)
//                        sfeeController.init(5);
//                    else if (i == 1) {
//                        sfeeController.init(6);
//                    } else if (i == 2) {
//                        sfeeController.init(7);
//                    }
//                } else if (scene == 3) {
//                    if (SFEM_idx == 0) {
//                        sfeeController.init(scene + 5);
//                    } else if (SFEM_idx == 1) {
//                        sfeeController.init(scene + 6 + i);
//                        firstRun(false, i);
//                    }
//                } else if (scene == 4) {
//                    sfeeController.init(scene + 8);
//                } else if (scene == 5) {
//                    sfeeController.init(scene + 8);
//                } else if (scene == 6) {
//                    if (i == 0) {
//                        sfeeController.init(scene + 8);
//                    } else {
//                        sfeeController.init(scene + 8 + i);
//                        firstRun(false, i);
//                    }
//                }

                sfeeController.initFailures();

                sfeeControllers.add(sfeeController);
//                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void init_after_XML_loading() {

        for (int i = 0; i < sfem.getSFEEs().size(); i++) {
            sfeeControllers.get(i).setSfee(sfem.getSFEEbyIndex(i));
        }

        for (cSFEE_production cSFEEProduction : sfeeControllers) {
            cSFEEProduction.init_after_XML_load();
        }
    }

    public void openConnections() {
        try {
            // Open the first connection
            sfeeControllers.get(0).openCommunication();
            System.out.println(sfeeControllers.get(0).getSFEE().getName() + " IP:" + sfeeControllers.get(0).getMb().getIp() + " Port:" + sfeeControllers.get(0).getMb().getPort());
            // For the rest, first check if there are common connections
            for (int i = 1; i < sfeeControllers.size(); i++) {
                cSFEE_production to_define = sfeeControllers.get(i);
                System.out.println(to_define.getSFEE().getName() + " IP:" + to_define.getMb().getIp() + " Port:" + to_define.getMb().getPort());
                modbus found_mb = null;

                for (int j = 0; j < sfeeControllers.size(); j++) {
                    cSFEE_production temp = sfeeControllers.get(j);
                    if (j == i)
                        continue;
                    if (!to_define.getMb().isConfigured() && temp.getMb().isConfigured()) {
                        if (to_define.getMb().getIp().equals(temp.getMb().getIp()))
                            if (to_define.getMb().getPort() == temp.getMb().getPort()) {
                                found_mb = temp.getMb();
                                System.out.println(" Found for " + to_define.getSFEE().getName() +
                                        " IP:" + temp.getMb().getIp() + " Port:" + temp.getMb().getPort() +
                                        " of " + temp.getSFEE().getName());
                                break;
                            }
                    }
                }

                if (found_mb != null) {
                    to_define.setMb(found_mb);
                } else {
                    to_define.openCommunication();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

//    public void firstRun(boolean run, int itr) {
//        if (run)
//            for (cSFEE_production sfeeController : sfeeControllers) {
//                sfeeController.launchSetup();
//            }
//        else {
//            int[] array = new int[]{9, 33, 8};
//            for (int i = 0; i < sfem.getSFEEbyIndex(itr).getSFEIs().size(); i++) {
//                sfem.getSFEEbyIndex(itr).getSFEIbyIndex(i).setMinOperationTime(array[i]);
//            }
//
///*            sfem.getSFEEbyIndex(1).getSFEIbyIndex(0).setMinOperationTime(9);
//            sfem.getSFEEbyIndex(1).getSFEIbyIndex(1).setMinOperationTime(33);
//            sfem.getSFEEbyIndex(1).getSFEIbyIndex(2).setMinOperationTime(8);*/
//        }
//
//    }

    public modbus searchMBbySFEE(String sfeeName) {
        try {
            modbus mb = null;
            for (cSFEE_production sfeeProduction : sfeeControllers) {
                if (sfeeProduction.getSFEE_name().equals(sfeeName)) {
                    mb = sfeeProduction.getMb();
                    break;
                }
            }
            if (mb == null)
                throw new RuntimeException("Not found MB connection for SFEE " + sfeeName);
            return mb;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean firstExe = true;

    public void startSimulation() {

        if (firstExe) {

            for (cSFEE_production sfeeController : sfeeControllers) {
                sfeeController.getMb().reOpenConnection();
            }

            firstExe = false;

            // In case of load from XML
            if (sfemMonitor == null)
                init_SFEM_production_monitor();
        }
        sfeeControllers.get(0).launchSimulation();
    }

    public void endSimulation() {
        sfeeControllers.get(0).stopSimulation();

        for (cSFEE_production sfeeController : sfeeControllers) {
            sfeeController.closeCommunication();
        }

    }


//    private final List<Long> C_Runtime = new ArrayList<>();

    @Override
    public void run() {
        try {
//            Instant start_t = Instant.now();

            for (cSFEE_production sfeeController : sfeeControllers) {
                sfeeController.loop();
            }
//            sfemMonitor.loop(/*C_Runtime*/);

//            C_Runtime.add(Duration.between(start_t, Instant.now()).toMillis());
        } catch (Exception e) {
            // In child thread, it must print the Exception because the main thread do not catch Runtime Exception from the others
            e.printStackTrace();
        }
    }


}
