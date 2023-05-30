package viewers.runtimeUI;

import java.awt.*;

public class MyRectangle {
    private final int xPos, yPos;
    private final int width, height;
    private Color color;
    private String title, text;

    public MyRectangle(int xPos, int yPos, int width, int height, Color color, String title) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
        this.color = color;
        this.title = title;
        this.text = "#parts: 0";
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Color getColor() {
        return color;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public void setText(int n) {
        this.text = "#parts: ".concat(String.valueOf(n));
    }
}
