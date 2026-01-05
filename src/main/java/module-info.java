module com.tmdna {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.tmdna to javafx.fxml;
    exports com.tmdna;
}
