# Subnotway — Instrucciones de Instalación, Uso y Guía de Demostración

Proyecto académico oficial para el curso de **Programación II** (Universidad Mariano Gálvez de Guatemala).  
Sistema de Pedidos, Facturación y Fidelización con plataforma Web MVC y aplicación móvil Android.

---

## Contenido del Paquete

1. **`Subnotway/`**: Aplicación Web administrativa y de punto de venta (Spring Boot 3.4, Java 17+, Thymeleaf, OpenPDF, MySQL).
2. **`Subnotway-Android/`**: Aplicación móvil nativa en Java para consulta en tiempo real de puntos acumulados.
3. **`pan_puntos_premios.sql`**: Script SQL consolidado con todas las tablas del sistema, datos históricos de prueba, catálogo completo de productos y menús, y recompensas oficiales.
4. **`Diagrama_Clases_UML.png`**: Diagrama de clases UML con la arquitectura completa en capas (Modelos, DAOs, Servicios, Controladores, DTOs).
5. **`Diagrama_Entidad_Relacion.pdf`**: Diagrama Entidad-Relación oficial del modelo relacional de base de datos en MySQL Workbench.
6. **`Guia_Defensa_Proyecto.md`**: Guía exhaustiva con respuestas técnicas, justificaciones de arquitectura y preguntas clave para la defensa oral del proyecto.

---

## Paso 1: Configurar la Base de Datos (MySQL / XAMPP)

1. Abre el panel de control de **XAMPP** e inicia los servicios **Apache** y **MySQL**.
2. Ingresa a phpMyAdmin desde tu navegador: `http://localhost/phpmyadmin`.
3. Ve a la pestaña **Importar**.
4. Haz clic en **Seleccionar archivo** y elige `pan_puntos_premios.sql` (ubicado en la raíz de esta carpeta).
5. Haz clic en **Importar** al final de la página.
6. Se creará automáticamente la base de datos `pan_puntos_premios` con las 12 tablas relacionales, los 29 productos del menú oficial, 5 combos, 10 clientes de prueba, 14 pedidos históricos con pagos en efectivo y tarjeta, auditorías y 6 canjes registrados.

---

## Paso 2: Ejecutar el Servidor Web (Spring Boot)

### Opción A (Recomendada y Rápida):
- Ingresa a la carpeta `Subnotway/` y haz doble clic sobre el archivo `ejecutar_servidor.bat`.
- El script detectará automáticamente tu configuración de Maven o NetBeans y levantará el servidor web.

### Opción B (Desde tu IDE favorito):
- Abre **Apache NetBeans**, **IntelliJ IDEA** o **VS Code**.
- Selecciona **Open Project** y selecciona la carpeta `Subnotway`.
- Haz clic derecho sobre el proyecto y selecciona **Run Project** (o ejecuta la clase principal `SubnotwayApplication.java`).

### Opción C (Desde la terminal):
- Abre una consola o PowerShell en la carpeta `Subnotway/` y ejecuta:
  ```bash
  mvn spring-boot:run
  ```

Una vez iniciado el servidor, abre tu navegador web en:  
👉 **`http://localhost:8080`**

### Módulos Web Disponibles:
- **Inicio / Dashboard:** `http://localhost:8080/`
- **Punto de Venta (POS):** `http://localhost:8080/ventas`  
  *(Permite seleccionar clientes, agregar sándwiches individuales o combos al carrito, calcular cambio, acumular puntos y marcar pedidos como entregados).*
- **Productos e Inventario:** `http://localhost:8080/productos`  
  *(Catálogo de 29 productos con precios oficiales, edición de stock, alertas de stock mínimo y registro de auditoría).*
- **Clientes:** `http://localhost:8080/clientes`  
  *(Registro de nuevos clientes, edición, consulta de saldo de puntos y acceso directo al historial de pedidos).*
- **Fidelización y Canjes:** `http://localhost:8080/fidelizacion`  
  *(Consulta interactiva por cliente, catálogo de recompensas de 10, 15, 30 y 50 pts, validación de stock y saldo, y canje transaccional seguro).*
- **Centro de Reportes PDF:** `http://localhost:8080/reportes`  
  *(Generación y descarga dinámica en PDF de los 6 reportes empresariales con filtros de fechas).*
- **Endpoint API REST:** `http://localhost:8080/api/clientes/puntos/{identificador}`  
  *(Retorna en formato JSON el saldo y estado del cliente para la app móvil).*

---

## Paso 3: Ejecutar la Aplicación Móvil (Android Studio)

1. Abre **Android Studio**.
2. Selecciona **File > Open** y elige la carpeta `Subnotway-Android`.
3. Espera que finalice la sincronización de dependencias Gradle.
4. Conecta un dispositivo físico con depuración USB activada o inicia un Emulador (ej. Pixel 6 / API 34).
5. Haz clic en el botón verde **Run 'app'** (icono de Play).
6. Una vez abierta la app en el dispositivo:
   - Ingresa un identificador de prueba: `CL-001` (o el NIT `12345678-9`).
   - Presiona el botón **Consultar Saldo**.
   - La aplicación mostrará el nombre del cliente, su estado (`ACTIVO`) y su balance de puntos en tiempo real.

