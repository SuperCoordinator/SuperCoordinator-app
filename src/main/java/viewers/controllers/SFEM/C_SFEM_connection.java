package viewers.controllers.SFEM;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import viewers.controllers.C_ShopFloor;
import viewers.controllers.SFEE.C_SFEEs;
import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;
import viewers.drawables.MyCircle;
import viewers.drawables.MyRectangle;
import viewers.drawables.SelectionHandler;

import java.util.ArrayList;

public class C_SFEM_connection {

    @FXML
    private ScrollPane build_pane;
    @FXML
    private ListView<String> SFEMs_list;

    private final BorderPane canvasWrapper = new BorderPane();

    private final ArrayList<Integer> drawnElements = new ArrayList<>();

    public void initialize() {

        ArrayList<String> list = new ArrayList<>();
        for (C_SFEM cSfem : C_ShopFloor.getInstance().getCmSfems()) {
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


        SFEMs_list.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                System.out.println(SFEMs_list.getSelectionModel().getSelectedItem());
                if (!drawnElements.contains(SFEMs_list.getSelectionModel().getSelectedIndices().get(0))) {
                    drawSFEM_elements(SFEMs_list.getSelectionModel().getSelectedIndices());
                    drawnElements.add(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));
                }
            }
        });

    }

    private double Rwidth = 100.0;
    private double Rheight = 50.0;

    private double xPos = 60.0;
    private double yPos = 35.0;


    private void drawSFEM_elements(ObservableList<Integer> selectedIndices) {


        for (Integer selectedIndex : selectedIndices) {

            C_SFEM cSfem = C_ShopFloor.getInstance().getCmSfems().get(selectedIndex);

            for (C_SFEEs cSfees : cSfem.getSfeesControllers()) {

                for (C_SFEI_conveyor cSfeiConveyor : cSfees.getItems().getSfeisController().getSfeiConveyors()) {
                    MyRectangle rectangle = new MyRectangle(cSfeiConveyor.getSfeiConveyor().getName(), Rwidth, Rheight);

                    rectangle.setTranslateX(xPos);
                    rectangle.setTranslateY(yPos);

                    canvasWrapper.getChildren().add(rectangle);
                    xPos += 15.0 + Rwidth/2;

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

}
