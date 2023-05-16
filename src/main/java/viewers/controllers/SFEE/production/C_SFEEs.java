package viewers.controllers.SFEE.production;

import controllers.production.cSFEE_production;
import failures.stochasticTime;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import models.SFEx.SFEI_conveyor;
import models.SFEx.SFEI_machine;
import models.base.SFEE;
import models.base.SFEI;
import viewers.controllers.C_ShopFloor;
import viewers.controllers.SFEI.C_SFEI_conveyor;
import viewers.controllers.SFEI.C_SFEI_machine;
import viewers.mediators.CM_SFEE;

import java.net.URL;
import java.util.*;


public class C_SFEEs extends CM_SFEE implements Initializable {

    // For use when load config
    private cSFEE_production cSFEEProduction;
    private ArrayList<ToggleButton> menu_bar = new ArrayList<>();

    public C_SFEEs() {
        super();
    }

    public void setcSFEEProduction(cSFEE_production cSFEEProduction) {
        this.cSFEEProduction = cSFEEProduction;
        loadData();
    }

    public cSFEE_production getcSFEEProduction() {
        return cSFEEProduction;
    }

    private enum Panes {
        PROPERTIES,
        COMMUNICATION,
        ITEMS,
        FAILURE,
        FINISH;

    }

    private Panes activePane;

    @FXML
    private TextField SFEE_name;
    @FXML
    private AnchorPane SFEE_body;
    @FXML
    private ToggleButton toggleProperties;
    @FXML
    private ToggleButton toggleCommunication;
    @FXML
    private ToggleButton toggleItems;
    @FXML
    private ToggleButton toggleFailure;
    @FXML
    private ToggleButton toggleFinish;
    @FXML
    private ToggleGroup bar;

    @FXML
    private ImageView error_icon;
    @FXML
    private Tooltip errorMsg;
    @FXML
    private Button next;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
/*        CM_SFEE.getInstance().registerC_SFEE_body_properties(new C_SFEE_properties());
        CM_SFEE.getInstance().registerC_SFEE_body_communication(new C_SFEE_communication());
        CM_SFEE.getInstance().registerC_SFEE_body_items(new C_SFEE_items());
        CM_SFEE.getInstance().registerC_SFEE_body_failure(new C_SFEE_failure());
        CM_SFEE.getInstance().registerC_SFEE_body_finish(new C_SFEE_finish());*/

        if (C_ShopFloor.getInstance().isLoadedConfig())
            SFEE_name.setText(cSFEEProduction.getSFEE_name());

        registerC_SFEE_body_properties(new C_SFEE_properties());
        registerC_SFEE_body_communication(new C_SFEE_communication());
        registerC_SFEE_body_items(new C_SFEE_items());
        registerC_SFEE_body_failure(new C_SFEE_failure());
        registerC_SFEE_body_finish(new C_SFEE_finish());

        menu_bar.add(toggleProperties);
        menu_bar.add(toggleCommunication);
        menu_bar.add(toggleItems);
        menu_bar.add(toggleFailure);
        menu_bar.add(toggleFinish);

