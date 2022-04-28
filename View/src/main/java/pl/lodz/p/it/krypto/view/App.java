package pl.lodz.p.it.krypto.view;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    public static final String RESOURCE_BUNDLE_NAME = App.class.getPackageName() + ".language";
    public static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        ResourceBundle rb = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, Locale.forLanguageTag("pl"));
        App.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("window.fxml"), rb);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("AES");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
