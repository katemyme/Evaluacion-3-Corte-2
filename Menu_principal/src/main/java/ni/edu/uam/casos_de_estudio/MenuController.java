package ni.edu.uam.casos_de_estudio;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ni.edu.uam.caja_de_ventas.CajaVentasApplication;
import ni.edu.uam.procesamiento_de_pedidos.PedidoApplication;
import ni.edu.uam.reserva_concurrente.ReservaApplication;

public class MenuController {

    @FXML
    protected void onCajaButtonClick() {
        abrir(new CajaVentasApplication());
    }

    @FXML
    protected void onRegistroButtonClick() {
        abrir(new PedidoApplication());
    }

    @FXML
    protected void onReservaButtonClick() {
        abrir(new ReservaApplication());
    }


    private void abrir(Application app) {
        try {
            app.start(new Stage());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir: " + e.getMessage()).show();
        }
    }
}
