module ni.edu.uam.caja_de_ventas {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.edu.uam.caja_de_ventas to javafx.fxml;
    exports ni.edu.uam.caja_de_ventas;
}