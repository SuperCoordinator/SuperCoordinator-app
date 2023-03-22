package failures;

import models.base.SFEE;

import java.util.*;

public class SFEE_transport_failures {

    private enum SM {
        INIT,
        PROCESS_STOCHASTIC,
        END
    }

    private SM state;
    private SM old_state;
    private final SFEE sfee;
    private stochasticTime stochasticTimeTask;
    private final stochasticTime.timeOptions stochasticType;
    private final String[] stochasticFormulas;


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
                    System.out.println("SFEE_transp_failures part " + sfee.getSFEIbyIndex(0).getPartsATM().first());
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


        // Depends on the piece at the emitter of SFEE
      /*  boolean newPiece = checkNewPiece();
        if (newPiece) {
            System.out.println("New Piece to transport");
//            int pickSFEI = pickSFEI(false);
            int pickSFEI = 0;

            // The part is in the initial SFEI, so it is needed to select the part and
            // associate with the correct SFEI to manipulate the time
            if (sfee.getSFEIbyIndex(0).getPartsATM().size() > 0) {

                stochasticTime stochasticTime = new stochasticTime(
                        sfee.getSFEIbyIndex(pickSFEI),
                        sfee.getSFEIbyIndex(0).getPartsATM().first(),
                        stochasticType,
                        stochasticFormulas,
                        0);
                stochasticTimeTask.add(stochasticTime);
            }

        }

        // Runs the tasks
        for (stochasticTime object : stochasticTimeTask) {
            object.loop(sensorsState, actuatorsState);
        }

        // Delete the completed tasks
        stochasticTimeTask.removeIf(object -> object.isConveyorFinished() || object.isMachineFinished() || object.isTransportFinished());
*/
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
