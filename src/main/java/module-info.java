module ni.edu.uam.casos_de_estudio {
    requires javafx.controls;
    requires javafx.fxml;
    requires ni.edu.uam.caja_de_ventas;
    requires ni.edu.uam.procesamiento_de_pedidos;
    requires ni.edu.uam.reserva_concurrente;


    opens ni.edu.uam.casos_de_estudio to javafx.fxml;
    exports ni.edu.uam.casos_de_estudio;
}
