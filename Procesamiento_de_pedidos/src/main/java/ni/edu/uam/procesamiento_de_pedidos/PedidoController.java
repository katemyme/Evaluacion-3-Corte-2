package ni.edu.uam.procesamiento_de_pedidos;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PedidoController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onSaludarButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
