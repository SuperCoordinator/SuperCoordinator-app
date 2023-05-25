package failures;

import failures.formulas.gaussFormula;
import models.SFEx.SFEM_transport;
import models.base.SFEE;
import models.base.SFEI;
import models.base.part;
import utility.utils;


import javax.xml.bind.annotation.*;
import java.time.Duration;
import java.time.Instant;
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

    @XmlAttribute
    private stochasticTime.timeOptions stochasticType;
    @XmlElement
    private String[] stochasticFormulas;

    private stochasticTime stochasticTimeTask;
    private gaussFormula gaussFormula;
    private SFEM_transport.configuration configuration;

    public SFEE_transport_failures() {
    }

    public SFEE_transport_failures(SFEE sfee, stochasticTime.timeOptions stochasticType, String[] stochasticTime_f, SFEM_transport.configuration configuration) {
        this.sfee = sfee;
        this.stochasticType = stochasticType;
        this.stochasticFormulas = stochasticTime_f;
        this.configuration = configuration;

        if (this.stochasticType.equals(stochasticTime.timeOptions.GAUSSIAN))
            this.gaussFormula = new gaussFormula();
    }

    public void setSfee(SFEE sfee) {
        this.sfee = sfee;
    }

    public void setConfiguration(SFEM_transport.configuration configuration) {
        this.configuration = configuration;

        if (this.stochasticType.equals(stochasticTime.timeOptions.GAUSSIAN))
            this.gaussFormula = new gaussFormula();
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
                                        calculateDelay()
 /*                                       stochasticType,
                                        stochasticFormulas,
                                        0*/);
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

    private double calculateDelay() {

        // Calculate mean and dev
        // If different from previous value, new formula (of any type)
        try {
            SFEI sfei = sfee.getSFEIbyIndex(0);

            double m = utils.getInstance().getCustomCalculator().calcExpression(stochasticFormulas[0],
                    sfei.getnPiecesMoved(),
                    (double) Duration.between(sfei.getDayOfBirth(), Instant.now()).toMinutes(),
                    (double) Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toMinutes());

            double total_Time = m;

            if (stochasticType.equals(stochasticTime.timeOptions.GAUSSIAN)) {

                double dev = utils.getInstance().getCustomCalculator().calcExpression(stochasticFormulas[1],
                        sfei.getnPiecesMoved(),
                        (double) Duration.between(sfei.getDayOfBirth(), Instant.now()).toMinutes(),
                        (double) Duration.between(sfei.getDayOfLastMaintenance(), Instant.now()).toMinutes());

                // if mean and/or dev change value (due to dependency of variables like n,a,m)
                if (gaussFormula.getCurrentValue() == -1) {
                    // First execution
                    gaussFormula = new gaussFormula(m, dev, false);
                } else {
                    // Others executions
                    double mean = gaussFormula.getMean();
                    double deviation = gaussFormula.getDev();
                    if (!(mean - 1.0 < m && m < mean + 1.0) || !(deviation - 1.0 < dev && dev < deviation + 1.0)) {
                        // If is a new value for mean or deviation
                        gaussFormula = new gaussFormula(m, dev, false);
                    }
                }

                do {
                    total_Time = gaussFormula.getCurrentValue();
                    gaussFormula.setNextValue();
                } while (total_Time < 0);
            }

            if (total_Time < 0)
                return 0.0;
            // For the result in millis
            return total_Time * 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;


    }

}
