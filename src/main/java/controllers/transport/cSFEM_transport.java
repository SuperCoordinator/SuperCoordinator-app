package controllers.transport;

import communication.modbus;
import models.base.SFE_role;
import models.sfe_x.SFEM_transport;
import models.base.SFEE;
import models.base.SFEI;
import org.apache.commons.math3.util.Pair;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class cSFEM_transport implements Runnable {

    @XmlElement
    private SFEM_transport sfem;
    @XmlElement
    private cSFEE_transport sfeeTransportController;

    private final viewers.SFEM_transport viewer = new viewers.SFEM_transport();

    public cSFEM_transport() {
    }

    public cSFEM_transport(SFEM_transport sfemTransport) {
        this.sfem = sfemTransport;
    }

    public SFEM_transport getSfem() {
        return sfem;
    }

    public void init_SFEE_transport(String SFEE_transport_name) {
        try {
            SFEE sfeeTransp = new SFEE(SFEE_transport_name,
                    SFEE.SFEE_environment.SIMULATION,
                    SFE_role.TRANSPORT,
                    SFEE.communicationOption.MODBUS);
            sfem.setSfeeTransport(sfeeTransp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init_after_XML_load() {
        sfeeTransportController.setSfee(sfem.getSfeeTransport());
        sfeeTransportController.setConfiguration(sfem.getTransport_configuration());
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
        } catch (Exception e) {
            // In child thread, it must print the Exception because the main thread do not catch Runtime Exception from the others
            e.printStackTrace();
        }

    }


}
