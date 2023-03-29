package controllers.transport;

import communication.modbus;
import models.SFEx_particular.SFEM_transport;
import models.base.SFEE;
import monitor.transport.SFEM_transport_monitor;
import org.apache.commons.math3.util.Pair;

import javax.xml.bind.annotation.*;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@XmlRootElement(name = "SFEM_trans_controller")
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(propOrder = {"SFEM", "SFEM_monitor"})
public class cSFEM_transport implements Runnable, Externalizable {

    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(sfem);
        out.writeObject(sfemTransportMonitor);
        out.writeObject(sfeeTransportController);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.sfem = (SFEM_transport) in.readObject();
        this.sfemTransportMonitor = (SFEM_transport_monitor) in.readObject();
        this.sfeeTransportController = (cSFEE_transport) in.readObject();

    }

    private SFEM_transport sfem;
    private SFEM_transport_monitor sfemTransportMonitor;

    private cSFEE_transport sfeeTransportController;

    private viewers.SFEM_transport viewer = new viewers.SFEM_transport();

    public cSFEM_transport() {
    }

    public cSFEM_transport(SFEM_transport sfemTransport) {
        this.sfem = sfemTransport;
    }

    @XmlElement(name = "SFEM")
    private SFEM_transport getSfem() {
        return sfem;
    }

    @XmlElement(name = "SFEM_monitor")
    private SFEM_transport_monitor getSfemTransportMonitor() {
        return sfemTransportMonitor;
    }

    @XmlElement(name = "SFEE_controller")
    private cSFEE_transport getSfeeTransportController() {
        return sfeeTransportController;
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
        this.sfemTransportMonitor = new SFEM_transport_monitor(sfem);

    }

    public void initSFEETransportController(modbus inMB, modbus outMB, SFEE inSFEE, SFEE outSFEE) {
        try {
            // Create new SFEE_tranport controller instance
            sfeeTransportController = new cSFEE_transport(sfem.getSfeeTransport());

            // init that instance
            sfeeTransportController.cSFEE_transport_init(inSFEE, outSFEE, inMB, outMB);

            sfeeTransportController.initOperationMode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Pair<String, String> getPrevNextSFEE_names() {
        return sfeeTransportController.prevNextSFEE();
    }

    public void setupSFEETransportController(modbus inMB, modbus outMB, SFEE inSFEE, SFEE outSFEE) {
        sfeeTransportController.cSFEE_transport_setup(inSFEE, outSFEE, inMB, outMB);
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
