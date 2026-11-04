# Guía Maestra para la Defensa del Proyecto: Subnotway

> **Proyecto:** Sistema de Pedidos y Fidelización "Pan, Puntos y Premios"  
> **Curso:** Programación II — Universidad Mariano Gálvez de Guatemala (UMG)  
> **Stack Tecnológico:** Java 17+, Spring Boot 3.4 (Web MVC, REST API), Thymeleaf, Bootstrap 5, MySQL (InnoDB), OpenPDF, Android Nativo (Java).

---

## 1. Resumen Ejecutivo (El "Elevator Pitch")

> *"Subnotway es una solución tecnológica integral diseñada para optimizar tanto la operación comercial y de inventarios de un restaurante de comida rápida como la lealtad y retención de clientes. Consta de dos componentes clave totalmente desacoplados: una **plataforma web administrativa y de punto de venta (POS)** construida sobre Spring Boot y Thymeleaf, y una **aplicación móvil Android nativa** que permite a los clientes consultar su saldo de puntos en tiempo real a través de una API REST segura con persistencia transaccional en MySQL."*

### Los Módulos del Sistema:
1. **Terminal de Ventas (POS):** Toma de pedidos con catálogo reactivo, control de precios, cálculo de cambio, acumulación automática de puntos y transición de estados (`PENDIENTE` $\rightarrow$ `PAGADO` $\rightarrow$ `ENTREGADO`).
2. **Gestión de Productos e Inventario:** Control de 29 productos oficiales, alertas de stock mínimo ($\le 5$ unidades) y kardex de entradas/salidas en auditoría.
3. **Directorio de Clientes:** Registro, consulta y mantenimiento con identificación única formal (NIT o DPI).
4. **Módulo de Fidelización:** Consulta de saldo, historial de compras y canje transaccional de recompensas (10, 15, 30 y 50 pts) con validación estricta de stock e inventario.
5. **Centro de Reportes PDF:** Generación en memoria de 6 reportes gerenciales con filtros de fecha dinámicos utilizando OpenPDF.
6. **Componente Móvil (Android + REST):** Consulta desacoplada y multihilo en tiempo real para el cliente consumidor.

---

## 2. Arquitectura de Software y Patrones de Diseño

El sistema implementa una arquitectura en capas desacopladas con separación estricta de responsabilidades:

```mermaid
graph TD
    subgraph "Clientes / Frontend"
        Browser["Navegador Web (Thymeleaf + Bootstrap 5)"]
        AndroidApp["App Móvil Android (Native Java + Material Design)"]
    end

    subgraph "Capa Controladora (Presentation / API)"
        WebControllers["Spring Web MVC Controllers (Venta, Cliente, Fidelizacion, Reporte)"]
        RestController["ClienteApiController (@RestController / JSON)"]
    end

    subgraph "Capa de Negocio (Service Layer)"
        VentaService["VentaServicio (Cálculo de Totales, Stock, Estados)"]
        FidService["FidelizacionService (Fórmula P = S*2 + M*8, Canjes)"]
        ReporteService["ReportePdfService (Construcción de Documentos OpenPDF)"]
    end

    subgraph "Capa de Acceso a Datos (DAO Layer)"
        DAOs["ClienteDAO, ProductoDAO, PedidoDAO, MenuDAO, RecompensaDAO, CanjeDAO, ReporteDAO"]
        DBPool["ConexionDB (JDBC Connection Pool)"]
    end

    subgraph "Persistencia"
        MySQL[("MySQL Database (pan_puntos_premios)")]
    end

    Browser -->|HTTP GET/POST / HTML| WebControllers
    AndroidApp -->|HTTP GET / JSON (REST)| RestController
    WebControllers --> VentaService
    WebControllers --> FidService
    WebControllers --> ReporteService
    RestController --> DAOs
    VentaService --> DAOs
    FidService --> DAOs
    ReporteService --> DAOs
    DAOs --> DBPool
    DBPool --> MySQL
```

