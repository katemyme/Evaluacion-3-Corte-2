package ni.edu.uam.casos_de_estudio;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ni.edu.uam.caja_de_ventas.Application.CajaVentasApplication;
import ni.edu.uam.procesamiento_de_pedidos.PedidosApplication;
import ni.edu.uam.reserva_concurrente.ReservaApplication;

public class MenuController {

    // Abre el caso 3: caja de ventas.
    @FXML
    protected void onCajaButtonClick() {
        abrir(new CajaVentasApplication());
    }

    // Abre el caso 2: procesamiento de pedidos.
    @FXML
    protected void onRegistroButtonClick() {
        abrir(new PedidosApplication());
    }

    // Abre el caso 1: reserva concurrente.
    @FXML
    protected void onReservaButtonClick() {
        abrir(new ReservaApplication());
    }


    // Muestra el caso seleccionado en una ventana nueva.
    private void abrir(Application app) {
        try {
            app.start(new Stage());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir: " + e.getMessage()).show();
        }
    }
}
