package controllers.transport;

import communication.modbus;
import models.SFEx_particular.SFEM_transport;
import models.base.SFEE;
import monitor.SFEE_transport_monitor;
import monitor.base.SFEM_transport_monitor;

public class cSFEM_transport implements Runnable {

    private final SFEM_transport sfem;
    private SFEM_transport_monitor sfemTransportMonitor;

    private cSFEE_transport sfeeTransportController;

    private final viewers.SFEM_transport viewer;


    public cSFEM_transport(SFEM_transport sfemTransport) {
        this.sfem = sfemTransport;

        this.viewer = new viewers.SFEM_transport();
    }

    public void init_SFEE_transport() {

        try {
            //String[] input = viewer.init_SFEE_transport();
            String[] input = new String[]{"SFEE_Connection_test", "1", "1", "1"};
            SFEE.communicationOption com = null;
            if (!input[2].equals(input[3])) {
                com = SFEE.communicationOption.MIXED;
            } else {
                com = Integer.parseInt(input[1]) == 1 ? SFEE.communicationOption.MODBUS : SFEE.communicationOption.OPC_UA;
            }

            SFEE sfeeTransp = new SFEE(input[0],
                    Integer.parseInt(input[1]) == 1 ? SFEE.SFEE_type.SIMULATION : SFEE.SFEE_type.REAL,
                    SFEE.SFEE_function.TRANSPORT,
                    com);

            sfem.setSfeeTransport(sfeeTransp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Here initialization of SFEM_transport_monitor in case it will be needed !
        this.sfemTransportMonitor = new SFEM_transport_monitor(sfem);

    }

    public void init_SFEE_transport_controller(modbus inMB, modbus outMB, SFEE inSFEE, SFEE outSFEE) {

        // Create new SFEE_tranport controller instance
        sfeeTransportController = new cSFEE_transport(sfem.getSfeeTransport(), inMB, outMB);

        // init that instance
        sfeeTransportController.cSFEE_transport_init(inSFEE, outSFEE);

        sfeeTransportController.initOperationMode();

    }

    @Override
    public void run() {
        sfeeTransportController.loop();

        sfemTransportMonitor.loop();
    }

}
