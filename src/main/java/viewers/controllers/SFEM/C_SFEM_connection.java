package viewers.controllers.SFEM;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import viewers.controllers.C_ShopFloor;
import viewers.controllers.SFEE.C_SFEEs;
import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;
import viewers.drawables.MyRectangle;
import viewers.drawables.SelectionHandler;
import viewers.drawables.connectionLines.boundLine;

import java.util.ArrayList;
import java.util.List;

public class C_SFEM_connection {

    @FXML
    private ScrollPane build_pane;
    @FXML
    private ListView<String> SFEMs_list;
    @FXML
    private Pane sfemTransport_pane;
    @FXML
    private Button drawConnection;
    @FXML
    private Tooltip connectBtn;

    private final BorderPane canvasWrapper = new BorderPane();

    private final ArrayList<Integer> drawnElements = new ArrayList<>();

    private List<Node> clickedNodes = new ArrayList<>();

    public void initialize() {

        ArrayList<String> list = new ArrayList<>();
        for (C_SFEM_production cSfem : C_ShopFloor.getInstance().getcSfemProductions()) {
            list.add(cSfem.getSfemName());
        }
        SFEMs_list.getItems().clear();
        SFEMs_list.getItems().addAll(list);


        canvasWrapper.setPrefSize(build_pane.getPrefWidth(), build_pane.getPrefHeight());
        canvasWrapper.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        canvasWrapper.setStyle("-fx-background-color: darkgray; -fx-border-color: black; -fx-border-width: 2px;");

        SelectionHandler selectionHandler = new SelectionHandler(canvasWrapper);
        canvasWrapper.addEventHandler(MouseEvent.MOUSE_PRESSED, selectionHandler.getMousePressedEventHandler());
        canvasWrapper.addEventHandler(MouseEvent.MOUSE_DRAGGED, selectionHandler.getMouseDraggedEventHandler());
        canvasWrapper.addEventHandler(MouseEvent.MOUSE_RELEASED, selectionHandler.getMouseReleasedEventHandler());

        canvasWrapper.setOnMouseClicked(event -> {
            Node target = ((Node) event.getTarget()).getParent();
            if (event.isControlDown()) {
/*            if (clickedNodes.size() == 2)
                clickedNodes.remove(0);*/
                if (!clickedNodes.contains(target) && target.getId() != null) {
                    System.out.println("Adding node: " + target.getId());
                    clickedNodes.add(target);
                }
            }
            if (clickedNodes.size() == 2) {
                System.out.println("Create connection with " + clickedNodes.get(0).getId() + " " + clickedNodes.get(1).getId());
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEM/SFEM_transport.fxml"));
                    C_SFEM_transport cSfemTransport = new C_SFEM_transport("SS_Metal", clickedNodes.get(0).getId(), clickedNodes.get(1).getId());
                    C_ShopFloor.getInstance().getcSfemTransports().add(C_ShopFloor.getInstance().getcSfemTransports().size(), cSfemTransport);
                    loader.setController(C_ShopFloor.getInstance().getcSfemTransports().get(C_ShopFloor.getInstance().getcSfemTransports().size() - 1));
                    AnchorPane pane = loader.load();
                    sfemTransport_pane.getChildren().setAll(pane);

                    C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(cSfemTransport);
//                    clickedNodes.clear();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        SFEMs_list.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                System.out.println(SFEMs_list.getSelectionModel().getSelectedItem());
                if (!drawnElements.contains(SFEMs_list.getSelectionModel().getSelectedIndices().get(0))) {
                    drawSFEM_elements(SFEMs_list.getSelectionModel().getSelectedIndices());
                    drawnElements.add(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));
                }
            }
        });

        drawConnection.setOnMouseClicked(this::drawConnectionLine);

    }

    private double Rwidth = 100.0;
    private double Rheight = 50.0;

    private double xPos = 60.0;
    private double yPos = 35.0;


    private void drawSFEM_elements(ObservableList<Integer> selectedIndices) {


        for (Integer selectedIndex : selectedIndices) {

            C_SFEM_production cSfem = C_ShopFloor.getInstance().getcSfemProductions().get(selectedIndex);

            for (C_SFEEs cSfees : cSfem.getSfeesControllers()) {

                for (C_SFEI_conveyor cSfeiConveyor : cSfees.getItems().getSfeisController().getSfeiConveyors()) {
                    MyRectangle rectangle = new MyRectangle(cSfeiConveyor.getSfeiConveyor().getName(), Rwidth, Rheight);

                    rectangle.setTranslateX(xPos);
                    rectangle.setTranslateY(yPos);

                    canvasWrapper.getChildren().add(rectangle);
                    xPos += 15.0 + Rwidth;

                }
                for (C_SFEI_machine cSfeiMachine : cSfees.getItems().getSfeisController().getSfeiMachines()) {
                    MyRectangle rectangle = new MyRectangle(cSfeiMachine.getSfeiMachine().getName(), Rwidth / 2, Rheight);

                    rectangle.setTranslateX(xPos);
                    rectangle.setTranslateY(yPos);

                    canvasWrapper.getChildren().add(rectangle);
                    xPos += 15.0 + Rwidth / 2;

                }
                xPos = 60.0;
                yPos += 15.0 + Rheight;
            }
        }
        build_pane.setContent(canvasWrapper);

    }

    private void drawConnectionLine(MouseEvent event) {

        if (C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().validateMoveOn()) {

            DoubleProperty startX = new SimpleDoubleProperty(clickedNodes.get(0).getLayoutX());
            DoubleProperty startY = new SimpleDoubleProperty(clickedNodes.get(0).getLayoutY());
            DoubleProperty endX = new SimpleDoubleProperty(clickedNodes.get(1).getLayoutX());
            DoubleProperty endY = new SimpleDoubleProperty(clickedNodes.get(1).getLayoutY());

            Anchor start = new Anchor((MyRectangle) clickedNodes.get(0), startX, startY);
            Anchor end = new Anchor((MyRectangle) clickedNodes.get(1), endX, endY);

            Line line = new boundLine(startX, startY, endX, endY);
            line.setId(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getSfemTransportName());

            line.setCursor(Cursor.HAND);

            line.setOnMouseClicked(event1 -> {
                System.out.println(((Node) event1.getTarget()).getId());
            });

            canvasWrapper.getChildren().add(/*new Group(start, end, line)*/line);
//        canvasWrapper.getChildren().removeAll(clickedNodes);
            build_pane.setContent(canvasWrapper);

            sfemTransport_pane.getChildren().clear();
            clickedNodes.clear();
            Tooltip.uninstall(drawConnection, connectBtn);
        } else {
            connectBtn.setText(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getErrorMsg());
            Tooltip.install(drawConnection, connectBtn);
        }
    }

    class Anchor extends MyRectangle {
        Anchor(MyRectangle myRectangle, DoubleProperty x, DoubleProperty y) {
            super(myRectangle);
            x.bind(myRectangle.layoutXProperty());
            y.bind(myRectangle.layoutYProperty());
        }

    }


}
