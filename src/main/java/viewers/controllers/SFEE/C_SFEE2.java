package viewers.controllers.SFEE;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import viewers.controllers.C_SFEM_layout;
import viewers.mediators.CM_SFEE;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class C_SFEE2 extends CM_SFEE implements Initializable {
    ArrayList<ToggleButton> menu_bar = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CM_SFEE.getInstance().registerC_SFEE_body_properties(new C_SFEE_properties());
        CM_SFEE.getInstance().registerC_SFEE_body_communication(new C_SFEE_communication());
        CM_SFEE.getInstance().registerC_SFEE_body_items(new C_SFEE_items());
        CM_SFEE.getInstance().registerC_SFEE_body_failure(new C_SFEE_failure());
        CM_SFEE.getInstance().registerC_SFEE_body_finish(new C_SFEE_finish());

        menu_bar.add(toggleProperties);
        menu_bar.add(toggleCommunication);
        menu_bar.add(toggleItems);
        menu_bar.add(toggleFailure);
        menu_bar.add(toggleFinish);

        for (int i = 1; i < menu_bar.size(); i++) {
            menu_bar.get(i).setDisable(true);
        }

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


    public C_SFEE2() {
        super();
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
//            Parent root = FXMLLoader.load(getClass().getResource("/fxml/SFEM_layout.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEM_layout.fxml"));
            loader.setController(new C_SFEM_layout(""));
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
            case PROPERTIES -> CM_SFEE.getInstance().getProperties().setSaveValues();
            case COMMUNICATION -> CM_SFEE.getInstance().getCommunication().setSaveValues();
            case ITEMS -> CM_SFEE.getInstance().getItems().setSaveValues();
            case FAILURE -> CM_SFEE.getInstance().getFailure().setSaveValues();
        }
    }

    @FXML
    void goNext(ActionEvent event) {
        ToggleButton selected = (ToggleButton) bar.getSelectedToggle();

        switch (selected.getText()) {
            case "Properties" -> {
                if (CM_SFEE.getInstance().getProperties().validation_moveON()) {
                    CM_SFEE.getInstance().getProperties().setSaveValues();
                    activePane = Panes.COMMUNICATION;
                    toggleCommunication.setDisable(false);
//                    toggleCommunication.requestFocus();
                    bar.selectToggle(toggleCommunication);
                    Tooltip.uninstall(next, errorMsg);
                } else {
                    error_icon.setVisible(true);
                    errorMsg.setText(CM_SFEE.getInstance().getProperties().getErrorMsg());
                    Tooltip.install(next, errorMsg);
//                    next.setTooltip(errorMsg);

                }
            }
            case "Communication" -> {
                if (CM_SFEE.getInstance().getCommunication().validation_moveON()) {
                    CM_SFEE.getInstance().getCommunication().setSaveValues();
                    activePane = Panes.ITEMS;
                    toggleItems.setDisable(false);
//                    toggleItems.requestFocus();
                    bar.selectToggle(toggleItems);
                    Tooltip.uninstall(next, errorMsg);
                } else {
                    error_icon.setVisible(true);
                    errorMsg.setText(CM_SFEE.getInstance().getCommunication().getErrorMsg());
//                    next.setTooltip(errorMsg);
                    Tooltip.install(next, errorMsg);

                }
            }
            case "Items" -> {
                if (CM_SFEE.getInstance().getItems().validation_moveON()) {
                    CM_SFEE.getInstance().getItems().setSaveValues();
                    activePane = Panes.FAILURE;
                    toggleFailure.setDisable(false);
//                    toggleFailure.requestFocus();
                    bar.selectToggle(toggleFailure);
                    Tooltip.uninstall(next, errorMsg);
                } else {
                    error_icon.setVisible(true);
                    errorMsg.setText(CM_SFEE.getInstance().getItems().getErrorMsg());
                    Tooltip.install(next, errorMsg);
//                    next.setTooltip(errorMsg);

                }
            }
            case "Failure" -> {
                if (CM_SFEE.getInstance().getFailure().validation_moveON()) {
                    CM_SFEE.getInstance().getFailure().setSaveValues();
                    activePane = Panes.FINISH;
                    toggleFinish.setDisable(false);
//                    toggleFinish.requestFocus();
                    bar.selectToggle(toggleFinish);
                    Tooltip.uninstall(next, errorMsg);
                } else {
                    error_icon.setVisible(true);
                    errorMsg.setText(CM_SFEE.getInstance().getFailure().getErrorMsg());
                    Tooltip.install(next, errorMsg);
                }
            }
            case "Finish" -> {
                // to do
            }
            default -> {
                System.out.println("THIS SHOULDN'T WRITE " + selected.getText());
            }

        }
        adjustStyle();
        setPane();

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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SFEE_" + name + ".fxml"));
            switch (activePane) {
                case PROPERTIES -> {

                    loader.setController(CM_SFEE.getInstance().getProperties());
                }
                case COMMUNICATION -> {
                    loader.setController(CM_SFEE.getInstance().getCommunication());
                }
                case ITEMS -> {
                    CM_SFEE.getInstance().getItems().setIo(CM_SFEE.getInstance().getCommunication().getIo());
                    loader.setController(CM_SFEE.getInstance().getItems());
                }
                case FAILURE -> {
                    loader.setController(CM_SFEE.getInstance().getFailure());
                }
                case FINISH -> loader.setController(CM_SFEE.getInstance().getFinish());
            }
            Pane pane = loader.load();
            SFEE_body.getChildren().setAll(pane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
