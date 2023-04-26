package viewers.controllers.SFEE.production;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import models.base.SFEE;
import models.sensor_actuator;
import viewers.controllers.SFEI.C_SFEIs;
import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;

import java.util.TreeMap;

public class C_SFEE_items {

    private C_SFEIs sfeisController;
    private TreeMap<Integer, sensor_actuator> io = new TreeMap<>();

    public C_SFEE_items() {
    }

    public void setIo(TreeMap<Integer, sensor_actuator> io, SFEE.SFEE_type sfeeType) {
        this.io = io;
        if (sfeisController == null)
            this.sfeisController = new C_SFEIs(io, sfeeType);
    }

    public C_SFEIs getSfeisController() {
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

    @FXML
    private Button new_sfei;
    @FXML
    private Button edit_sfei;
    @FXML
    private Button remove_sfei;
    @FXML
    private Button refresh_sfee;


    public void initialize() {
        updateTreeView();

        // Install Tooltips over buttons

        Tooltip.install(new_sfei, new Tooltip("Add new SFEI"));
        Tooltip.install(edit_sfei, new Tooltip("Edit selected SFEI"));
        Tooltip.install(remove_sfei, new Tooltip("Delete selected SFEI"));
        Tooltip.install(refresh_sfee, new Tooltip("Refresh List"));
    }

    @FXML
    void buttonPressed(ActionEvent event) {
        Button temp = (Button) event.getSource();
        sfeisController.deactivateEditMode();

        //Update SFEE name
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        for (Node node : stage.getScene().getRoot().getChildrenUnmodifiable()) {
            TextField title = (TextField) node.lookup("#SFEE_name");
            if (title != null) {
                sfeeName = title.getText();
            }
        }

        try {
            switch (temp.getId()) {
                case "new_sfei" -> {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI/SFEI.fxml"));
                    loader.setController(sfeisController);
                    AnchorPane pane = loader.load();
                    newSFEI_Pane.getChildren().setAll(pane);
                    add.setText("Add");
                    add.setVisible(true);

                }
                case "edit_sfei" -> {
                    if (treeView_SFEIs.getSelectionModel().getSelectedItem() != null) {
                        String sfeiName = treeView_SFEIs.getSelectionModel().getSelectedItem().getValue();
                        System.out.println(sfeiName);

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEI/SFEI.fxml"));
                        sfeisController.activeEditMode(sfeiName);
                        loader.setController(sfeisController);
                        AnchorPane pane = loader.load();
                        newSFEI_Pane.getChildren().setAll(pane);
                        add.setText("Update");
                        add.setVisible(true);
                    }
                }
                case "remove_sfei" -> {
                    if (treeView_SFEIs.getSelectionModel().getSelectedItem() != null) {
                        String sfeiName = treeView_SFEIs.getSelectionModel().getSelectedItem().getValue();
                        System.out.println(sfeiName);
                        sfeisController.removeSFEI(sfeiName);
                        updateTreeView();
                        add.setVisible(false);
                    } else {
                        error_icon.setVisible(true);
                        addSFEI_error.setText("Please select the SFEI to remove !");
                        Tooltip.install(add, addSFEI_error);
                    }

                }
                case "refresh_sfee" -> {
                    updateTreeView();
                    newSFEI_Pane.getChildren().clear();
                    add.setVisible(false);

                }
                case "add" -> {

                    switch (temp.getText()) {
                        case "Add" -> {
                            if (!sfeisController.verifyFields()) {
                                sfeisController.add_updateSFEI("");
                                updateTreeView();
                                Tooltip.uninstall(remove_sfei, addSFEI_error);
                                newSFEI_Pane.getChildren().clear();
                                add.setVisible(false);
                            } else {
                                error_icon.setVisible(true);
                                addSFEI_error.setText(sfeisController.getErrorMsg());
                                Tooltip.install(remove_sfei, addSFEI_error);
                            }
                        }
                        case "Update" -> {
                            String sfeiName = "";
                            if (treeView_SFEIs.getSelectionModel().getSelectedItem() != null) {
                                sfeiName = treeView_SFEIs.getSelectionModel().getSelectedItem().getValue();
                                System.out.println(sfeiName);
                            }

                            if (!sfeisController.verifyFields()) {
                                sfeisController.add_updateSFEI(sfeiName);
                                updateTreeView();
                                Tooltip.uninstall(remove_sfei, addSFEI_error);
                                newSFEI_Pane.getChildren().clear();
                                add.setVisible(false);
                            } else {
                                error_icon.setVisible(true);
                                addSFEI_error.setText(sfeisController.getErrorMsg());
                                Tooltip.install(remove_sfei, addSFEI_error);
                            }
                        }
                    }


                }
            }


        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    private String errorMsg;

    public boolean validateMoveOn() {
//        System.out.println("Conveyors: " + sfeisController.getSfeiConveyors().size());
//        System.out.println("Machines: " + sfeisController.getSfeiMachines().size());
        if (sfeisController.getSfeiConveyors().size() == 0 && sfeisController.getSfeiMachines().size() == 0) {
            errorMsg = "Add SFEIs to the SFEE !";
            return false;
        }
        return true;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    private String sfeeName = " ";

    private void updateTreeView() {

        TreeItem<String> rootItem = new TreeItem<>(sfeeName);
//        System.out.println("#Conv: " + sfeisController.getSfeiConveyors().size());
        for (C_SFEI_conveyor conveyor : getSfeisController().getSfeiConveyors()) {
//            System.out.println("SFEI name: " + conveyor.getSfeiConveyor().getName());
            TreeItem<String> branchItem = new TreeItem<>(conveyor.getSfeiConveyor().getName());
            rootItem.getChildren().add(branchItem);
        }
//        System.out.println("#Mach: " + sfeisController.getSfeiMachines().size());
        for (C_SFEI_machine conveyor : getSfeisController().getSfeiMachines()) {
//            System.out.println("SFEI name: " + conveyor.getSfeiMachine().getName());
            TreeItem<String> branchItem = new TreeItem<>(conveyor.getSfeiMachine().getName());
            rootItem.getChildren().add(branchItem);
        }

        treeView_SFEIs.setRoot(rootItem);
    }


    public void setSaveValues() {

    }

}
