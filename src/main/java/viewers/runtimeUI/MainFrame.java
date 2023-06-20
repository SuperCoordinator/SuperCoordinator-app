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

    }

    public ArrayList<ElementPanel> getElementPanels() {
        return elementPanels;
    }
}