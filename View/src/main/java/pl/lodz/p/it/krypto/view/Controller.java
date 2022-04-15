package pl.lodz.p.it.krypto.view;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class Controller {
    @FXML
    private HBox keyContainer;

    @FXML
    private TextField keyTextField;

    @FXML
    private RadioButton stringRadioButton;

    @FXML
    private RadioButton fileRadioButton;

    @FXML
    private HBox buttonsContainer;

    @FXML
    private ToggleGroup group = new ToggleGroup();

    @FXML
    private Pane content;

    @FXML
    public void initialize() {
        System.out.println(group.getToggles());

        group.selectedToggleProperty().addListener((observableValue, oldValue, newValue) -> {
            RadioButton rb = (RadioButton) group.getSelectedToggle();

            if (rb != null) {
                System.out.println(rb.getText());

                if (rb == stringRadioButton) {
                    content.getChildren().clear();
                    try {
                        content.getChildren().add(FXMLLoader.load(getClass().getResource("textAreas.fxml")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    content.getChildren().clear();
                }
            }
        });
    }

    public Controller() {

    }
}
