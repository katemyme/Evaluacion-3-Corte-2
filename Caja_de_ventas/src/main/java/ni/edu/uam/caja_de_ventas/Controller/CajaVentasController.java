package ni.edu.uam.caja_de_ventas.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.WindowEvent;
import ni.edu.uam.caja_de_ventas.Model.CajaCompartida;
import ni.edu.uam.caja_de_ventas.Model.ProcesadorVentasTask;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CajaVentasController {

    private static final int VENTAS_POR_VENDEDOR = 5;
    private static final int TOTAL_VENDEDORES = 3;
    private static final int VENTAS_ESPERADAS = VENTAS_POR_VENDEDOR * TOTAL_VENDEDORES;
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    private Label lblTotalVentas;
    @FXML
    private Label lblRecaudacion;
    @FXML
    private Label lblEstadoGeneral;
    @FXML
    private ProgressBar pbVendedor1;
    @FXML
    private Label lblVentasV1;
    @FXML
    private Label lblEstadoV1;
    @FXML
    private ProgressBar pbVendedor2;
    @FXML
    private Label lblVentasV2;
    @FXML
    private Label lblEstadoV2;

    @FXML
    private ProgressBar pbVendedor3;
    @FXML
    private Label lblVentasV3;
    @FXML
    private Label lblEstadoV3;

    @FXML
    private TextArea txtBitacora;
    @FXML
    private Button btnIniciar;
    @FXML
    private Button btnCancelar;

    private final CajaCompartida cajaCompartida = new CajaCompartida();
    private ExecutorService executorService;
    private final List<ProcesadorVentasTask> tareasActivas = new ArrayList<>();

    private boolean simulacionEnCurso = false;
    private boolean cancelacionSolicitada = false;
    private boolean cerrarAlTerminar = false;
    private int numeroSimulacion = 0;

    // Deja los botones y la bitácora en su estado inicial al cargar el FXML.
    @FXML
    public void initialize() {
        btnIniciar.setDisable(false);
        btnCancelar.setDisable(true);
        agregarBitacora("Sistema listo. Presione 'Iniciar Simulación' para comenzar.");
    }

    // Inicia una simulación nueva con tres vendedores y datos restablecidos.
    @FXML
    protected void onIniciarClick() {
        if (simulacionEnCurso) {
            agregarBitacora("ADVERTENCIA: Ya hay una simulación en curso.");
            return;
        }

        simulacionEnCurso = true;
        cancelacionSolicitada = false;
        numeroSimulacion++;
        cajaCompartida.reiniciar();

        btnIniciar.setDisable(true);
        btnCancelar.setDisable(false);

        mostrarTotales(cajaCompartida.obtenerEstadoActual());
        actualizarEstiloEstadoGeneral("En ejecución...", "#d97706");
        txtBitacora.clear();
        agregarBitacora("=== INICIANDO SIMULACIÓN #" + numeroSimulacion + " CON 3 VENDEDORES ===");

        AtomicInteger numeroHilo = new AtomicInteger(1);
        // ExecutorService administra un grupo de 3 hilos, uno por vendedor. Son daemon para que no impidan cerrar el programa.
        executorService = Executors.newFixedThreadPool(TOTAL_VENDEDORES, r -> {
            Thread t = new Thread(r, "vendedor-" + numeroHilo.getAndIncrement());
            t.setDaemon(true);
            return t;
        });

        tareasActivas.clear();
        tareasActivas.add(crearTareaVendedor("Vendedor 1", pbVendedor1, lblEstadoV1, lblVentasV1));
        tareasActivas.add(crearTareaVendedor("Vendedor 2", pbVendedor2, lblEstadoV2, lblVentasV2));
        tareasActivas.add(crearTareaVendedor("Vendedor 3", pbVendedor3, lblEstadoV3, lblVentasV3));

        tareasActivas.forEach(executorService::execute);
        // shutdown() no detiene a los vendedores: solo indica que no se enviarán más tareas.
        executorService.shutdown();
        esperarTerminacionEnSegundoPlano(executorService);
    }

    // Atiende el botón Cancelar.
    @FXML
    protected void onCancelarClick() {
        solicitarCancelacion();
    }

    // Pide a los tres vendedores que se detengan; cancel(true) interrumpe su espera actual.
    private void solicitarCancelacion() {
        if (!simulacionEnCurso || cancelacionSolicitada) {
            return;
        }

        cancelacionSolicitada = true;
        btnCancelar.setDisable(true);
        actualizarEstiloEstadoGeneral("Cancelando... esperando cierre de hilos", "#dc2626");
        agregarBitacora(">>> SOLICITUD DE CANCELACIÓN ENVIADA. Esperando que finalicen ventas en curso...");

        for (ProcesadorVentasTask tarea : tareasActivas) {
            tarea.cancel(true);
        }
    }

    // Crea la tarea de un vendedor y enlaza su barra y su estado con los controles de la ventana.
    private ProcesadorVentasTask crearTareaVendedor(String nombre, ProgressBar pb, Label lblEstado, Label lblVentas) {
        pb.progressProperty().unbind();
        lblEstado.textProperty().unbind();
        lblVentas.setText("0 / " + VENTAS_POR_VENDEDOR + " ventas");

        ProcesadorVentasTask task = new ProcesadorVentasTask(
                nombre,
                VENTAS_POR_VENDEDOR,
                cajaCompartida,
                // Estas acciones las ejecuta el vendedor desde su hilo; Platform.runLater pasa la actualización al hilo de JavaFX.
                estadoCaja -> Platform.runLater(() -> {
                    mostrarTotales(cajaCompartida.obtenerEstadoActual());
                    agregarBitacora(estadoCaja.detalleVenta());
                }),
                ventasVendedor -> Platform.runLater(() ->
                        lblVentas.setText(ventasVendedor + " / " + VENTAS_POR_VENDEDOR + " ventas")
                )
        );

        pb.progressProperty().bind(task.progressProperty());
        lblEstado.textProperty().bind(task.messageProperty());

        return task;
    }

    // Task.cancel() marca la tarea como CANCELLED de inmediato aunque el hilo siga dentro de call(),
    // por eso la terminación real se confirma esperando al executor, sin bloquear el hilo de JavaFX.
    private void esperarTerminacionEnSegundoPlano(ExecutorService ejecutor) {
        Thread supervisor = new Thread(() -> {
            try {
                ejecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            Platform.runLater(this::finalizarSimulacion);
        }, "supervisor-caja");
        supervisor.setDaemon(true);
        supervisor.start();
    }

    // Se ejecuta cuando los tres vendedores terminaron: muestra el resultado y habilita un nuevo inicio.
    private void finalizarSimulacion() {
        CajaCompartida.EstadoCaja estadoFinal = cajaCompartida.obtenerEstadoActual();
        mostrarTotales(estadoFinal);

        int sumaVendedores = 0;
        for (ProcesadorVentasTask tarea : tareasActivas) {
            sumaVendedores += tarea.getVentasRealizadas();
            agregarBitacora("-> Trabajador [" + tarea.getNombreVendedor() + "] terminado: "
                    + tarea.getVentasRealizadas() + " ventas confirmadas.");
        }

        simulacionEnCurso = false;
        btnIniciar.setDisable(false);
        btnCancelar.setDisable(true);

        if (cancelacionSolicitada) {
            actualizarEstiloEstadoGeneral("Cancelada y estabilizada", "#dc2626");
            agregarBitacora(String.format(
                    "=== SIMULACIÓN #%d CANCELADA: 3/3 trabajadores detenidos. Ventas: %d | Recaudación: C$%,d ===",
                    numeroSimulacion, estadoFinal.totalVentas(), estadoFinal.recaudacionTotal()));
        } else {
            actualizarEstiloEstadoGeneral("Simulación completada", "#16a34a");
            agregarBitacora(String.format(
                    "=== SIMULACIÓN #%d EXITOSA: 3/3 trabajadores terminaron. Ventas: %d | Recaudación: C$%,d ===",
                    numeroSimulacion, estadoFinal.totalVentas(), estadoFinal.recaudacionTotal()));
        }

        boolean consistente = sumaVendedores == estadoFinal.totalVentas()
                && estadoFinal.recaudacionTotal() == estadoFinal.totalVentas() * CajaCompartida.MONTO_VENTA;
        String verificacion = String.format(
                "Verificación: %d ventas x C$%d = C$%,d (suma por vendedor: %d) -> %s",
                estadoFinal.totalVentas(), CajaCompartida.MONTO_VENTA, estadoFinal.recaudacionTotal(),
                sumaVendedores, consistente ? "CONSISTENTE" : "INCONSISTENTE");
        agregarBitacora(verificacion);
        System.out.println("[Caja de ventas] Simulación #" + numeroSimulacion + " -> " + verificacion);

        if (cerrarAlTerminar) {
            System.out.println("[Caja de ventas] Ventana cerrada con 3/3 trabajadores terminados (executor terminado: "
                    + executorService.isTerminated() + ").");
            btnIniciar.getScene().getWindow().hide();
        }
    }

    // Si hay vendedores activos, cancela y cierra la ventana solo cuando todos hayan terminado.
    public void cerrarLimpiamente(WindowEvent evento) {
        if (!simulacionEnCurso) {
            return;
        }

        evento.consume();
        cerrarAlTerminar = true;
        solicitarCancelacion();
        actualizarEstiloEstadoGeneral("Cerrando... esperando cierre de hilos", "#dc2626");
        agregarBitacora(">>> CIERRE DE VENTANA SOLICITADO. Se cerrará cuando terminen los 3 vendedores.");
    }

    // Muestra el total de ventas y la recaudación de la caja.
    private void mostrarTotales(CajaCompartida.EstadoCaja estado) {
        lblTotalVentas.setText(estado.totalVentas() + " / " + VENTAS_ESPERADAS);
        lblRecaudacion.setText("C$ " + String.format("%,d", estado.recaudacionTotal()));
    }

    // Cambia el texto y el color del estado general de la simulación.
    private void actualizarEstiloEstadoGeneral(String texto, String colorHex) {
        lblEstadoGeneral.setText(texto);
        lblEstadoGeneral.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold;");
    }

    // Agrega un mensaje con la hora actual a la bitácora.
    private void agregarBitacora(String mensaje) {
        String linea = "[" + LocalTime.now().format(HORA_FMT) + "] " + mensaje + "\n";
        txtBitacora.appendText(linea);
    }
}