### Notas de Conectividad Móvil:
- **Si usas el Emulador de Android Studio:** Deja el campo *Servidor API* con el valor predeterminado `http://10.0.2.2:8080`. Es la dirección especial que Android asigna para comunicarse con el `localhost` de tu computadora.
- **Si usas un Teléfono Físico por Wi-Fi:** Escribe en el campo *Servidor API* la dirección IP local de tu computadora en la red Wi-Fi (por ejemplo: `http://192.168.1.15:8080`). Asegúrate de que ambos dispositivos estén conectados a la misma red Wi-Fi.

---

## 📊 Guía de Demostración para los Reportes en PDF (`/reportes`)

El sistema cuenta con datos precargados a lo largo del tiempo para demostrar el funcionamiento de los filtros por fecha y métricas:

### 1. Reporte de Ventas por Período
- **Prueba Hoy:** Selecciona fecha inicio `2026-11-03` y fecha fin `2026-11-03`.  
  *Muestra:* Ventas realizadas en el día actual (2 en Efectivo con Q 360 recaudados y Q 23 cambio, 1 en Tarjeta por Q 85).
- **Prueba Mes Anterior (Octubre):** Selecciona `2026-10-01` al `2026-10-31`.  
  *Muestra:* 6 operaciones (3 en Efectivo por Q 410, 3 en Tarjeta por Q 1,170).
- **Prueba Histórica (Septiembre):** Selecciona `2026-09-01` al `2026-09-30`.  
  *Muestra:* 3 operaciones (2 en Efectivo por Q 300, 1 en Tarjeta por Q 160).
- **Últimos 30 días:** Deja los campos de fecha predeterminados para consolidar el período reciente.

### 2. Reporte de Productos Más Vendidos
- Demuestra la consulta avanzada con `UNION ALL` que suma tanto las ventas directas individuales como los componentes consumidos dentro de los combos.
- *Líderes en ventas:* Sándwich de ensalada de atún (16 unidades), Cocacola (13 unidades), Doritos (13 unidades), Pollo al granjero (9 unidades) y Pepsi (8 unidades).

### 3. Reporte de Stock Crítico (Existencias $\le 5$)
- Muestra los productos con existencias en nivel de alerta para reabastecimiento:
  - `BEB003` — Monster rosa: **0 unidades** (Agotado).
  - `SAN003` — Pork Jhon: **2 unidades** (Crítico).
  - `ACO008` — Doritos incógnita: **3 unidades**.
  - `ACO003` — Cheetos flaming hot: **4 unidades**.
  - `ACO012` — Galleta de queso con frambuesa: **5 unidades**.

### 4. Reporte de Puntos Acumulados por Cliente
- Muestra el listado de los 10 clientes clasificados en orden descendente por puntos.
- Permite evidenciar clientes frecuentes (`CL-001` con 1316 pts, `CL-009` con 320 pts, `CL-002` con 240 pts) y clientes nuevos o sin puntos (`CL-008` y `CL-010`).

### 5. Reporte de Historial de Canjes
- Lista las 6 redenciones de recompensas realizadas entre septiembre y noviembre, detallando cliente, nombre del premio, costo en puntos y sello de tiempo.

### 6. Reporte de Ventas de Menús Completos
- Presenta el consolidado de ventas de cada uno de los 5 combos oficiales del restaurante, mostrando unidades vendidas e ingresos totales recaudados.

---

## 🍽️ Catálogo Oficial de Productos y Menús Cargados

### Sándwiches:
- `SAN001` — Sándwich de ensalada de atún (Q 75.00)
- `SAN002` — Cheese party (Q 80.00)
- `SAN003` — Pork Jhon (Q 95.00)
- `SAN004` — Sándwich de chompipe (Q 75.00)
- `SAN005` — Pollo al granjero (Q 55.00)
- `SAN006` — Ensalada panizada (Q 65.00)

### Bebidas:
- `BEB001` a `BEB005` — Monster tradicional, blanca, rosa, tropical y verde (Q 20.00)
- `BEB006` — Tampico (Q 10.00)
- `BEB007` — Agua pura (Q 8.00)
- `BEB008` — Pepsi (Q 12.00)
- `BEB009` — Cocacola (Q 12.00)
- `BEB010` — Horchata (Q 15.00)

### Acompañamientos:
- `ACO001` a `ACO003` — Doritos, Cheetos, Cheetos flaming hot (Q 8.00)
- `ACO004` a `ACO006` — Lays originales, crema y especias, pollo campero (Q 8.00)
- `ACO007` a `ACO008` — Doritos verdes, Doritos incógnita (Q 8.00)
- `ACO009` a `ACO011` — Tortrix barbacoa, cobanero, salsa criolla (Q 5.00)
- `ACO012` a `ACO013` — Galleta queso frambuesa, Cookie chocolate explosion (Q 12.00)

### Combos (Menús):
- **Combo 1:** 3 panes, 3 acompañamientos y 3 bebidas (Q 230.00)
- **Combo 2:** 10 panes, 10 acompañamientos y 10 bebidas (Q 720.00)
- **Combo 3:** 5 panes, 5 acompañamientos y 5 bebidas (Q 375.00)
- **Combo 4:** 2 panes, 2 acompañamientos y 2 bebidas (Q 160.00)
- **Combo 5:** 1 pan, 1 bebida y 1 acompañamiento (Q 85.00)
