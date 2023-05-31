package viewers.runtimeUI;

import models.base.SFEI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private ArrayList<ElementPanel> elementPanels = new ArrayList<>();

    public MainFrame(String[] titles, List<String[]> itemsTitles) {
        super("SC Runtime");
        try {

            this.setLayout(new GridLayout(titles.length, 1));

            if (titles.length != itemsTitles.size())
                throw new RuntimeException("Titles Length do not match with number of Items");

            for (int i = 0; i < titles.length; i++) {
                ElementPanel elementPanel = new ElementPanel(titles[i], itemsTitles.get(i));

                this.add(elementPanel);
                elementPanels.add(elementPanel);
            }

        } catch (HeadlessException e) {
            e.printStackTrace();
        }

//        Timer timer = new Timer(2000, e -> {
//            switch (cnt) {
//                case 0 -> panels.forEach(panel -> panel.turnOn(0));
//                case 1 -> panels.forEach(panel -> panel.turnOn(3));
//                case 2 -> panels.forEach(panel -> panel.turnOn(2));
//                case 3 -> panels.forEach(panel -> panel.turnOn(1));
//            }
//            cnt++;
//            if (cnt == 4)
//                cnt = 0;
//        });
//        Timer timer2 = new Timer(3000, e -> {
//            switch (cnt) {
//                case 0 -> panels.forEach(panel -> panel.turnOff(0));
//                case 1 -> panels.forEach(panel -> panel.turnOff(3));
//                case 2 -> panels.forEach(panel -> panel.turnOff(2));
//                case 3 -> panels.forEach(panel -> panel.turnOff(1));
//            }
//            cnt++;
//            if (cnt == 4)
//                cnt = 0;
//        });
//
////        timer.setRepeats(false);
//        timer.start();
//        timer2.start();
    }

    public ArrayList<ElementPanel> getElementPanels() {
        return elementPanels;
    }
}