# Caso 2: procesamiento de pedidos

Simula 30 pedidos de 200 ms en un trabajador separado de JavaFX. Requiere JDK 21;
el proyecto utiliza JavaFX 21.0.6 y el Maven Wrapper del repositorio.

## Ejecución y verificaciones

Desde la raíz del repositorio, configura `JAVA_HOME` con la carpeta de tu JDK 21:

```powershell
.\mvnw.cmd -B verify
.\mvnw.cmd javafx:run
```

En el menú, selecciona **Registro de pedidos**. **Iniciar** comienza un ciclo;
**Cancelar** interrumpe cooperativamente el procesamiento. Un nuevo inicio
restablece contador, progreso y registro cuando el trabajador anterior ya terminó.

Las cinco pruebas de `ProcesadorPedidosTest` se ejecutan con `verify`.
Las nueve pruebas de `PedidosControllerTest` necesitan un escritorio gráfico:

```powershell
.\mvnw.cmd -B verify -DpruebasJavafx=true
```

Sin esa propiedad, JUnit informa las pruebas JavaFX como omitidas. Con ella, se
abren ventanas reales y se prueban el FXML y los controles, sin TestFX ni mocks.
Los resultados de cada ejecución quedan en `Procesamiento_de_pedidos/target/surefire-reports/`;
son archivos generados y no deben incluirse en Git.

## Estados y concurrencia

| Estado | Significado |
| --- | --- |
| Listo | Todavía no se ha iniciado un ciclo. |
| En ejecución | Un trabajador procesa los pedidos. |
| Cancelando | Se solicitó interrupción; aún falta confirmar la terminación. |
| Cancelado | El trabajador terminó con menos de 30 pedidos tras cancelar. |
| Completado | Se completaron los 30 pedidos, incluso si llegó una cancelación tardía. |
| Error | Ocurrió un fallo o el trabajador terminó inesperadamente. |

`interrupt()` despierta al trabajador si está en `sleep(200)`; también se
comprueba la interrupción entre pedidos. Solo se cuentan pedidos completos.
El contador compartido usa `AtomicInteger`, y la solicitud de cancelación y el
error usan `volatile` para que sean visibles entre hilos.

Los controles solo cambian desde JavaFX. Los avisos de cada pedido se encolan con
`Platform.runLater`; un vigilante espera con `join()` fuera del hilo de interfaz
y después encola la finalización. Así, los avisos anteriores se aplican antes de
habilitar otro ciclo. La referencia al trabajador impide inicios duplicados
incluso si ya murió pero todavía está pendiente su aviso final.

El filtro de cierre se instala antes de mostrar la ventana. Si hay un ciclo,
consume el evento, pide cancelar y cierra únicamente después del `join()`.
Las alertas de error usan `show()` para no introducir un bucle de eventos anidado
durante la liberación del trabajador.

## Escenarios de aceptación

- Inicio: `Listo`, `0 / 30`, progreso cero y Cancelar deshabilitado.
- Ejecución completa: 30 avisos ordenados, `30 / 30`, progreso 100 % y `Completado`.
- Cancelación inmediata y parcial: `Cancelando` seguido de `Cancelado`, conservando
  contador y progreso de los pedidos terminados.
- Inicio repetido y reinicio durante cancelación: no crean otro trabajador.
- Reinicio tras completar o cancelar: contador, progreso y registro parten de cero.
- Cancelación después del pedido 30, antes de actualizar la interfaz: `Completado`.
- Interrupción inesperada: `Error`, sin dejar controles bloqueados por la alerta.
- Cierre sin iniciar, durante la ejecución e inmediatamente al abrir: cierre seguro.

Estas pruebas verifican el comportamiento técnico del caso 2. No sustituyen la
rúbrica oficial, la documentación de aportes de los integrantes ni la demostración
académica que se solicite para la entrega.
