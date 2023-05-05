package viewers.controllers.SFEM;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
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
import models.SFEx.SFEM_transport;
import models.base.SFEE;
import org.apache.commons.math3.util.Pair;
import utility.serialize.serializer;
import viewers.controllers.C_ShopFloor;
import viewers.controllers.SFEE.production.C_SFEEs;
import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;
import viewers.drawables.MyRectangle;
import viewers.drawables.SelectionHandler;
import viewers.drawables.connectionLines.boundLine;

import java.util.ArrayList;
import java.util.List;

import static viewers.drawables.MyRectangle.Rheight;
import static viewers.drawables.MyRectangle.Rwidth;

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

        canvasWrapper.addEventHandler(MouseEvent.MOUSE_CLICKED, getMouseClickedEventHandler());

        SFEMs_list.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                // PAINT elements with other color !

/*                System.out.println(SFEMs_list.getSelectionModel().getSelectedItem());
                if (!drawnElements.contains(SFEMs_list.getSelectionModel().getSelectedIndices().get(0))) {
                    drawSFEM_elements(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));
                    drawnElements.insert(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));
                }*/
            }
        });

        drawSFEM_elements();
        build_pane.setContent(canvasWrapper);
        // Draw all loaded connections
        drawExistingConnections();
        build_pane.setContent(canvasWrapper);
        connectionBtn.setOnMouseClicked(event -> drawConnectionLine(false));
    }


    private EventHandler<MouseEvent> getMouseClickedEventHandler() {

        return event -> {
            Node target = ((Node) event.getTarget()).getParent();
            if (event.isControlDown()) {
                if (!clickedNodes.contains(target) && target.getId() != null) {
//                    System.out.println("Adding node: " + target.getId());
                    clickedNodes.add(target);
                }
            }
            if (clickedNodes.size() == 2) {
                try {
//                    System.out.println("Create connection with " + clickedNodes.get(0).getId() + " " + clickedNodes.get(1).getId());

                    // Detect if it is a new connection or an existing one
                    boolean existing_conection = false;
                    for (C_SFEM_transport cSfemTransport : C_ShopFloor.getInstance().getcSfemTransports()) {
                        System.out.println(cSfemTransport.getSfemName());
                        Pair<Pair<String, String>, Pair<String, String>> pair = cSfemTransport.getcSFEMTransport().getSfeeTransportController().prevNextSFEE();
                        if (pair.getFirst().getSecond().equals(clickedNodes.get(0).getId())) {
                            C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(cSfemTransport);
                            existing_conection = true;
                            break;
                        }
                        // Not sure about this, because one SFEI theoretically can have 2 connections, 1 IN 1 OUT
                        if (pair.getSecond().getSecond().equals(clickedNodes.get(0).getId())) {
                            C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(cSfemTransport);
                            existing_conection = true;
                            break;
                        }
                    }

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEM/SFEM_transport.fxml"));

                    if (!existing_conection) {
                        String sfemName_empty = "SFEM_T#" + C_ShopFloor.getInstance().getcSfemTransports().size();
                        System.out.println("New conn with " + clickedNodes.get(0).getId() + " " + clickedNodes.get(1).getId());
                        C_SFEM_transport cSfemTransport = new C_SFEM_transport(sfemName_empty, clickedNodes.get(0).getId(), clickedNodes.get(1).getId());
                        C_ShopFloor.getInstance().getcSfemTransports().add(C_ShopFloor.getInstance().getcSfemTransports().size(), cSfemTransport);
                        C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(cSfemTransport);

                        sfem_transport_name.setText(sfemName_empty);
                        sfem_transport_name.setEditable(true);
                        connectionBtn.setText("Connect");
                    } else {
                        sfem_transport_name.setText(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getSfemName());
                        sfem_transport_name.setEditable(false);
                        connectionBtn.setText("Update");
                    }
                    loader.setController(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport());
                    AnchorPane pane = loader.load();
                    sfem_transport_pane.getChildren().setAll(pane);

                    sfem_transport_name.setVisible(true);
                    connectionBtn.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            event.consume();
        };

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

        if (setup) {
            drawLine(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getSfemName());
            clickedNodes.clear();
            return;
        }

        if (!C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().validateMoveOn()) {
            if (!C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getcSfeeTransport().getInSFEI_name().getText().equals(clickedNodes.get(0).getId())) {
                // Swap order
                Node temp = clickedNodes.remove(0);
                clickedNodes.add(1, temp);
            }
            create_cSFEM_transport();

            drawLine(sfem_transport_name.getText());
            clickedNodes.clear();

            Tooltip.uninstall(connectionBtn, connectionBtnTooltip);
            // Disable visible elements
            sfem_transport_name.setVisible(false);
            connectionBtn.setVisible(false);
            sfem_transport_pane.getChildren().clear();
            build_pane.setContent(canvasWrapper);
//                C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().setEditMode(false);
            C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().setSfemName(sfem_transport_name.getText());

        } else {
            connectionBtnTooltip.setText(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getErrorMsg());
            Tooltip.install(connectionBtn, connectionBtnTooltip);
        }

    }

    private void drawLine(String line_id) {

        MyRectangle inRectangle = (MyRectangle) clickedNodes.get(0);
        MyRectangle outRectangle = (MyRectangle) clickedNodes.get(1);

        DoubleProperty startX = new SimpleDoubleProperty(inRectangle.getLayoutX());
        DoubleProperty startY = new SimpleDoubleProperty(inRectangle.getLayoutY());
        DoubleProperty endX = new SimpleDoubleProperty(outRectangle.getLayoutX());
        DoubleProperty endY = new SimpleDoubleProperty(outRectangle.getLayoutY());

        MyRectangle.Anchor start = new MyRectangle.Anchor(inRectangle, startX, startY, true);
        MyRectangle.Anchor end = new MyRectangle.Anchor(outRectangle, endX, endY, false);

        Line line = new boundLine(startX, startY, endX, endY);
        line.setId(line_id);
        line.setCursor(Cursor.HAND);

        line.setOnMouseClicked(event -> {
            Node node = (Node) event.getTarget();
//                System.out.println("Clicked node: " + node.getId());
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEM/SFEM_transport.fxml"));
                // Search for an existing connection
                for (C_SFEM_transport temp : C_ShopFloor.getInstance().getcSfemTransports()) {
                    if (node.getId().equals(temp.getSfemName())) {
//                            System.out.println("EDIT MODE ON!");
//                            System.out.println("SFEI_Transport: " + temp.getcSfeeTransport().getC_SFEI_Transport().getSfeiTransport().getName());
                        C_ShopFloor.getInstance().setCurrent_C_SFEMTransport(temp);
//                            C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().setEditMode(true);
                        clickedNodes.clear();
                        clickedNodes.add(0, searchNodeById(temp.getcSfeeTransport().getInSFEI()));
                        clickedNodes.add(1, searchNodeById(temp.getcSfeeTransport().getOutSFEI()));
                        break;
                    }
                }
                loader.setController(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport());
                AnchorPane pane = loader.load();
                sfem_transport_pane.getChildren().setAll(pane);

//                sfem_transport_name.setText(C_ShopFloor.getInstance().getCurrent_C_SFEMTransport().getSfemName());
//                sfem_transport_name.setVisible(true);
//                sfem_transport_name.setEditable(false);

//                connectionBtn.setText("Update");
//                connectionBtn.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        canvasWrapper.getChildren().add(line);
    }

    private void create_cSFEM_transport() {

        C_SFEM_transport cSfemTransport = C_ShopFloor.getInstance().getCurrent_C_SFEMTransport();

        ArrayList<Object> data = new ArrayList<>();
        data.add(0, sfem_transport_name.getText());  // SFEM_transport name
        data.add(1, cSfemTransport.getcSfeeTransport().getSfee_transport_name().getText()); // SFEE_transport name

        ArrayList<Object> controllerData = new ArrayList<>();
        controllerData.add(0, "sfei_of_" + cSfemTransport.getcSfeeTransport().getSfee_transport_name().getText()); // SFEI_transport name
        controllerData.add(1, null);   // inSFEE modbus connection
        controllerData.add(2, null);   // outSFEE modbus connection
        controllerData.add(3, null);   // inSFEE  -> TO BE FOUND in serializer class
        controllerData.add(4, null);   // outSFEE -> TO BE FOUND in serializer class
        controllerData.add(5, cSfemTransport.getcSfeeTransport().getInSFEI());     // string value -> search in serializer class
        controllerData.add(6, cSfemTransport.getcSfeeTransport().getOutSFEI());    // string value -> search in serializer class

        controllerData.addAll(cSfemTransport.getcSfeeTransport().getC_SFEI_Transport().getSensAct());

        ArrayList<Object> operationModeData = new ArrayList<>(cSfemTransport.getcSfeeTransport().getFormulaSplit());

/*        System.out.println("Before update data: C_ShopFloor - " + C_ShopFloor.getInstance().getcSfemTransports().size()
                + " serializer - " + serializer.getInstance().getC_Transport().size());*/

        serializer.getInstance().new_cSFEM_transport(SFEM_transport.configuration.SFEI2SFEI, data, controllerData, operationModeData);
        // Update C_ShopFloor singleton with the new C_SFEM_transport
        C_ShopFloor.getInstance().updateData(serializer.getInstance().getC_Production(), serializer.getInstance().getC_Transport());

/*        System.out.println("After update data: C_ShopFloor - " + C_ShopFloor.getInstance().getcSfemTransports().size()
                + " serializer - " + serializer.getInstance().getC_Transport().size());*/
    }

    private double xPos = 10 + (double) Rwidth / 2;
    private double yPos = 10 + (double) Rheight / 2;

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
                    MyRectangle rectangle = new MyRectangle(cSfeiMachine.getSfeiMachine().getName(), Rheight, Rheight);

                    rectangle.setLayoutX(xPos);
                    rectangle.setLayoutY(yPos);

                    canvasWrapper.getChildren().add(rectangle);
                    xPos += 15.0 + Rheight;
                }
                xPos = 60.0;
                yPos += 15.0 + Rheight;
            }
        }
    }
}
