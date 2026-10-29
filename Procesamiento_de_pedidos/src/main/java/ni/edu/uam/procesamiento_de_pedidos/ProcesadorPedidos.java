package ni.edu.uam.procesamiento_de_pedidos;

public class ProcesadorPedidos implements Runnable {

    public static final int TOTAL_PEDIDOS = 30;
    private static final int DURACION_PEDIDO_MS = 200;

    private final PedidosController controlador;

    // Recibe el controlador al que se avisará cada vez que termine un pedido.
    public ProcesadorPedidos(PedidosController controlador) {
        this.controlador = controlador;
    }

    // Tarea del hilo trabajador: procesa los 30 pedidos uno por uno.
    @Override
    public void run() {
        try {
            for (int numeroPedido = 1; numeroPedido <= TOTAL_PEDIDOS; numeroPedido++) {
                // Revisa entre un pedido y otro si se pidió cancelar con interrupt().
                if (Thread.currentThread().isInterrupted()) return;

                // Simula el trabajo del pedido. Si llega interrupt() durante la espera, sleep() lanza InterruptedException.
                Thread.sleep(DURACION_PEDIDO_MS);

                // El pedido solo se cuenta después de terminar su espera completa.
                controlador.registrarPedidoCompletado(numeroPedido);
            }
        } catch (InterruptedException e) {
            // Se canceló durante la espera: el pedido en curso no se cuenta y el hilo termina.
        }
    }
}
