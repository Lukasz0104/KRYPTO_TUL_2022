package pl.lodz.p.it.krypto.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
    private RadioButton bitButton;

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
        encryptTextBinding = Bindings.equal(stringRadioButton, group.selectedToggleProperty());
        stringRadioButton.setSelected(true);

        keyTextField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                if (bitButton.isSelected()) {
                    disableButtons = true;
                    if (newValue.matches("^[0-9a-fA-F]{32}$")) {
                        disableButtons = HexFormat.of().parseHex(newValue).length != 16;
                    }
                } else if (!bitButton.isSelected()) {
                    disableButtons = newValue.getBytes(StandardCharsets.US_ASCII).length != 16;
                }
                encryptButton.disableProperty().set(disableButtons);
                decryptButton.disableProperty().set(disableButtons);
            }
        });

        bitButton.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                String enteredKey = keyTextField.getText();
                if (newValue) {
                    disableButtons = !enteredKey.matches("^[0-9a-fA-F]{32}$")
                            || HexFormat.of().parseHex(enteredKey).length != 16;
                } else {
                    disableButtons = enteredKey.getBytes(StandardCharsets.US_ASCII).length != 16;
                }

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
        Alert alert;
        byte[] key;
        if (bitButton.isSelected()) {
            key = HexFormat.of().parseHex(keyTextField.textProperty().get());
        } else {
            key = keyTextField.textProperty().get().getBytes(StandardCharsets.US_ASCII);
        }
        AES aes = new AES(key);

        if (encryptTextBinding.get()) { // encrypting text
            if (plainTextTextArea.getText().length() == 0) {
                alert = new Alert(Alert.AlertType.WARNING,
                        "Tekst do zaszyfrowania nie może byc pusty!");
            } else {
                byte[] original = plainTextTextArea.getText().getBytes(StandardCharsets.UTF_8);
                byte[] encrypted = aes.encryptAllBytes(original);
                String encryptedString = HexFormat.of().formatHex(encrypted);
                cypherTextTextArea.setText(encryptedString);

                alert = new Alert(Alert.AlertType.INFORMATION, "Pomyślnie zaszyfrowano tekst");
            }
        } else { // encrypting file
            if (inputFile == null || outFile == null) {
                alert = new Alert(Alert.AlertType.WARNING,
                        "Należy wybrać plik wejściowy i wyjściowy");
            } else {
                try {
                    aes.encryptFile(inputFile, outFile.getAbsolutePath());
                    alert = new Alert(Alert.AlertType.INFORMATION, "Pomyślnie zaszyfrowano plik");
                } catch (IOException e) {
                    e.printStackTrace();
                    alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
                }
            }
        }
        alert.showAndWait();
    }

    @FXML
    public void decrypt() {
        Alert alert;
        byte[] key;
        if (bitButton.isSelected()) {
            key = HexFormat.of().parseHex(keyTextField.textProperty().get());
        } else {
            key = keyTextField.textProperty().get().getBytes(StandardCharsets.US_ASCII);
        }
        AES aes = new AES(key);
        if (encryptTextBinding.get()) { // decrypting text
            if (cypherTextTextArea.getText().length() == 0) {
                alert = new Alert(Alert.AlertType.WARNING,
                        "Tekst do odszyfrowania nie może być pusty");
            } else if (cypherTextTextArea.getText().length() % 16 != 0) {
                alert = new Alert(Alert.AlertType.WARNING,
                        "Długość zaszyfrowanego tekstu musi być wielokrotnością 16");
            } else {
                String s = cypherTextTextArea.getText();
                byte[] cypherText = HexFormat.of().parseHex(s);
                byte[] decrypted = aes.decryptAllBytes(cypherText);
                String decryptedText = new String(decrypted, StandardCharsets.UTF_8);
                plainTextTextArea.setText(decryptedText);
                alert = new Alert(Alert.AlertType.INFORMATION, "Pomyślnie zdeszyfrowano tekst");
            }
        } else { // decrypting file
            if (inputFile == null || outFile == null) {
                alert = new Alert(Alert.AlertType.WARNING, "Należy wybrać plik wejściowy i wyjściowy");
            } else {
                try {
                    aes.decryptFile(inputFile, outFile.getAbsolutePath());
                    alert = new Alert(Alert.AlertType.INFORMATION, "Pomyślnie zaszyfrowano plik");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    alert = new Alert(Alert.AlertType.ERROR, "Nie udało się zdeszyfrować pliku");
                }
            }
        }
        alert.showAndWait();
    }
}
