package ni.edu.uam.procesamiento_de_pedidos;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ProcesadorPedidosTest {

    // Verifica los 30 pedidos en orden y la espera de cada uno con tiempo real.
    @Test
    void completaTreintaPedidosDeDoscientosMilisegundos() throws Exception {
        List<Integer> pedidos = new ArrayList<>();
        List<Long> intervalos = new ArrayList<>();
        long[] anterior = {System.nanoTime()};

        new ProcesadorPedidos().procesarPedidos(numero -> {
            long ahora = System.nanoTime();
            intervalos.add(ahora - anterior[0]);
            anterior[0] = ahora;
            pedidos.add(numero);
        });

        assertEquals(IntStream.rangeClosed(1, 30).boxed().toList(), pedidos);
        assertTrue(intervalos.stream().allMatch(intervalo -> intervalo >= TimeUnit.MILLISECONDS.toNanos(190)),
                "Cada pedido debe esperar 200 ms (tolerancia de reloj: 10 ms)");
    }

    // Una interrupción previa evita incluso el primer pedido y conserva la señal.
    @Test
    void respetaInterrupcionAntesDeComenzar() throws Exception {
        AtomicInteger completados = new AtomicInteger();
        AtomicReference<Throwable> fallo = new AtomicReference<>();
        Thread trabajador = new Thread(() -> {
            Thread.currentThread().interrupt();
            try {
                new ProcesadorPedidos().procesarPedidos(numero -> completados.incrementAndGet());
                assertTrue(Thread.currentThread().isInterrupted());
            } catch (Throwable error) {
                fallo.set(error);
            }
        });
        trabajador.start();
        trabajador.join(2000);
        assertFalse(trabajador.isAlive());
        assertNull(fallo.get());
        assertEquals(0, completados.get());
    }

    // Interrumpe una espera real y comprueba que no se cuenta el pedido incompleto.
    @Test
    void cancelaDuranteLaEspera() throws Exception {
        AtomicInteger completados = new AtomicInteger();
        AtomicReference<Throwable> resultado = new AtomicReference<>();
        CountDownLatch primero = new CountDownLatch(1);
        Thread trabajador = new Thread(() -> {
            try {
                new ProcesadorPedidos().procesarPedidos(numero -> {
                    completados.incrementAndGet();
                    primero.countDown();
                });
            } catch (Throwable error) {
                resultado.set(error);
            }
        });
        trabajador.start();
        try {
            assertTrue(primero.await(2, TimeUnit.SECONDS));
            long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (trabajador.getState() != Thread.State.TIMED_WAITING && System.nanoTime() < limite) {
                Thread.sleep(1);
            }
            assertEquals(Thread.State.TIMED_WAITING, trabajador.getState());
            trabajador.interrupt();
            trabajador.join(2000);
            assertFalse(trabajador.isAlive());
            assertInstanceOf(InterruptedException.class, resultado.get());
            assertEquals(1, completados.get());
        } finally {
            trabajador.interrupt();
            trabajador.join(2000);
        }
    }

    // La cancelación entre pedidos no debe producir un segundo aviso.
    @Test
    void cancelaEntrePedidos() throws Exception {
        AtomicInteger completados = new AtomicInteger();
        try {
            new ProcesadorPedidos().procesarPedidos(numero -> {
                completados.incrementAndGet();
                Thread.currentThread().interrupt();
            });
            assertEquals(1, completados.get());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    // Un fallo del consumidor debe llegar al controlador, sin seguir procesando.
    @Test
    void propagaErroresDeNotificacion() {
        IllegalStateException fallo = new IllegalStateException("Fallo simulado");
        assertSame(fallo, assertThrows(IllegalStateException.class,
                () -> new ProcesadorPedidos().procesarPedidos(numero -> { throw fallo; })));
    }
}
