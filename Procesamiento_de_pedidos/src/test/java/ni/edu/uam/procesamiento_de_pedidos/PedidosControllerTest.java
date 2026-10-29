package ni.edu.uam.procesamiento_de_pedidos;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named = "pruebasJavafx", matches = "true")
class PedidosControllerTest {

    private Stage ventana;
    private PedidosController controlador;

    // Estas pruebas abren JavaFX real; se activan explícitamente en un escritorio.
    @BeforeAll
    static void iniciarJavafx() throws Exception {
        FutureTask<Void> inicio = new FutureTask<>(() -> {
            Platform.setImplicitExit(false);
            return null;
        });
        Platform.startup(inicio);
        inicio.get(10, TimeUnit.SECONDS);
    }

    // Cada escenario carga el FXML de producción y dispone de una ventana propia.
    @BeforeEach
    void abrirVentana() throws Exception {
        enJavafx(() -> {
            FXMLLoader cargador = new FXMLLoader(PedidosApplication.class.getResource("pedidos-view.fxml"));
            ventana = new Stage();
            ventana.setScene(new Scene(cargador.load()));
            controlador = cargador.getController();
            controlador.configurarCierre(ventana);
            ventana.show();
            return null;
        });
    }

    // Aun si falla una aserción, no deja trabajadores ni alertas para la prueba siguiente.
    @AfterEach
    void cerrarVentanas() throws Exception {
        Thread trabajador = enJavafx(() -> {
            Thread actual = trabajador();
            if (actual != null) invocar("cancelarProcesamiento");
            return actual;
        });
        if (trabajador != null) trabajador.join(2000);
        enJavafx(() -> {
            List.copyOf(Window.getWindows()).forEach(Window::hide);
            return null;
        });
    }

    // Libera el toolkit al finalizar la clase, sin terminar la JVM de JUnit.
    @AfterAll
    static void terminarJavafx() {
        Platform.exit();
    }

    // Comprueba los valores iniciales del FXML y la disponibilidad de los botones.
    @Test
    void comienzaListo() throws Exception {
        enJavafx(() -> {
            assertEquals("Listo", estado());
            comprobarProgreso(0);
            assertFalse(boton("btnIniciar").isDisabled());
            assertTrue(boton("btnCancelar").isDisabled());
            assertNull(trabajador());
            return null;
        });
    }

    // Un ciclo real debe llegar a 30/30, mantener la UI operativa y reiniciar desde cero.
    @Test
    void completaYPermiteReiniciar() throws Exception {
        Thread primero = enJavafx(() -> {
            boton("btnIniciar").fire();
            assertEquals("En ejecución", estado());
            assertTrue(boton("btnIniciar").isDisabled());
            assertNotSame(Thread.currentThread(), trabajador());
            return trabajador();
        });
        esperar(() -> progreso() > 0);
        enJavafx(() -> {
            assertTrue(primero.isAlive());
            ventana.setWidth(850);
            assertEquals(850, ventana.getWidth(), 1);
            return null;
        });
        esperar(() -> estado().equals("Completado"));
        enJavafx(() -> {
            assertFalse(primero.isAlive());
            assertNull(trabajador());
            comprobarProgreso(30);
            TextArea registro = (TextArea) ventana.getScene().lookup("#txtRegistro");
            assertEquals(30, registro.getText().lines().filter(linea -> linea.matches("Pedido \\d+ completado\\.")).count());
            boton("btnIniciar").fire();
            assertNotSame(primero, trabajador());
            comprobarProgreso(0);
            assertFalse(registro.getText().contains("Pedido 30 completado."));
            boton("btnCancelar").fire();
            return null;
        });
        esperar(() -> estado().equals("Cancelado"));
    }

    // Invoca también el manejador directamente para verificar la guardia, no solo el botón.
    @Test
    void impideDuplicadosYReinicioMientrasCancela() throws Exception {
        enJavafx(() -> {
            invocar("iniciarProcesamiento");
            Thread primero = trabajador();
            invocar("iniciarProcesamiento");
            assertSame(primero, trabajador());
            invocar("cancelarProcesamiento");
            invocar("cancelarProcesamiento");
            invocar("iniciarProcesamiento");
            assertSame(primero, trabajador());
            assertEquals("Cancelando", estado());
            assertTrue(boton("btnIniciar").isDisabled());
            assertTrue(boton("btnCancelar").isDisabled());
            return null;
        });
        esperar(() -> estado().equals("Cancelado"));
        enJavafx(() -> {
            comprobarProgreso(0);
            assertNull(trabajador());
            assertFalse(boton("btnIniciar").isDisabled());
            return null;
        });
    }

    // El resultado cancelado conserva exactamente los pedidos completos y permite otro ciclo.
    @Test
    void cancelaConProgresoParcial() throws Exception {
        enJavafx(() -> { boton("btnIniciar").fire(); return null; });
        esperar(() -> progreso() >= 3.0 / 30);
        Thread trabajador = enJavafx(() -> {
            boton("btnCancelar").fire();
            return trabajador();
        });
        esperar(() -> estado().equals("Cancelado"));
        enJavafx(() -> {
            assertFalse(trabajador.isAlive());
            int completados = (int) Math.round(progreso() * 30);
            assertTrue(completados >= 3 && completados < 30);
            comprobarProgreso(completados);
            boton("btnIniciar").fire();
            comprobarProgreso(0);
            assertEquals("En ejecución", estado());
            return null;
        });
    }

