package ni.edu.uam.caja_de_ventas.Model;

public class CajaCompartida {

    public static final int MONTO_VENTA = 100;
    private int totalVentas = 0;
    private int recaudacionTotal = 0;

    public record EstadoCaja(int totalVentas, int recaudacionTotal, String detalleVenta) {}

    public synchronized EstadoCaja registrarVenta(String nombreVendedor, int numeroVentaVendedor) {
        totalVentas++;
        recaudacionTotal += MONTO_VENTA;
        String detalle = String.format("[%s] Venta #%d confirmada (+C$%d) -> Total Caja: %d ventas | C$%d",
                nombreVendedor, numeroVentaVendedor, MONTO_VENTA, totalVentas, recaudacionTotal);
        return new EstadoCaja(totalVentas, recaudacionTotal, detalle);
    }


    public synchronized EstadoCaja obtenerEstadoActual() {
        return new EstadoCaja(totalVentas, recaudacionTotal, "");
    }

    public synchronized void reiniciar() {
        totalVentas = 0;
        recaudacionTotal = 0;
    }
}