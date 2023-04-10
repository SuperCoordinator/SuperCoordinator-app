package viewers.controllers.SFEM;

import viewers.mediators.CM_SFEM;

public class C_SFEM extends CM_SFEM {

    private String sfemName;

    public C_SFEM() {
    }

    public C_SFEM(String sfemName) {
        this.sfemName = sfemName;
    }

    public String getSfemName() {
        return sfemName;
    }
}
