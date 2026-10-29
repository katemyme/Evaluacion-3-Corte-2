package ni.edu.uam.reserva_concurrente.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import ni.edu.uam.reserva_concurrente.model.AgenteReserva;
import ni.edu.uam.reserva_concurrente.model.Inventario;
import java.net.URL;

public class ReservaController {
    @FXML
    private BorderPane raiz;
    @FXML
    private Label lblEstado;
    @FXML
    private ToggleButton tglTema;
    @FXML
    private Label lblDisponibles;
    @FXML
    private Label lblConfirmadas;
    @FXML
    private Label lblRechazadas;
    @FXML
    private Label lblIntentosA1;
    @FXML
    private Label lblConfirmadasA1;
    @FXML
    private Label lblRechazadasA1;
    @FXML
    private ProgressBar prgA1;
    @FXML
    private Label lblEstadoA1;
    @FXML
    private Label lblIntentosA2;
    @FXML
    private Label lblConfirmadasA2;
    @FXML
    private Label lblRechazadasA2;
    @FXML
    private ProgressBar prgA2;
    @FXML
    private Label lblEstadoA2;
    @FXML
    private Label lblIntentosA3;
    @FXML
    private Label lblConfirmadasA3;
    @FXML
    private Label lblRechazadasA3;
    @FXML
    private ProgressBar prgA3;
    @FXML
    private Label lblEstadoA3;
    @FXML
    private Button btnIniciar;
    @FXML
    private Button btnNuevaSimulacion;
    @FXML
    private Label lblMensaje;

    // Constantes de la simulación
    private static final int TOTAL_ENTRADAS = 20;
    private static final int CANTIDAD_AGENTES = 3;
    private static final int INTENTOS_POR_AGENTE = 10;

    // Rutas de las hojas de estilo del modo claro y del modo oscuro
    private static final String CSS_CLARO = "/ni/edu/uam/reserva_concurrente/claro.css";
    private static final String CSS_OSCURO = "/ni/edu/uam/reserva_concurrente/oscuro.css";

    // Arreglos que agrupan los elementos de cada agente (posición 0 = Agente 1)
    private Label[] lblIntentosAgentes;
    private Label[] lblConfirmadasAgentes;
    private Label[] lblRechazadasAgentes;
    private ProgressBar[] prgAgentes;
    private Label[] lblEstadoAgentes;

    // Datos de la simulación
    private final Inventario inventario = new Inventario(TOTAL_ENTRADAS);
    private boolean simulacionEnCurso = false;
    private int numeroSimulacion = 0;

    @FXML
    private void initialize() {
        lblIntentosAgentes = new Label[] { lblIntentosA1, lblIntentosA2, lblIntentosA3 };
        lblConfirmadasAgentes = new Label[] { lblConfirmadasA1, lblConfirmadasA2, lblConfirmadasA3 };
        lblRechazadasAgentes = new Label[] { lblRechazadasA1, lblRechazadasA2, lblRechazadasA3 };
        prgAgentes = new ProgressBar[] { prgA1, prgA2, prgA3 };
        lblEstadoAgentes = new Label[] { lblEstadoA1, lblEstadoA2, lblEstadoA3 };
    }

    /**
     * Inicia una simulación creando y arrancando un hilo por cada agente.
     * Si ya hay una simulación en curso, no crea agentes nuevos y muestra un aviso
     * (así se evitan los inicios duplicados).
     * Al final crea un hilo supervisor que espera a que terminen los agentes.
     */
    @FXML
    private void iniciarSimulacion() {
        if (simulacionEnCurso) {
            lblMensaje.setText("Ya hay una simulación en curso: no se crearon agentes nuevos.");
            return;
        }
        simulacionEnCurso = true;
        numeroSimulacion++;

        btnNuevaSimulacion.setDisable(true);
        lblEstado.setText("Estado: En ejecución (simulación " + numeroSimulacion + ")");
        lblMensaje.setText("");
        for (Label estadoAgente : lblEstadoAgentes) {
            estadoAgente.setText("Trabajando");
        }

        Thread[] hilosAgentes = new Thread[CANTIDAD_AGENTES];
        for (int i = 0; i < CANTIDAD_AGENTES; i++) {
            AgenteReserva agente = new AgenteReserva(i + 1, INTENTOS_POR_AGENTE, inventario, this);
            hilosAgentes[i] = new Thread(agente);
            hilosAgentes[i].start();
        }

        Thread supervisor = new Thread(() -> esperarAgentes(hilosAgentes));
        supervisor.start();
    }

