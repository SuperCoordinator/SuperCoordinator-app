import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import viewers.UIcontrollers.drawables.connectionLines.boundLine;

/**
 * Example of dragging anchors around to manipulate a line.
 */
public class LineManipulator extends Application {
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        DoubleProperty startX = new SimpleDoubleProperty(100);
        DoubleProperty startY = new SimpleDoubleProperty(100);
        DoubleProperty endX = new SimpleDoubleProperty(300);
        DoubleProperty endY = new SimpleDoubleProperty(200);

        Anchor start = new Anchor(Color.PALEGREEN, startX, startY);
        Anchor end = new Anchor(Color.TOMATO, endX, endY);


        Line line = new boundLine(startX, startY, endX, endY);
        stage.setTitle("Line Manipulation Sample");
        stage.setScene(new Scene(new Group(line, start, end), 400, 400, Color.ALICEBLUE));
        stage.show();
    }

    // a draggable anchor displayed around a point.
    class Anchor extends Circle {
        Anchor(Color color, DoubleProperty x, DoubleProperty y) {
            super(x.get(), y.get(), 10);
            setFill(color.deriveColor(1, 1, 1, 0.5));
            setStroke(color);
            setStrokeWidth(2);
            setStrokeType(StrokeType.OUTSIDE);

            x.bind(centerXProperty());
            y.bind(centerYProperty());
            enableDrag();
        }

        // make a node movable by dragging it around with the mouse.
        private void enableDrag() {
            final Delta dragDelta = new Delta();
            setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    // record a delta distance for the drag and drop operation.
                    dragDelta.x = getCenterX() - mouseEvent.getX();
                    dragDelta.y = getCenterY() - mouseEvent.getY();
                    getScene().setCursor(Cursor.MOVE);
                }
            });
            setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    getScene().setCursor(Cursor.HAND);
                }
            });
            setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    double newX = mouseEvent.getX() + dragDelta.x;
                    if (newX > 0 && newX < getScene().getWidth()) {
                        setCenterX(newX);
                    }
                    double newY = mouseEvent.getY() + dragDelta.y;
                    if (newY > 0 && newY < getScene().getHeight()) {
                        setCenterY(newY);
                    }
                }
            });
            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (!mouseEvent.isPrimaryButtonDown()) {
                        getScene().setCursor(Cursor.HAND);
                    }
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (!mouseEvent.isPrimaryButtonDown()) {
                        getScene().setCursor(Cursor.DEFAULT);
                    }
                }
            });
        }

        // records relative x and y co-ordinates.
        private class Delta {
            double x, y;
        }
    }

/*    class Center {
        private ReadOnlyDoubleWrapper centerX = new ReadOnlyDoubleWrapper();
        private ReadOnlyDoubleWrapper centerY = new ReadOnlyDoubleWrapper();

        public Center(Node node) {
            calcCenter(node.getBoundsInParent());
            node.boundsInParentProperty().addListener(new ChangeListener<Bounds>() {
                @Override
                public void changed(
                        ObservableValue<? extends Bounds> observableValue,
                        Bounds oldBounds,
                        Bounds bounds
                ) {
                    calcCenter(bounds);
                }
            });
        }

        private void calcCenter(Bounds bounds) {
            centerX.set(bounds.getMinX() + bounds.getWidth() / 2);
            centerY.set(bounds.getMinY() + bounds.getHeight() / 2);
        }

        ReadOnlyDoubleProperty centerXProperty() {
            return centerX.getReadOnlyProperty();
        }

        ReadOnlyDoubleProperty centerYProperty() {
            return centerY.getReadOnlyProperty();
        }
    }*/

}
