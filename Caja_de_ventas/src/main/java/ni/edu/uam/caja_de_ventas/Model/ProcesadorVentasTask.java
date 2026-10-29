package ni.edu.uam.caja_de_ventas.Model;

import javafx.concurrent.Task;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class ProcesadorVentasTask extends Task<Integer> {

    private final String nombreVendedor;
    private final int metaVentas;
    private final CajaCompartida caja;
    private final Consumer<CajaCompartida.EstadoCaja> onVentaConfirmada;
    private final Consumer<Integer> onProgresoVendedor;
    private volatile int ventasRealizadas = 0;

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

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public int getVentasRealizadas() {
        return ventasRealizadas;
    }
}
