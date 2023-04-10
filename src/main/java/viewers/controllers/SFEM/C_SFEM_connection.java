package viewers.controllers.SFEM;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import viewers.controllers.C_ShopFloor;
import viewers.controllers.SFEE.C_SFEEs;
import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;

import java.util.ArrayList;

public class C_SFEM_connection {

    @FXML
    private ScrollPane build_pane;
    @FXML
    private ListView<String> SFEMs_list;

    public void initialize() {

        ArrayList<String> list = new ArrayList<>();
        for (C_SFEM cSfem : C_ShopFloor.getInstance().getCmSfems()) {
            list.add(cSfem.getSfemName());
        }
        SFEMs_list.getItems().clear();
        SFEMs_list.getItems().addAll(list);

        SFEMs_list.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    System.out.println(SFEMs_list.getSelectionModel().getSelectedItem());
                    drawSFEM_elements(SFEMs_list.getSelectionModel().getSelectedIndices());
                }
            }
        });
    }

    private void drawSFEM_elements(ObservableList<Integer> selectedIndices) {

        double xPos = 10.0;
        double yPos = 10.0;

        double Rwidth = 50;
        double Rheight = 25;

        Pane canvasWrapper = new Pane();
        canvasWrapper.setPrefSize(build_pane.getPrefWidth(), build_pane.getPrefHeight());
        canvasWrapper.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        canvasWrapper.setStyle("-fx-background-color: lightgray; -fx-border-color: green; -fx-border-width: 2px;");
        for (Integer selectedIndex : selectedIndices) {

            C_SFEM cSfem = C_ShopFloor.getInstance().getCmSfems().get(selectedIndex);
            System.out.println("SFEM controllers size: " + cSfem.getSfeesControllers().size());
            for (C_SFEEs cSfees : cSfem.getSfeesControllers()) {

                ArrayList<Node> rectangles = new ArrayList<>();

                for (C_SFEI_conveyor cSfeiConveyor : cSfees.getItems().getSfeisController().getSfeiConveyors()) {
                    Rectangle sfei = new Rectangle(xPos, yPos, Rwidth, Rheight);
                    sfei.setFill(Color.rgb(255, (int) (255 - xPos), (int) (255 - yPos)));
                    rectangles.add(sfei);

                    xPos += 15.0 + Rwidth;

                }
                for (C_SFEI_machine cSfeiMachine : cSfees.getItems().getSfeisController().getSfeiMachines()) {
                    Rectangle sfei = new Rectangle(xPos, yPos, Rwidth, Rheight);
                    sfei.setFill(Color.rgb(255, (int) (255 - xPos), (int) (255 - yPos)));
                    rectangles.add(sfei);

                    xPos += 15.0;

                }
                Group group = new Group(rectangles);
                canvasWrapper.getChildren().add(group);

                xPos = 10.0;
                yPos += 15.0 + Rheight;
            }
        }
        build_pane.setContent(canvasWrapper);

    }

}
