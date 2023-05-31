package viewers.runtimeUI;

import models.base.SFEI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ItemsPanel extends JPanel {

    private final Color[] colors = {Color.BLACK, Color.green.darker().darker()};
    private final ArrayList<MyRectangle> rectangles = new ArrayList<>();

    private final String[] itemsTitles;

    public ItemsPanel(String[] itemsTitles) {
        this.itemsTitles = itemsTitles;
        this.setLayout(new FlowLayout());
        repaint();
    }

    public void turnOn(int index) {
        rectangles.get(index).setColor(colors[1]);
        super.repaint();
    }

    public void turnOff(int index) {
        rectangles.get(index).setColor(colors[0]);
        super.repaint();
    }

    public void updateNparts(int n, int index) {
        rectangles.get(index).setText(n);
        super.repaint();
    }

    private final int init_xPos = 10;
    private final int init_yPos = 10;
    private int width = 100;
    private int height = 50;

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        int x = init_xPos, y = init_yPos;

        if (rectangles.size() == 0) {
            // Draw rectangles
            for (int i = 0; i < itemsTitles.length; i++) {
                MyRectangle rectangle = new MyRectangle(x, y, width, height, colors[0], itemsTitles[i]);
                rectangles.add(rectangle);
                x += width + init_xPos;
                if (i % 3 == 0 && i > 0) {
                    x = init_xPos;
                    y += height + init_xPos;
                }
            }
        }

        for (MyRectangle rectangle : rectangles) {
            drawMyRectangle(graphics, rectangle);

        }
//        drawSeparators(graphics, itemsTitles.length);

    }

    private void drawMyRectangle(Graphics graphics, MyRectangle rectangle) {
        Color oldColor = graphics.getColor();

        if (!rectangle.getColor().equals(Color.BLACK)) {
            Stroke stroke = new BasicStroke(3f);
            Graphics2D g2D = (Graphics2D) graphics;
            g2D.setStroke(stroke);
            graphics.setColor(rectangle.getColor());
            graphics.drawRect(rectangle.getxPos(), rectangle.getyPos(), rectangle.getWidth(), rectangle.getHeight());
            g2D.setStroke(new BasicStroke());
        } else {
            graphics.setColor(rectangle.getColor());
            graphics.drawRect(rectangle.getxPos(), rectangle.getyPos(), rectangle.getWidth(), rectangle.getHeight());
        }
        graphics.setColor(oldColor);

        drawCenteredString(
                graphics,
                rectangle.getTitle(),
                new Rectangle(rectangle.getxPos(), rectangle.getyPos() - rectangle.getHeight() / 2 + 10, rectangle.getWidth(), rectangle.getHeight()),
                graphics.getFont());

        drawCenteredString(
                graphics,
                rectangle.getText(),
                new Rectangle(rectangle.getxPos(), rectangle.getyPos() - rectangle.getHeight() / 2 + 30, rectangle.getWidth(), rectangle.getHeight()),
                graphics.getFont());

    }

    private void drawSeparators(Graphics graphics, int nRect) {
        float[] dashingPattern2 = {10f, 4f};
        Stroke stroke2 = new BasicStroke(4f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1.0f, dashingPattern2, 0.0f);
        Graphics2D g2D = (Graphics2D) graphics;
        g2D.setStroke(stroke2);

        g2D.drawLine(init_xPos, init_yPos + (nRect / 3 + 1) * height, init_xPos + 4 * 125, init_yPos + (nRect / 3 + 1) * height);

        g2D.setStroke(new BasicStroke());
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
