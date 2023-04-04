package viewers.controllers.SFEE;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import models.base.SFEI;
import models.sensor_actuator;
import viewers.controllers.SFEI.C_SFEI;
import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class C_SFEE_items implements Initializable {

    private C_SFEI sfeisController;
    private TreeMap<Integer, sensor_actuator> io = new TreeMap<>();

    public C_SFEE_items() {
    }

    public void setIo(TreeMap<Integer, sensor_actuator> io) {
        this.io = io;
        this.sfeisController = new C_SFEI(io);
    }

    public C_SFEI getSfeisController() {
        return sfeisController;
    }

    @FXML
    private Button add;
    @FXML
    private Tooltip addSFEI_error;
    @FXML
    private ImageView error_icon;

    @FXML
    private Pane newSFEI_Pane;

    @FXML
    private TreeView<String> treeView_SFEIs;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    void buttonPressed(ActionEvent event) {
        Button temp = (Button) event.getSource();

        try {
            switch (temp.getId()) {
                case "new_sfei" -> {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI.fxml"));
                    loader.setController(sfeisController);
                    AnchorPane pane = loader.load();
                    newSFEI_Pane.getChildren().setAll(pane);
                    add.setVisible(true);

                }
                case "delete_sfei" -> {

                }
                case "edit_sfei" -> {

                }
                case "add" -> {
                    if (!sfeisController.verifyFields()) {
                        sfeisController.addSFEI();
                        newSFEI_Pane.getChildren().clear();
                        updateTreeView(event);
                        Tooltip.uninstall(add, addSFEI_error);
                    } else {
                        error_icon.setVisible(true);
                        addSFEI_error.setText(sfeisController.getErrorMsg());
                        Tooltip.install(add, addSFEI_error);
                    }
                }
            }

//            setPane();

        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }


    private void setPane() {

       /* String name = String.valueOf(activePane);
        name = name.toLowerCase();
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE_" + name + ".fxml"));
            switch (activePane) {
                case PROPERTIES -> loader.setController(CM_SFEE.getInstance().getProperties());
                case COMMUNICATION -> loader.setController(CM_SFEE.getInstance().getCommunication());
                case ITEMS -> loader.setController(CM_SFEE.getInstance().getItems());
                case FAILURE -> loader.setController(CM_SFEE.getInstance().getFailure());
                case FINISH -> loader.setController(CM_SFEE.getInstance().getFinish());
            }
            Pane pane = loader.load();
            SFEE_body.getChildren().setAll(pane);

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private String errorMsg;

    public boolean validation_moveON() {
        System.out.println("Conveyors: " + sfeisController.getSfeiConveyors().size());
        System.out.println("Machines: " + sfeisController.getSfeiMachines().size());
        if (sfeisController.getSfeiConveyors().size() == 0 && sfeisController.getSfeiMachines().size() == 0) {
            errorMsg = "Add SFEIs to the SFEE !";
            return false;
        }
        return true;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @FXML
    public void updateTreeView(ActionEvent event) {

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        for (Node node : stage.getScene().getRoot().getChildrenUnmodifiable()) {
//            System.out.println(node.getId());
            TextField title = (TextField) node.lookup("#SFEE_name");
            if (title != null) {
//                System.out.println(title.getText());
                TreeItem<String> rootItem = new TreeItem<>(title.getText());

//                System.out.println("N of SFEI_conveyors: " + getSfeisController().getSfeiConveyors().size());
                for (C_SFEI_conveyor conveyor : getSfeisController().getSfeiConveyors()) {
//                    System.out.println("SFEI name: " + conveyor.getSfeiConveyor().getName());
                    TreeItem<String> branchItem = new TreeItem<>(conveyor.getSfeiConveyor().getName());
                    rootItem.getChildren().add(branchItem);
                }
                for (C_SFEI_machine conveyor : getSfeisController().getSfeiMachines()) {
//                    System.out.println("SFEI name: " + conveyor.getSfeiMachine().getName());
                    TreeItem<String> branchItem = new TreeItem<>(conveyor.getSfeiMachine().getName());
                    rootItem.getChildren().add(branchItem);
                }

                treeView_SFEIs.setRoot(rootItem);
            }
        }

    }


}
