package ni.edu.uam.caja_de_ventas.Model;

public class CajaCompartida {

    public static final int MONTO_VENTA = 100;
    private int totalVentas = 0;
    private int recaudacionTotal = 0;

    // Copia de los totales leída en un mismo momento, para mostrarlos juntos en la interfaz.
    public record EstadoCaja(int totalVentas, int recaudacionTotal, String detalleVenta) {}

    // Registra una venta completa. synchronized permite que solo un vendedor a la vez modifique ventas y recaudación.
    public synchronized EstadoCaja registrarVenta(String nombreVendedor, int numeroVentaVendedor) {
        totalVentas++;
        recaudacionTotal += MONTO_VENTA;
        String detalle = String.format("[%s] Venta #%d confirmada (+C$%d) -> Total Caja: %d ventas | C$%d",
                nombreVendedor, numeroVentaVendedor, MONTO_VENTA, totalVentas, recaudacionTotal);
        return new EstadoCaja(totalVentas, recaudacionTotal, detalle);
    }


    // Lee ventas y recaudación juntas, sin que otro vendedor las cambie a mitad de la lectura.
    public synchronized EstadoCaja obtenerEstadoActual() {
        return new EstadoCaja(totalVentas, recaudacionTotal, "");
    }

    // Restablece los totales antes de una nueva simulación.
    public synchronized void reiniciar() {
        totalVentas = 0;
        recaudacionTotal = 0;
    }
}