        for (int i = 1; i < menu_bar.size(); i++) {
            menu_bar.get(i).setDisable(true);
        }

    }

    private void loadData() {

        // Only executed when C_ShopFloor.getInstance().isLoadedConfig() == TRUE
        C_SFEE_properties cSfeeProperties = new C_SFEE_properties();
        cSfeeProperties.loadData(cSFEEProduction.getSFEE().getSFEE_environment(), cSFEEProduction.getSFEE().getSFEE_role());

        C_SFEE_communication cSfeeCommunication = new C_SFEE_communication();
        String[] comsFields = {cSFEEProduction.getMb().getIp(), String.valueOf(cSFEEProduction.getMb().getPort()), String.valueOf(cSFEEProduction.getMb().getSlaveID())};
        cSfeeCommunication.loadData(cSFEEProduction.getSFEE().getCom(), comsFields, cSFEEProduction.getSFEE().getIo());

        C_SFEE_items cSfeeItems = new C_SFEE_items();
        // setIo() by default create new sfeisController instance
        cSfeeItems.setIo(cSFEEProduction.getSFEE().getIo(), cSFEEProduction.getSFEE().getSFEE_environment());

        for (Map.Entry<Integer, SFEI> entry : cSFEEProduction.getSFEE().getSFEIs().entrySet()) {
            switch (entry.getValue().getSfeiType()) {
                case CONVEYOR -> {
                    C_SFEI_conveyor cSfeiConveyor = new C_SFEI_conveyor(cSFEEProduction.getSFEE().getIo());
                    cSfeiConveyor.setSfeiConveyor((SFEI_conveyor) entry.getValue());
                    cSfeeItems.getSfeisController().registerC_SFEI_conveyor(cSfeiConveyor);
                }
                case MACHINE -> {
                    C_SFEI_machine cSfeiMachine = new C_SFEI_machine(cSFEEProduction.getSFEE().getIo());
                    cSfeiMachine.setSfeiMachine((SFEI_machine) entry.getValue());
                    cSfeeItems.getSfeisController().registerC_SFEI_machine(cSfeiMachine);
                }
            }
        }


        C_SFEE_failure cSfeeFailure = new C_SFEE_failure();
        ArrayList<String> loadedData = new ArrayList<>();
        if (cSFEEProduction.getOpMode().equals(cSFEE_production.operationMode.PROG_FAILURES)) {
            if (cSFEEProduction.getSfeeFailures2().getStochasticType().equals(stochasticTime.timeOptions.GAUSSIAN)) {
                // Rebuild string
                cSFEEProduction.getSfeeFailures2().getFailuresFormulas().forEach(strings -> {
                    loadedData.addAll(Arrays.stream(strings).toList());
                });

                String mean = cSFEEProduction.getSfeeFailures2().getStochasticFormulas()[0];
                String dev = cSFEEProduction.getSfeeFailures2().getStochasticFormulas()[1];
                loadedData.add(0, "gauss [ " + mean + " ; " + dev + " ]");
            }
        }
        cSfeeFailure.loadData(cSFEEProduction.getOpMode(), cSFEEProduction.getOpMode().equals(cSFEE_production.operationMode.NORMAL) ? null : loadedData);

        registerC_SFEE_body_properties(cSfeeProperties);
        registerC_SFEE_body_communication(cSfeeCommunication);
        registerC_SFEE_body_items(cSfeeItems);
        registerC_SFEE_body_failure(cSfeeFailure);
        // For the Finish it is not needed because is a summary of these previous Controllers

    }

    public String getSFEE_name() {
        if (SFEE_name == null)
            return cSFEEProduction.getSFEE_name();
        else
            return SFEE_name.getText();
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
//            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ShopFloor.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShopFloor.fxml"));
            loader.setController(new C_ShopFloor(""));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void buttonPressed(ActionEvent event) {

        ToggleButton temp = (ToggleButton) event.getSource();

        // Save data of current Pane
        if (activePane != null)
            saveActivePaneData();
        try {
            switch (temp.getText()) {
                case "Properties" -> {
                    activePane = Panes.PROPERTIES;
                    next.setVisible(true);
                }
                case "Communication" -> {
                    activePane = Panes.COMMUNICATION;
                }
                case "Items" -> {
                    activePane = Panes.ITEMS;
                }
                case "Failure" -> {
                    activePane = Panes.FAILURE;
                }
                case "Finish" -> {
                    activePane = Panes.FINISH;
                }
            }
            adjustStyle();
            setPane();

        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    private void saveActivePaneData() {
        switch (activePane) {
            case PROPERTIES -> getProperties().setSaveValues();
            case COMMUNICATION -> getCommunication().setSaveValues();
            case ITEMS -> getItems().setSaveValues();
            case FAILURE -> getFailure().setSaveValues();
        }
    }

    @FXML
    void goNext(ActionEvent event) {
        try {
            ToggleButton selected = (ToggleButton) bar.getSelectedToggle();

            switch (selected.getText()) {
                case "Properties" -> {
                    if (getProperties().validateMoveOn()) {
                        getProperties().setSaveValues();
                        getFailure().setSfeeType(getProperties().getEnvironment().equalsIgnoreCase("simulation") ? SFEE.SFEE_environment.SIMULATION : SFEE.SFEE_environment.REAL);
                        activePane = Panes.COMMUNICATION;
                        toggleCommunication.setDisable(false);
                        bar.selectToggle(toggleCommunication);
                        Tooltip.uninstall(next, errorMsg);
                    } else {
                        error_icon.setVisible(true);
                        errorMsg.setText(getProperties().getErrorMsg());
                        Tooltip.install(next, errorMsg);
                    }
                }
                case "Communication" -> {
                    if (getCommunication().validateMoveOn()) {

                        getCommunication().setSaveValues();
                        activePane = Panes.ITEMS;
                        toggleItems.setDisable(false);
                        bar.selectToggle(toggleItems);
                        Tooltip.uninstall(next, errorMsg);
                    } else {
                        error_icon.setVisible(true);
                        errorMsg.setText(getCommunication().getErrorMsg());
                        Tooltip.install(next, errorMsg);

                    }
                }
                case "Items" -> {
                    if (getItems().validateMoveOn()) {
                        getItems().setSaveValues();
                        activePane = Panes.FAILURE;
                        toggleFailure.setDisable(false);
                        bar.selectToggle(toggleFailure);
                        Tooltip.uninstall(next, errorMsg);
                    } else {
                        error_icon.setVisible(true);
                        errorMsg.setText(getItems().getErrorMsg());
                        Tooltip.install(next, errorMsg);
                    }
                }
                case "Failure" -> {
                    if (getFailure().validateMoveOn()) {
                        getFailure().setSaveValues();
                        activePane = Panes.FINISH;
                        getFinish().setSfeiConveyors(getItems().getSfeisController().getSfeiConveyors());
                        getFinish().setSfeiMachines(getItems().getSfeisController().getSfeiMachines());
                        toggleFinish.setDisable(false);
                        bar.selectToggle(toggleFinish);
                        Tooltip.uninstall(next, errorMsg);
                    } else {
                        error_icon.setVisible(true);
                        errorMsg.setText(getFailure().getErrorMsg());
                        Tooltip.install(next, errorMsg);
                    }
                }
                case "Finish" -> {
                    if (getFinish().validateMoveOn(event)) {
                        Tooltip.uninstall(next, errorMsg);

                        // return to C_ShopFloor
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShopFloor.fxml"));
                        loader.setController(C_ShopFloor.getInstance());
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(loader.load(), 1200, 900));
                        stage.show();

                    } else {
                        error_icon.setVisible(true);
                        errorMsg.setText(/*CM_SFEE.getInstance().*/getFinish().getErrorMsg());
                        Tooltip.install(next, errorMsg);
                    }
                }
                default -> {
                    System.out.println("THIS SHOULDN'T WRITE " + selected.getText());
                }

            }
            adjustStyle();
            setPane();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void adjustStyle() {

        for (int i = 0; i < menu_bar.size(); i++) {

//            System.out.println(menu_bar.get(i).getText() + " " + menu_bar.get(i).getStyleClass().toString());

            if (i == activePane.ordinal()) {
                if (!menu_bar.get(i).getStyleClass().contains("sfee-bar-btn-active"))
                    menu_bar.get(i).getStyleClass().add("sfee-bar-btn-active");
                bar.selectToggle(menu_bar.get(i));
            } else {
                if (!menu_bar.get(i).isDisabled()) {
//                    menubar.get(i).getStyleClass().clear();
                    menu_bar.get(i).getStyleClass().remove("sfee-bar-btn-active");
                }
            }
        }

    }

    private void setPane() {

        String name = String.valueOf(activePane);
        name = name.toLowerCase();
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE/SFEE_" + name + ".fxml"));
            switch (activePane) {
                case PROPERTIES -> {

                    loader.setController(getProperties());
                }
                case COMMUNICATION -> {
                    loader.setController(getCommunication());
                }
                case ITEMS -> {
                    getItems().setIo(getCommunication().getIo(), getProperties().getEnvironment().equalsIgnoreCase("simulation") ? SFEE.SFEE_environment.SIMULATION : SFEE.SFEE_environment.REAL);
                    loader.setController(getItems());
                }
                case FAILURE -> {
                    loader.setController(getFailure());
                }
                case FINISH -> loader.setController(getFinish());
            }
            Pane pane = loader.load();
            SFEE_body.getChildren().setAll(pane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
