package viewers.mediators;

import viewers.controllers.SFEE.C_SFEE_communication;
import viewers.controllers.SFEE.C_SFEE_failure;
import viewers.controllers.SFEE.C_SFEE_finish;
import viewers.controllers.SFEE.C_SFEE_properties;

public class CM_SFEE implements IMC_SFEE {

    private C_SFEE_properties properties;
    private C_SFEE_communication communication;
    private C_SFEE_failure failure;
    private C_SFEE_finish finish;

    @Override
    public void registerC_SFEE_body_properties(C_SFEE_properties controller) {
        this.properties = controller;
    }

    @Override
    public void registerC_SFEE_body_communication(C_SFEE_communication controller) {
        this.communication = controller;
    }

    @Override
    public void registerC_SFEE_body_failure(C_SFEE_failure controller) {
        this.failure = controller;
    }

    @Override
    public void registerC_SFEE_body_finish(C_SFEE_finish controller) {
        this.finish = controller;
    }


    public C_SFEE_properties getProperties() {
        return properties;
    }

    public C_SFEE_communication getCommunication() {
        return communication;
    }

    public C_SFEE_failure getFailure() {
        return failure;
    }

    public C_SFEE_finish getFinish() {
        return finish;
    }

    /**
     * Everything below here is in support of Singleton pattern
     */
    public CM_SFEE() {
    }

    public static CM_SFEE getInstance() {
        return CM_SFEEHolder.INSTANCE;
    }

    private static class CM_SFEEHolder {
        private static final CM_SFEE INSTANCE = new CM_SFEE();
    }

}
