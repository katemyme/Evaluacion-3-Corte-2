package ni.edu.uam.procesamiento_de_pedidos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PedidosApplication extends Application {
    // Instala el cierre seguro antes de que la ventana pueda recibir eventos.
    @Override
    public void start(Stage ventana) throws IOException {
        FXMLLoader cargador = new FXMLLoader(PedidosApplication.class.getResource("pedidos-view.fxml"));
        Scene escena = new Scene(cargador.load());
        PedidosController controlador = cargador.getController();
        ventana.setTitle("Procesamiento de pedidos");
        ventana.setScene(escena);
        controlador.configurarCierre(ventana);
        ventana.show();
    }
}
