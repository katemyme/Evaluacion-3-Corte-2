package ni.edu.uam.reserva_concurrente.model;

public class Inventario {
    private int totalEntradas;
    private int disponibles;
    private int confirmadas;
    private int rechazadas;

    // Crea el inventario con todas las entradas disponibles.
    public Inventario(int totalEntradas) {
        this.totalEntradas = totalEntradas;
        this.disponibles = totalEntradas;
        this.confirmadas = 0;
        this.rechazadas = 0;
    }

    /**
     * Sección crítica de la aplicación.
     * synchronized permite que solamente un hilo ejecute el método a la vez.
     * La comprobación de disponibilidad y el descuento de la entrada se hacen
     * dentro del mismo método, por lo que dos agentes nunca pueden tomar la
     * misma entrada ni dejar el inventario en negativo (race condition).
     * @return true si la reserva se confirmó, false si se rechazó por falta de entradas
     */
    public synchronized boolean reservar() {
        if (disponibles > 0) {
            disponibles--;
            confirmadas++;
            return true;
        }
        rechazadas++;
        return false;
    }

    /**
     * Devuelve los tres valores del inventario leídos al mismo tiempo.
     * Es synchronized para que ningún agente pueda reservar a mitad de la lectura;
     * si se leyeran por separado, la interfaz podría mostrar una suma de
     * disponibles + confirmadas distinta de 20.
     * @return arreglo con: [0] disponibles, [1] confirmadas, [2] rechazadas
     */
    public synchronized int[] obtenerEstado() {
        return new int[] { disponibles, confirmadas, rechazadas };
    }

    /**
     * Restablece el inventario a su estado inicial para una nueva simulación.
     */
    public synchronized void reiniciar() {
        disponibles = totalEntradas;
        confirmadas = 0;
        rechazadas = 0;
    }

    // Devuelve la cantidad inicial de entradas.
    public int getTotalEntradas() {
        return totalEntradas;
    }

    // Devuelve las entradas disponibles.
    public synchronized int getDisponibles() {
        return disponibles;
    }

    // Devuelve las reservas confirmadas.
    public synchronized int getConfirmadas() {
        return confirmadas;
    }

    // Devuelve las solicitudes rechazadas.
    public synchronized int getRechazadas() {
        return rechazadas;
    }
}