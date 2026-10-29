package ni.edu.uam.procesamiento_de_pedidos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PedidosApplication extends Application {

    // Carga la vista, conecta el cierre seguro con el controlador y muestra la ventana.
    @Override
    public void start(Stage ventana) throws IOException {
        FXMLLoader cargador = new FXMLLoader(PedidosApplication.class.getResource("pedidos-view.fxml"));
        Scene escena = new Scene(cargador.load());
        PedidosController controlador = cargador.getController();
        ventana.setTitle("Procesamiento de pedidos");
        ventana.setScene(escena);
        ventana.setOnCloseRequest(controlador::cerrarVentana);
        ventana.show();
    }
}
