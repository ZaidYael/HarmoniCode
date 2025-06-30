module org.example.harmonicode {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxmisc.richtext;


    opens org.example.harmonicode to javafx.fxml;
    exports org.example.harmonicode;
    opens org.example.harmonicode.Controller to javafx.fxml;
    exports org.example.harmonicode.Controller;
}