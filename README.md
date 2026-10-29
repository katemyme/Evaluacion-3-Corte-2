# Evaluación 3 – Corte 2: casos de estudio de concurrencia

Proyecto grupal de **Programación de Aplicaciones de Escritorio** (UAM). Es una aplicación JavaFX con un menú principal que abre tres casos de estudio:

| Caso | Módulo | Descripción |
| --- | --- | --- |
| 1 | `Reserva_Concurrente` | Tres agentes reservan 20 entradas al mismo tiempo, con una sección crítica `synchronized`. |
| 2 | `Procesamiento_de_pedidos` | Un hilo trabajador procesa 30 pedidos de 200 ms con cancelación cooperativa. |
| 3 | `Caja_de_ventas` | Tres vendedores concurrentes registran 15 ventas de C$100 con totales sincronizados y cancelación. |

Repositorio: <https://github.com/katemyme/Evaluacion-3-Corte-2>

## Versiones

- JDK 21 (se verificó con Microsoft OpenJDK 21.0.13).
- JavaFX 21.0.6 (dependencias `javafx-controls` y `javafx-fxml`, descargadas por Maven).
- IntelliJ IDEA (se verificó con la versión 2026.2.3).
- Maven Wrapper 3.8.5 incluido en el repositorio; JUnit 5.12.1 para las pruebas del caso 2.

## Ejecución desde IntelliJ IDEA

1. Abrir la carpeta raíz del repositorio (`Evaluacion-3-Corte-2`) con **File › Open**. IntelliJ reconoce el `pom.xml` padre y carga los cuatro módulos Maven. La primera vez necesita Internet para descargar JavaFX.
2. En **File › Project Structure › Project**, seleccionar un **SDK 21** y nivel de lenguaje 21.
3. En la barra superior, abrir el selector de configuraciones de ejecución. El repositorio incluye cuatro configuraciones listas en la carpeta `.run/`:
   - **Menu principal**: abre el menú con los tres casos.
   - **Caso 1 - Reserva concurrente**
   - **Caso 2 - Procesamiento de pedidos**
   - **Caso 3 - Caja de ventas**
4. Elegir una y pulsar **Run** (triángulo verde).

Cada configuración ejecuta la clase `Launcher` de su módulo. Si se prefiere, también se puede abrir `Launcher.java` del módulo deseado y pulsar el triángulo verde junto a `main`.

Maven forma parte de la configuración del proyecto (dependencias y compilación). Para compilar y correr las pruebas sin abrir la interfaz se puede usar `mvnw.cmd verify` desde la raíz, con `JAVA_HOME` apuntando a un JDK 21, pero la forma habitual de ejecución del grupo es IntelliJ.

## Estructura

```
Evaluacion-3-Corte-2/
├── pom.xml                    proyecto padre: versiones de JDK, JavaFX y JUnit
├── .run/                      configuraciones de ejecución compartidas de IntelliJ
├── Menu_principal/            menú que abre los tres casos
├── Reserva_Concurrente/       caso 1
├── Procesamiento_de_pedidos/  caso 2 (incluye pruebas JUnit)
├── Caja_de_ventas/            caso 3
└── informes/                  informes de cada caso y capturas de evidencia
```

Cada módulo tiene su `module-info.java`, su clase `Launcher`, una clase `Application` que carga el FXML y su controlador.

## Pruebas automáticas (caso 2)

`ProcesadorPedidosTest` verifica el procesamiento y la cancelación del hilo trabajador. `PedidosControllerTest` abre ventanas JavaFX reales y solo se ejecuta si se activa la propiedad `pruebasJavafx`. Desde IntelliJ se pueden ejecutar con clic derecho sobre la carpeta `src/test/java` › **Run 'All Tests'** (para las pruebas de interfaz, agregar `-DpruebasJavafx=true` en las opciones de VM de la configuración). Estas pruebas complementan, pero no sustituyen, la verificación manual de la interfaz documentada en los informes.

## Informes

Los informes de cada caso y sus capturas están en la carpeta [`informes/`](informes/).
