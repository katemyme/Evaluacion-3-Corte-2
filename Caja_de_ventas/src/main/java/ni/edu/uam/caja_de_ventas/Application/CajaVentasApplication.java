package ni.edu.uam.caja_de_ventas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.caja_de_ventas.Controller.CajaVentasController;

import java.io.IOException;

public class CajaVentasApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CajaVentasApplication.class.getResource("caja-ventas-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 680, 580);

        // Obtener instancia del controlador para gestionar el cierre limpio (IL3.3)
        CajaVentasController controller = fxmlLoader.getController();

        stage.setTitle("Caja de Ventas Concurrente - UAM");
        stage.setMinWidth(650);
        stage.setMinHeight(550);
        stage.setScene(scene);

        // Garantizar que al cerrar la ventana no queden trabajadores activos
        stage.setOnCloseRequest(event -> {
            controller.cerrarLimpiamente();
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}