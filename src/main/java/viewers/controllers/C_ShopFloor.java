package viewers.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import models.SFEx_particular.SFEI_conveyor;
import net.synedra.validatorfx.Validator;
import viewers.controllers.SFEE.C_SFEEs;
import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;
import viewers.controllers.SFEM.C_SFEM;
import viewers.mediators.CM_SFEM;

public class C_ShopFloor implements Initializable {

    private final ArrayList<C_SFEM> cmSfems = new ArrayList<>();

    private String layout_name;

    public C_ShopFloor() {
    }

    public C_ShopFloor(String layout_name) {
        this.layout_name = layout_name;
    }

    /**
     * Everything below here is in support of Singleton pattern
     */
    public static C_ShopFloor getInstance() {
        return C_ShopFloor.C_ShopFloorHolder.INSTANCE;
    }

    private static class C_ShopFloorHolder {
        private static final C_ShopFloor INSTANCE = new C_ShopFloor();
    }

    /**
     * Until here !
     */


    @FXML
    private TextField sfem_name;
    @FXML
    private Button submit_name_btn;
    @FXML
    private ListView<String> SFEMs_list;

    @FXML
    private TreeView<String> sfem_elements_tree;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sfem_name.setText(layout_name);

        updateSFEMlist();
        updateElementsTreeView();
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/homepage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void buttonPressed(ActionEvent event) {
        try {
            Button temp = (Button) event.getSource();
            switch (temp.getId()) {
                case "new_sfee" -> {
                    // Get selected row -> C_SFEM instance

                    if (SFEMs_list.getSelectionModel().getSelectedIndices().size() == 1) {
                        C_SFEM cSfem = cmSfems.get(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE/SFEE.fxml"));
                        // create new C_SFEEs instance
                        C_SFEEs cSfees = new C_SFEEs();

                        loader.setController(cSfees);
                        cSfem.registerC_SFEEs(cSfees);
                        // open scene
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(loader.load()));
                        stage.show();
                    } else {
                        // ERROR
                        System.out.println("Select only 1 row!");
                    }


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    void newSFEMname(ActionEvent event) {
        sfem_name.setVisible(true);
        submit_name_btn.setVisible(true);
    }

    @FXML
    void newSFEM(ActionEvent event) {

        Validator validator = new Validator();

        validator.createCheck()
                .dependsOn("SFEM_name", sfem_name.textProperty())
                .withMethod(c -> {
                    String name = c.get("SFEM_name");
                    if (name.isBlank())
                        c.error("This field can not be empty");
                })
                .decorates(sfem_name)
                .immediate();

        if (validator.validate()) {
            cmSfems.add(cmSfems.size(), new C_SFEM(sfem_name.getText()));
            updateSFEMlist();
        }
    }

    private void updateSFEMlist() {
        ArrayList<String> list = new ArrayList<>();
        for (C_SFEM cSfem : cmSfems) {
            list.add(cSfem.getSfemName());
        }
        SFEMs_list.getItems().addAll(list);
    }

    @FXML
    void updateElementsTreeView() {
        TreeItem<String> root = new TreeItem<>(" ");
        if (SFEMs_list.getSelectionModel().getSelectedIndices().size() == 1) {
            C_SFEM cSfem = cmSfems.get(SFEMs_list.getSelectionModel().getSelectedIndices().get(0));
            for (C_SFEEs cSfees : cSfem.getSfeesControllers()) {
                TreeItem<String> branch = new TreeItem<>(cSfees.getSFEE_name());
                for (C_SFEI_conveyor conveyor : cSfees.getItems().getSfeisController().getSfeiConveyors()) {
                    TreeItem<String> leaf = new TreeItem<>(conveyor.getSfeiConveyor().getName());
                    branch.getChildren().add(leaf);
                }
                for (C_SFEI_machine machine : cSfees.getItems().getSfeisController().getSfeiMachines()) {
                    TreeItem<String> leaf = new TreeItem<>(machine.getSfeiMachine().getName());
                    branch.getChildren().add(leaf);
                }
                root.getChildren().add(branch);
            }
            sfem_elements_tree.setRoot(root);
        }
    }


}
