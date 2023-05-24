package failures;

import communication.modbus;
import models.SFEx.SFEI_transport;
import models.SFEx.SFEM_transport;
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

    private SFEM_transport.configuration configuration;

    public SFEE_transport_failures() {
    }

    public SFEE_transport_failures(SFEE sfee, stochasticTime.timeOptions stochasticType, String[] stochasticTime_f, SFEM_transport.configuration configuration) {
        this.sfee = sfee;
        this.stochasticType = stochasticType;
        this.stochasticFormulas = stochasticTime_f;
        this.configuration = configuration;
    }

    public void setSfee(SFEE sfee) {
        this.sfee = sfee;
    }

    public void setConfiguration(SFEM_transport.configuration configuration) {
        this.configuration = configuration;
    }

    public stochasticTime.timeOptions getStochasticType() {
        return stochasticType;
    }

    public String[] getStochasticFormulas() {
        return stochasticFormulas;
    }

    private boolean firstRun = true;

    public void loop(ArrayList<List<Object>> sensorsState, ArrayList<List<Object>> actuatorsState, boolean isNextSFEE_free) {
        try {
            if (firstRun) {
                this.state = SM.INIT;
                this.old_state = state;
                firstRun = false;
            }

            switch (state) {
                case INIT -> {
                    if (isNextSFEE_free) {
                        if (checkNewPiece()) {
                            state = SM.PROCESS_STOCHASTIC;
                        }
                    }
                }
                case PROCESS_STOCHASTIC -> {
                    if (stochasticTimeTask != null)
                        if (stochasticTimeTask.isTransportFinished())
                            state = SM.END;
                }
                case END -> state = SM.INIT;
            }

            switch (state) {
                case INIT -> {

                }
                case PROCESS_STOCHASTIC -> {
                    if (old_state != state) {
                        for (part movingPart : sfee.getSFEIbyIndex(0).getPartsATM()) {
                            if (movingPart.getState().equals(part.status.WAIT_TRANSPORT)) {
                                movingPart.setState(part.status.IN_TRANSPORT);
                                stochasticTimeTask = new stochasticTime(
                                        sfee.getSFEIbyIndex(0),
                                        0,
                                        movingPart,
                                        stochasticType,
                                        stochasticFormulas,
                                        0);
                                stochasticTimeTask.setTransportConfiguration(configuration);
                                break;
                            }
                        }
                    }

                    if (stochasticTimeTask != null) {
                        stochasticTimeTask.loop(sensorsState, actuatorsState);

                    }
                }
                case END -> {
                    stochasticTimeTask = null;
                }
            }
            old_state = state;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int oldPartID = -1;

    private boolean checkNewPiece() {
        int currID = oldPartID;
//        System.out.println("[" + SFEE_transport_failures.class + "]  partsATM size:" + sfee.getSFEIbyIndex(0).getPartsATM().size());
//        sfee.getSFEIbyIndex(0).getPartsATM().forEach(System.out::println);
        if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {
            currID = sfee.getSFEIbyIndex(0).getPartsATM().first().getId();
        }
        if (currID != oldPartID) {
            if (currID >= 0)
                oldPartID = currID;
            return true;
        }
        return false;
    }

}
