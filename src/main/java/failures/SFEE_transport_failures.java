package failures;

import communication.modbus;
import models.base.SFEE;
import monitor.transport.SFEE_transport_monitor;
import viewers.SFEE_transport;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

public class SFEE_transport_failures implements Externalizable {

    public static final long serialVersionUID = 1234L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(sfee);
        out.writeObject(stochasticType);
        out.writeObject(stochasticFormulas);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.sfee = (SFEE) in.readObject();
        this.stochasticType = (stochasticTime.timeOptions) in.readObject();
        this.stochasticFormulas = (String[]) in.readObject();

        this.state = SM.INIT;
        this.old_state = state;
    }

    private enum SM {
        INIT,
        PROCESS_STOCHASTIC,
        END
    }

    private SM state, old_state;
    private SFEE sfee;
    private stochasticTime stochasticTimeTask;
    private stochasticTime.timeOptions stochasticType;
    private String[] stochasticFormulas;

    public SFEE_transport_failures() {
    }

    public SFEE_transport_failures(SFEE sfee, stochasticTime.timeOptions stochasticType, String[] stochasticTime_f) {
        this.sfee = sfee;
        this.stochasticType = stochasticType;
        this.stochasticFormulas = stochasticTime_f;

        this.state = SM.INIT;
        this.old_state = state;
    }

    public void loop(ArrayList<List<Object>> sensorsState, ArrayList<List<Object>> actuatorsState) {

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
