package viewers.runtimeUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class ElementPanel extends JPanel {

    private final String title;
    private final LightsPanel lightsPanel;
    private final ItemsPanel itemsPanel;

    public ElementPanel(String title, String[] itemsTitles) {
        this.title = title;

        int height = 200;
        if (itemsTitles.length / 4 > 0)
            height = height * ((itemsTitles.length / 4) + 1);
        this.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(30, 30, 30, 30), BorderFactory.createLineBorder(Color.BLACK)));
        this.setMaximumSize(new Dimension(this.getWidth(), height));

        this.setLayout(new GridLayout(1, 2));

        this.itemsPanel = new ItemsPanel(itemsTitles);
        this.itemsPanel.setBackground(Color.white);
        this.add(itemsPanel, BorderLayout.NORTH);

        this.lightsPanel = new LightsPanel();
        this.lightsPanel.setBackground(Color.WHITE);
        this.add(lightsPanel, BorderLayout.NORTH);

        repaint();
    }

    public LightsPanel getLightsPanel() {
        return lightsPanel;
    }

    public ItemsPanel getItemsPanel() {
        return itemsPanel;
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        drawCenteredString(graphics,
                title,
                new Rectangle(0, 0, this.getWidth(), 40),
                new Font("Dialog", Font.BOLD, 16));
    }

    public void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        Font oldFont = g.getFont();
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
        g.setFont(oldFont);
    }


}
