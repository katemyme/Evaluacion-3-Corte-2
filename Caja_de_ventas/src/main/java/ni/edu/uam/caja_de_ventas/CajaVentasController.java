package ni.edu.uam.caja_de_ventas;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CajaVentasController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onSaludarButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
