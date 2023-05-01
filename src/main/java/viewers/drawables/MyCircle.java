package viewers.drawables;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class MyCircle extends Circle implements SelectableNode {


    public MyCircle(String id, double centerX, double centerY, double radius) {
        super(centerX, centerY, radius);
        super.setId(id);
        super.setFill(Color.GRAY);
    }

    @Override
    public boolean requestSelection(boolean select) {
        return true;
    }

    @Override
    public void notifySelection(boolean select) {
        if (select)
            this.setFill(Color.rgb(255, 0, 0));
        else
            this.setFill(Color.GRAY);
    }

}