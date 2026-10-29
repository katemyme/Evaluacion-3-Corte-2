package ni.edu.uam.procesamiento_de_pedidos;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ProcesadorPedidosTest {

    // Controlador de prueba: guarda los pedidos avisados en lugar de actualizar una ventana.
    static class ControladorDePrueba extends PedidosController {
        final List<Integer> pedidos = Collections.synchronizedList(new ArrayList<>());
        final List<Long> tiempos = Collections.synchronizedList(new ArrayList<>());

        @Override
        void registrarPedidoCompletado(int numeroPedido) {
            pedidos.add(numeroPedido);
            tiempos.add(System.nanoTime());
        }
    }

    // Verifica los 30 pedidos en orden y que cada uno espere unos 200 ms.
    @Test
    void completaTreintaPedidosDeDoscientosMilisegundos() {
        ControladorDePrueba controlador = new ControladorDePrueba();
        long inicio = System.nanoTime();

        new ProcesadorPedidos(controlador).run();

        assertEquals(IntStream.rangeClosed(1, 30).boxed().toList(), controlador.pedidos);
        long anterior = inicio;
        for (long tiempo : controlador.tiempos) {
            assertTrue(tiempo - anterior >= 190_000_000L, "Cada pedido debe esperar 200 ms");
            anterior = tiempo;
        }
    }

    // Si el hilo ya estaba interrumpido, no procesa ningún pedido.
    @Test
    void respetaInterrupcionAntesDeComenzar() throws Exception {
        ControladorDePrueba controlador = new ControladorDePrueba();
        Thread trabajador = new Thread(() -> {
            Thread.currentThread().interrupt();
            new ProcesadorPedidos(controlador).run();
        });

        trabajador.start();
        trabajador.join(2000);

        assertFalse(trabajador.isAlive());
        assertEquals(0, controlador.pedidos.size());
    }

    // Interrumpe durante una espera: termina pronto y no cuenta el pedido incompleto.
    @Test
    void cancelaDuranteLaEspera() throws Exception {
        ControladorDePrueba controlador = new ControladorDePrueba();
        Thread trabajador = new Thread(new ProcesadorPedidos(controlador));

        trabajador.start();
        Thread.sleep(500);
        trabajador.interrupt();
        trabajador.join(2000);

        assertFalse(trabajador.isAlive());
        int completados = controlador.pedidos.size();
        assertTrue(completados >= 1 && completados < 30);
        assertEquals(IntStream.rangeClosed(1, completados).boxed().toList(), controlador.pedidos);
    }

    // Una interrupción recibida entre dos pedidos impide comenzar el siguiente.
    @Test
    void cancelaEntrePedidos() throws Exception {
        ControladorDePrueba controlador = new ControladorDePrueba() {
            @Override
            void registrarPedidoCompletado(int numeroPedido) {
                super.registrarPedidoCompletado(numeroPedido);
                Thread.currentThread().interrupt();
            }
        };
        Thread trabajador = new Thread(new ProcesadorPedidos(controlador));

        trabajador.start();
        trabajador.join(2000);

        assertFalse(trabajador.isAlive());
        assertEquals(List.of(1), controlador.pedidos);
    }
}
