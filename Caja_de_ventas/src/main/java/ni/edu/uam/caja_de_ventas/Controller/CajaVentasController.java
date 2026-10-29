package ni.edu.uam.caja_de_ventas;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

public class CajaVentasController {

    // ESTADO GENERAL
    @FXML private Label lblTotalVentas;
    @FXML private Label lblRecaudacion;
    @FXML private Label lblEstadoGeneral;

    // Vendedor 1
    @FXML private ProgressBar pbVendedor1;
    @FXML private Label lblVentasV1;
    @FXML private Label lblEstadoV1;

    // Vendedor 2
    @FXML private ProgressBar pbVendedor2;
    @FXML private Label lblVentasV2;
    @FXML private Label lblEstadoV2;

    // Vendedor 3
    @FXML private ProgressBar pbVendedor3;
    @FXML private Label lblVentasV3;
    @FXML private Label lblEstadoV3;

    // Bitácora y Controles
    @FXML private TextArea txtBitacora;
    @FXML private Button btnIniciar;
    @FXML private Button btnCancelar;

    // Botones de evento
    protected void onIniciarClick() {

    }

    @FXML
    protected void onCancelarClick() {
    }
}
