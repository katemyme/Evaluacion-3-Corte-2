package ni.edu.uam.procesamiento_de_pedidos;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;

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

    // Estos campos solo se leen y modifican desde el hilo de JavaFX.
    private Thread hiloTrabajador;
    private int pedidosCompletados;
    private boolean cancelacionSolicitada;
    private boolean cerrarAlTerminar;

    // Deja la ventana en su estado inicial al cargar el FXML.
    @FXML
    private void initialize() {
        actualizarEstado("Listo");
        actualizarProgreso();
        btnCancelar.setDisable(true);
    }

    // Inicia una nueva ejecución con un hilo trabajador nuevo.
    @FXML
    private void iniciarProcesamiento() {
        // Evita un inicio duplicado mientras el trabajador anterior no haya terminado.
        if (hiloTrabajador != null) return;

        pedidosCompletados = 0;
        cancelacionSolicitada = false;

        txtRegistro.clear();
        actualizarProgreso();
        actualizarEstado("En ejecución");

        btnIniciar.setDisable(true);
        btnCancelar.setDisable(false);

        registrar("Iniciando procesamiento de " + ProcesadorPedidos.TOTAL_PEDIDOS + " pedidos.");

        // Un Thread terminado no puede volver a iniciarse, por eso cada ejecución crea uno nuevo.
        Thread trabajador = new Thread(new ProcesadorPedidos(this), "trabajador-pedidos");
        hiloTrabajador = trabajador;
        trabajador.start();

        // join() deja esperando al hilo que lo llama. Si se llamara aquí, la ventana se congelaría,
        // por eso la espera se hace en un hilo vigilante aparte.
        Thread vigilante = new Thread(() -> esperarTrabajador(trabajador), "vigilante-pedidos");
        vigilante.start();
    }

    // Se ejecuta en el hilo vigilante: espera con join() a que el trabajador termine de verdad.
    private void esperarTrabajador(Thread trabajador) {
        try {
            trabajador.join();
        } catch (InterruptedException e) {
            return;
        }

        // Platform.runLater ejecuta las tareas en el orden en que se encolan,
        // así que este cierre llega después de todos los pedidos ya registrados.
        Platform.runLater(this::finalizarProcesamiento);
    }

    // Lo llama el trabajador al terminar cada pedido. La interfaz se actualiza en el hilo de JavaFX.
    void registrarPedidoCompletado(int numeroPedido) {
        Platform.runLater(() -> {
            pedidosCompletados++;
            actualizarProgreso();
            registrar("Pedido " + numeroPedido + " completado.");
        });
    }

    // Solicita la cancelación cooperativa del trabajador, sin detenerlo a la fuerza.
    @FXML
    private void cancelarProcesamiento() {
        if (hiloTrabajador == null || cancelacionSolicitada) return;

        cancelacionSolicitada = true;
        actualizarEstado("Cancelando");
        btnCancelar.setDisable(true);
        registrar("Se solicitó cancelar el procesamiento.");

        // interrupt() despierta al trabajador si está en sleep() o lo detiene antes del siguiente pedido.
        hiloTrabajador.interrupt();
    }

    // Se ejecuta cuando el trabajador ya terminó: muestra el resultado y permite otra ejecución.
    private void finalizarProcesamiento() {
        if (pedidosCompletados == ProcesadorPedidos.TOTAL_PEDIDOS) {
            actualizarEstado("Completado");
            registrar("Se completaron los " + ProcesadorPedidos.TOTAL_PEDIDOS + " pedidos.");
        } else if (cancelacionSolicitada) {
            actualizarEstado("Cancelado");
            registrar("Procesamiento cancelado. Pedidos completados: " + pedidosCompletados);
        } else {
            // El trabajador terminó sin completar los pedidos y sin que se pidiera cancelar.
            actualizarEstado("Error");
            registrar("El procesamiento terminó antes de completar los pedidos.");
            if (!cerrarAlTerminar) mostrarError("El trabajador terminó antes de completar los pedidos.");
        }

        hiloTrabajador = null;
        btnIniciar.setDisable(false);
        btnCancelar.setDisable(true);

        if (cerrarAlTerminar) btnIniciar.getScene().getWindow().hide();
    }

    // Si hay pedidos en proceso, cancela primero y cierra la ventana cuando el trabajador termine.
    public void cerrarVentana(WindowEvent evento) {
        if (hiloTrabajador == null) return;

        // consume() evita que la ventana se cierre en este momento.
        evento.consume();
        cerrarAlTerminar = true;
        cancelarProcesamiento();
    }

    // Muestra el contador y la barra de progreso con los pedidos completados.
    private void actualizarProgreso() {
        lblContador.setText(pedidosCompletados + " / " + ProcesadorPedidos.TOTAL_PEDIDOS);
        barraProgreso.setProgress((double) pedidosCompletados / ProcesadorPedidos.TOTAL_PEDIDOS);
    }

    // Muestra el estado actual de la aplicación.
    private void actualizarEstado(String estado) {
        lblEstado.setText(estado);
    }

    // Agrega un mensaje al registro de procesamiento.
    private void registrar(String mensaje) {
        txtRegistro.appendText(mensaje + "\n");
    }

    // Muestra una alerta de error sin detener el hilo de JavaFX.
    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.initOwner(btnIniciar.getScene().getWindow());
        alerta.setTitle("Error de procesamiento");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.show();
    }
}
