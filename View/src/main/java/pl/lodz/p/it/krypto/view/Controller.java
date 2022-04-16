package pl.lodz.p.it.krypto.view;

import java.io.IOException;
import java.net.URL;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

public class Controller {
    private final URL stringContent = getClass().getResource("textAreas.fxml");
    private final URL fileContent = getClass().getResource("chooseFiles.fxml");
    private boolean encryptText = true;

    @FXML
    private TextField keyTextField;

    @FXML
    private RadioButton stringRadioButton;

    @FXML
    private RadioButton fileRadioButton;

    @FXML
    private Button encryptButton;

    @FXML
    private Button decryptButton;

    @FXML
    private ToggleGroup group = new ToggleGroup();

    @FXML
    private Pane content;

    @FXML
    private TextArea plainTextTextArea;

    @FXML
    private TextArea cypherTextTextArea;

    @FXML
    public void initialize() {

        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue,
                                Toggle oldToggle,
                                Toggle newToggle) {
                if (oldToggle != newToggle) {
                    System.out.println(((RadioButton) newToggle).getText());
                    try {
                        FXMLLoader loader;
                        if (newToggle == stringRadioButton) {
                            encryptText = true;
                            loader = new FXMLLoader(stringContent);
                        } else {
                            encryptText = false;
                            loader = new FXMLLoader(fileContent);
                        }
                        loader.setController(this);
                        Pane pane = loader.load();
                        content.getChildren().clear();
                        content.getChildren().setAll(pane);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        stringRadioButton.setSelected(true);
    }

    @FXML
    public void encrypt() {

    }

    @FXML
    public void decrypt() {

    }
}
