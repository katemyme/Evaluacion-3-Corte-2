module ni.edu.uam.procesamiento_de_pedidos {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.edu.uam.procesamiento_de_pedidos to javafx.fxml;
    exports ni.edu.uam.procesamiento_de_pedidos;
}