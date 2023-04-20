package viewers.mediators;

import viewers.controllers.SFEE.C_SFEEs;

import java.util.ArrayList;

public class CM_SFEM_production implements IMC_SFEM_production {

    private final ArrayList<C_SFEEs> sfeesControllers = new ArrayList<>();

    @Override
    public void registerC_SFEEs(C_SFEEs controller) {
        sfeesControllers.add(sfeesControllers.size(), controller);
    }

    public ArrayList<C_SFEEs> getSfeesControllers() {
        return sfeesControllers;
    }

    /**
     * Everything below here is in support of Singleton pattern
     */
    public CM_SFEM_production() {
    }

    public static CM_SFEM_production getInstance() {
        return CM_SFEM_production.CM_SFEMHolder.INSTANCE;
    }

    private static class CM_SFEMHolder {
        private static final CM_SFEM_production INSTANCE = new CM_SFEM_production();
    }
}
