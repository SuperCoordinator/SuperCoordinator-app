package models.SFEx_particular;

import communication.modbus;
import models.base.SFEE;
import models.base.SFEM;
import models.sensor_actuator;
import org.apache.commons.math3.util.Pair;
import viewers.SFEE_transport;

public class SFEM_transport extends SFEM {

    private SFEE sfeeTransport;

    public SFEM_transport(String name) {
        super(name, SFEM_type.TRANSPORT);
    }

    public void setSfeeTransport(SFEE sfeeTransport) {
        this.sfeeTransport = sfeeTransport;
    }

    public SFEE getSfeeTransport() {
        return sfeeTransport;
    }
}
