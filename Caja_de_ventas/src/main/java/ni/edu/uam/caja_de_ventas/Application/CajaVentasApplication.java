package ni.edu.uam.caja_de_ventas.Application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.caja_de_ventas.Controller.CajaVentasController;

import java.io.IOException;

public class CajaVentasApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CajaVentasApplication.class.getResource("/ni/edu/uam/caja_de_ventas/caja-ventas-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 680, 580);
        CajaVentasController controller = fxmlLoader.getController();
        stage.setTitle("Caja de Ventas Concurrente - UAM");
        stage.setMinWidth(650);
        stage.setMinHeight(550);
        stage.setScene(scene);
        stage.setOnCloseRequest(controller::cerrarLimpiamente);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}