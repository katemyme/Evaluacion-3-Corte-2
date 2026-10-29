package ni.edu.uam.reserva_concurrente.model;

import ni.edu.uam.reserva_concurrente.controller.ReservaController;

public class AgenteReserva implements Runnable {
    private static final int PAUSA_MINIMA_MS = 200;
    private static final int PAUSA_VARIABLE_MS = 300;

    private int numero;
    private int totalIntentos;
    private Inventario inventario;
    private ReservaController controlador;
    private int confirmadas;
    private int rechazadas;

    // Recibe el número del agente, sus intentos, el inventario compartido y el controlador al que avisa.
    public AgenteReserva(int numero, int totalIntentos, Inventario inventario, ReservaController controlador) {
        this.numero = numero;
        this.totalIntentos = totalIntentos;
        this.inventario = inventario;
        this.controlador = controlador;
        this.confirmadas = 0;
        this.rechazadas = 0;
    }

    /**
     * Tarea que ejecuta el hilo del agente.
     * En cada intento espera una pausa aleatoria entre 200 y 500 ms con sleep(),
     * para que se aprecie la concurrencia y el orden entre agentes cambie en cada
     * simulación. Luego intenta reservar en el inventario compartido, actualiza
     * sus contadores y avisa al controlador para que actualice la vista.
     * Los contadores confirmadas y rechazadas son propios del agente: solo los
     * modifica su hilo, por eso no necesitan synchronized.
     */
    @Override
    public void run() {
        for (int intento = 1; intento <= totalIntentos; intento++) {
            try {
                long pausa = PAUSA_MINIMA_MS + (long) (Math.random() * PAUSA_VARIABLE_MS);
                Thread.sleep(pausa);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            boolean reservaConfirmada = inventario.reservar();
            if (reservaConfirmada) {
                confirmadas++;
            } else {
                rechazadas++;
            }
            controlador.actualizarAgente(numero, intento, confirmadas, rechazadas);
        }
    }

    // Devuelve el número del agente.
    public int getNumero() {
        return numero;
    }

    // Devuelve la cantidad de intentos asignados al agente.
    public int getTotalIntentos() {
        return totalIntentos;
    }

    // Devuelve las reservas confirmadas por el agente.
    public int getConfirmadas() {
        return confirmadas;
    }

    // Devuelve las solicitudes rechazadas del agente.
    public int getRechazadas() {
        return rechazadas;
    }
}