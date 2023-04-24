package viewers.controllers.SFEM;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
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
    private Pane sfem_transport_pane;

    @FXML
    private Tooltip connectBtn;

    @FXML
    private Button drawConnection;
    @FXML
    private TextField sfem_transport_name;

    private final BorderPane canvasWrapper = new BorderPane();
    private final ArrayList<Integer> drawnElements = new ArrayList<>();

    private final List<Node> clickedNodes = new ArrayList<>();


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
                if (!clickedNodes.contains(target) && target.getId() != null) {
                    System.out.println("Adding node: " + target.getId());
                    clickedNodes.add(target);
                }
            }
            if (clickedNodes.size() == 2) {
                System.out.println("Create connection with " + clickedNodes.get(0).getId() + " " + clickedNodes.get(1).getId());
                String sfemName_empty = "SFEM_transport name" + C_ShopFloor.getInstance().getcSfemTransports().size();

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE/SFEE_transport.fxml"));
                    C_SFEM_transport cSfemTransport = new C_SFEM_transport(sfemName_empty, clickedNodes.get(0).getId(), clickedNodes.get(1).getId());
                    C_ShopFloor.getInstance().getcSfemTransports().add(C_ShopFloor.getInstance().getcSfemTransports().size(), cSfemTransport);
//                    loader.setController(C_ShopFloor.getInstance().getcSfemTransports().get(C_ShopFloor.getInstance().getcSfemTransports().size() - 1));
                    loader.setController(cSfemTransport);
                    AnchorPane pane = loader.load();
                    sfem_transport_pane.getChildren().setAll(pane);

                    C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(cSfemTransport);
//                    clickedNodes.clear();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        SFEMs_list.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                // PAINT elements with other color !


/*                System.out.println(SFEMs_list.getSelectionModel().getSelectedItem());
                if (!drawnElements.contains(SFEMs_list.getSelectionModel().getSelectedIndices().get(0))) {
                    drawSFEM_elements(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));
                    drawnElements.add(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));
                }*/
            }
        });

        sfem_transport_name.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().setSfemName(sfem_transport_name.getText());
            }
        });

        drawSFEM_elements();
        // Draw all loaded connections
        drawExistingConnections();

        drawConnection.setOnMouseClicked(event -> drawConnectionLine(false));

    }

    private void drawExistingConnections() {
        for (C_SFEM_transport cSfemTransport : C_ShopFloor.getInstance().getcSfemTransports()) {
            C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(cSfemTransport);

            String prevSFEI = cSfemTransport.getcSFEMTransport().getSfeeTransportController().getPrevSFEI_name();
            String nextSFEI = cSfemTransport.getcSFEMTransport().getSfeeTransportController().getNextSFEI_name();

            // search node by its name
            for (Node node : canvasWrapper.getChildren()) {
                if (node instanceof MyRectangle) {
                    if (node.getId().equals(prevSFEI)) {
                        clickedNodes.add(0, node);
                        break;
                    }
                }
            }

            for (Node node : canvasWrapper.getChildren()) {
                if (node instanceof MyRectangle) {
                    if (node.getId().equals(nextSFEI)) {
                        clickedNodes.add(1, node);
                        break;
                    }
                }
            }
            drawConnectionLine(true);
        }
    }

    private void drawConnectionLine(boolean setup) {
        if (setup || C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().validateMoveOn()) {

            MyRectangle inRectangle = (MyRectangle) clickedNodes.get(0);
            MyRectangle outRectangle = (MyRectangle) clickedNodes.get(1);

            DoubleProperty startX = new SimpleDoubleProperty(inRectangle.getLayoutX());
            DoubleProperty startY = new SimpleDoubleProperty(inRectangle.getLayoutY());
            DoubleProperty endX = new SimpleDoubleProperty(outRectangle.getLayoutX());
            DoubleProperty endY = new SimpleDoubleProperty(outRectangle.getLayoutY());

            Anchor start = new Anchor(inRectangle, startX, startY, true);
            Anchor end = new Anchor(outRectangle, endX, endY, false);

            Line line = new boundLine(startX, startY, endX, endY);
            line.setId(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getSfemName());

            line.setCursor(Cursor.HAND);

            line.setOnMouseClicked(event -> {
                Node node = (Node) event.getTarget();
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE/SFEE_transport.fxml"));
                    for (C_SFEM_transport temp : C_ShopFloor.getInstance().getcSfemTransports()) {
                        if (node.getId().equals(temp.getSfemName())) {
                            C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(temp);
                            break;
                        }
                    }
                    loader.setController(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport());
                    AnchorPane pane = loader.load();
                    sfem_transport_pane.getChildren().setAll(pane);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

            canvasWrapper.getChildren().add(line);

            build_pane.setContent(canvasWrapper);
            clickedNodes.clear();
            Tooltip.uninstall(drawConnection, connectBtn);
            // Disable visible elements
            sfem_transport_name.setVisible(false);
            drawConnection.setVisible(false);

            // create SFEI_transport in C_SFEI_transport !!!

        } else {
            connectBtn.setText(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getErrorMsg());
            Tooltip.install(drawConnection, connectBtn);
        }

    }

    class Anchor extends MyRectangle {
        Anchor(MyRectangle myRectangle, DoubleProperty x, DoubleProperty y, boolean input) {
            super(myRectangle);
            x.bind(myRectangle.layoutXProperty().add(input ? myRectangle.getRectangle().getWidth() / 2 : -myRectangle.getRectangle().getWidth() / 2));
            y.bind(myRectangle.layoutYProperty());
        }

    }

    private double Rwidth = 100.0;
    private double Rheight = 50.0;

    private double xPos = 60.0;
    private double yPos = 35.0;


    private void drawSFEM_elements(/*ObservableList<Integer> *//*int selectedIndices*/) {


        for (C_SFEM_production cSfem : C_ShopFloor.getInstance().getcSfemProductions()) {
            for (C_SFEEs cSfees : cSfem.getSfeesControllers()) {
                for (C_SFEI_conveyor cSfeiConveyor : cSfees.getItems().getSfeisController().getSfeiConveyors()) {
                    MyRectangle rectangle = new MyRectangle(cSfeiConveyor.getSfeiConveyor().getName(), Rwidth, Rheight);

//                    rectangle.setTranslateX(xPos);
//                    rectangle.setTranslateY(yPos);
                    rectangle.setLayoutX(xPos);
                    rectangle.setLayoutY(yPos);

                    canvasWrapper.getChildren().add(rectangle);
                    xPos += 15.0 + Rwidth;
                }
                for (C_SFEI_machine cSfeiMachine : cSfees.getItems().getSfeisController().getSfeiMachines()) {
                    MyRectangle rectangle = new MyRectangle(cSfeiMachine.getSfeiMachine().getName(), Rwidth / 2, Rheight);

//                    rectangle.setTranslateX(xPos);
//                    rectangle.setTranslateY(yPos);
                    rectangle.setLayoutX(xPos);
                    rectangle.setLayoutY(yPos);

                    canvasWrapper.getChildren().add(rectangle);
                    xPos += 15.0 + Rwidth / 2;

                }
                xPos = 60.0;
                yPos += 15.0 + Rheight;
            }
        }
        build_pane.setContent(canvasWrapper);

    }

}
