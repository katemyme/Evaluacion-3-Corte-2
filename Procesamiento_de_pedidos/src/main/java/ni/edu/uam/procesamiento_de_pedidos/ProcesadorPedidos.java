
package ni.edu.uam.procesamiento_de_pedidos;

import java.util.function.IntConsumer;

public class ProcesadorPedidos {

    public static final int TOTAL_PEDIDOS = 30;
    private static final int DURACION_PEDIDO_MS = 200;

    // Procesa los 30 pedidos y notifica cada uno al completarse.
    public void procesarPedidos(IntConsumer notificarPedidoCompletado)
            throws InterruptedException {

        for (int numeroPedido = 1; numeroPedido <= TOTAL_PEDIDOS; numeroPedido++) {

            // Comprueba si el hilo recibió una solicitud de interrupción.
            if (Thread.currentThread().isInterrupted()) return;

            Thread.sleep(DURACION_PEDIDO_MS);

            // Solo registra pedidos cuyo procesamiento terminó.
            if (Thread.currentThread().isInterrupted()) return;

            notificarPedidoCompletado.accept(numeroPedido);
        }
    }
}
