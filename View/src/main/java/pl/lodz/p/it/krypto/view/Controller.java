package pl.lodz.p.it.krypto.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import pl.lodz.p.it.krypto.aes.AES;

public class Controller {
    private boolean disableButtons = true;
    private BooleanBinding encryptTextBinding;

    File inputFile;
    File outFile;

    @FXML
    private TextField keyTextField;

    @FXML
    private RadioButton stringRadioButton;

    @FXML
    private Button encryptButton;

    @FXML
    private Button decryptButton;

    @FXML
    private ToggleGroup group = new ToggleGroup();

    @FXML
    private HBox textControlsContainer;

    @FXML
    private TextArea plainTextTextArea;

    @FXML
    private TextArea cypherTextTextArea;

    @FXML
    private VBox fileControlsContainer;

    @FXML
    private Label inFilePath;

    @FXML
    private Label outFilePath;

    @FXML
    public void initialize() {
        encryptButton.disableProperty().set(disableButtons);
        decryptButton.disableProperty().set(disableButtons);
        encryptTextBinding = Bindings.equal(stringRadioButton, group.selectedToggleProperty());
        stringRadioButton.setSelected(true);

        keyTextField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                disableButtons = newValue.getBytes(StandardCharsets.US_ASCII).length != 16;
                encryptButton.disableProperty().set(disableButtons);
                decryptButton.disableProperty().set(disableButtons);
            }
        });
        keyTextField.textProperty().set("");

        textControlsContainer.visibleProperty().bind(encryptTextBinding);
        textControlsContainer.managedProperty().bind(textControlsContainer.visibleProperty());

        fileControlsContainer.visibleProperty().bind(Bindings.not(encryptTextBinding));
        fileControlsContainer.managedProperty().bind(fileControlsContainer.visibleProperty());
    }

    @FXML
    public void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik");
        inputFile = fileChooser.showOpenDialog(App.stage);
        if (inputFile != null) {
            inFilePath.setText(inputFile.getAbsolutePath());
        } else {
            inFilePath.setText("");
        }
    }

    @FXML
    public void saveFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik wyjściowy");
        outFile = fileChooser.showSaveDialog(App.stage);
        if (outFile != null) {
            outFilePath.setText(outFile.getAbsolutePath());
        } else {
            outFilePath.setText("");
        }
    }

    @FXML
    public void encrypt() {
        AES aes = new AES(keyTextField.textProperty().get().getBytes(StandardCharsets.US_ASCII));
        if (encryptTextBinding.get()) {
            byte[] original = plainTextTextArea.getText().getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = aes.encryptAllBytes(original);
            String encryptedString = HexFormat.of().formatHex(encrypted);
            cypherTextTextArea.setText(encryptedString);
        } else if (inputFile != null && outFile != null) {
            try {
                aes.encryptFile(inputFile, outFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                // todo add modal
            }
        }
    }

    @FXML
    public void decrypt() {
        AES aes = new AES(keyTextField.textProperty().get().getBytes(StandardCharsets.US_ASCII));
        if (encryptTextBinding.get()) {
            String s = cypherTextTextArea.getText();
            byte[] cypherText = HexFormat.of().parseHex(s);
            byte[] decrypted = aes.decryptAllBytes(cypherText);
            String decryptedText = new String(decrypted, StandardCharsets.UTF_8);
            plainTextTextArea.setText(decryptedText);
        } else if (inputFile != null && outFile != null) {
            try {
                aes.decryptFile(inputFile, outFile.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
                // todo add modal
            }
        }
    }
}
