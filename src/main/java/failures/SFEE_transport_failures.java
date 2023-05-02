package failures;

import communication.modbus;
import models.base.SFEE;
import models.base.part;
import monitor.transport.SFEE_transport_monitor;
import org.apache.commons.lang3.SerializationUtils;
import viewers.SFEE_transport;

import javax.xml.bind.annotation.*;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SFEE_transport_failures {
    private enum SM {
        INIT,
        PROCESS_STOCHASTIC,
        END
    }

    private SM state, old_state;
    private SFEE sfee;

    private stochasticTime stochasticTimeTask;
    @XmlAttribute
    private stochasticTime.timeOptions stochasticType;
    @XmlElement
    private String[] stochasticFormulas;

    public SFEE_transport_failures() {
    }

    public SFEE_transport_failures(SFEE sfee, stochasticTime.timeOptions stochasticType, String[] stochasticTime_f) {
        this.sfee = sfee;
        this.stochasticType = stochasticType;
        this.stochasticFormulas = stochasticTime_f;
    }

    public void setSfee(SFEE sfee) {
        this.sfee = sfee;
    }

    public stochasticTime.timeOptions getStochasticType() {
        return stochasticType;
    }

    public String[] getStochasticFormulas() {
        return stochasticFormulas;
    }

    public boolean waitNewPart() {
        return state.equals(SM.INIT);
    }

    private boolean first_exe = true;

    public void loop(ArrayList<List<Object>> sensorsState, ArrayList<List<Object>> actuatorsState) {
        if (first_exe) {
            this.state = SM.INIT;
            this.old_state = state;
            first_exe = false;
        }

        switch (state) {
            case INIT -> {
                if (checkNewPiece())
                    state = SM.PROCESS_STOCHASTIC;
            }
            case PROCESS_STOCHASTIC -> {
                if (stochasticTimeTask.isTransportFinished())
                    state = SM.END;
            }
            case END -> {
                state = SM.INIT;
            }
        }
        switch (state) {
            case INIT -> {

            }
            case PROCESS_STOCHASTIC -> {
                if (old_state != state) {

                    stochasticTimeTask = new stochasticTime(
                            sfee.getSFEIbyIndex(0),
                            /*new db_part(sfee.getSFEIbyIndex(0).getPartsATM().first()),*/
                            sfee.getSFEIbyIndex(0).getPartsATM().first(),
                            stochasticType,
                            stochasticFormulas,
                            0);
                }

                stochasticTimeTask.loop(sensorsState, actuatorsState);
            }
            case END -> {
                stochasticTimeTask = null;
            }
        }

        if (old_state != state)
            System.out.println(state);

        old_state = state;

    }

    private int oldPartID = -1;

    private boolean checkNewPiece() {
        int currID = oldPartID;
/*        System.out.println("[" + SFEE_transport_failures.class + "]  partsATM size:" + sfee.getSFEIbyIndex(0).getPartsATM().size());
        sfee.getSFEIbyIndex(0).getPartsATM().forEach(System.out::println);*/
        if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {
            currID = sfee.getSFEIbyIndex(0).getPartsATM().first().getId();
        }
        if (currID != oldPartID) {


            oldPartID = currID;
            return true;
        }
        return false;
    }

}
