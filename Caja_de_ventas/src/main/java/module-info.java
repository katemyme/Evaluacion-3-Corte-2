module ni.edu.uam.caja_de_ventas {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.edu.uam.caja_de_ventas to javafx.fxml;
    exports ni.edu.uam.caja_de_ventas;
    exports ni.edu.uam.caja_de_ventas.Controller;
    opens ni.edu.uam.caja_de_ventas.Controller to javafx.fxml;
    exports ni.edu.uam.caja_de_ventas.Application;
    opens ni.edu.uam.caja_de_ventas.Application to javafx.fxml;
}