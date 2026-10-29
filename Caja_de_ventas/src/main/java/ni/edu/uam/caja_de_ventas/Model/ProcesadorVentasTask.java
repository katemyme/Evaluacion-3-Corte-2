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
        int ventasRealizadas = 0;
        updateMessage("Activo (Vendiendo...)");

        for (int i = 1; i <= metaVentas; i++) {
            if (isCancelled()) {
                break;
            }

            updateMessage("Procesando venta " + i + "...");

            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(600, 1401));
            } catch (InterruptedException e) {
            }

            CajaCompartida.EstadoCaja nuevoEstado = caja.registrarVenta(nombreVendedor, i);
            ventasRealizadas++;

            updateProgress(ventasRealizadas, metaVentas);
            onProgresoVendedor.accept(ventasRealizadas);
            onVentaConfirmada.accept(nuevoEstado);

            if (isCancelled()) {
                break;
            }
        }

        if (isCancelled()) {
            updateMessage("Cancelado (" + ventasRealizadas + "/5)");
        } else {
            updateMessage("Completado (5/5)");
        }

        return ventasRealizadas;
    }
}