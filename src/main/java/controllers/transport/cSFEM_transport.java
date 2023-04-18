package controllers.transport;

import communication.modbus;
import models.SFEx_particular.SFEM_transport;
import models.base.SFEE;
import models.base.SFEI;
import monitor.transport.SFEM_transport_monitor;
import org.apache.commons.math3.util.Pair;

import javax.xml.bind.annotation.*;

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

    public void init_SFEE_transport() {

        try {
//            String[] input = viewer.init_SFEE_transport();
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
        sfemTransportMonitor = new SFEM_transport_monitor(sfem);

    }

    public void init_after_XML_load() {
        sfeeTransportController.setSfee(sfem.getSfeeTransport());
        sfemTransportMonitor = new SFEM_transport_monitor(sfem);
    }

    public void initSFEETransportController(modbus inMB, modbus outMB, SFEE inSFEE, SFEE outSFEE, SFEI inSFEI, SFEI outSFEI) {
        try {
            // Create new SFEE_tranport controller instance
            sfeeTransportController = new cSFEE_transport(sfem.getSfeeTransport());

            // init that instance
            sfeeTransportController.cSFEE_transport_init(inSFEE, outSFEE, inMB, outMB, inSFEI, outSFEI);

            sfeeTransportController.initOperationMode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Pair<Pair<String, String>,Pair<String, String>> getPrevNext_SFEE_SFEI_names() {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
