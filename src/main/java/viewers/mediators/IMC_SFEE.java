package viewers.mediators;

import models.base.SFEE;
import viewers.controllers.SFEE.*;

public interface IMC_SFEE {

    void registerC_SFEE_body_properties(C_SFEE_properties controller);
    void registerC_SFEE_body_communication(C_SFEE_communication controller);
    void registerC_SFEE_body_items(C_SFEE_items controller);
    void registerC_SFEE_body_failure(C_SFEE_failure controller);
    void registerC_SFEE_body_finish(C_SFEE_finish controller);


}
