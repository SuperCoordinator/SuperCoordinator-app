package viewers.drawables;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class MyRectangle extends StackPane implements SelectableNode {

    private final Rectangle rectangle;
//    private final Group group;

    public MyRectangle(String id, double width, double height) {
        super();
        super.setId(id);
        Text text = new Text(id);
//        this.group = new Group(this, label);
        this.rectangle = new Rectangle(width, height, Color.GRAY);
        this.rectangle.setId(id);
        this.getChildren().addAll(rectangle, text);

    }

    @Override
    public boolean requestSelection(boolean select) {
        return true;
    }

    @Override
    public void notifySelection(boolean select) {
        if (select) {
//            this.setFill(Color.RED);
            this.rectangle.setFill(Color.LIGHTGRAY);
//            rectangle.setStyle("-fx-background-color: red;");
        } else {
            this.rectangle.setFill(Color.GRAY);

//            this.setFill(Color.GRAY);
//            rectangle.setStyle("-fx-background-color: gray;");
        }
    }

/*    public StackPane getRectangle() {
        return rectangle;
    }*/

/*    public Group getGroup() {
        return group;
    }*/
}