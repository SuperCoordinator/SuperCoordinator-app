package viewers.mediators;

import viewers.controllers.SFEE.C_SFEE_communication;
import viewers.controllers.SFEE.C_SFEE_failure;
import viewers.controllers.SFEE.C_SFEE_finish;
import viewers.controllers.SFEE.C_SFEE_properties;

public interface IMC_SFEE {


    void registerC_SFEE_body_properties(C_SFEE_properties controller);
    void registerC_SFEE_body_communication(C_SFEE_communication controller);
    void registerC_SFEE_body_failure(C_SFEE_failure controller);
    void registerC_SFEE_body_finish(C_SFEE_finish controller);

}
