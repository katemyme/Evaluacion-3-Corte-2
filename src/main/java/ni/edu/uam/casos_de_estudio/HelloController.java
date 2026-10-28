package ni.edu.uam.casos_de_estudio;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class HelloController {

    @FXML
    protected void onCajaButtonClick() {
        abrir(new ni.edu.uam.caja_de_ventas.HelloApplication());
    }

    @FXML
    protected void onRegistroButtonClick() {
        abrir(new ni.edu.uam.procesamiento_de_pedidos.HelloApplication());
    }

    @FXML
    protected void onReservaButtonClick() {
        abrir(new ni.edu.uam.reserva_concurrente.HelloApplication());
    }

    // Abre la aplicacion en una ventana nueva; el menu sigue abierto
    private void abrir(Application app) {
        try {
            app.start(new Stage());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir: " + e.getMessage()).show();
        }
    }
}
