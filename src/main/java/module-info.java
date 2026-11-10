module com.mycompany.subscribe {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.subscribe to javafx.fxml;
    exports com.mycompany.subscribe;
}
