module View {
    requires java.base;
    requires Model;
    requires javafx.controls;
    requires javafx.fxml;
	requires transitive javafx.graphics;

    opens pl.lodz.p.it.krypto.view to javafx.fxml;
    exports pl.lodz.p.it.krypto.view;
}