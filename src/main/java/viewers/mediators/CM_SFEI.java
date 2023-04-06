package viewers.mediators;

import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;

import java.util.ArrayList;

public class CM_SFEI implements IMC_SFEI {

    private final ArrayList<C_SFEI_conveyor> sfeiConveyors = new ArrayList<>();
    private final ArrayList<C_SFEI_machine> sfeiMachines = new ArrayList<>();

    @Override
    public void registerC_SFEI_conveyor(C_SFEI_conveyor controller) {
        sfeiConveyors.add(sfeiConveyors.size(), controller);
    }

    @Override
    public void registerC_SFEI_machine(C_SFEI_machine controller) {
        sfeiMachines.add(sfeiMachines.size(), controller);
    }

    @Override
    public void updateC_SFEI_conveyor(int pos, C_SFEI_conveyor controller) {
        sfeiConveyors.set(pos, controller);
    }

    @Override
    public void updateC_SFEI_machine(int pos, C_SFEI_machine controller) {
        sfeiMachines.set(pos, controller);
    }

    public ArrayList<C_SFEI_conveyor> getSfeiConveyors() {
        return sfeiConveyors;
    }

    public ArrayList<C_SFEI_machine> getSfeiMachines() {
        return sfeiMachines;
    }

    /**
     * Everything below here is in support of Singleton pattern
     */
    public CM_SFEI() {
    }

    public static CM_SFEI getInstance() {
        return CM_SFEI.CM_SFEIHolder.INSTANCE;
    }

    private static class CM_SFEIHolder {
        private static final CM_SFEI INSTANCE = new CM_SFEI();
    }
}
