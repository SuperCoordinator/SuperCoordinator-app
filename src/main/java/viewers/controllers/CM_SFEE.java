package viewers.controllers;

public class CM_SFEE implements IMC_SFEE {

    private C_SFEE_properties properties;

    @Override
    public void registerC_SFEE_body_properties(C_SFEE_properties controller) {
        this.properties = controller;
    }


    /**
     * Everything below here is in support of Singleton pattern
     */
    private CM_SFEE() {
    }

    public static CM_SFEE getInstance() {
        return CM_SFEEHolder.INSTANCE;
    }

    private static class CM_SFEEHolder {
        private static final CM_SFEE INSTANCE = new CM_SFEE();
    }

}
