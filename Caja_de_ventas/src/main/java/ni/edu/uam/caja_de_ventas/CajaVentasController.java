package ni.edu.uam.caja_de_ventas;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

public class CajaVentasController {

    @FXML
    private Label lblTotalVentas, lblRecaudacion, lblEstadoGeneral;


    @FXML
    private ProgressBar pbVendedor1, pbVendedor2, pbVendedor3;
    @FXML
    private Label lblVentasV1, lblVentasV2, lblVentasV3;
    @FXML
    private Label lblEstadoV1, lblEstadoV2, lblEstadoV3;
    @FXML
    private TextArea txtBitacora;
    @FXML
    private Button btnIniciar, btnCancelar;

    @FXML
    protected void onIniciarClick() {

    }

    @FXML
    protected void onCancelarClick() {
    }
}
