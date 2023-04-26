package viewers.mediators;

import viewers.controllers.SFEE.transport.C_SFEE_transport;

public class CM_SFEM_transport implements IMC_SFEM_transport {

    private C_SFEE_transport cSfeeTransport;

    @Override
    public void registerC_SFEE_transport(C_SFEE_transport controller) {
        this.cSfeeTransport = controller;
    }

    public C_SFEE_transport getcSfeeTransport() {
        return cSfeeTransport;
    }
}
