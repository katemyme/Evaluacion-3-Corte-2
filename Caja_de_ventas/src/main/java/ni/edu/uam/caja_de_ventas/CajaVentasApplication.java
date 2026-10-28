package ni.edu.uam.caja_de_ventas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CajaVentasApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CajaVentasApplication.class.getResource("caja-ventas-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Caja de ventas");
        stage.setScene(scene);
        stage.show();
    }
}
