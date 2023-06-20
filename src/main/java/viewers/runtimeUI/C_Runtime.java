package viewers.runtimeUI;

import controllers.production.cSFEE_production;
import models.base.SFEI;

import javax.swing.*;
import java.util.ArrayList;

import java.util.List;

public class C_Runtime implements Runnable {

    private MainFrame frame;
    private final ArrayList<cSFEE_production> sfees;

    public C_Runtime(ArrayList<cSFEE_production> sfees) {
        this.sfees = sfees;
    }

    boolean firstRun = true;

    @Override
    public void run() {
        try {
            if (firstRun) {
                String[] titles = new String[sfees.size()];
                List<String[]> itemsTitles = new ArrayList<>();

                for (int i = 0; i < sfees.size(); i++) {
                    titles[i] = sfees.get(i).getSFEE_name();
                    String[] sfeisTitles = new String[sfees.get(i).getSFEE().getSFEIs().size()];

                    for (int j = 0; j < sfeisTitles.length; j++) {
                        sfeisTitles[j] = sfees.get(i).getSFEE().getSFEIs().get(j).getName();
                    }
                    itemsTitles.add(sfeisTitles);
                }

                frame = new MainFrame(titles, itemsTitles);
                frame.setSize(1000, sfees.size() * 200);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                Thread.sleep(1000);
                disableUndefinedFailures();

                firstRun = false;
            }
            monitorFailures();
            trackingParts();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void disableUndefinedFailures() {
        for (int i = 0; i < sfees.size(); i++) {
            boolean[] undefined;
            if (sfees.get(i).getSfeeFailures() == null)
                undefined = new boolean[]{true, true, true, true};
            else
                undefined = sfees.get(i).getSfeeFailures().getUndefinedFailures();
            for (int j = 0; j < undefined.length; j++) {
                if (undefined[j])
                    frame.getElementPanels().get(i).getLightsPanel().desactive(j);
            }
        }
    }

    private void monitorFailures() {
        for (int i = 0; i < sfees.size(); i++) {
            if (sfees.get(i).getSfeeFailures() == null)
                continue;
            switch (sfees.get(i).getSfeeFailures().getFailuresState()) {
                case STOCHASTIC -> // Turn OFF all Indicators
                        frame.getElementPanels().get(i).getLightsPanel().turnOffAll();
                case BREAKDOWN_WITH_REPAIR -> frame.getElementPanels().get(i).getLightsPanel().turnOn(0);
                case PRODUCE_FAULTY -> frame.getElementPanels().get(i).getLightsPanel().turnOn(1);
                case PRODUCE_MORE -> frame.getElementPanels().get(i).getLightsPanel().turnOn(2);
                case PRODUCE_LESS -> frame.getElementPanels().get(i).getLightsPanel().turnOn(3);
            }
        }
    }

    private void trackingParts() {
        for (int i = 0; i < sfees.size(); i++) {
            ArrayList<SFEI> sfeis = new ArrayList<>(sfees.get(i).getSFEE().getSFEIs().values());
            for (int j = 0; j < sfeis.size(); j++) {

                if (sfeis.get(j).getPartsATM().size() > 0)
                    frame.getElementPanels().get(i).getItemsPanel().turnOn(j);
                else
                    frame.getElementPanels().get(i).getItemsPanel().turnOff(j);

                frame.getElementPanels().get(i).getItemsPanel().updateNparts(sfeis.get(j).getnPartsMoved(), j);
            }
        }
    }


}