### Patrones de Diseño Aplicados:
- **MVC (Modelo - Vista - Controlador):** Desacopla la lógica de datos (`modelo/`), la presentación visual (`templates/`) y el flujo de control (`controlador/`).
- **DAO (Data Access Object):** Aísla las sentencias SQL y la conexión física a la base de datos de la lógica de negocio. Si el motor de datos cambiara, solo se modifica el DAO sin tocar los servicios ni controladores.
- **Service Layer (Capa de Servicio):** Centraliza las reglas de negocio empresariales. Los controladores se mantienen livianos (*Thin Controllers*): reciben la petición HTTP, delegan el procesamiento al servicio y retornan la vista o el stream de datos.
- **REST (Representational State Transfer):** `ClienteApiController` expone un recurso JSON sin estado (*stateless*), consumible por cualquier plataforma cliente (móvil, web, quiosco).
- **Asincronía y Concurrencia (Multi-threading en Android):** Uso de `ExecutorService` para la petición HTTP de red fuera del hilo principal (*Background Thread*) y `Handler(Looper.getMainLooper())` para actualizar la interfaz de usuario en el *Main Thread*.
- **DTO (Data Transfer Object):** `ItemPedidoDTO` transporta las líneas de venta desde la vista hacia el backend sin acoplar entidades de persistencia.

---

## 3. Explicación del Código Fuente Sección por Sección

Esta es la sección técnica clave si los evaluadores piden abrir el código en el IDE:

### A. Módulo de Ventas y Punto de Venta (POS)
* **Controlador:** `controlador/VentaController.java`
* **Servicio:** `servicio/VentaServicio.java`
* **DAOs:** `dao/impl/PedidoDAO.java`, `dao/impl/ProductoDAO.java`, `dao/impl/MenuDAO.java`

#### ¿Cómo funciona el flujo de cobro en el código?
1. La vista `ventas.html` envía por `POST /ventas/procesar` el cliente, el método de pago (`EFECTIVO` o `TARJETA`), el monto recibido y una cadena JSON con los productos del carrito.
2. `VentaServicio.procesarVenta(...)`:
   - Parsea los ítems del carrito con `ItemPedidoDTO`.
   - Suma los subtotales para determinar el total exacto de la orden.
   - Valida que `montoRecibido >= total`.
   - Calcula el cambio entregado al cliente: `cambio = montoRecibido - total`.
   - Descuenta las existencias físicas en inventario mediante `productoDAO.descontarStock(...)`. Si el ítem es un combo, consulta sus ingredientes en `menu_componentes` y descuenta cada producto integrante.
   - Inserta el pedido en la tabla `pedidos` con estado inicial `'PAGADO'`.
   - Inserta los detalles en `pedido_detalles`.
   - Inserta el registro contable en `pagos` guardando método, monto y cambio.
   - Aplica la fórmula de acumulación de puntos y actualiza el saldo del cliente.
   - Inserta auditoría en `movimientos_puntos` (tipo `'ACUMULACION'`) y en `movimientos_inventario` (tipo `'VENTA'`).
3. Transición de entrega: Cuando el personal despacha la comida, presiona "Entregar", ejecutando `POST /ventas/entregar/{id}` que cambia el estado a `'ENTREGADO'` mediante `pedidoDAO.cambiarEstado(idPedido, "ENTREGADO")`.

---

### B. Módulo de Fidelización y Canje Atómico de Premios
* **Controlador:** `controlador/FidelizacionController.java`
* **Servicio:** `servicio/FidelizacionService.java`
* **DAOs:** `dao/impl/RecompensaDAO.java`, `dao/impl/ClienteDAO.java`, `dao/impl/CanjeDAO.java`

#### 1. Cálculo de Puntos Acumulados:
La lógica reside en `FidelizacionService.calcularPuntos(int sandwiches, int menus)`:
```java
public int calcularPuntos(int sandwiches, int menus) {
    return (sandwiches * 2) + (menus * 8);
}
```
* **Sándwich individual:** Otorga **2 puntos**.
* **Menú / Combo completo:** Otorga **8 puntos** (fomenta la venta de combos de mayor margen).

