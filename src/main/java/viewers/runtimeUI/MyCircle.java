package viewers.runtimeUI;

import java.awt.*;

public class MyCircle {
    private final int xPos;
    private final int yPos;
    private final int radius;
    private Color color;

    public MyCircle(int xPos, int yPos, int radius, Color color) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.radius = radius;
        this.color = color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public int getRadius() {
        return radius;
    }

    public Color getColor() {
        return color;
    }


}
