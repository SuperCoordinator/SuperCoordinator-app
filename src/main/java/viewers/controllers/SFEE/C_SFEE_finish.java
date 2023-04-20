package viewers.controllers.SFEE;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.base.SFEI;
import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;

import java.util.ArrayList;

public class C_SFEE_finish {

    private ArrayList<C_SFEI_conveyor> sfeiConveyors;
    private ArrayList<C_SFEI_machine> sfeiMachines;

    public C_SFEE_finish() {
    }

    public void setSfeiConveyors(ArrayList<C_SFEI_conveyor> sfeiConveyors) {
        this.sfeiConveyors = sfeiConveyors;
    }

    public void setSfeiMachines(ArrayList<C_SFEI_machine> sfeiMachines) {
        this.sfeiMachines = sfeiMachines;
    }

    @FXML
    private TableView<SFEI> sumTable;
    @FXML
    private TableColumn<SFEI, String> name_col;
    @FXML
    private TableColumn<SFEI, String> type_col;
    @FXML
    private TableColumn<SFEI, String> inSensor_col;
    @FXML
    private TableColumn<SFEI, String> outSensor_col;

    public void initialize() {

        name_col.setCellValueFactory(new PropertyValueFactory<>("name"));
        type_col.setCellValueFactory(new PropertyValueFactory<>("sfeiType"));
        inSensor_col.setCellValueFactory(inSensorName -> new SimpleStringProperty(inSensorName.getValue().getInSensor().getName()));
        outSensor_col.setCellValueFactory(outSensorName -> new SimpleStringProperty(outSensorName.getValue().getOutSensor().getName()));
        // Define cell height
        sumTable.setFixedCellSize(30.0);

        updateTable();
    }

    private void updateTable() {

        ArrayList<SFEI> sfeiArrayList = new ArrayList<>();

        for (C_SFEI_conveyor cSfeiConveyor : /*CM_SFEE.getInstance().getItems().getSfeisController().getSfeiConveyors()*/sfeiConveyors) {
            sfeiArrayList.add(cSfeiConveyor.getSfeiConveyor());
        }
        for (C_SFEI_machine cSfeiMachine : /*CM_SFEE.getInstance().getItems().getSfeisController().getSfeiMachines()*/sfeiMachines) {
            sfeiArrayList.add(cSfeiMachine.getSfeiMachine());
        }
        ObservableList<SFEI> list_in = FXCollections.observableArrayList(sfeiArrayList);
        sumTable.setItems(list_in);
    }


    private String errorMsg;

    public boolean validateMoveOn(ActionEvent event) {

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        for (Node node : stage.getScene().getRoot().getChildrenUnmodifiable()) {
            TextField title = (TextField) node.lookup("#SFEE_name");
            if (title != null) {
                if (title.getText().isBlank()) {
                    errorMsg = "Please give a name to the SFEE";
                    return false;
                }
            }
        }
        return true;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
