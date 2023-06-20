package viewers.runtimeUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class LightsPanel extends JPanel {
    private final Color[] colors = {Color.BLACK, Color.RED};

    private final ArrayList<MyCircle> circles = new ArrayList<>();

    public LightsPanel() {
        this.setLayout(new FlowLayout());
        repaint();
    }


    public void turnOn(int index) {
        circles.get(index).setColor(colors[1]);
        super.repaint();
    }

    public void turnOff(int index) {
        circles.get(index).setColor(colors[0]);
        super.repaint();
    }

    public void desactive(int index) {
        circles.get(index).setColor(Color.gray);
        super.repaint();
    }


    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        int init_xPos = 10, init_yPos = 10, radius = 50;
//        graphics.clearRect(init_xPos, init_yPos, 30, 50);
        if (circles.size() == 0) {
            // Draw Circles
            for (int i = 0; i < 4; i++) {
                MyCircle circle = new MyCircle(init_xPos, init_yPos, radius, colors[0]);

                circles.add(circle);
                init_xPos += 100;
            }
        }
        String[] legends = new String[]{"BDwR", "P_Faulty", "P_More", "P_Less"};
        for (int i = 0; i < circles.size(); i++) {
            MyCircle circle = circles.get(i);

            graphics.setColor(circle.getColor());
            graphics.fillOval(circle.getxPos(), circle.getyPos(), circle.getRadius(), circle.getRadius());

            drawCenteredString(
                    graphics,
                    legends[i],
                    new Rectangle(circle.getxPos(), circle.getyPos() + circle.getRadius() + 5, circle.getRadius(), 10),
                    graphics.getFont());
        }

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

    public void turnOffAll() {
        circles.forEach(circle -> {
            if (!circle.getColor().equals(Color.gray))
                circle.setColor(colors[0]);
        });
        super.repaint();
    }
}
