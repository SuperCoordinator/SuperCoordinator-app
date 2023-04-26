package viewers.mediators;

import viewers.controllers.SFEE.production.*;

public class CM_SFEE implements IMC_SFEE {

    private C_SFEE_properties properties;
    private C_SFEE_communication communication;
    private C_SFEE_items items;
    private C_SFEE_failure failure;
    private C_SFEE_finish finish;

    @Override
    public void registerC_SFEE_body_properties(C_SFEE_properties controller) {
        if (properties == null)
            this.properties = controller;
    }

    @Override
    public void registerC_SFEE_body_communication(C_SFEE_communication controller) {
        if (communication == null)
            this.communication = controller;
    }

    @Override
    public void registerC_SFEE_body_items(C_SFEE_items controller) {
        if (items == null)
            this.items = controller;
    }

    @Override
    public void registerC_SFEE_body_failure(C_SFEE_failure controller) {
        if (failure == null)
            this.failure = controller;
    }

    @Override
    public void registerC_SFEE_body_finish(C_SFEE_finish controller) {
        if (finish == null)
            this.finish = controller;
    }

    public C_SFEE_properties getProperties() {
        return properties;
    }

    public C_SFEE_communication getCommunication() {
        return communication;
    }

    public C_SFEE_items getItems() {
        return items;
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

/*    public static CM_SFEE getInstance() {
        return CM_SFEEHolder.INSTANCE;
    }

    private static class CM_SFEEHolder {
        private static final CM_SFEE INSTANCE = new CM_SFEE();
    }*/

}
