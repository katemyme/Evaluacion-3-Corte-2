package ni.edu.uam.reserva_concurrente;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ReservaController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onSaludarButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