    // Fuerza la carrera: el trabajador termina mientras JavaFX aún no recibe sus avisos.
    @Test
    void cancelarTrasElUltimoPedidoConservaCompletado() throws Exception {
        enJavafx(() -> {
            invocar("iniciarProcesamiento");
            Thread primero = trabajador();
            // Solo la prueba bloquea JavaFX para controlar el orden de entrega de los eventos.
            primero.join(8000);
            assertFalse(primero.isAlive());
            invocar("cancelarProcesamiento");
            invocar("iniciarProcesamiento");
            assertSame(primero, trabajador());
            return null;
        });
        esperar(() -> estado().equals("Completado"));
        enJavafx(() -> { comprobarProgreso(30); return null; });
    }

    // Una interrupción ajena a Cancelar debe producir Error y liberar los controles.
    @Test
    void errorLiberaTrabajadorSinEsperarLaAlerta() throws Exception {
        enJavafx(() -> { boton("btnIniciar").fire(); return null; });
        esperar(() -> progreso() > 0);
        enJavafx(() -> { trabajador().interrupt(); return null; });
        esperar(() -> estado().equals("Error"));
        enJavafx(() -> {
            assertNull(trabajador());
            assertFalse(boton("btnIniciar").isDisabled());
            assertTrue(boton("btnCancelar").isDisabled());
            invocar("cancelarProcesamiento");
            assertEquals("Error", estado());
            return null;
        });
    }

    // El cierre solicita cancelación y espera la muerte real del trabajador antes de ocultarse.
    @Test
    void cierraDeFormaSeguraDuranteProcesamiento() throws Exception {
        enJavafx(() -> { boton("btnIniciar").fire(); return null; });
        esperar(() -> progreso() > 0);
        Thread trabajador = enJavafx(() -> {
            solicitarCierre(ventana);
            assertTrue(ventana.isShowing());
            assertEquals("Cancelando", estado());
            return trabajador();
        });
        esperar(() -> !ventana.isShowing());
        assertFalse(trabajador.isAlive());
        enJavafx(() -> {
            invocar("iniciarProcesamiento");
            assertNull(trabajador());
            return null;
        });
    }

    // Reproduce un cierre en el mismo turno que start(), antes de cualquier runLater.
    @Test
    void aplicacionInstalaCierreAntesDeMostrar() throws Exception {
        Stage inmediata = enJavafx(() -> {
            Stage nueva = new Stage();
            new PedidosApplication().start(nueva);
            ((Button) nueva.getScene().lookup("#btnIniciar")).fire();
            solicitarCierre(nueva);
            assertTrue(nueva.isShowing());
            assertEquals("Cancelando", ((Label) nueva.getScene().lookup("#lblEstado")).getText());
            return nueva;
        });
        esperar(() -> !inmediata.isShowing());
    }

    // Una ventana sin trabajador puede cerrarse sin entrar en Cancelando.
    @Test
    void cierraSinIniciar() throws Exception {
        enJavafx(() -> {
            solicitarCierre(ventana);
            assertFalse(ventana.isShowing());
            assertEquals("Listo", estado());
            return null;
        });
    }

    // Emite el mismo evento que el botón de cierre del sistema operativo.
    private static void solicitarCierre(Stage ventana) {
        Event.fireEvent(ventana, new WindowEvent(ventana, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    // Localiza controles por sus identificadores reales del FXML.
    private Button boton(String identificador) {
        return (Button) ventana.getScene().lookup("#" + identificador);
    }

    // Lee el estado visible para comprobar el comportamiento que recibe el usuario.
    private String estado() {
        return ((Label) ventana.getScene().lookup("#lblEstado")).getText();
    }

    // Lee el avance mostrado, no una copia preparada por la prueba.
    private double progreso() {
        return ((ProgressBar) ventana.getScene().lookup("#barraProgreso")).getProgress();
    }

    // Exige que la etiqueta y la barra representen el mismo número de pedidos.
    private void comprobarProgreso(int completados) {
        assertEquals(completados + " / 30", ((Label) ventana.getScene().lookup("#lblContador")).getText());
        assertEquals(completados / 30.0, progreso(), 0.000001);
    }

    // La reflexión permite observar el hilo sin agregar accesos exclusivos para pruebas.
    private Thread trabajador() throws Exception {
        Field campo = PedidosController.class.getDeclaredField("hiloTrabajador");
        campo.setAccessible(true);
        return (Thread) campo.get(controlador);
    }

    // Comprueba las guardias del controlador incluso cuando un botón está deshabilitado.
    private void invocar(String nombre) throws Exception {
        Method metodo = PedidosController.class.getDeclaredMethod(nombre);
        metodo.setAccessible(true);
        metodo.invoke(controlador);
    }

    // Espera condiciones observables con límite, sin asumir un orden exacto del planificador.
    private void esperar(Callable<Boolean> condicion) throws Exception {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < limite) {
            if (enJavafx(condicion)) return;
            Thread.sleep(10);
        }
        fail("JavaFX no alcanzó el resultado esperado en 10 segundos");
    }

    // Toda interacción con controles sucede en el hilo JavaFX y propaga sus fallos a JUnit.
    private static <T> T enJavafx(Callable<T> accion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(12, TimeUnit.SECONDS);
    }
}
