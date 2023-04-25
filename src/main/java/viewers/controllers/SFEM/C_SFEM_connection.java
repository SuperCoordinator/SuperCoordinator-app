package viewers.controllers.SFEM;

import com.sun.javafx.scene.shape.ArcHelper;
import controllers.transport.cSFEM_transport;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import models.SFEx_particular.SFEM_transport;
import org.apache.commons.math3.util.Pair;
import utility.serialize.serializer;
import viewers.controllers.C_ShopFloor;
import viewers.controllers.SFEE.C_SFEE_transport;
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
    private Tooltip connectionBtnTooltip;

    @FXML
    private Button connectionBtn;
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
                try {
                    System.out.println("Create connection with " + clickedNodes.get(0).getId() + " " + clickedNodes.get(1).getId());

                    // Detect if it is a new connection or an existing one
                    boolean existing_conection = false;
                    for (C_SFEM_transport cSfemTransport : C_ShopFloor.getInstance().getcSfemTransports()) {
                        Pair<Pair<String, String>, Pair<String, String>> pair = cSfemTransport.getcSFEMTransport().getSfeeTransportController().prevNextSFEE();
                        if (pair.getFirst().getSecond().equals(clickedNodes.get(0).getId())) {
                            C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(cSfemTransport);
                            existing_conection = true;
                            break;
                        }
                        if (pair.getSecond().getSecond().equals(clickedNodes.get(0).getId())) {
                            C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(cSfemTransport);
                            existing_conection = true;
                            break;
                        }
                    }

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEM/SFEM_transport.fxml"));
                    C_SFEM_transport cSfemTransport;
                    if (!existing_conection) {
                        String sfemName_empty = "SFEM_transport name" + C_ShopFloor.getInstance().getcSfemTransports().size();
                        cSfemTransport = new C_SFEM_transport(sfemName_empty, clickedNodes.get(0).getId(), clickedNodes.get(1).getId());
                        C_ShopFloor.getInstance().getcSfemTransports().add(C_ShopFloor.getInstance().getcSfemTransports().size(), cSfemTransport);
                        C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(cSfemTransport);
                        sfem_transport_name.setVisible(true);
                        sfem_transport_name.setText(sfemName_empty);
                    } else {
                        cSfemTransport = C_ShopFloor.getInstance().getCurrent_C_SFEMTransport();
                        sfem_transport_name.setVisible(true);
                        sfem_transport_name.setText(cSfemTransport.getSfemName());
                    }
                    loader.setController(cSfemTransport);
                    AnchorPane pane = loader.load();
                    sfem_transport_pane.getChildren().setAll(pane);
                    connectionBtn.setText("Connect");
                    connectionBtn.setVisible(true);
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


        drawSFEM_elements();
        // Draw all loaded connections
        drawExistingConnections();

        connectionBtn.setOnMouseClicked(event -> drawConnectionLine(false));

    }

    private void drawExistingConnections() {
        for (C_SFEM_transport cSfemTransport : C_ShopFloor.getInstance().getcSfemTransports()) {
            C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(cSfemTransport);

            String prevSFEI = cSfemTransport.getcSFEMTransport().getSfeeTransportController().getPrevSFEI_name();
            String nextSFEI = cSfemTransport.getcSFEMTransport().getSfeeTransportController().getNextSFEI_name();

            // search node by its name
            clickedNodes.add(0, searchNodeById(prevSFEI));
            clickedNodes.add(1, searchNodeById(nextSFEI));

            drawConnectionLine(true);
        }
    }

    private Node searchNodeById(String id) {
        Node retNode = null;
        for (Node node : canvasWrapper.getChildren()) {
            if (node instanceof MyRectangle) {
                if (node.getId().equals(id)) {
                    retNode = node;
                    break;
                }
            }
        }
        if (retNode == null)
            throw new RuntimeException("Node " + id + " not found in canvasWrapper!");
        return retNode;
    }

    private void drawConnectionLine(boolean setup) {
        if (setup || !C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().validateMoveOn()) {

            if (!setup) {
                if (!C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getcSfeeTransport().getInSFEI_name().getText().equals(clickedNodes.get(0).getId())) {
                    // Swap order
                    Node temp = clickedNodes.remove(0);
                    clickedNodes.add(1, temp);
                }
                create_cSFEM_transport();
            }

            MyRectangle inRectangle = (MyRectangle) clickedNodes.get(0);
            MyRectangle outRectangle = (MyRectangle) clickedNodes.get(1);

            DoubleProperty startX = new SimpleDoubleProperty(inRectangle.getLayoutX());
            DoubleProperty startY = new SimpleDoubleProperty(inRectangle.getLayoutY());
            DoubleProperty endX = new SimpleDoubleProperty(outRectangle.getLayoutX());
            DoubleProperty endY = new SimpleDoubleProperty(outRectangle.getLayoutY());

            Anchor start = new Anchor(inRectangle, startX, startY, true);
            Anchor end = new Anchor(outRectangle, endX, endY, false);

            Line line = new boundLine(startX, startY, endX, endY);
            if (setup) {
                line.setId(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getSfemName());
            } else {
                line.setId(sfem_transport_name.getText());
            }
            line.setCursor(Cursor.HAND);

            line.setOnMouseClicked(event -> {
                Node node = (Node) event.getTarget();
                System.out.println("Clicked node: " + node.getId());
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEM/SFEM_transport.fxml"));
                    // Search for an existing connection
                    for (C_SFEM_transport temp : C_ShopFloor.getInstance().getcSfemTransports()) {
                        if (node.getId().equals(temp.getSfemName())) {
                            System.out.println("EDIT MODE ON!");
                            C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(temp);
                            C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().setEditMode(true);
/*                            clickedNodes.clear();
                            clickedNodes.add(0, searchNodeById(temp.getcSfeeTransport().getInSFEI()));
                            clickedNodes.add(1, searchNodeById(temp.getcSfeeTransport().getOutSFEI()));*/
                            break;
                        }
                    }
                    System.out.println(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getSfemName());
                    loader.setController(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport());
                    AnchorPane pane = loader.load();
                    sfem_transport_pane.getChildren().setAll(pane);
                    sfem_transport_name.setVisible(true);
                    connectionBtn.setText("Update");
                    connectionBtn.setVisible(true);
                    sfem_transport_name.setText(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getSfemName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

            canvasWrapper.getChildren().add(line);
            build_pane.setContent(canvasWrapper);
            clickedNodes.clear();
            if (!setup) {
                Tooltip.uninstall(connectionBtn, connectionBtnTooltip);
                // Disable visible elements
                sfem_transport_name.setVisible(false);
                connectionBtn.setVisible(false);
                sfem_transport_pane.getChildren().clear();

                C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().setEditMode(false);
                C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().setSfemName(sfem_transport_name.getText());

            }
        } else {
            connectionBtnTooltip.setText(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getErrorMsg());
            Tooltip.install(connectionBtn, connectionBtnTooltip);
        }

    }

    private void create_cSFEM_transport() {

        C_SFEM_transport cSfemTransport = C_ShopFloor.getInstance().getCurrent_C_SFEMTransport();

        ArrayList<Object> data = new ArrayList<>();
        data.add(0, sfem_transport_name.getText());  // SFEM_transport name
        data.add(1, cSfemTransport.getcSfeeTransport().getSfee_transport_name().getText()); // SFEE_transport name

        ArrayList<Object> initController_data = new ArrayList<>();
        initController_data.add(0, "sfei_of_" + cSfemTransport.getcSfeeTransport().getSfee_transport_name().getText()); // SFEI_transport name
        initController_data.add(1, null);   // inSFEE modbus connection
        initController_data.add(2, null);   // outSFEE modbus connection
        initController_data.add(3, null);   // inSFEE  -> TO BE FOUND in serializer class
        initController_data.add(4, null);   // outSFEE -> TO BE FOUND in serializer class
        initController_data.add(5, cSfemTransport.getcSfeeTransport().getInSFEI());     // string value -> search in serializer class
        initController_data.add(6, cSfemTransport.getcSfeeTransport().getOutSFEI());    // string value -> search in serializer class

        initController_data.addAll(cSfemTransport.getcSfeeTransport().getC_SFEI_Transport().getSensAct());

        ArrayList<Object> init_OperationMode_data = new ArrayList<>(cSfemTransport.getcSfeeTransport().getFormulaSplitted());

        data.addAll(initController_data);
        data.addAll(init_OperationMode_data);

        serializer.getInstance().new_cSFEM_transport(data);
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


    private void drawSFEM_elements() {
        for (C_SFEM_production cSfem : C_ShopFloor.getInstance().getcSfemProductions()) {
            for (C_SFEEs cSfees : cSfem.getSfeesControllers()) {
                for (C_SFEI_conveyor cSfeiConveyor : cSfees.getItems().getSfeisController().getSfeiConveyors()) {
                    MyRectangle rectangle = new MyRectangle(cSfeiConveyor.getSfeiConveyor().getName(), Rwidth, Rheight);

                    rectangle.setLayoutX(xPos);
                    rectangle.setLayoutY(yPos);

                    canvasWrapper.getChildren().add(rectangle);
                    xPos += 15.0 + Rwidth;
                }
                for (C_SFEI_machine cSfeiMachine : cSfees.getItems().getSfeisController().getSfeiMachines()) {
                    MyRectangle rectangle = new MyRectangle(cSfeiMachine.getSfeiMachine().getName(), Rwidth / 2, Rheight);

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
