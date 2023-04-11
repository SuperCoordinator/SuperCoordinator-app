package viewers.drawables;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;


public class SelectionHandler {
    private Clipboard clipboard;

    private Pair<Double, Double> sceneXY;
    private Pair<Double, Double> nodeXY;


    private EventHandler<MouseEvent> mousePressedEventHandler;

    private EventHandler<MouseEvent> mouseDraggedEventHandler;

    private EventHandler<MouseEvent> mouseReleasedEventHandler;


    public SelectionHandler(final Parent root) {
        this.clipboard = new Clipboard();
        this.mousePressedEventHandler = event -> {
            SelectionHandler.this.doOnMousePressed(root, event);
            event.consume();
        };
        this.mouseDraggedEventHandler = event -> {
            SelectionHandler.this.doOnMouseDraggedEvent(root, event);
            event.consume();
        };
        this.mouseReleasedEventHandler = event -> {
            SelectionHandler.this.doOnMouseReleasedEvent(root, event);
            event.consume();
        };

    }


    public EventHandler<MouseEvent> getMousePressedEventHandler() {
        return mousePressedEventHandler;
    }

    private void doOnMousePressed(Parent root, MouseEvent event) {
        // The target is the rectangle, but the root child is the StackPane
        Node target = ((Node) event.getTarget()).getParent();
        System.out.println(target.getId());
/*        if (target.equals(root))
            clipboard.unselectAll();*/
        if (root.getChildrenUnmodifiable().contains(target) && target instanceof SelectableNode) {
//            SelectableNode selectableTarget = (SelectableNode) ((StackPane) target).getChildren().get(0);
            SelectableNode selectableTarget = (SelectableNode) target;
            if (!clipboard.getSelectedItems().contains(selectableTarget))
                clipboard.unselectAll();
            clipboard.select(selectableTarget, true);
            if (event.isPrimaryButtonDown()) {
                sceneXY = new Pair<>(event.getSceneX(), event.getSceneY());
                nodeXY = new Pair<>(target.getLayoutX(), target.getLayoutY());

/*                System.out.println("SceneXY: " + sceneXY + " \nNodeXY: " + nodeXY);
                System.out.println();*/
            }
        } else
            clipboard.unselectAll();
    }

    public EventHandler<MouseEvent> getMouseDraggedEventHandler() {
        return mouseDraggedEventHandler;
    }

    private void doOnMouseDraggedEvent(Parent root, MouseEvent event) {
        // The target is the rectangle, but the root child is the StackPane
        Node target = ((Node) event.getTarget()).getParent();
/*        if (target.equals(root))
            clipboard.unselectAll();*/
        if (root.getChildrenUnmodifiable().contains(target) && target instanceof SelectableNode) {
            SelectableNode selectableTarget = (SelectableNode) target;
            if (!clipboard.getSelectedItems().contains(selectableTarget))
                clipboard.unselectAll();
            clipboard.select(selectableTarget, true);

            target.setTranslateX(event.getSceneX() - sceneXY.getFirst());
            target.setTranslateY(event.getSceneY() - sceneXY.getSecond());
/*            System.out.println("TranslateXY: " + (event.getSceneX() - sceneXY.getFirst()) + " " + (event.getSceneY() - sceneXY.getSecond()));
            System.out.println();*/

        }
    }

    public EventHandler<MouseEvent> getMouseReleasedEventHandler() {
        return mouseReleasedEventHandler;
    }

    private void doOnMouseReleasedEvent(Parent root, MouseEvent event) {
        // The target is the rectangle, but the root child is the StackPane
        Node target = ((Node) event.getTarget()).getParent();
/*        if (target.equals(root))
            clipboard.unselectAll();*/
        if (root.getChildrenUnmodifiable().contains(target) && target instanceof SelectableNode) {
            SelectableNode selectableTarget = (SelectableNode) target;
            if (!clipboard.getSelectedItems().contains(selectableTarget))
                clipboard.unselectAll();
            clipboard.select(selectableTarget, true);

            //commit changes to LayoutX and LayoutY
            target.setLayoutX(nodeXY.getFirst() + target.getTranslateX());
            target.setLayoutY(nodeXY.getSecond() + target.getTranslateY());
            //clear changes from TranslateX and TranslateY
            target.setTranslateX(0);
            target.setTranslateY(0);
/*
            System.out.println("LayoutXY: " + (event.getSceneX() - nodeXY.getFirst()) + " " + (event.getSceneY() - nodeXY.getSecond()));
            System.out.println();*/

        }
    }

    /**
     * This class is based on jfxtras-labs
     * <a href="https://github.com/JFXtras/jfxtras-labs/blob/8.0/src/main/java/jfxtras/labs/scene/control/window/Clipboard.java">Clipboard</a>
     * and
     * <a href="https://github.com/JFXtras/jfxtras-labs/blob/8.0/src/main/java/jfxtras/labs/util/WindowUtil.java">WindowUtil</a>
     */
    private class Clipboard {
        private final ObservableList<SelectableNode> selectedItems = FXCollections.observableArrayList();

        public ObservableList<SelectableNode> getSelectedItems() {
            return selectedItems;
        }

        public boolean select(SelectableNode n, boolean selected) {
            if (n.requestSelection(selected)) {
                if (selected) {
                    selectedItems.add(n);
                } else {
                    selectedItems.remove(n);
                }
                n.notifySelection(selected);
                return true;
            } else {
                return false;
            }
        }

        public void unselectAll() {
            List<SelectableNode> unselectList = new ArrayList<>(selectedItems);

            for (SelectableNode sN : unselectList) {
                select(sN, false);
            }
        }

    }
}