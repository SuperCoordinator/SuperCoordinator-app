package viewers.mediators;

import viewers.controllers.SFEE.C_SFEEs;
import viewers.controllers.SFEM.C_SFEM;

import java.util.ArrayList;

public class CM_SFEM implements IMC_SFEM {

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
    public CM_SFEM() {
    }

    public static CM_SFEM getInstance() {
        return CM_SFEM.CM_SFEMHolder.INSTANCE;
    }

    private static class CM_SFEMHolder {
        private static final CM_SFEM INSTANCE = new CM_SFEM();
    }
}
