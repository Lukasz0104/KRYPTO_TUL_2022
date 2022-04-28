package pl.lodz.p.it.krypto.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
    private ResourceBundle rb;
    private boolean disableButtons = true;
    private BooleanBinding encryptTextBinding;
    private AES aes;

    File inputFile;
    File outFile;

    @FXML
    private TextField keyTextField;

    @FXML
    private RadioButton bitButton;

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
    private ChoiceBox<String> languageSelection;

    @FXML
    private Label keyLabel;

    @FXML
    private Label languageLabel;

    @FXML
    private Label chosenFileLabel1;

    @FXML
    private Label chosenFileLabel2;


    @FXML
    public void initialize() {
        languageSelection.setItems(FXCollections.observableArrayList("pl", "en"));

        languageSelection.setOnAction(e -> changeLanguage());
        languageSelection.setValue("pl");

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

        textControlsContainer.visibleProperty().bind(encryptTextBinding);
        textControlsContainer.managedProperty().bind(textControlsContainer.visibleProperty());

        fileControlsContainer.visibleProperty().bind(Bindings.not(encryptTextBinding));
        fileControlsContainer.managedProperty().bind(fileControlsContainer.visibleProperty());
    }

    @FXML
    public void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(rb.getString("button.file.input"));
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
        fileChooser.setTitle(rb.getString("button.file.output"));
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
        changeKey();

        if (encryptTextBinding.get()) { // encrypting text
            if (plainTextTextArea.getText().length() == 0) {
                alert = new Alert(AlertType.WARNING, rb.getString("error.emptyTextToEncrypt"));

            } else {
                try {
                    byte[] original = plainTextTextArea.getText().getBytes(StandardCharsets.UTF_8);
                    byte[] encrypted = aes.encryptAllBytes(original);
                    String encryptedString = HexFormat.of().formatHex(encrypted);
                    cypherTextTextArea.setText(encryptedString);

                    alert = new Alert(AlertType.INFORMATION, rb.getString("success.text.encryption"));
                } catch (IOException e) {
                    e.printStackTrace();
                    alert = new Alert(AlertType.ERROR, rb.getString("error.text.encryptFail"));
                }
            }
        } else { // encrypting file
            if (inputFile == null || outFile == null) {
                alert = new Alert(AlertType.WARNING, rb.getString("error.fileNotChosen"));
            } else {
                try {
                    aes.encryptFile(inputFile, outFile.getAbsolutePath());
                    alert = new Alert(AlertType.INFORMATION, rb.getString("success.file.encryption"));
                } catch (IOException e) {
                    e.printStackTrace();
                    alert = new Alert(AlertType.ERROR, rb.getString("error.file.encryptFail"));
                }
            }
        }
        alert.showAndWait();
    }

    @FXML
    public void decrypt() {
        Alert alert;
        changeKey();

        if (encryptTextBinding.get()) { // decrypting text
            if (cypherTextTextArea.getText().length() == 0) {
                alert = new Alert(AlertType.WARNING, rb.getString("error.emptyTextToDecrypt"));

            } else if (cypherTextTextArea.getText().length() % 16 != 0) {
                alert = new Alert(AlertType.WARNING, rb.getString("error.invalidTextToDecryptLength"));

            } else {
                try {
                    String s = cypherTextTextArea.getText();
                    byte[] cypherText = HexFormat.of().parseHex(s);
                    byte[] decrypted = aes.decryptAllBytes(cypherText);
                    String decryptedText = new String(decrypted, StandardCharsets.UTF_8);
                    plainTextTextArea.setText(decryptedText);

                    alert = new Alert(AlertType.INFORMATION, rb.getString("success.text.decryption"));
                } catch (IOException e) {
                    e.printStackTrace();
                    alert = new Alert(AlertType.ERROR, rb.getString("error.text.decryptFail"));
                }

            }
        } else { // decrypting file
            if (inputFile == null || outFile == null) {
                alert = new Alert(AlertType.WARNING, rb.getString("error.fileNotChosen"));
            } else {
                try {
                    aes.decryptFile(inputFile, outFile.getAbsolutePath());
                    alert = new Alert(AlertType.INFORMATION, rb.getString("success.file.decryption"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    alert = new Alert(AlertType.ERROR, rb.getString("error.file.decryptFail"));
                }
            }
        }
        alert.showAndWait();
    }

    private void changeKey() {
        byte[] key;
        if (bitButton.isSelected()) {
            key = HexFormat.of().parseHex(keyTextField.textProperty().get());
        } else {
            key = keyTextField.textProperty().get().getBytes(StandardCharsets.US_ASCII);
        }

        if (aes == null) {
            aes = new AES(key);
        } else {
            aes.changeKey(key);
        }
    }

    private void changeLanguage() {
        String lang = languageSelection.getValue();
        Locale.setDefault(Locale.forLanguageTag(lang));
        rb = ResourceBundle.getBundle(App.RESOURCE_BUNDLE_NAME);

        keyTextField.setPromptText(rb.getString("prompt.key"));
        bitButton.setText(rb.getString("binaryKey"));
        stringRadioButton.setText(rb.getString("textEncryption"));
        fileRadioButton.setText(rb.getString("fileEncryption"));
        encryptButton.setText(rb.getString("button.encrypt"));
        decryptButton.setText(rb.getString("button.decrypt"));
        plainTextTextArea.setPromptText(rb.getString("prompt.plainText"));
        cypherTextTextArea.setPromptText(rb.getString("prompt.cypherText"));
        keyLabel.setText(rb.getString("key"));
        languageLabel.setText(rb.getString("chooseLanguage"));
        chosenFileLabel1.setText(rb.getString("chosenFile"));
        chosenFileLabel2.setText(rb.getString("chosenFile"));
    }
}