    /**
     * Restablece el inventario y todos los valores de la vista, y luego
     * inicia una nueva simulación.
     * Solo está habilitado cuando terminó la simulación anterior.
     */
    @FXML
    private void iniciarNuevaSimulacion() {
        if (simulacionEnCurso) {
            return;
        }

        inventario.reiniciar();
        actualizarTarjetas();

        for (int i = 0; i < CANTIDAD_AGENTES; i++) {
            lblIntentosAgentes[i].setText("0/" + INTENTOS_POR_AGENTE);
            lblConfirmadasAgentes[i].setText("0");
            lblRechazadasAgentes[i].setText("0");
            prgAgentes[i].setProgress(0);
            lblEstadoAgentes[i].setText("En espera");
        }

        lblMensaje.setText("");
        btnIniciar.setDisable(false);

        iniciarSimulacion();
    }

    /**
     * Cambia la hoja de estilo de la ventana entre modo claro y modo oscuro,
     * según si el botón de tema está seleccionado.
     */
    @FXML
    private void cambiarTema() {
        boolean modoOscuro = tglTema.isSelected();
        String ruta = modoOscuro ? CSS_OSCURO : CSS_CLARO;

        URL hoja = getClass().getResource(ruta);
        if (hoja == null) {
            lblMensaje.setText("No se encontró la hoja de estilo del tema seleccionado.");
            return;
        }

        raiz.getStylesheets().setAll(hoja.toExternalForm());
        tglTema.setText(modoOscuro ? "Modo claro" : "Modo oscuro");
    }

    /**
     * Se ejecuta en el hilo supervisor. Espera con join() a que terminen todos
     * los agentes y luego, con Platform.runLater(), cierra la simulación en el
     * hilo de JavaFX.
     * Se usa un hilo aparte porque llamar join() desde la ventana la congelaría
     * hasta que terminaran los agentes.
     * @param hilosAgentes, hilos de los agentes que se deben esperar
     */
    private void esperarAgentes(Thread[] hilosAgentes) {
        try {
            for (Thread hilo : hilosAgentes) {
                hilo.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        Platform.runLater(() -> finalizarSimulacion());
    }

    /**
     * Lo llama cada agente desde su propio hilo después de cada intento.
     * Como los elementos de la interfaz solo pueden modificarse desde el hilo
     * de JavaFX, Platform.runLater() le encarga a ese hilo la actualización.
     * @param numeroAgente, número del agente que avisa (1, 2 o 3)
     * @param intentos, intentos que lleva el agente
     * @param confirmadas, reservas confirmadas por el agente
     * @param rechazadas, solicitudes rechazadas del agente
     */
    public void actualizarAgente(int numeroAgente, int intentos, int confirmadas, int rechazadas) {
        Platform.runLater(() -> {
            int i = numeroAgente - 1;

            lblIntentosAgentes[i].setText(intentos + "/" + INTENTOS_POR_AGENTE);
            lblConfirmadasAgentes[i].setText(String.valueOf(confirmadas));
            lblRechazadasAgentes[i].setText(String.valueOf(rechazadas));
            prgAgentes[i].setProgress((double) intentos / INTENTOS_POR_AGENTE);

            if (intentos == INTENTOS_POR_AGENTE) {
                lblEstadoAgentes[i].setText("Finalizado");
            }

            actualizarTarjetas();
        });
    }

    /**
     * Cierra la simulación cuando terminan los tres agentes.
     * Verifica las condiciones de las pruebas de aceptación (20 confirmadas,
     * 10 rechazadas, 0 disponibles y disponibles + confirmadas = 20),
     * muestra el resultado y habilita el botón de nueva simulación.
     */
    private void finalizarSimulacion() {
        simulacionEnCurso = false;

        int[] estado = inventario.obtenerEstado();
        int disponibles = estado[0];
        int confirmadas = estado[1];
        int rechazadas = estado[2];

        int rechazadasEsperadas = (CANTIDAD_AGENTES * INTENTOS_POR_AGENTE) - TOTAL_ENTRADAS;

        boolean resultadoCorrecto = disponibles >= 0
                && disponibles + confirmadas == TOTAL_ENTRADAS
                && confirmadas == TOTAL_ENTRADAS
                && rechazadas == rechazadasEsperadas
                && disponibles == 0;

        String resultado = resultadoCorrecto ? "Resultado correcto" : "Resultado incorrecto";
        String resumen = "Simulación " + numeroSimulacion + ": " + confirmadas + " confirmadas, "
                + rechazadas + " rechazadas, " + disponibles + " disponibles. " + resultado + ".";

        lblEstado.setText("Estado: Finalizada (simulación " + numeroSimulacion + ")");
        lblMensaje.setText(resumen);

        btnIniciar.setDisable(true);
        btnNuevaSimulacion.setDisable(false);
    }

    /**
     * Muestra en las tarjetas los valores actuales del inventario.
     * Usa obtenerEstado() para leer los tres valores al mismo tiempo.
     */
    private void actualizarTarjetas() {
        int[] estado = inventario.obtenerEstado();
        lblDisponibles.setText(String.valueOf(estado[0]));
        lblConfirmadas.setText(String.valueOf(estado[1]));
        lblRechazadas.setText(String.valueOf(estado[2]));
    }
}