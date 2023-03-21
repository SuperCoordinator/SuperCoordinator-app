package failures;

import failures.newVersion.breakdown2;
import failures.newVersion.breakdown_repair2;
import failures.newVersion.produce_faulty2;
import failures.newVersion.produce_more2;
import models.SFEx_particular.SFEI_conveyor;
import models.SFEx_particular.SFEI_machine;
import models.base.SFEE;
import models.base.SFEI;

import java.util.*;

public class SFEE_transport_failures {

    private final SFEE sfee;
    private final LinkedList<stochasticTime> stochasticTimeTasks;
    private final stochasticTime.timeOptions stochasticType;
    private final String[] stochasticFormulas;


    public SFEE_transport_failures(SFEE sfee, stochasticTime.timeOptions stochasticType, String[] stochasticTime_f) {
        this.sfee = sfee;
        this.stochasticTimeTasks = new LinkedList<>();
        this.stochasticType = stochasticType;
        this.stochasticFormulas = stochasticTime_f;

    }

    public void loop(ArrayList<List<Object>> sensorsState, ArrayList<List<Object>> actuatorsState) {
        // Depends on the piece at the emitter of SFEE
        boolean newPiece = checkNewPiece();
        if (newPiece) {
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
                stochasticTimeTasks.add(stochasticTime);
            }

        }

        // Runs the tasks
        for (stochasticTime object : stochasticTimeTasks) {
            object.loop(sensorsState, actuatorsState);
        }

        // Delete the completed tasks
        stochasticTimeTasks.removeIf(object -> object.isConveyorFinished() || object.isMachineFinished() || object.isTransportFinished());

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