#### 2. Transacción Atómica de Canje:
En `FidelizacionService.procesarCanje(int idCliente, int idRecompensa)` se ejecutan validaciones en cascada:
1. **Validación de Cliente:** Verifica que el cliente exista y su estado sea `'ACTIVO'`. Si está inactivo, rechaza la operación.
2. **Validación de Recompensa:** Verifica que la recompensa exista y esté activa.
3. **Validación de Saldo:** Verifica que `cliente.getPuntos() >= recompensa.getCostoPuntos()`. Si tiene menos puntos, rechaza sin alterar datos.
4. **Validación de Stock Físico:** Si es un producto, verifica que `existencia >= 1`. Si es un combo, verifica que todos los productos componentes tengan existencia disponible.
5. **Descuento Seguro:** Descuenta los puntos del cliente (`clienteDAO.actualizarPuntos`), descuenta el producto del inventario, registra la fila en `canjes`, registra la auditoría en `movimientos_puntos` (tipo `'CANJE'`) y en `movimientos_inventario` (tipo `'CANJE'`).

---

### C. Generación de Reportes en PDF con OpenPDF
* **Controlador:** `controlador/ReporteController.java`
* **Servicio:** `servicio/ReportePdfService.java`
* **DAO:** `dao/impl/ReporteDAO.java`

#### ¿Cómo se genera el archivo PDF sin guardarlo en disco?
1. El controlador recibe la petición (ej. `GET /reportes/pdf/ventas?fechaInicio=2026-11-03&fechaFin=2026-11-03`).
2. Configura los encabezados HTTP de respuesta:
   - `response.setContentType("application/pdf")`
   - `response.setHeader("Content-Disposition", "inline; filename=Reporte_Ventas_Periodo.pdf")`
