package ni.edu.uam.caja_de_ventas.Model;

import javafx.concurrent.Task;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

// Task es una clase de JavaFX para trabajo en segundo plano; permite cancelar y publicar progreso y mensajes de forma segura.
public class ProcesadorVentasTask extends Task<Integer> {

    private final String nombreVendedor;
    private final int metaVentas;
    private final CajaCompartida caja;
    private final Consumer<CajaCompartida.EstadoCaja> onVentaConfirmada;
    private final Consumer<Integer> onProgresoVendedor;
    // volatile hace que el hilo de JavaFX vea el último valor escrito por el hilo del vendedor.
    private volatile int ventasRealizadas = 0;

    // Recibe los datos del vendedor y las acciones que se ejecutan al confirmar cada venta.
    public ProcesadorVentasTask(String nombreVendedor, int metaVentas, CajaCompartida caja,
                        Consumer<CajaCompartida.EstadoCaja> onVentaConfirmada,
                        Consumer<Integer> onProgresoVendedor) {
        this.nombreVendedor = nombreVendedor;
        this.metaVentas = metaVentas;
        this.caja = caja;
        this.onVentaConfirmada = onVentaConfirmada;
        this.onProgresoVendedor = onProgresoVendedor;

        updateProgress(0, metaVentas);
        updateMessage("Inactivo");
    }

    // Trabajo del vendedor en segundo plano: realiza sus ventas hasta terminar o ser cancelado.
    @Override
    protected Integer call() {
        updateMessage("Activo (Vendiendo...)");

        for (int i = 1; i <= metaVentas && !isCancelled(); i++) {
            updateMessage("Procesando venta " + i + "...");

            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(600, 1401));
            } catch (InterruptedException e) {
                // La cancelacion interrumpe la espera: la venta en curso se registra completa y el ciclo termina.
            }

            CajaCompartida.EstadoCaja nuevoEstado = caja.registrarVenta(nombreVendedor, i);
            ventasRealizadas++;

            updateProgress(ventasRealizadas, metaVentas);
            onProgresoVendedor.accept(ventasRealizadas);
            onVentaConfirmada.accept(nuevoEstado);
        }

        if (isCancelled()) {
            updateMessage("Cancelado (" + ventasRealizadas + "/" + metaVentas + ")");
        } else {
            updateMessage("Completado (" + ventasRealizadas + "/" + metaVentas + ")");
        }

        return ventasRealizadas;
    }

    // Devuelve el nombre del vendedor.
    public String getNombreVendedor() {
        return nombreVendedor;
    }

    // Devuelve las ventas confirmadas por este vendedor.
    public int getVentasRealizadas() {
        return ventasRealizadas;
    }
}
