package viewers.drawables;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.geometry.Bounds;

public class MyRectangle extends StackPane implements SelectableNode {

    private final Rectangle rectangle;

    public MyRectangle(String id, double width, double height) {
        super();
        super.setId(id);
        Text text = new Text(id);
        this.rectangle = new Rectangle(width, height, Color.GRAY);
        this.rectangle.setId(id);
        this.getChildren().addAll(rectangle, text);

    }
    public MyRectangle(MyRectangle myRectangle) {
        super();
        super.setId(myRectangle.getId());
        Text text = new Text(myRectangle.getId());
        this.rectangle = new Rectangle(myRectangle.getWidth(), getHeight(), Color.GRAY);
        this.rectangle.setId(myRectangle.getId());
        this.getChildren().addAll(rectangle, text);
    }
    @Override
    public boolean requestSelection(boolean select) {
        return true;
    }

    @Override
    public void notifySelection(boolean select) {
        if (select) {
            this.rectangle.setFill(Color.LIGHTGRAY);

        } else {
            this.rectangle.setFill(Color.GRAY);

        }
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}