3. Pasa el stream de red directamente al servicio: `response.getOutputStream()`.
4. En `ReportePdfService`:
   - Instancia el documento: `Document document = new Document(PageSize.A4, 36, 36, 36, 36)`.
   - Conecta el escritor con el stream: `PdfWriter.getInstance(document, outputStream)`.
   - Abre el documento: `document.open()`.
   - Crea tablas con `PdfPTable` y celdas con `PdfPCell`, aplicando fuentes, colores corporativos (#008938) y alineaciones.
   - Aplica *Zebra Striping* (filas alternadas en gris claro) para máxima legibilidad.
   - Itera sobre los datos devueltos por `ReporteDAO` y calcula totales en memoria.
   - Cierra con `document.close()`. Al cerrarse, el PDF viaja directamente por la red al navegador del usuario, logrando una descarga rápida con consumo mínimo de RAM y cero residuos en el disco duro.

#### ¿Cómo funciona la consulta con `UNION ALL` en Productos Más Vendidos?
En `ReporteDAO.obtenerProductosMasVendidos()`:
```sql
SELECT prod.codigo, prod.nombre, prod.categoria, SUM(t.unidades) AS total_unidades
FROM (
    -- 1. Unidades vendidas directamente como producto individual
    SELECT pd.id_producto, pd.cantidad AS unidades
    FROM pedido_detalles pd
    JOIN pedidos p ON pd.id_pedido = p.id_pedido
    WHERE pd.id_producto IS NOT NULL AND p.estado IN ('PAGADO', 'ENTREGADO', 'COMPLETADO')
    
    UNION ALL
    
    -- 2. Unidades consumidas dentro de combos (multiplicando cantidad de combos por cantidad de componentes)
    SELECT mc.id_producto, (pd.cantidad * mc.cantidad) AS unidades
    FROM pedido_detalles pd
    JOIN pedidos p ON pd.id_pedido = p.id_pedido
    JOIN menu_componentes mc ON pd.id_menu = mc.id_menu
    WHERE pd.id_menu IS NOT NULL AND p.estado IN ('PAGADO', 'ENTREGADO', 'COMPLETADO')
) t
JOIN productos prod ON t.id_producto = prod.id_producto
GROUP BY prod.id_producto, prod.codigo, prod.nombre, prod.categoria
ORDER BY total_unidades DESC;
```
*Esta consulta es una demostración avanzada de SQL relacional: no solo cuenta productos sueltos, sino que desagrega las recetas de los combos para reflejar el consumo real de ingredientes.*

---

### D. Aplicación Móvil Android (Native Java)
* **Actividad Principal:** `MainActivity.java`
* **Layout:** `activity_main.xml`
* **Manifiesto:** `AndroidManifest.xml` (permiso `android.permission.INTERNET`)

#### ¿Cómo se realiza la consulta asíncrona de puntos?
1. El usuario escribe su NIT o DPI y presiona "Consultar Saldo".
2. **Hilo Secundario (*Worker Thread*):** En Android no se puede ejecutar red en el hilo principal (`NetworkOnMainThreadException`). Se usa `ExecutorService executor = Executors.newSingleThreadExecutor()`:
   - Establece una conexión HTTP mediante `HttpURLConnection`.
   - Se conecta al endpoint REST: `http://10.0.2.2:8080/api/clientes/puntos/{identificador}`.
   - Configura timeouts de conexión y lectura de 6,000 ms.
   - Lee el flujo de bytes con `BufferedReader` y construye un `StringBuilder`.
   - Parsea el contenido con `JSONObject json = new JSONObject(respuestaStr)`.
3. **Regreso al Hilo de UI (*Main Thread*):** Para modificar vistas en pantalla se usa `Handler mainHandler = new Handler(Looper.getMainLooper())`:
   - `mainHandler.post(() -> { ... })` recibe los datos parseados y actualiza el nombre del cliente, los puntos y el estado sin congelar la pantalla ni bloquear la experiencia de usuario.

---

### E. Capa de Persistencia y Seguridad (JDBC DAO)
* **Archivo:** `dao/impl/ClienteDAO.java`, `ProductoDAO.java`, etc.
* **Seguridad contra SQL Injection:** Se utiliza exclusivamente `PreparedStatement`. Los valores nunca se concatenan como cadenas, sino que se envían como parámetros parametrizados con tipos de datos estrictos (`setString`, `setInt`, `setTimestamp`). El motor de MySQL compila el plan de ejecución antes de vincular los parámetros, anulando cualquier intento de inyección SQL.
* **Búsqueda Normalizada de Clientes:** En `ClienteDAO.buscarPorIdentificador`:
  - Acepta NITs con guion (ej. `87654321-0`).
  - Acepta NITs sin guion (ej. `876543210`).
  - Acepta DPIs de 13 dígitos (ej. `2548123450101`).
  - Acepta códigos de cliente legados o ID numérico (ej. `CL-002`, `2`).

---

## 4. Directorio Oficial de Clientes (NIT y DPI)

| ID | NIT / DPI | Nombre del Cliente | Puntos | Estado | Caso de Demostración |
| :---: | :--- | :--- | :---: | :---: | :--- |
| **1** | `12345678-9` | Juan Carlos Pérez | **1,316** | ACTIVO | Cliente VIP con puntos altos (probar canjes en vivo). |
| **2** | `87654321-0` | María Fernanda Gómez | **240** | ACTIVO | *(Reemplazo de CL-002)*. Cliente frecuente. |
| **3** | `2548123450101` | Carlos Eduardo López | **85** | ACTIVO | Demostración de consulta con DPI (13 dígitos). |
| **4** | `45678912-3` | Ana Lucía Morales | **52** | ACTIVO | Umbral exacto para canjear Menú Completo (50 pts). |
| **5** | `1892345670101` | Roberto Alejandro Soto | **38** | ACTIVO | Umbral exacto para canjear Sándwich (30 pts). |
| **6** | `98765432-1` | Sofía Isabel Martínez | **18** | ACTIVO | Umbral exacto para canjear Acompañamiento (15 pts). |
| **7** | `3012456780101` | Diego Fernando Castillo | **12** | ACTIVO | Umbral exacto para canjear Bebida (10 pts). |
| **8** | `54321678-K` | Jimena Alejandra Valdez | **0** | ACTIVO | Muestra alerta de saldo insuficiente. |
| **9** | `2145678900101` | César Augusto Ramírez | **320** | ACTIVO | Cliente corporativo. |
| **10** | `CF` | Consumidor Final | **0** | INACTIVO | Muestra validación de cliente inactivo. |

---

## 5. Preguntas "Trampa" de Catedráticos y Respuestas Ideales

### P1: *"¿Por qué la app móvil no se conecta directo a MySQL con JDBC?"*
> **Respuesta:** *"Conectar una app móvil directamente a la base de datos es una grave vulnerabilidad de seguridad. Requeriría guardar el usuario y contraseña de MySQL dentro del código descompilable del APK, abriría el puerto 3306 a internet exponiendo la base de datos a ataques de fuerza bruta o inyecciones directas, y saturaría el pool de conexiones del servidor. Al usar una **API REST intermedia**, desacoplamos la aplicación móvil, centralizamos la seguridad y validamos las reglas de negocio en el backend."*

### P2: *"¿Dónde reside la lógica de negocio: en la base de datos, en el controlador o en el servicio?"*
> **Respuesta:** *"En la **capa de servicio (`FidelizacionService`, `VentaServicio`, `ReportePdfService`)**. Los controladores son livianos (*Thin Controllers*) y solo se encargan de enrutar peticiones HTTP y devolver respuestas. Tampoco sobrecargamos la base de datos con stored procedures complejos para mantener la lógica testeable en Java y fácilmente portable entre motores de datos."*

### P3: *"¿Qué sucede si dos usuarios intentan canjear puntos al mismo tiempo?"*
> **Respuesta:** *"El motor de base de datos **InnoDB** implementa bloqueos a nivel de fila (*Row-level Locking*) y transacciones con propiedades ACID. Además, en la capa de servicio se realiza una validación previa de saldo y de existencias antes de ejecutar la actualización. La operación de descuento se ejecuta de forma atómica sobre el registro del cliente y el stock del producto."*

### P4: *"¿Por qué usar OpenPDF en lugar de iText antiguo o librerías pesadas?"*
> **Respuesta:** *"OpenPDF es una bifurcación moderna, de código abierto (LGPL/MPL) y libre de restricciones de licenciamiento comercial que tiene iText 7. Se integra perfectamente con Spring Boot 3 y Java 17+, permitiendo escribir el documento directamente en el `OutputStream` del servlet sin escribir archivos temporales en el disco del servidor."*

### P5: *"¿Qué es la dirección 10.0.2.2 que tiene la app de Android?"*
> **Respuesta:** *"Es el alias de loopback especial provisto por el entorno de emulación de Android. Desde la perspectiva de la máquina virtual del emulador, `127.0.0.1` apunta al emulador mismo; para alcanzar el `localhost` de la máquina anfitriona (donde corre Spring Boot en el puerto 8080), se debe usar `10.0.2.2`."*

---

## 6. Guía Paso a Paso para la Demostración en Vivo (Demo)

Si los evaluadores piden una demostración en vivo, este es el orden ideal con mayor impacto:

1. **Terminal de Ventas (`/ventas`):**
   - Selecciona a **Juan Carlos Pérez** (`12345678-9`).
   - Agrega 1 Combo 1 (Q 230) y 1 Sándwich de Ensalada de Atún (Q 75).
   - Ingresa un monto de pago (ej. Q 350 en efectivo) y haz clic en **Cobrar**.
   - Muestra cómo se calcula el cambio (Q 45.00) y cómo se suman **+10 puntos** (8 por el combo + 2 por el sándwich).
   - Haz clic en **Entregar** para demostrar la transición de estado del pedido.
2. **Consulta Móvil en Android:**
   - Abre la app en el emulador o celular.
   - Ingresa el NIT de Juan Carlos: `12345678-9` (o también el de María Fernanda Gómez: `87654321-0`).
   - Presiona **Consultar Saldo**.
   - Muestra cómo los puntos acumulados en la venta recién hecha se reflejan inmediatamente en la pantalla del celular vía REST API.
3. **Módulo de Fidelización Web (`/fidelizacion`):**
   - Consulta el cliente recién premiado.
   - Realiza el canje de una recompensa (ej. "Bebida Gratuita" por 10 pts).
   - Muestra cómo se descuenta el stock de inventario y se descuentan los puntos del cliente, quedando reflejado en la tabla de historial de canjes.
4. **Centro de Reportes PDF (`/reportes`):**
   - **Ventas por período:** Filtra la fecha de hoy (`2026-11-03` al `2026-11-03`) para mostrar las ventas recién efectuadas.
   - **Stock crítico:** Abre el reporte para mostrar los 5 productos con stock bajo o agotado (`Monster rosa` en 0, `Pork Jhon` en 2 unidades).
   - **Productos más vendidos:** Muestra el desglose de productos individuales y componentes de combos.
