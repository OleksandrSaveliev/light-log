module com.tmdna {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.csv;

    opens com.tmdna to javafx.fxml;
    opens com.tmdna.model to com.fasterxml.jackson.databind;
    exports com.tmdna;
    exports com.tmdna.service;
    exports com.tmdna.utils;
}
