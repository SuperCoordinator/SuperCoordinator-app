package controllers.transport;

import communication.modbus;
import models.SFEx.SFEM_transport;
import models.base.SFEE;
import models.base.SFEI;
import models.base.SFEM;
import models.base.part;
import monitor.transport.SFEM_transport_monitor;
import org.apache.commons.math3.util.Pair;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class cSFEM_transport implements Runnable {

    @XmlElement
    private SFEM_transport sfem;
    private SFEM_transport_monitor sfemTransportMonitor;
    @XmlElement
    private cSFEE_transport sfeeTransportController;

    private viewers.SFEM_transport viewer = new viewers.SFEM_transport();

    public cSFEM_transport() {
    }

    public cSFEM_transport(SFEM_transport sfemTransport) {
        this.sfem = sfemTransport;
    }

    public SFEM_transport getSfem() {
        return sfem;
    }

    public cSFEE_transport getSfeeTransportController() {
        return sfeeTransportController;
    }

    public void initSFEE_transport_FromTerminal() {

        try {
//            String[] input = viewer.initSFEE_transport_FromTerminal();
            String[] input = new String[]{"SFEE_Connection_test", "1", "1", "1"};
            SFEE.communicationOption com;
            if (!input[2].equals(input[3])) {
                com = SFEE.communicationOption.MIXED;
            } else {
                com = Integer.parseInt(input[1]) == 1 ? SFEE.communicationOption.MODBUS : SFEE.communicationOption.OPC_UA;
            }

            SFEE sfeeTransp = new SFEE(input[0],
                    Integer.parseInt(input[1]) == 1 ? SFEE.SFEE_environment.SIMULATION : SFEE.SFEE_environment.REAL,
                    SFEE.SFEE_role.TRANSPORT,
                    com);

            sfem.setSfeeTransport(sfeeTransp);

            // Initialization of SFEM_transport_monitor
            sfemTransportMonitor = new SFEM_transport_monitor(sfem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init_SFEE_transport(String SFEE_transport_name) {
        try {
            SFEE sfeeTransp = new SFEE(SFEE_transport_name,
                    SFEE.SFEE_environment.SIMULATION,
                    SFEE.SFEE_role.TRANSPORT,
                    SFEE.communicationOption.MODBUS);
            sfem.setSfeeTransport(sfeeTransp);
            sfemTransportMonitor = new SFEM_transport_monitor(sfem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init_after_XML_load() {
        sfeeTransportController.setSfee(sfem.getSfeeTransport());
        sfeeTransportController.setConfiguration(sfem.getTransport_configuration());
        sfemTransportMonitor = new SFEM_transport_monitor(sfem);
    }

    public void init_cSFEETransport(SFEM_transport.configuration configuration, ArrayList<Object> initController_data, ArrayList<Object> initOperationMode_data) {
        try {
            // Create new SFEE_tranport controller instance
            sfeeTransportController = new cSFEE_transport(sfem.getSfeeTransport(), configuration);

            // init that instance
            sfeeTransportController.init(initController_data);

            sfeeTransportController.init_OperationMode(initOperationMode_data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Pair<Pair<String, String>, Pair<String, String>> getPrevNext_SFEE_SFEI_names() {
        return sfeeTransportController.prevNextSFEE();
    }

    public void setupSFEETransportController(modbus inMB, modbus outMB, SFEI inSFEI, SFEI outSFEI) {
        sfeeTransportController.cSFEE_transport_setup(inSFEI, outSFEI, inMB, outMB);
    }

    @Override
    public void run() {
        try {
            sfeeTransportController.loop();
            sfemTransportMonitor.loop();
//            printDBG();
        } catch (Exception e) {
            // In child thread, it must print the Exception because the main thread do not catch Runtime Exception from the others
            e.printStackTrace();
        }

    }

    private void printDBG() {
        for (SFEI sfei : sfeeTransportController.getSfee().getSFEIs().values()) {
            System.out.println(sfei.getName());
            for (part movingPart : sfei.getPartsATM()) {
                System.out.println(movingPart);
            }
        }
    }


}
