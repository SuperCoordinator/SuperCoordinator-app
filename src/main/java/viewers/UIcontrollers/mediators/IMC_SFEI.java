package viewers.UIcontrollers.mediators;

import viewers.UIcontrollers.SFEI.C_SFEI_conveyor;
import viewers.UIcontrollers.SFEI.C_SFEI_machine;

public interface IMC_SFEI {

    void registerC_SFEI_conveyor(C_SFEI_conveyor controller);
    void registerC_SFEI_machine(C_SFEI_machine controller);

    void updateC_SFEI_conveyor(int pos, C_SFEI_conveyor controller);
    void updateC_SFEI_machine(int pos, C_SFEI_machine controller);

}
