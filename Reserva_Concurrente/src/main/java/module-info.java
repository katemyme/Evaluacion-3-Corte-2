module ni.edu.uam.reserva_concurrente {
    requires javafx.controls;
    requires javafx.fxml;

    opens ni.edu.uam.reserva_concurrente to javafx.fxml;
    exports ni.edu.uam.reserva_concurrente;
    exports ni.edu.uam.reserva_concurrente.controller;
    opens ni.edu.uam.reserva_concurrente.controller to javafx.fxml;
}