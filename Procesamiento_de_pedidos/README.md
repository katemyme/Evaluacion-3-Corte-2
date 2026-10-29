# Caso 2: procesamiento de pedidos con cancelación

Simula 30 pedidos de 200 ms en un hilo trabajador separado del hilo de JavaFX. Requiere JDK 21 y JavaFX 21.0.6.

## Ejecución

Desde IntelliJ IDEA, con la carpeta raíz del repositorio abierta, elegir la configuración **Caso 2 - Procesamiento de pedidos** (o **Menu principal** › *Procesamiento de pedidos*) y pulsar **Run**. Las instrucciones completas están en el [README principal](../README.md).

**Iniciar** comienza una ejecución nueva y **Cancelar** pide al trabajador que se detenga.

## Clases

| Clase | Responsabilidad |
| --- | --- |
| `PedidosApplication` | Carga `pedidos-view.fxml` y conecta el cierre de la ventana con el controlador. |
| `PedidosController` | Botones, contador, barra de progreso, estados y registro. Crea el hilo trabajador y el hilo vigilante. |
| `ProcesadorPedidos` | `Runnable` con el ciclo de los 30 pedidos; avisa al controlador cada vez que termina uno. |

## Estados

| Estado | Significado |
| --- | --- |
| Listo | Todavía no se ha iniciado un procesamiento. |
| En ejecución | El trabajador está procesando pedidos. |
| Cancelando | Se pidió cancelar y se espera que el trabajador termine. |
| Cancelado | El trabajador terminó después de cancelar, con menos de 30 pedidos. |
| Completado | Se procesaron los 30 pedidos (también si la cancelación llegó después del último). |
| Error | El trabajador terminó antes de tiempo sin que se pidiera cancelar. |

## Cómo funciona la concurrencia

- **Hilo trabajador:** cada ejecución crea un `Thread` nuevo con `ProcesadorPedidos`, porque un hilo terminado no puede volver a iniciarse.
- **Cancelación cooperativa:** Cancelar llama a `interrupt()`. El trabajador revisa `isInterrupted()` antes de cada pedido, y si la interrupción llega durante `sleep(200)` se lanza `InterruptedException`. El pedido en curso no se cuenta. No se usa `Thread.stop()`.
- **Hilo vigilante:** espera al trabajador con `join()`. Se usa otro hilo porque `join()` en el hilo de JavaFX congelaría la ventana. Al terminar, usa `Platform.runLater()` para mostrar el resultado y habilitar otra ejecución.
- **Interfaz segura:** el contador y los controles solo se modifican dentro de `Platform.runLater()`, es decir, en el hilo de JavaFX. Por eso no hacen falta `AtomicInteger` ni `volatile`.
- **Cierre:** si hay un procesamiento activo, el cierre se consume, se cancela y la ventana se cierra cuando el vigilante confirma que el trabajador terminó.

## Pruebas automáticas

- `ProcesadorPedidosTest` (4 pruebas): orden y duración de los pedidos, interrupción antes de empezar, durante la espera y entre pedidos.
- `PedidosControllerTest` (9 pruebas): abre ventanas JavaFX reales; solo se ejecuta con la propiedad `-DpruebasJavafx=true` (en IntelliJ, en las opciones de VM de la configuración de prueba).

Las pruebas complementan la verificación manual documentada en `informes/Informe_Caso2_Procesamiento_de_Pedidos.pdf`.
