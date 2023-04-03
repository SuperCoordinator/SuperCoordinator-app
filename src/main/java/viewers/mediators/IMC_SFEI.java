package viewers.mediators;

import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;

public interface IMC_SFEI {

    void registerC_SFEI_conveyor(C_SFEI_conveyor controller);
    void registerC_SFEI_machine(C_SFEI_machine controller);

}