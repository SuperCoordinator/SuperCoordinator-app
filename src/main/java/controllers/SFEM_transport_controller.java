package controllers;

import communication.modbus;
import models.SFEx_particular.SFEM_transport;
import models.base.SFEE;

public class SFEM_transport_controller implements Runnable {

    private final SFEM_transport sfemTransport;
    private final modbus inMB;
    private final modbus outMB;

    public SFEM_transport_controller(SFEM_transport sfemTransport, SFEE inSFEE, SFEE outSFEE, modbus inMB, modbus outMB) {
        this.sfemTransport = sfemTransport;
        this.inMB = inMB;
        this.outMB = outMB;

        complete_SFEM_transport_initialization(inSFEE, outSFEE);
    }

    private void complete_SFEM_transport_initialization(SFEE inSFEE, SFEE outSFEE) {

    }

    @Override
    public void run() {

    }

}
