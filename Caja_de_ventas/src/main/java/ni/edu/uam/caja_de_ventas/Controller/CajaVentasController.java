package ni.edu.uam.caja_de_ventas.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
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
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML private Label lblTotalVentas;
    @FXML private Label lblRecaudacion;
    @FXML private Label lblEstadoGeneral;

    @FXML private ProgressBar pbVendedor1;
    @FXML private Label lblVentasV1;
    @FXML private Label lblEstadoV1;

    @FXML private ProgressBar pbVendedor2;
    @FXML private Label lblVentasV2;
    @FXML private Label lblEstadoV2;

    @FXML private ProgressBar pbVendedor3;
    @FXML private Label lblVentasV3;
    @FXML private Label lblEstadoV3;

    @FXML private TextArea txtBitacora;
    @FXML private Button btnIniciar;
    @FXML private Button btnCancelar;

    private final CajaCompartida cajaCompartida = new CajaCompartida();
    private ExecutorService executorService;
    private final List<ProcesadorVentasTask> tareasActivas = new ArrayList<>();
    private final AtomicInteger trabajadoresTerminados = new AtomicInteger(0);

    private volatile boolean simulacionEnCurso = false;
    private volatile boolean cancelacionSolicitada = false;

    @FXML
    public void initialize() {
        btnIniciar.setDisable(false);
        btnCancelar.setDisable(true);
        agregarBitacora("Sistema listo. Presione 'Iniciar Simulación' para comenzar.");
    }

    @FXML
    protected void onIniciarClick() {
        if (simulacionEnCurso) {
            agregarBitacora("ADVERTENCIA: Ya hay una simulación en curso.");
            return;
        }

        simulacionEnCurso = true;
        cancelacionSolicitada = false;
        trabajadoresTerminados.set(0);
        cajaCompartida.reiniciar();

        btnIniciar.setDisable(true);
        btnCancelar.setDisable(false);

        lblTotalVentas.setText("0 / 15");
        lblRecaudacion.setText("C$ 0");
        actualizarEstiloEstadoGeneral("En ejecución...", "#d97706");
        txtBitacora.clear();
        agregarBitacora("=== INICIANDO NUEVA SIMULACIÓN CON 3 VENDEDORES ===");

        executorService = Executors.newFixedThreadPool(TOTAL_VENDEDORES, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        tareasActivas.clear();

        ProcesadorVentasTask v1 = crearTareaVendedor("Vendedor 1", pbVendedor1, lblEstadoV1, lblVentasV1);
        ProcesadorVentasTask v2 = crearTareaVendedor("Vendedor 2", pbVendedor2, lblEstadoV2, lblVentasV2);
        ProcesadorVentasTask v3 = crearTareaVendedor("Vendedor 3", pbVendedor3, lblEstadoV3, lblVentasV3);

        tareasActivas.add(v1);
        tareasActivas.add(v2);
        tareasActivas.add(v3);

        tareasActivas.forEach(executorService::submit);
        executorService.shutdown();
    }

    @FXML
    protected void onCancelarClick() {
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

    private ProcesadorVentasTask crearTareaVendedor(String nombre, ProgressBar pb, Label lblEstado, Label lblVentas) {
        pb.progressProperty().unbind();
        lblEstado.textProperty().unbind();
        lblVentas.setText("0 / " + VENTAS_POR_VENDEDOR + " ventas");

        ProcesadorVentasTask task = new ProcesadorVentasTask(
                nombre,
                VENTAS_POR_VENDEDOR,
                cajaCompartida,
                estadoCaja -> Platform.runLater(() -> {
                    lblTotalVentas.setText(estadoCaja.totalVentas() + " / 15");
                    lblRecaudacion.setText("C$ " + String.format("%,d", estadoCaja.recaudacionTotal()));
                    agregarBitacora(estadoCaja.detalleVenta());
                }),
                ventasVendedor -> Platform.runLater(() ->
                        lblVentas.setText(ventasVendedor + " / " + VENTAS_POR_VENDEDOR + " ventas")
                )
        );

        pb.progressProperty().bind(task.progressProperty());
        lblEstado.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> verificarTerminacionTrabajador(nombre, "FINALIZADO"));
        task.setOnCancelled(e -> verificarTerminacionTrabajador(nombre, "CANCELADO"));
        task.setOnFailed(e -> verificarTerminacionTrabajador(nombre, "ERROR"));

        return task;
    }

    private void verificarTerminacionTrabajador(String nombreVendedor, String motivo) {
        agregarBitacora("-> Trabajador [" + nombreVendedor + "] ha terminado (" + motivo + ").");

        int terminados = trabajadoresTerminados.incrementAndGet();
        if (terminados == TOTAL_VENDEDORES) {
            CajaCompartida.EstadoCaja estadoFinal = cajaCompartida.obtenerEstadoActual();
            simulacionEnCurso = false;

            btnIniciar.setDisable(false);
            btnCancelar.setDisable(true);

            if (cancelacionSolicitada) {
                actualizarEstiloEstadoGeneral("Cancelada y estabilizada", "#dc2626");
                agregarBitacora(String.format(
                        "=== SIMULACIÓN CANCELADA: 3/3 trabajadores detenidos. Ventas: %d | Recaudación: C$%,d ===",
                        estadoFinal.totalVentas(), estadoFinal.recaudacionTotal()));
            } else {
                actualizarEstiloEstadoGeneral("Simulación completada", "#16a34a");
                agregarBitacora(String.format(
                        "=== SIMULACIÓN EXITOSA: 3/3 trabajadores terminaron. Ventas: %d | Recaudación: C$%,d ===",
                        estadoFinal.totalVentas(), estadoFinal.recaudacionTotal()));
            }
        }
    }

    public void cerrarLimpiamente() {
        if (simulacionEnCurso) {
            for (ProcesadorVentasTask tarea : tareasActivas) {
                tarea.cancel(true);
            }
        }
        if (executorService != null && !executorService.isTerminated()) {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void actualizarEstiloEstadoGeneral(String texto, String colorHex) {
        lblEstadoGeneral.setText(texto);
        lblEstadoGeneral.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold;");
    }

    private void agregarBitacora(String mensaje) {
        String linea = "[" + LocalTime.now().format(HORA_FMT) + "] " + mensaje + "\n";
        txtBitacora.appendText(linea);
    }
}