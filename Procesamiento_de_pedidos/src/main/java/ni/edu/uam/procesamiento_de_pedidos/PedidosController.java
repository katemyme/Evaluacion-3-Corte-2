
package ni.edu.uam.procesamiento_de_pedidos;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class PedidosController {

    @FXML
    private Button btnIniciar;

    @FXML
    private Button btnCancelar;

    @FXML
    private ProgressBar barraProgreso;

    @FXML
    private Label lblContador;

    @FXML
    private Label lblEstado;

    @FXML
    private TextArea txtRegistro;

    private final AtomicInteger pedidosCompletados = new AtomicInteger(0);

    private Thread hiloTrabajador;
    private volatile boolean cancelacionSolicitada;
    private volatile Exception errorProceso;
    private boolean cierrePendiente;

    // Restablece los controles al cargar el FXML, sin depender de una ventana.
    @FXML
    private void initialize() {
        actualizarEstado("Listo");
        actualizarProgreso(0);
        btnCancelar.setDisable(true);
    }

    // Inicia una nueva simulación sin bloquear la interfaz JavaFX.
    @FXML
    private void iniciarProcesamiento() {
        if (hiloTrabajador != null || cierrePendiente) return;

        pedidosCompletados.set(0);
        cancelacionSolicitada = false;
        errorProceso = null;

        txtRegistro.clear();
        actualizarProgreso(0);
        actualizarEstado("En ejecución");

        btnIniciar.setDisable(true);
        btnCancelar.setDisable(false);

        registrar("Iniciando procesamiento de 30 pedidos.");

        // Cada ejecución utiliza un hilo trabajador nuevo.
        Thread trabajador = new Thread(this::procesarPedidos, "ProcesadorPedidos");
        hiloTrabajador = trabajador;
        trabajador.start();

        // join() se ejecuta en otro hilo para no congelar JavaFX.
        Thread vigilante = new Thread(() -> {
            try {
                trabajador.join();

                // Los avisos de pedidos ya están encolados antes de este cierre.
                Platform.runLater(() -> finalizarProcesamiento(trabajador));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "VigilantePedidos");

        vigilante.setDaemon(true);
        vigilante.start();
    }

    // Ejecuta los pedidos y captura interrupciones o errores inesperados.
    private void procesarPedidos() {
        ProcesadorPedidos procesador = new ProcesadorPedidos();

        try {
            procesador.procesarPedidos(this::registrarPedidoCompletado);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            if (!cancelacionSolicitada) errorProceso = e;

        } catch (Exception e) {
            errorProceso = e;
        }
    }

    // Contabiliza el pedido y actualiza la interfaz desde el hilo JavaFX.
    private void registrarPedidoCompletado(int numeroPedido) {
        int completados = pedidosCompletados.incrementAndGet();

        // Los controles JavaFX solo deben actualizarse desde su propio hilo.
        Platform.runLater(() -> {
            actualizarProgreso(completados);
            registrar("Pedido " + numeroPedido + " completado.");
        });
    }

    // Solicita la cancelación sin detener forzosamente al trabajador.
    @FXML
    private void cancelarProcesamiento() {
        if (hiloTrabajador == null || cancelacionSolicitada) return;

        cancelacionSolicitada = true;
        actualizarEstado("Cancelando");

        btnCancelar.setDisable(true);
        registrar("Se solicitó cancelar el procesamiento.");

        // Interrumpe la espera de sleep() si el trabajador está procesando.
        hiloTrabajador.interrupt();
    }

    // Confirma la terminación del trabajador antes de permitir otro inicio.
    private void finalizarProcesamiento(Thread trabajador) {
        if (hiloTrabajador != trabajador) return;

        int completados = pedidosCompletados.get();
        actualizarProgreso(completados);

        if (errorProceso != null) {
            actualizarEstado("Error");
            registrar("Error durante el procesamiento: " + errorProceso);

            if (!cierrePendiente) {
                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        "Error de procesamiento",
                        "Ocurrió un error al procesar los pedidos:\n" + errorProceso
                );
            }

        } else if (completados == ProcesadorPedidos.TOTAL_PEDIDOS) {
            actualizarEstado("Completado");
            registrar("Se completaron los 30 pedidos correctamente.");

        } else if (cancelacionSolicitada) {
            actualizarEstado("Cancelado");
            registrar("Procesamiento cancelado. Pedidos completados: " + completados);

        } else {
            actualizarEstado("Error");
            registrar("El procesamiento terminó inesperadamente.");

            if (!cierrePendiente) {
                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        "Procesamiento incompleto",
                        "El trabajador terminó antes de completar los 30 pedidos."
                );
            }
        }

        hiloTrabajador = null;

        btnIniciar.setDisable(false);
        btnCancelar.setDisable(true);

        if (cierrePendiente) {
            Stage ventana = (Stage) btnIniciar.getScene().getWindow();
            ventana.close();
        }
    }

    // Actualiza el contador y calcula el porcentaje de progreso.
    private void actualizarProgreso(int completados) {
        lblContador.setText(completados + " / " + ProcesadorPedidos.TOTAL_PEDIDOS);

        double progreso = (double) completados / ProcesadorPedidos.TOTAL_PEDIDOS;
        barraProgreso.setProgress(progreso);
    }

    // Muestra el estado actual del procesamiento.
    private void actualizarEstado(String estado) {
        lblEstado.setText(estado);
    }

    // Agrega un mensaje al registro de operaciones.
    private void registrar(String mensaje) {
        txtRegistro.appendText(mensaje + "\n");
    }

    // Muestra una alerta con el tipo, título y mensaje indicados.
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.initOwner(btnIniciar.getScene().getWindow());
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        // Evita un bucle de eventos anidado mientras se libera el trabajador.
        alerta.show();
    }

    // Cancela la tarea antes de cerrar la ventana, si todavía está activa.
    void configurarCierre(Stage ventana) {
        ventana.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, evento -> {
            if (hiloTrabajador == null) return;

            evento.consume();
            cierrePendiente = true;

            cancelarProcesamiento();
        });
    }
}
