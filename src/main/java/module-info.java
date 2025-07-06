module org.duo.notionminimain1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.kordamp.ikonli.feather;

    opens org.duo.notionminimain1 to javafx.fxml;
    exports org.duo.notionminimain1;
}