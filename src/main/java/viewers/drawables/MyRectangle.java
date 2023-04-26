package viewers.drawables;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.geometry.Bounds;
import javafx.scene.text.TextAlignment;

public class MyRectangle extends StackPane implements SelectableNode {

    public static int Rwidth = 100;
    public static int Rheight = 60;

    private final Rectangle rectangle;

    public MyRectangle(String id, double width, double height) {
        super();
        super.setId(id);
/*        Text text = new Text(id);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setStyle("-fx-font-size : 12 px;");
        text.setX(Rwidth);*/

        TextField text = new TextField(id);
        text.setEditable(false);
        text.setMouseTransparent(true);
        text.setFocusTraversable(false);
        text.setMinWidth(width);
        text.setMaxWidth(width);
        text.setAlignment(Pos.CENTER);
        text.setStyle("-fx-background-color : transparent;");
//        text.setCursor(Cursor.DEFAULT);

        this.rectangle = new Rectangle(width, height, Color.GRAY);
//        this.rectangle.setStyle("-fx-stroke: black; -fx-stroke-width: 2;");
        this.rectangle.setId(id);

        Tooltip tooltip = new Tooltip(id);
        Tooltip.install(rectangle, tooltip);
        Tooltip.install(text, tooltip);

        this.getChildren().addAll(rectangle, text);

//        this.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,BorderWidths.DEFAULT)));
    }

    public MyRectangle(MyRectangle myRectangle) {
        super();
        super.setId(myRectangle.getId());
//        Label text = new Label(myRectangle.getId());

        TextField text = new TextField(myRectangle.getId());
        text.setEditable(false);
        text.setMouseTransparent(true);
        text.setFocusTraversable(false);
        text.setMinWidth(myRectangle.getWidth());
        text.setMaxWidth(myRectangle.getWidth());
        text.setAlignment(Pos.CENTER);
        text.setStyle("-fx-background-color : transparent;");
//        text.setCursor(Cursor.DEFAULT);

        this.rectangle = new Rectangle(myRectangle.getWidth(), getHeight(), Color.GRAY);
//        this.rectangle.setStyle("-fx-stroke: black; -fx-stroke-width: 2;");
        this.rectangle.setId(myRectangle.getId());

        Tooltip tooltip = new Tooltip(myRectangle.getId());
        Tooltip.install(rectangle, tooltip);
        Tooltip.install(text, tooltip);

        this.getChildren().addAll(rectangle, text);

    }

    @Override
    public boolean requestSelection(boolean select) {
        return true;
    }

    @Override
    public void notifySelection(boolean select) {
        if (select) {
            this.rectangle.setStyle("-fx-stroke: black; -fx-stroke-width: 2;");
//            this.rectangle.setFill(Color.LIGHTGRAY);

        } else {
            this.rectangle.setStyle("-fx-stroke: black; -fx-stroke-width: 0;");
//            this.rectangle.setFill(Color.GRAY);

        }
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public static class Anchor extends MyRectangle {
        public Anchor(MyRectangle myRectangle, DoubleProperty x, DoubleProperty y, boolean input) {
            super(myRectangle);
            x.bind(myRectangle.layoutXProperty().add(input ? myRectangle.getRectangle().getWidth() / 2 : -myRectangle.getRectangle().getWidth() / 2));
            y.bind(myRectangle.layoutYProperty());
        }
    }

}